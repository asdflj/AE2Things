package com.asdflj.ae2thing.coremod.mixin.nei;

import static com.asdflj.ae2thing.nei.NEI_TH_Config.getConfigValue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.adapter.terminal.ITerminal;
import com.asdflj.ae2thing.client.event.UpdateAmountTextEvent;
import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.util.Util;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.client.gui.AEBaseGui;
import appeng.client.me.ItemRepo;
import appeng.container.AEBaseContainer;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PanelWidget;
import codechicken.nei.Widget;
import codechicken.nei.guihook.IContainerTooltipHandler;

@Mixin(PanelWidget.class)
public abstract class MixinPanelWidget extends Widget implements IContainerTooltipHandler {

    @Shadow(remap = false)
    public abstract ItemStack getStackMouseOver(int mousex, int mousey);

    @Shadow(remap = false)
    public ItemStack draggedStack;

    @Inject(method = "handleClick", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void handleClick(int mousex, int mousey, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 2) return;
        try {
            ItemStack is = this.getStackMouseOver(mousex, mousey);
            if (is == null) return;
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof AEBaseGui g) {
                IDisplayRepo repo = Util.getDisplayRepo(g);
                if (repo == null) return;
                if (NEIClientUtils.altKey()) {
                    repo.setSearchString(is.getDisplayName());
                    Util.setSearchFieldText(g, Platform.getItemDisplayName(is));
                } else
                    if (g.inventorySlots instanceof AEBaseContainer c && getConfigValue(ButtonConstants.NEI_CRAFT_ITEM)
                        && repo instanceof ItemRepo itemRepo
                        && canCraftItem(itemRepo, is)) {
                            is = is.copy();
                            if (is.stackSize <= 0) {
                                is.stackSize = 1;
                            }
                            if (c.getTarget() instanceof IGuiItemObject o && o.getItemStack() != null
                                && o.getItemStack()
                                    .getItem() != null) {
                                openCraftAmount(
                                    o.getItemStack()
                                        .getItem()
                                        .getClass(),
                                    c,
                                    is);
                            } else if (c.getTarget() instanceof IPart p) {
                                ItemStack part = p.getItemStack(PartItemStack.Network);
                                if (part != null && part.getItem() != null) {
                                    openCraftAmount(
                                        part.getItem()
                                            .getClass(),
                                        c,
                                        is);
                                }
                            }
                        }
                draggedStack = null;
                cir.setReturnValue(true);
            }
        } catch (Exception ignored) {}
    }

    private boolean canCraftItem(ItemRepo repo, ItemStack is) {
        if (repo == null || is == null) return false;
        IAEItemStack item = repo.getAvailableItems()
            .findPrecise(AEItemStack.create(is));
        if (item == null) return false;
        return item.isCraftable();
    }

    private void openCraftAmount(Class<? extends Item> o, AEBaseContainer c, ItemStack is) {
        for (ITerminal terminal : AE2ThingAPI.instance()
            .terminal()
            .getTerminalSet()) {
            if (terminal.getClasses()
                .contains(o)) {
                c.setTargetStack(AEItemStack.create(is));
                terminal.openCraftAmount();
                MinecraftForge.EVENT_BUS.post(new UpdateAmountTextEvent(is.stackSize));
                break;
            }
        }
    }
}
