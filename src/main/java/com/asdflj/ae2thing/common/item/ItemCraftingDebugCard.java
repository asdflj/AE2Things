package com.asdflj.ae2thing.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.CraftingDebugCardObject;
import com.asdflj.ae2thing.api.CraftingDebugHelper;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.util.NameConst;

import appeng.tile.networking.TileController;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCraftingDebugCard extends BaseItem {

    public ItemCraftingDebugCard() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_CRAFTING_DEBUG_CARD);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_CRAFTING_DEBUG_CARD)
                .toString());
    }

    @Override
    public ItemCraftingDebugCard register() {
        GameRegistry.registerItem(this, NameConst.ITEM_CRAFTING_DEBUG_CARD, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (Platform.isServer()) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileController tc) {
                CraftingDebugCardObject obj = CraftingDebugHelper.getObject(itemstack);
                obj.sendRecordToPlayer(obj.getGrid(tc), player);
            }
        }
        return super.onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player) {
        if (player.isSneaking()) {
            CraftingDebugCardObject obj = CraftingDebugHelper.getObject(item);
            obj.setMode(obj.getNextMode());
            if (Platform.isClient()) {
                player.addChatComponentMessage(
                    new ChatComponentText(
                        I18n.format(NameConst.CRAFTING_DEBUG_CARD_CURRENT_MODE) + " " + getMode(obj)));
            }
        }
        return item;
    }

    private String getMode(CraftingDebugCardObject object) {
        String text;
        switch (object.getMode()) {
            case Player -> text = I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_REQUEST_TYPE_PLAYER);
            case Machine -> text = I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_REQUEST_TYPE_MACHINE);
            default -> text = I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_REQUEST_TYPE_EVERYTHING);
        }
        return text;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        CraftingDebugCardObject obj = CraftingDebugHelper.getObject(stack);
        lines.add(I18n.format(NameConst.CRAFTING_DEBUG_CARD_CURRENT_MODE) + " " + getMode(obj));
        if (isShiftKeyDown()) {
            lines.addAll(
                Arrays.asList(
                    I18n.format(NameConst.TT_CRAFTING_DEBUG_CARD_DESC)
                        .split("\\\\n")));
        } else {
            lines.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
