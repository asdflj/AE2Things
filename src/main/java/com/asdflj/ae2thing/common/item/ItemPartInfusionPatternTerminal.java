package com.asdflj.ae2thing.common.item;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.parts.PartInfusionPatternTerminal;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartInfusionPatternTerminal extends BaseItem implements IPartItem {

    public ItemPartInfusionPatternTerminal() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_INFUSION_PATTERN_TERMINAL);
        AEApi.instance()
            .partHelper()
            .setItemBusRenderer(this);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float xOffset, float yOffset, float zOffset) {
        return AEApi.instance()
            .partHelper()
            .placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        return new PartInfusionPatternTerminal(is);
    }

    @Override
    public void registerIcons(IIconRegister _iconRegister) {}

    @Override
    public ItemPartInfusionPatternTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_INFUSION_PATTERN_TERMINAL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }
}
