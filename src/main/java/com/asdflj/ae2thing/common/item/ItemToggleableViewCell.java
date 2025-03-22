package com.asdflj.ae2thing.common.item;

import static com.asdflj.ae2thing.client.textures.ItemTexture.View_Cell;
import static com.asdflj.ae2thing.client.textures.ItemTexture.View_Cell_Off;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.loader.IRegister;

import appeng.items.storage.ItemViewCell;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemToggleableViewCell extends ItemViewCell implements IRegister<ItemToggleableViewCell> {

    public ItemToggleableViewCell() {
        super();
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_TOGGLEABLE_VIEW_CELL);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_TOGGLEABLE_VIEW_CELL)
                .toString());
    }

    @Override
    public ItemToggleableViewCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_TOGGLEABLE_VIEW_CELL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        NBTTagCompound data = Platform.openNbtData(stack);
        if (!data.getBoolean(Constants.VIEW_CELL)) {
            return View_Cell.IIcon;
        } else {
            return View_Cell_Off.IIcon;
        }
    }

    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        if (GuiScreen.isShiftKeyDown()) {
            lines.add(I18n.format(NameConst.TT_TOGGLEABLE_VIEW_CELL));
        } else {
            lines.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
