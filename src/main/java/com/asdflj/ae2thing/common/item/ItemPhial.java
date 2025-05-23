package com.asdflj.ae2thing.common.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.ItemEssence;
import thaumicenergistics.common.fluids.GaseousEssentia;

public class ItemPhial extends ItemEssence implements IRegister<ItemPhial> {

    public ItemPhial() {
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName("ItemEssence");
    }

    @Override
    public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
        float f1, float f2, float f3) {
        return false;
    }

    @Override
    public ItemPhial register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PHIAL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("unchecked")
    public void getSubItems(Item par1, CreativeTabs tab, List par3List) {
        par3List.addAll(getItems());
    }

    public static List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(ItemAndBlockHolder.PHIAL, 1, 0));
        for (Aspect tag : Aspect.aspects.values()) {
            ItemStack i = new ItemStack(ItemAndBlockHolder.PHIAL, 1, 1);
            ItemAndBlockHolder.PHIAL.setAspects(i, (new AspectList()).add(tag, 1));
            list.add(i);
        }
        return list;
    }

    public static IAEFluidStack getAeEssentiaStack(IAEItemStack item) {
        Aspect aspect = getAspect(item);
        if (aspect == null) return null;
        return newEssentiaStack(aspect, item.getStackSize());
    }

    public static FluidStack getEssentiaStack(ItemStack item) {
        Aspect aspect = getAspect(item);
        if (aspect == null) return null;
        return newEssentiaStack(aspect, item.stackSize).getFluidStack();
    }

    public static Aspect getAspect(IAEItemStack item) {
        return getAspect(item.getItemStack());
    }

    public static IAEFluidStack newEssentiaStack(Aspect aspect, long size) {
        FluidStack fs = new FluidStack(GaseousEssentia.getGasFromAspect(aspect), 1);
        IAEFluidStack ifs = AEFluidStack.create(fs);
        ifs.setStackSize(size * AspectUtil.R);
        return ifs;
    }

    @Nullable
    public static IAEItemStack newAeStack(@Nullable IAEFluidStack fluid) {
        if (fluid != null && fluid.getStackSize() >= 0 && fluid.getFluid() instanceof GaseousEssentia) {
            ItemStack phial = new ItemStack(ItemAndBlockHolder.PHIAL, 1, 1);
            Aspect aspect = AspectUtil.getAspectFromGas(fluid.getFluidStack());
            ItemPhial.setAspects(phial, aspect);
            IAEItemStack item = AEItemStack.create(phial);
            item.setStackSize(fluid.getStackSize() / AspectUtil.R);
            return item;
        }
        return null;
    }

    public static ItemStack newStack(Aspect aspect, int size) {
        ItemStack is = new ItemStack(ItemAndBlockHolder.PHIAL, size, 1);
        setAspects(is, aspect, 1);
        return is;
    }

    public static Aspect getAspect(ItemStack item) {
        if (item.getItem() instanceof ItemPhial && item.hasTagCompound()) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(item.getTagCompound());
            if (aspects.size() > 0) {
                Optional<Aspect> result = Arrays.stream(aspects.getAspects())
                    .findFirst();
                if (result.isPresent()) return result.get();
            }
        }
        return null;
    }

    public static AspectList getAspectList(ItemStack item) {
        Aspect aspect = getAspect(item);
        AspectList aspects = new AspectList();
        if (aspect == null) return aspects;
        aspects.add(aspect, item.stackSize);
        return aspects;
    }

    public static void setAspects(ItemStack item, Aspect aspect) {
        setAspects(item, aspect, 1);
    }

    public static void setAspects(ItemStack item, Aspect aspect, int amount) {
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        AspectList list = new AspectList();
        list.add(aspect, amount);
        list.writeToNBT(item.getTagCompound());
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        Aspect aspect = getAspect(itemStack);
        if (aspect == null) {
            return super.getItemStackDisplayName(itemStack);
        }
        return String.format("%s-%s", super.getItemStackDisplayName(itemStack), aspect.getName());
    }

    public static boolean isItemPhial(IAEItemStack item) {
        return item.getItem() instanceof ItemPhial;
    }

    public static boolean isItemPhial(ItemStack item) {
        return item.getItem() instanceof ItemPhial;
    }
}
