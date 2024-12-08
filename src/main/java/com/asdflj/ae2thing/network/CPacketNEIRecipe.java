package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.IPartitionList;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CPacketNEIRecipe implements IMessage {

    private ItemStack[][] recipe;
    private NBTTagCompound r;

    public CPacketNEIRecipe() {}

    public CPacketNEIRecipe(NBTTagCompound r) {
        this.r = r;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(buf.array());
            bytes.skip(1);
            final NBTTagCompound comp = CompressedStreamTools.readCompressed(bytes);
            if (comp != null) {
                this.recipe = new ItemStack[9][];
                for (int x = 0; x < this.recipe.length; x++) {
                    final NBTTagList list = comp.getTagList("#" + x, 10);
                    if (list.tagCount() > 0) {
                        this.recipe[x] = new ItemStack[list.tagCount()];
                        for (int y = 0; y < list.tagCount(); y++) {
                            NBTTagCompound tag = list.getCompoundTagAt(y);
                            ItemStack itemStack = ItemStack.loadItemStackFromNBT(tag);
                            // Set the stack size again, but load it as a short
                            if (itemStack != null) {
                                itemStack.stackSize = tag.getShort("Count");
                            }

                            this.recipe[x][y] = itemStack;
                        }
                    }
                }
            }
        } catch (IOException ignored) {

        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            final ByteBuf data = Unpooled.buffer();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(bytes);

            CompressedStreamTools.writeCompressed(r, outputStream);
            data.writeBytes(bytes.toByteArray());
            data.capacity(data.readableBytes());
            buf.writeBytes(data);

        } catch (IOException ignored) {

        }

    }

    public static class Handler implements IMessageHandler<CPacketNEIRecipe, IMessage> {

        private ItemStack[][] recipe;

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public IMessage onMessage(CPacketNEIRecipe message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof ContainerCraftingTerminal cct) {
                this.recipe = message.recipe;
                serverPacketData(ctx.getServerHandler().playerEntity, cct);
            }
            return null;
        }

        private ItemStack extractItemFromPlayerInventory(final EntityPlayer player, final Actionable mode,
            final ItemStack patternItem) {
            final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
            final AEItemStack request = AEItemStack.create(patternItem);
            final boolean isSimulated = mode == Actionable.SIMULATE;
            final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE
                || patternItem.hasTagCompound()
                || patternItem.isItemStackDamageable();

            if (!checkFuzzy) {
                if (isSimulated) {
                    return ia.simulateRemove(1, patternItem, null);
                } else {
                    return ia.removeItems(1, patternItem, null);
                }
            } else {
                if (isSimulated) {
                    return ia.simulateSimilarRemove(1, patternItem, FuzzyMode.IGNORE_ALL, null);
                } else {
                    return ia.removeSimilarItems(1, patternItem, FuzzyMode.IGNORE_ALL, null);
                }
            }
        }

        @SuppressWarnings("all")
        public void serverPacketData(final EntityPlayer player, ContainerCraftingTerminal cct) {
            final EntityPlayerMP pmp = (EntityPlayerMP) player;
            final IInventory craftMatrix = cct.getInventoryByName(Constants.CRAFTING);
            final IInventory playerInventory = cct.getInventoryByName(Constants.PLAYER);

            final IEnergySource energy = cct.getPowerSource();

            final Actionable realForFake = cct.useRealItems() ? Actionable.MODULATE : Actionable.SIMULATE;

            if (this.recipe != null) {
                final InventoryCrafting testInv = new InventoryCrafting(new ContainerNull(), 3, 3);
                for (int x = 0; x < 9; x++) {
                    if (this.recipe[x] != null && this.recipe[x].length > 0) {
                        testInv.setInventorySlotContents(x, this.recipe[x][0]);
                    }
                }

                final IRecipe r = Platform.findMatchingRecipe(testInv, pmp.worldObj);

                if (r != null) {
                    final ItemStack is = r.getCraftingResult(testInv);

                    if (is != null) {
                        final IMEMonitor<IAEItemStack> storage = cct.getMonitor();
                        final IItemList all = storage.getStorageList();
                        final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(cct.getViewCells());

                        for (int x = 0; x < craftMatrix.getSizeInventory(); x++) {
                            final ItemStack patternItem = testInv.getStackInSlot(x);

                            ItemStack currentItem = craftMatrix.getStackInSlot(x);
                            if (currentItem != null) {
                                testInv.setInventorySlotContents(x, currentItem);
                                final ItemStack newItemStack = r.matches(testInv, pmp.worldObj)
                                    ? r.getCraftingResult(testInv)
                                    : null;
                                testInv.setInventorySlotContents(x, patternItem);

                                if (newItemStack == null || !Platform.isSameItemPrecise(newItemStack, is)) {
                                    final IAEItemStack in = AEItemStack.create(currentItem);
                                    if (in != null) {
                                        final IAEItemStack out = realForFake == Actionable.SIMULATE ? null
                                            : Platform.poweredInsert(energy, storage, in, cct.getActionSource());
                                        if (out != null) {
                                            craftMatrix.setInventorySlotContents(x, out.getItemStack());
                                        } else {
                                            craftMatrix.setInventorySlotContents(x, null);
                                        }

                                        currentItem = craftMatrix.getStackInSlot(x);
                                    }
                                }
                            }

                            // True if we need to fetch an item for the recipe
                            if (patternItem != null && currentItem == null) {
                                // Grab from network by recipe
                                ItemStack whichItem = Platform.extractItemsByRecipe(
                                    energy,
                                    cct.getActionSource(),
                                    storage,
                                    player.worldObj,
                                    r,
                                    is,
                                    testInv,
                                    patternItem,
                                    x,
                                    all,
                                    realForFake,
                                    filter);

                                // If that doesn't get it, grab exact items from network (?)
                                // TODO see if this code is necessary
                                if (whichItem == null) {
                                    for (int y = 0; y < this.recipe[x].length; y++) {
                                        final IAEItemStack request = AEItemStack.create(this.recipe[x][y]);
                                        if (request != null) {
                                            if (filter == null || filter.isListed(request)) {
                                                request.setStackSize(1);
                                                final IAEItemStack out = Platform
                                                    .poweredExtraction(energy, storage, request, cct.getActionSource());
                                                if (out != null) {
                                                    whichItem = out.getItemStack();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                // If that doesn't work, grab from the player's inventory
                                if (whichItem == null && playerInventory != null) {
                                    whichItem = this.extractItemFromPlayerInventory(player, realForFake, patternItem);
                                }

                                craftMatrix.setInventorySlotContents(x, whichItem);
                            }
                        }
                        cct.onCraftMatrixChanged(craftMatrix);
                    }
                }
            }
        }
    }

}
