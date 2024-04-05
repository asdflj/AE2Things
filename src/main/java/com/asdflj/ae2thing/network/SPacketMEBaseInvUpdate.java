package com.asdflj.ae2thing.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

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
                    this.getList()
                        .add(AEItemStack.loadItemStackFromPacket(uncompressed));
                } else {
                    this.getList()
                        .add(AEFluidStack.loadFluidStackFromPacket(uncompressed));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
