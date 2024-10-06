package com.asdflj.ae2thing.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;

import appeng.api.util.IInterfaceViewable;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import gregtech.api.metatileentity.BaseMetaTileEntity;

public class GTUtil {

    public static IInterfaceViewable getIInterfaceViewable(TileEntity tile) {
        if (tile instanceof BaseMetaTileEntity bmte && bmte.getMetaTileEntity() instanceof IInterfaceViewable iv) {
            return iv;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static boolean isDataStick() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack dataStick = player.inventory.getItemStack();
        return ItemList.Tool_DataStick.isStackEqual(dataStick, false, true);
    }

    public static void setDataStick(int x, int y, int z, EntityPlayer player, World w) {
        TileEntity tile = w.getTileEntity(x, y, z);
        if (tile instanceof BaseMetaTileEntity bmte && bmte.getMetaTileEntity() instanceof IInterfaceViewable) {
            ItemStack dataStick = player.inventory.getItemStack();
            if (ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("type", "CraftingInputBuffer");
                tag.setInteger("x", x);
                tag.setInteger("y", y);
                tag.setInteger("z", z);
                dataStick.setTagCompound(tag);
                dataStick
                    .setStackDisplayName("Crafting Input Buffer Link Data Stick (" + x + ", " + y + ", " + z + ")");
                player.addChatMessage(new ChatComponentText("Saved Link Data to Data Stick"));
                SPacketMEItemInvUpdate packet = new SPacketMEItemInvUpdate((byte) 1);
                packet.appendItem(AEItemStack.create(dataStick));
                AE2Thing.proxy.netHandler.sendTo(packet, (EntityPlayerMP) player);
            }
        }
    }
}
