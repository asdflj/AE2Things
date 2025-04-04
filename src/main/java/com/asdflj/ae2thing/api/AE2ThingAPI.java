package com.asdflj.ae2thing.api;

import static com.asdflj.ae2thing.nei.NEI_TH_Config.getConfigValue;
import static net.minecraft.init.Items.glass_bottle;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.Tags;
import com.asdflj.ae2thing.client.event.NotificationEvent;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.fluids.Mana;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.network.CPacketFindCellItem;
import com.asdflj.ae2thing.network.CPacketSwitchGuis;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.Grid;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class AE2ThingAPI implements IAE2ThingAPI {

    public static final ItemStack BUCKET = new ItemStack(Items.bucket, 1);
    public static final ItemStack PHIAL = createEmptyPhial();
    public static final ItemStack GLASS_BOTTLE = new ItemStack(glass_bottle, 1);

    public static int maxSelectionRows = 5;
    private static final Fluid mana = new Mana();
    private static final AE2ThingAPI API = new AE2ThingAPI();
    public static final int CRAFTING_HISTORY_SIZE = Config.craftingHistorySize;
    private static final Set<Class<? extends Item>> backpackItems = new HashSet<>();
    private StorageManager storageManager = null;

    private static ItemStack fluidContainer = BUCKET;
    public static final ReadableNumberConverter readableNumber = ReadableNumberConverter.INSTANCE;

    public static AE2ThingAPI instance() {
        return API;
    }

    private static ItemStack createEmptyPhial() {
        if (ModAndClassUtil.THE) {
            return AspectUtil.HELPER.createEmptyPhial();
        }
        return null;
    }

    public Terminal terminal() {
        return Terminal.API;
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
    @SideOnly(Side.CLIENT)
    public Pinned getPinned() {
        return Pinned.INSTANCE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openBackpackTerminal() {
        AE2Thing.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.BACKPACK_TERMINAL));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openDualinterfaceTerminal() {
        AE2Thing.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openTerminalMenu() {
        AE2Thing.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.TERMINAL_MENU));
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
        fluidContainer = item;
    }

    @Override
    public ItemStack getDefaultFluidContainer() {
        return fluidContainer;
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
        return mana;
    }

    @Override
    public void findCellItem(ItemStack item) {
        AE2Thing.proxy.netHandler.sendToServer(new CPacketFindCellItem(item));
    }

    @Override
    public long getStorageMyID(Grid grid) {
        return Ae2Reflect.getMyStorage(grid)
            .getID();
    }

    @Override
    public LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> getHistory(Grid grid) {
        return CraftingDebugHelper.getHistory()
            .getOrDefault(getStorageMyID(grid), new LimitedSizeLinkedList<>());
    }

    @Override
    public LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> getHistory(long networkID) {
        return CraftingDebugHelper.getHistory()
            .getOrDefault(networkID, new LimitedSizeLinkedList<>());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void pushHistory(long networkID, LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> infos) {
        CraftingDebugHelper.getHistory()
            .clear();
        CraftingDebugHelper.getHistory()
            .put(networkID, infos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void saveHistory() {
        if (CraftingDebugHelper.getHistory()
            .isEmpty()) return;
        String json = CraftingDebugHelper.getGson()
            .toJson(CraftingDebugHelper.getHistory());
        File file = new File((File) FMLInjectionData.data()[6], Constants.DEBUG_CARD_EXPORT_FILENAME);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            player.addChatComponentMessage(
                new ChatComponentText(
                    I18n.format(NameConst.CRAFTING_DEBUG_CARD_EXPORT_FILE, Constants.DEBUG_CARD_EXPORT_FILENAME)));
        } catch (Exception ignored) {}
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCraftingCompleteNotification(IAEItemStack item) {
        if (getConfigValue(ButtonConstants.CRAFTING_NOTIFICATION)) {
            MinecraftForge.EVENT_BUS.post(new NotificationEvent(item));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addNotification(String tile, String Content, ItemStack item) {
        if (getConfigValue(ButtonConstants.CRAFTING_NOTIFICATION)) {
            MinecraftForge.EVENT_BUS.post(new NotificationEvent(tile, Content, item));
        }
    }

}
