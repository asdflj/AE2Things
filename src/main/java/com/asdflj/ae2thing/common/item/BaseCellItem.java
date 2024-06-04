package com.asdflj.ae2thing.common.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.storage.Constants;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.ItemDiskCloneInventory;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.IItemInventory;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.storage.StorageChannel;
import appeng.util.Platform;

public abstract class BaseCellItem extends BaseItem implements IItemInventory {

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemDiskCloneInventory(player.inventory.mainInventory[x], "inv", player, x);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        if (player.isSneaking()) {
            NBTTagCompound data = Platform.openNbtData(item);
            if (data.getBoolean(Constants.COPY)) {
                item.setTagCompound(null);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, item);
            }
        } else {
            InventoryHandler.openGui(
                player,
                w,
                new BlockPos(player.inventory.currentItem, 0, 0),
                ForgeDirection.UNKNOWN,
                this.guiGuiType(item));
        }
        return super.onItemRightClick(item, w, player);
    }

    private GuiType guiGuiType(ItemStack item) {
        return GuiType.DISK_CLONE;
    }

    public abstract StorageChannel getChannel();

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        NBTTagCompound data = Platform.openNbtData(stack);
        if (data.getBoolean(Constants.COPY)) lines.add(I18n.format(NameConst.TT_COPY));
        if (GuiScreen.isShiftKeyDown()) {
            lines.addAll(
                Arrays.asList(
                    I18n.format(NameConst.TT_DISK_CLONE_DESC)
                        .split("\\\\n")));
        } else {
            lines.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
