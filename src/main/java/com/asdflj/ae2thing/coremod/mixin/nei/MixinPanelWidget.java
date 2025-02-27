package com.asdflj.ae2thing.coremod.mixin.nei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.Util;

import appeng.api.storage.data.IDisplayRepo;
import appeng.client.gui.AEBaseGui;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
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

    @Inject(
        method = "handleClick",
        at = @At(
            value = "INVOKE",
            target = "Lcodechicken/nei/PanelWidget;getDraggedStackWithQuantity(I)Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER),
        remap = false,
        cancellable = true)
    public void handleClick(int mousex, int mousey, int button, CallbackInfoReturnable<Boolean> cir) {
        try {
            ItemStack is = this.getStackMouseOver(mousex, mousey);
            if (is != null) {
                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                if (gui instanceof AEBaseGui g) {
                    if (NEIClientUtils.altKey()) {
                        IDisplayRepo repo = Util.getDisplayRepo(g);
                        repo.setSearchString(is.getDisplayName());
                        Util.setSearchFieldText(g, Platform.getItemDisplayName(is));
                        draggedStack = null;
                        cir.setReturnValue(true);
                    } else if (g.inventorySlots instanceof AEBaseContainer c) {
                        c.setTargetStack(AEItemStack.create(is));
                        InventoryAction action;
                        if (GuiScreen.isCtrlKeyDown()) {
                            action = InventoryAction.PICKUP_SINGLE;
                        } else {
                            action = InventoryAction.PICKUP_OR_SET_DOWN;
                        }
                        final PacketInventoryAction p = new PacketInventoryAction(
                            action,
                            Ae2ReflectClient.getInventorySlots(g)
                                .size(),
                            gui instanceof GuiWirelessDualInterfaceTerminal ? -2 : 0);
                        NetworkHandler.instance.sendToServer(p);
                        draggedStack = null;
                        cir.setReturnValue(true);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

}
