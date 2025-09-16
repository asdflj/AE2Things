package com.asdflj.ae2thing.api.adapter.crafting;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.gui.GuiFluidCraftingWireless;
import com.glodblock.github.client.gui.container.ContainerCraftingWireless;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.nei.FluidPatternTerminalRecipeTransferHandler;

import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

public class FCCraftingTerminal implements ICraftingTerminalAdapter {

    private static boolean isRegisteredHandler = false;

    @Override
    public void openGui(EntityPlayerMP player, TileEntity tile, ForgeDirection face, Object target) {
        com.glodblock.github.inventory.InventoryHandler.openGui(
            player,
            player.worldObj,
            new com.glodblock.github.util.BlockPos(((IWirelessTerminal) target).getInventorySlot(), 0, 0),
            Objects.requireNonNull(face),
            com.glodblock.github.inventory.gui.GuiType.FLUID_CRAFTING_CONFIRM_ITEM);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerCraftingWireless.class;
    }

    @Override
    public void moveItems(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        if (isRegisteredHandler) return;
        try {
            final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            if (firstGui instanceof GuiFluidCraftingWireless
                && FluidPatternTerminalRecipeTransferHandler.craftSet.contains(recipe.getOverlayIdentifier())) {
                PacketNEIRecipe packet = new PacketNEIRecipe(packIngredients(firstGui, ingredients));
                NetworkHandler.instance.sendToServer(packet);
            }
        } catch (final Exception | Error ignored) {
            // NO-OP
        }
    }

    static {
        checkIsRegisterHandler();
    }

    private static void checkIsRegisterHandler() {
        try {
            Class.forName("com.glodblock.github.nei.FluidCraftingTransferHandler");
            isRegisteredHandler = true;
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {

        }

    }

    private NBTTagCompound packIngredients(GuiContainer gui, List<PositionedStack> ingredients) throws IOException {
        final NBTTagCompound recipe = new NBTTagCompound();
        for (final PositionedStack positionedStack : ingredients) {
            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;
            if (positionedStack.items != null && positionedStack.items.length > 0) {
                for (final Object o : gui.inventorySlots.inventorySlots) {
                    if (o instanceof SlotCraftingMatrix || o instanceof SlotFakeCraftingMatrix) {
                        Slot slot = (Slot) o;
                        if (slot.getSlotIndex() == col + row * 3) {
                            final NBTTagList tags = new NBTTagList();
                            final List<ItemStack> list = new LinkedList<>();
                            // prefer pure crystals.
                            for (int x = 0; x < positionedStack.items.length; x++) {
                                if (Platform.isRecipePrioritized(positionedStack.items[x])) {
                                    list.add(0, positionedStack.items[x]);
                                } else {
                                    list.add(positionedStack.items[x]);
                                }
                            }
                            for (final ItemStack is : list) {
                                final NBTTagCompound tag = new NBTTagCompound();
                                is.writeToNBT(tag);
                                tags.appendTag(tag);
                            }
                            recipe.setTag("#" + slot.getSlotIndex(), tags);
                            break;
                        }
                    }
                }
            }
        }
        return recipe;
    }
}
