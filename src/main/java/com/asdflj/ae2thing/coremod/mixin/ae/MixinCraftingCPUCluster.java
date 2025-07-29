package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;
import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import appeng.api.features.INetworkEncodable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftBranchFailure;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.misc.TileSecurity;

@Mixin(CraftingCPUCluster.class)
public abstract class MixinCraftingCPUCluster {

    @Unique
    private EntityPlayer player;

    private IAEItemStack output;

    private long networkKey = 0;

    @Inject(method = "submitJob", at = @At("RETURN"), remap = false)
    private void submitJob(IGrid g, ICraftingJob job, BaseActionSource src, ICraftingRequester requestingMachine,
        CallbackInfoReturnable<ICraftingLink> cir) {
        if (src instanceof PlayerSource ps && cir.getReturnValue() != null) {
            // real submit job
            Iterator<IGridNode> iterator = g.getMachines(TileSecurity.class)
                .iterator();
            if (iterator.hasNext()) {
                networkKey = ((TileSecurity) iterator.next()
                    .getMachine()).getLocatableSerial();
                player = ps.player;
                output = job.getOutput()
                    .copy();
            } else {
                setAsNull();
            }
        } else {
            setAsNull();
        }
    }

    private void setAsNull() {
        player = null;
        output = null;
        networkKey = 0;
    }

    @Inject(method = "handleCraftBranchFailure", at = @At("TAIL"), remap = false)
    private void handleCraftBranchFailure(CraftBranchFailure e, BaseActionSource src, CallbackInfo ci) {
        setAsNull();
    }

    @Inject(method = "completeJob", at = @At("TAIL"), remap = false)
    private void completeJob(CallbackInfo ci) {
        if (this.player != null && output != null && networkKey != 0) {
            for (int i = 0; i < this.player.inventory.mainInventory.length; i++) {
                ItemStack stack = this.player.inventory.mainInventory[i];
                if (isSameNetworkKey(stack)) return;
            }
            if (ModAndClassUtil.BAUBLES) {
                IInventory inv = BaublesUtil.getBaublesInv(this.player);
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (isSameNetworkKey(stack)) return;
                }
            }
        }
    }

    private boolean isSameNetworkKey(ItemStack item) {
        if (item != null && item.getItem() instanceof INetworkEncodable encodable) {
            String key = encodable.getEncryptionKey(item);
            if (key != null && key.equals(Long.toString(networkKey))) {
                SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate(Constants.MessageType.NOTIFICATION);
                piu.appendItem(output);
                AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) this.player);
                setAsNull();
                return true;
            }
        }
        return false;
    }

}
