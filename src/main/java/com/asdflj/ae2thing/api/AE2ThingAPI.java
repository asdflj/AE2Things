package com.asdflj.ae2thing.api;

import static net.minecraft.init.Items.glass_bottle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.Tags;
import com.asdflj.ae2thing.common.ManaFluid;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.network.CPacketSwitchGuis;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class AE2ThingAPI implements IAE2ThingAPI {

    public static final ItemStack BUCKET = new ItemStack(Items.bucket, 1);
    public static final ItemStack PHIAL = createEmptyPhial();
    public static final ItemStack GLASS_BOTTLE = new ItemStack(glass_bottle, 1);
    public static int maxPinSize = 9;
    public static int maxSelectionRows = 5;
    public static final Fluid Mana = new ManaFluid();
    private static final AE2ThingAPI API = new AE2ThingAPI();

    private final Set<Class<? extends Item>> backpackItems = new HashSet<>();
    private StorageManager storageManager = null;
    private final List<IAEItemStack> pinItems = new ArrayList<>();
    private ItemStack fluidContainer = BUCKET;

    public static AE2ThingAPI instance() {
        return API;
    }

    private static ItemStack createEmptyPhial() {
        if (ModAndClassUtil.THE) {
            return AspectUtil.HELPER.createEmptyPhial();
        }
        return null;
    }

    @Override
    public boolean isBlacklistedInStorage(Item item) {
        if (item instanceof IBackpackItem) return true;
        for (Class<? extends Item> cls : backpackItems) {
            if (cls.isInstance(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void blacklistItemInStorage(Class<? extends Item> item) {
        backpackItems.add(item);
    }

    @Override
    public void addBackpackItem(Class<? extends Item> item) {
        blacklistItemInStorage(item);
    }

    @Override
    public boolean isBackpackItem(Item item) {
        return isBlacklistedInStorage(item);
    }

    @Override
    public boolean isBackpackItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() != null && isBackpackItem(itemStack.getItem());
    }

    @Override
    public IInventory getBackpackInv(ItemStack is) {
        if (is == null || is.getItem() == null) return null;
        if (is.getItem() instanceof IBackpackItem ibi) {
            return ibi.getInventory(is);
        }
        return null;
    }

    @Override
    public boolean isBackpackItemInv(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        return is.getItem() instanceof IBackpackItem;
    }

    @Override
    public StorageManager getStorageManager() {
        return storageManager;
    }

    @Override
    public void setStorageManager(StorageManager manager) {
        storageManager = manager;
    }

    @Override
    public List<IAEItemStack> getPinItems() {
        return this.pinItems;
    }

    @Override
    public void setPinItems(List<IAEItemStack> items) {
        this.pinItems.clear();
        this.pinItems.addAll(items);
    }

    @Override
    public void togglePinItems(IAEItemStack stack) {
        if (stack == null || this.pinItems.remove(stack)) return;
        this.pinItems.add(stack);
        if (this.pinItems.size() > maxPinSize) {
            List<IAEItemStack> tmp = new ArrayList<>(
                this.pinItems.subList(this.pinItems.size() - maxPinSize, this.pinItems.size()));
            this.pinItems.clear();
            this.pinItems.addAll(tmp);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openBackpackTerminal() {
        AE2Thing.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.BACKPACK_TERMINAL));
    }

    @Override
    public ItemStack getFluidContainer(IAEFluidStack fluid) {
        return getFluidContainer(fluid.getFluidStack());
    }

    @Override
    public ItemStack getFluidContainer(FluidStack fluid) {
        if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fluid)) {
            return PHIAL;
        } else if (canFillContainer(BUCKET, fluid)) {
            return BUCKET;
        } else if (getDefaultFluidContainer() != BUCKET && canFillContainer(getDefaultFluidContainer(), fluid)) {
            return getDefaultFluidContainer();
        } else {
            return GLASS_BOTTLE;
        }
    }

    @Override
    public void setDefaultFluidContainer(ItemStack item) {
        this.fluidContainer = item;
    }

    @Override
    public ItemStack getDefaultFluidContainer() {
        return this.fluidContainer;
    }

    private boolean canFillContainer(ItemStack is, FluidStack fluidStack) {
        MutablePair<Integer, ItemStack> result = Util.FluidUtil.fillStack(is, fluidStack);
        return result != null && result.left != 0;
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }

    @Override
    public Fluid getMana() {
        return Mana;
    }

}
