package com.asdflj.ae2thing.util;

import static com.asdflj.ae2thing.api.Constants.MessageType.UPDATE_PLAYER_ITEM;
import static com.asdflj.ae2thing.nei.NEI_TH_Config.getConfigValue;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;
import com.glodblock.github.nei.object.OrderStack;

import appeng.api.util.IInterfaceViewable;
import appeng.util.item.AEItemStack;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.common.blocks.ItemMachines;
import gregtech.nei.GTNEIDefaultHandler;

public class GTUtil {

    public static String CoreModVersion = getCoreModVersion();

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

    public static String getRecipeName(IRecipeHandler recipe, List<OrderStack<?>> in) {
        if (recipe instanceof GTNEIDefaultHandler) {
            if (ModAndClassUtil.PH && getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL_FILL_CIRCUIT)) {
                return recipe.getRecipeName();
            }
            for (OrderStack<?> stack : in) {
                if (stack.getStack() instanceof ItemStack is && is.stackSize == 0) {
                    return getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL_APPEND_CIRCUIT_DAMAGE)
                        ? String.format("%s %s", recipe.getRecipeName(), is.getItemDamage())
                        : recipe.getRecipeName();
                }
            }
        }
        return recipe.getRecipeName();
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
                SPacketMEItemInvUpdate packet = new SPacketMEItemInvUpdate(UPDATE_PLAYER_ITEM);
                packet.appendItem(AEItemStack.create(dataStick));
                AE2Thing.proxy.netHandler.sendTo(packet, (EntityPlayerMP) player);
            }
        }
    }

    private static String getCoreModVersion() {
        Optional<ModContainer> mod = Loader.instance()
            .getActiveModList()
            .stream()
            .filter(
                x -> x.getModId()
                    .equals("dreamcraft"))
            .findFirst();
        if (mod.isPresent()) {
            return mod.get()
                .getVersion();
        }
        return "";
    }

    public static boolean isHatchItem(ItemStack item) {
        if (item != null && item.getItem() instanceof ItemMachines) {
            return ItemMachines.getMetaTileEntity(item) instanceof MTEHatch;
        }
        return false;
    }

    /*
     * @param v1
     * @param v2
     * @return 0 equals，1 left，-1 right
     */
    public static int compareVersion(String v1) {
        String v2 = "2.3.54"; // 2.6.1
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
            && (diff = Long.parseLong(version1Array[index]) - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
}
