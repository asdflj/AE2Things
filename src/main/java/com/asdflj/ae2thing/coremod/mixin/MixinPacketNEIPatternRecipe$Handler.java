package com.asdflj.ae2thing.coremod.mixin;

import static com.github.vfyjxf.nee.nei.NEECraftingHandler.OUTPUT_KEY;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;
import com.github.vfyjxf.nee.utils.ItemUtils;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.container.AEBaseContainer;
import appeng.helpers.IContainerCraftingPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

@Mixin(value = PacketNEIPatternRecipe.Handler.class)
public abstract class MixinPacketNEIPatternRecipe$Handler {

    @Inject(method = "onMessage", at = @At(value = "TAIL"), remap = false)
    private void onMessage(PacketNEIPatternRecipe message, MessageContext ctx, CallbackInfoReturnable<IMessage> cir) {
        ae2Things$addThingCraftSupport(message, ctx);
    }

    @Unique
    public void ae2Things$addThingCraftSupport(PacketNEIPatternRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container container = player.openContainer;
        AccessorPacketNEIPatternRecipe msg = (AccessorPacketNEIPatternRecipe) message;
        if (container instanceof ContainerWirelessDualInterfaceTerminal && msg.getOutput() == null) {
            ((ContainerWirelessDualInterfaceTerminal) container).setCrafting(true);
            msg.callCraftingTableRecipeHandler((ContainerWirelessDualInterfaceTerminal) container, message);
        } else if (container instanceof ContainerWirelessDualInterfaceTerminal) {
            ((ContainerWirelessDualInterfaceTerminal) container).setCrafting(false);
            ae2Things$processRecipeHandler((ContainerWirelessDualInterfaceTerminal) container, msg);
        }
    }

    @Unique
    private void ae2Things$processRecipeHandler(AEBaseContainer container, AccessorPacketNEIPatternRecipe message) {
        final IContainerCraftingPacket cct = (IContainerCraftingPacket) container;
        final IGridNode node = cct.getNetworkNode();

        if (node != null) {
            final IGrid grid = node.getGrid();
            if (grid == null) {
                return;
            }
            final IStorageGrid inv = grid.getCache(IStorageGrid.class);
            final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
            final IInventory craftMatrix = cct.getInventoryByName(Constants.CRAFTING_EX);
            final IInventory outputMatrix = cct.getInventoryByName(Constants.OUTPUT_EX);
            ItemStack[] recipeInput = new ItemStack[craftMatrix.getSizeInventory()];
            ItemStack[] recipeOutput = new ItemStack[outputMatrix.getSizeInventory()];

            for (int i = 0; i < recipeInput.length; i++) {
                NBTTagCompound currentStack = (NBTTagCompound) message.getInput()
                    .getTag("#" + i);
                recipeInput[i] = currentStack == null ? null : ItemUtils.loadItemStackFromNBT(currentStack);
            }

            for (int i = 0; i < recipeOutput.length; i++) {
                NBTTagCompound currentStack = (NBTTagCompound) message.getOutput()
                    .getTag(OUTPUT_KEY + i);
                recipeOutput[i] = currentStack == null ? null : ItemUtils.loadItemStackFromNBT(currentStack);
            }
            if (inv != null && message.getInput() != null && security != null) {
                for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
                    ItemStack currentItem = null;
                    if (recipeInput[i] != null) {
                        currentItem = recipeInput[i].copy();
                    }
                    craftMatrix.setInventorySlotContents(i, currentItem);
                }

                for (int i = 0; i < outputMatrix.getSizeInventory(); i++) {
                    ItemStack currentItem = null;
                    if (recipeOutput[i] != null) {
                        currentItem = recipeOutput[i].copy();
                    }
                    outputMatrix.setInventorySlotContents(i, currentItem);
                }
                container.onCraftMatrixChanged(craftMatrix);
            }
        }
    }
}
