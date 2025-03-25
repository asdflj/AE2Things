package com.asdflj.ae2thing.client.gui.container;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.container.slot.SlotEncodedPatternInput;
import com.asdflj.ae2thing.client.gui.container.slot.SlotReplaceFake;
import com.asdflj.ae2thing.inventory.item.PatternModifierInventory;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.recipe.StackInfo;

public class ContainerPatternModifier extends AEBaseContainer implements IPatternValueContainer {

    private final PatternModifierInventory it;
    private final SlotRestrictedInput[] pattern = new SlotRestrictedInput[36];
    private final SlotFake replaceSource;
    private final SlotFake replaceTarget;
    private static final ItemStack encodePattern = AEApi.instance()
        .definitions()
        .items()
        .encodedPattern()
        .maybeStack(1)
        .get();
    private final IInventory patterns;
    private final IInventory replace;

    public ContainerPatternModifier(InventoryPlayer ip, ITerminalHost host) {
        super(ip, host);
        this.it = (PatternModifierInventory) host;
        this.patterns = this.it.getInventoryByName(Constants.PATTERN);
        this.replace = this.it.getInventoryByName(Constants.REPLACE);
        for (int i = 0; i < this.patterns.getSizeInventory(); i++) {
            int x = (i % 9) * 18 + 8;
            int y = (i / 9) * 18 + 19;
            this.addSlotToContainer(this.pattern[i] = new SlotEncodedPatternInput(this.patterns, i, x, y, ip));
        }
        this.addSlotToContainer(this.replaceSource = new SlotReplaceFake(this.replace, 0, 8, 93));
        this.addSlotToContainer(this.replaceTarget = new SlotReplaceFake(this.replace, 1, 50, 93));
        this.lockPlayerInventorySlot(it.getInventorySlot());
        this.bindPlayerInventory(ip, 0, 125);
    }

    public ItemStack getSource() {
        return replaceSource.getStack();
    }

    public void clearPattern() {
        int blankPattern = 0;
        for (int i = 0; i < this.patterns.getSizeInventory(); i++) {
            ItemStack itemStack = this.patterns.getStackInSlot(i);
            if (itemStack != null) {
                blankPattern++;
                this.patterns.setInventorySlotContents(i, null);
            }
        }
        if (blankPattern <= 0) return;
        ItemStack pattern = AEApi.instance()
            .definitions()
            .materials()
            .blankPattern()
            .maybeStack(blankPattern)
            .get();
        if (!getPlayerInv().addItemStackToInventory(pattern)) {
            this.dropItem(pattern);
        }
    }

    public Slot getTargetSlot() {
        return this.replaceTarget;
    }

    public Slot getSourceSlot() {
        return this.replaceSource;
    }

    protected void dropItem(ItemStack is) {
        if (is == null || is.stackSize <= 0) return;
        ItemStack itemStack = is.copy();
        int i = itemStack.getMaxStackSize();
        while (itemStack.stackSize > 0) {
            if (i > itemStack.stackSize) {
                if (!getPlayerInv().addItemStackToInventory(itemStack.copy())) {
                    getPlayerInv().player.entityDropItem(itemStack.copy(), 0);
                }
                break;
            } else {
                itemStack.stackSize -= i;
                ItemStack item = itemStack.copy();
                item.stackSize = i;
                if (!getPlayerInv().addItemStackToInventory(item)) {
                    getPlayerInv().player.entityDropItem(item, 0);
                }
            }
        }
    }

    protected boolean checkHasFluidPattern(IAEItemStack[] in, IAEItemStack[] out) {
        return Arrays.stream(in)
            .filter(Objects::nonNull)
            .anyMatch(x -> ItemFluidDrop.isFluidStack(x.getItemStack()))
            || Arrays.stream(out)
                .filter(Objects::nonNull)
                .anyMatch(x -> ItemFluidDrop.isFluidStack(x.getItemStack()));
    }

    public void replacePattern() {
        if (!this.replaceSource.getHasStack()) return;
        ItemStack source = this.replaceSource.getStack();
        ItemStack target = this.replaceTarget.getStack();
        try {
            for (int i = 0; i < patterns.getSizeInventory(); i++) {
                ItemStack stack = patterns.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ICraftingPatternItem cpi) {
                    ICraftingPatternDetails details;
                    if (stack.getItem() instanceof ItemFluidEncodedPattern fluidEncodedPattern) {
                        details = fluidEncodedPattern
                            .getPatternForItem(stack, this.getInventoryPlayer().player.worldObj);
                    } else {
                        details = cpi.getPatternForItem(stack, this.getInventoryPlayer().player.worldObj);
                    }
                    IAEItemStack[] in = this.replacePattern(details.getInputs(), source, target, details);
                    IAEItemStack[] out = this.replacePattern(details.getOutputs(), source, target, details);
                    if (checkHasFluidPattern(in, out)) {
                        encodeFluidPattern(details, in, out, i, stack);
                    } else {
                        encode(details, in, out, i);
                    }

                }
            }
        } catch (Throwable ignored) {}
    }

    private void encodeFluidPattern(ICraftingPatternDetails details, IAEItemStack[] in, IAEItemStack[] out, int slot,
        ItemStack stack) {
        FluidPatternDetails fluidDetails;
        if (details instanceof FluidPatternDetails) {
            fluidDetails = (FluidPatternDetails) details;
        } else {
            ItemStack cp = ItemAndBlockHolder.PATTERN.stack();
            cp.setTagCompound(stack.getTagCompound());
            fluidDetails = (FluidPatternDetails) ItemAndBlockHolder.PATTERN
                .getPatternForItem(cp, this.getInventoryPlayer().player.worldObj);
        }
        fluidDetails.setInputs(in);
        fluidDetails.setOutputs(out);
        patterns.setInventorySlotContents(slot, fluidDetails.writeToStack());
    }

    private void encode(ICraftingPatternDetails cpi, IAEItemStack[] in, IAEItemStack[] out, int slot) {
        NBTTagList inList = list2tagList(in);
        NBTTagList outList = list2tagList(out);
        NBTTagCompound tag = (NBTTagCompound) Platform.openNbtData(cpi.getPattern())
            .copy();
        tag.setTag("in", inList);
        tag.setTag("out", outList);
        ItemStack cp = encodePattern.copy();
        cp.setTagCompound(tag);
        patterns.setInventorySlotContents(slot, cp);
    }

    private NBTTagList list2tagList(IAEItemStack[] list) {
        NBTTagList nbtTagList = new NBTTagList();
        for (IAEItemStack is : list) {
            if (is == null) {
                nbtTagList.appendTag(new NBTTagCompound());
            } else {
                nbtTagList.appendTag(createItemTag(is.getItemStack()));
            }
        }
        return nbtTagList;
    }

    private boolean isSameItem(ItemStack stack1, ItemStack stack2) {
        if (Util.isFluidPacket(stack1) || Util.isFluidPacket(stack2)) {
            FluidStack fs1 = StackInfo.getFluid(stack1);
            FluidStack fs2 = StackInfo.getFluid(stack2);
            if (fs1 != null && fs2 != null) {
                return fs1.getFluid()
                    .equals(fs2.getFluid());
            }
            return false;

        } else {
            return Platform.isSameItemPrecise(stack1, stack2);
        }
    }

    private IAEItemStack[] replacePattern(IAEItemStack[] list, ItemStack source, ItemStack target,
        ICraftingPatternDetails details) {
        IAEItemStack[] results = new IAEItemStack[list.length];
        for (int i = 0; i < list.length; i++) {
            IAEItemStack item = list[i];
            if (item == null) {
                results[i] = null;
                continue;
            }
            if (isSameItem(item.getItemStack(), source)) {
                if ((details.isCraftable() && target != null
                    && details.isValidItemForSlot(i, target, this.getPlayerInv().player.worldObj))
                    || (!details.isCraftable() && target != null)) {
                    if (Util.isFluidPacket(target)) {
                        IAEItemStack fluidDrop = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(target));
                        if (fluidDrop != null) {
                            fluidDrop.setStackSize(item.getStackSize());
                        }
                        results[i] = fluidDrop;
                        continue;
                    }
                    IAEItemStack t = AEItemStack.create(target);
                    t.setStackSize(item.getStackSize());
                    results[i] = t;
                } else if (target == null && !details.isCraftable()) {
                    results[i] = null;
                } else {
                    results[i] = item;
                }
            } else {
                results[i] = item;
            }
        }
        return results;
    }

    protected NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();
        if (i != null) {
            Util.writeItemStackToNBT(i, c);
        }
        return c;
    }

    @Override
    public boolean isValidContainer() {
        return true;
    }
}
