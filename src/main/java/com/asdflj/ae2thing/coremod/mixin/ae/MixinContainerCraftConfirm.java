package com.asdflj.ae2thing.coremod.mixin.ae;

import static com.asdflj.ae2thing.api.Constants.MessageType.ADD_PINNED_ITEM;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.TheUtil;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftConfirm;

@Mixin(ContainerCraftConfirm.class)
public abstract class MixinContainerCraftConfirm extends AEBaseContainer {

    private IAEItemStack is = null;

    public MixinContainerCraftConfirm(InventoryPlayer ip, ITerminalHost anchor) {
        super(ip, anchor);
    }

    @Inject(method = "setItemToCraft", at = @At("HEAD"), remap = false)
    public void setItemToCraft(IAEItemStack itemToCraft, CallbackInfo ci) {
        if (itemToCraft != null) {
            is = itemToCraft.copy();
        }
    }

    @Inject(method = "setAutoStart", at = @At("HEAD"), remap = false)
    public void setAutoStart(boolean autoStart, CallbackInfo ci) {
        if (autoStart && is != null) {
            if (ModAndClassUtil.THE && TheUtil.isItemCraftingAspect(is)) {
                is = TheUtil.itemCraftingAspect2FluidDrop(is);
            }
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate(ADD_PINNED_ITEM);
            piu.appendItem(is);
            AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) this.getPlayerInv().player);
            is = null;
        }
    }
}
