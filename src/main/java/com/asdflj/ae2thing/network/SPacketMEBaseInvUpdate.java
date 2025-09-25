package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class SPacketMEBaseInvUpdate implements IMessage {

    protected static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
    protected static final int OPERATION_BYTE_LIMIT = 2 * 1024;
    protected static final int TEMP_BUFFER_SIZE = 1024;
    protected static final int STREAM_MASK = 0xff;
    @Nullable
    protected GZIPOutputStream compressFrame;
    protected int writtenBytes = 0;
    protected final ByteBuf data = Unpooled.buffer(OPERATION_BYTE_LIMIT);
    protected byte ref = (byte) 0;
    protected final List<IAEStack<?>> list = new ArrayList<>();

    public SPacketMEBaseInvUpdate() {}

    public SPacketMEBaseInvUpdate(byte b) {
        setRef(b);
    }

    protected void compress() throws IOException, BufferOverflowException {
        for (IAEStack<?> is : getList()) {
            final ByteBuf tmp = Unpooled.buffer(OPERATION_BYTE_LIMIT);
            is.writeToPacket(tmp);
            assert this.compressFrame != null;
            this.compressFrame.flush();
            if (this.writtenBytes + tmp.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT) {
                throw new BufferOverflowException();
            } else {
                this.writtenBytes += tmp.readableBytes();
                this.compressFrame.write(tmp.array(), 0, tmp.readableBytes());
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(getRef());
        try {
            this.compressFrame = new GZIPOutputStream(new OutputStream() {

                @Override
                public void write(final int value) {
                    SPacketMEBaseInvUpdate.this.data.writeByte(value);
                }
            });
            compress();
            this.compressFrame.finish();
            buf.writeBytes(this.data);
            this.compressFrame.close();
        } catch (IOException io) {
            AELog.error(io);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ref = buf.readByte();
        try {
            final GZIPInputStream gzReader = new GZIPInputStream(new InputStream() {

                @Override
                public int read() {
                    if (buf.readableBytes() <= 0) {
                        return -1;
                    }
                    return buf.readByte() & STREAM_MASK;
                }
            });
            final ByteBuf uncompressed = Unpooled.buffer(buf.readableBytes());
            final byte[] tmp = new byte[TEMP_BUFFER_SIZE];
            while (gzReader.available() != 0) {
                final int bytes = gzReader.read(tmp);
                if (bytes > 0) {
                    uncompressed.writeBytes(tmp, 0, bytes);
                }
            }
            gzReader.close();
            while (uncompressed.readableBytes() > 0) {
                if (this instanceof SPacketMEItemInvUpdate) {
                    loadItemStackFromPacket(uncompressed);
                } else {
                    loadFluidStackFromPacket(uncompressed);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static long getPacketValue(final byte type, final ByteBuf tag) {
        if (type == 0) {
            long l = tag.readByte();
            l -= Byte.MIN_VALUE;
            return l;
        } else if (type == 1) {
            long l = tag.readShort();
            l -= Short.MIN_VALUE;
            return l;
        } else if (type == 2) {
            long l = tag.readInt();
            l -= Integer.MIN_VALUE;
            return l;
        }

        return tag.readLong();
    }

    private void loadFluidStackFromPacket(final ByteBuf data) throws IOException {
        final byte mask = data.readByte();
        // byte PriorityType = (byte) (mask & 0x03);
        final byte stackType = (byte) ((mask & 0x0C) >> 2);
        final byte countReqType = (byte) ((mask & 0x30) >> 4);
        final boolean isCraftable = (mask & 0x40) > 0;
        final boolean hasTagCompound = (mask & 0x80) > 0;

        // don't send this...
        final NBTTagCompound d = new NBTTagCompound();

        final byte len2 = data.readByte();
        final byte[] name = new byte[len2];
        data.readBytes(name, 0, len2);

        d.setString("FluidName", new String(name, StandardCharsets.UTF_8));
        d.setByte("Count", (byte) 0);

        if (hasTagCompound) {
            final int len = data.readInt();

            final byte[] bd = new byte[len];
            data.readBytes(bd);

            final DataInputStream di = new DataInputStream(new ByteArrayInputStream(bd));
            d.setTag("tag", CompressedStreamTools.read(di));
        }

        // long priority = getPacketValue( PriorityType, data );
        final long stackSize = getPacketValue(stackType, data);
        final long countRequestable = getPacketValue(countReqType, data);

        final byte mask2 = data.readByte();
        final byte countReqMadeType = (byte) ((mask2 & 0x3));
        final byte usedPercentType = (byte) ((mask2 & 0xC) >> 2);
        final long countRequestableCrafts = getPacketValue(countReqMadeType, data);
        final long longUsedPercent = getPacketValue(usedPercentType, data);

        final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(d);
        if (fluidStack == null) {
            AELog.warn(
                String.format("fluid is null, name:%s count:%s", new String(name, StandardCharsets.UTF_8), stackSize));
            return;
        }

        final AEFluidStack fluid = AEFluidStack.create(fluidStack);
        // fluid.priority = (int) priority;
        fluid.setStackSize(stackSize);
        fluid.setCountRequestable(countRequestable);
        fluid.setCraftable(isCraftable);
        fluid.setCountRequestableCrafts(countRequestableCrafts);
        fluid.setUsedPercent(longUsedPercent / 10000f);
        this.getList()
            .add(fluid);
    }

    private void loadItemStackFromPacket(final ByteBuf data) throws IOException {
        final byte mask = data.readByte();
        // byte PriorityType = (byte) (mask & 0x03);
        final byte stackType = (byte) ((mask & 0x0C) >> 2);
        final byte countReqType = (byte) ((mask & 0x30) >> 4);
        final boolean isCraftable = (mask & 0x40) > 0;
        final boolean hasTagCompound = (mask & 0x80) > 0;

        // don't send this...
        final NBTTagCompound d = new NBTTagCompound();

        d.setShort("id", data.readShort());
        d.setShort("Damage", data.readShort());
        d.setByte("Count", (byte) 0);

        if (hasTagCompound) {
            final int len = data.readInt();

            final byte[] bd = new byte[len];
            data.readBytes(bd);

            final ByteArrayInputStream di = new ByteArrayInputStream(bd);
            d.setTag("tag", CompressedStreamTools.read(new DataInputStream(di)));
        }

        // long priority = getPacketValue( PriorityType, data );
        final long stackSize = getPacketValue(stackType, data);
        final long countRequestable = getPacketValue(countReqType, data);

        final byte mask2 = data.readByte();
        final byte countReqMadeType = (byte) ((mask2 & 0x3));
        final byte usedPercentType = (byte) ((mask2 & 0xC) >> 2);
        final long countRequestableCrafts = getPacketValue(countReqMadeType, data);
        final long longUsedPercent = getPacketValue(usedPercentType, data);

        final ItemStack itemstack = ItemStack.loadItemStackFromNBT(d);
        if (itemstack == null) {
            String id = String.valueOf(d.getShort("id"));
            int damage = d.getShort("Damage");
            AELog.warn(String.format("itemstack is null, id:%s count:%s damage:%s", id, stackSize, damage));
            return;
        }

        final AEItemStack item = AEItemStack.create(itemstack);
        // item.priority = (int) priority;
        item.setStackSize(stackSize);
        item.setCountRequestable(countRequestable);
        item.setCraftable(isCraftable);
        item.setCountRequestableCrafts(countRequestableCrafts);
        item.setUsedPercent(longUsedPercent / 10000f);
        this.getList()
            .add(item);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public List<IAEStack<?>> getList() {
        return this.list;
    }

    public byte getRef() {
        return this.ref;
    }

    public void setRef(byte ref) {
        this.ref = ref;
    }

}
