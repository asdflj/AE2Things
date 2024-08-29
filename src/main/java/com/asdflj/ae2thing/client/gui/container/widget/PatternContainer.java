package com.asdflj.ae2thing.client.gui.container.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.container.ContainerInterfaceWireless;
import com.asdflj.ae2thing.client.gui.container.IPatternContainer;
import com.asdflj.ae2thing.client.gui.container.slot.ProcessingSlotPattern;
import com.asdflj.ae2thing.client.gui.container.slot.SlotPattern;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PatternContainer implements IPatternContainer, IOptionalSlotHost {

    protected final IInventory crafting;
    protected final IInventory output;
    protected final IInventory patternInv;
    protected final SlotPattern patternSlotIN;
    protected final SlotPattern patternSlotOUT;
    protected ProcessingSlotPattern[] craftingSlots;
    protected ProcessingSlotPattern[] outputSlots;
    private static final int CRAFTING_GRID_PAGES = 2;
    private static final int CRAFTING_GRID_WIDTH = 4;
    private static final int CRAFTING_GRID_HEIGHT = 4;
    private static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;

    private final IPatternTerminal it;
    private final ContainerInterfaceWireless container;

    public PatternContainer(InventoryPlayer ip, ITerminalHost host, ContainerInterfaceWireless container) {
        this.container = container;
        this.it = (IPatternTerminal) host;
        this.crafting = this.it.getInventoryByName(Constants.CRAFTING_EX);
        this.output = this.it.getInventoryByName(Constants.OUTPUT_EX);
        this.patternInv = this.it.getInventoryByName(Constants.PATTERN);
        this.craftingSlots = new ProcessingSlotPattern[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
        this.outputSlots = new ProcessingSlotPattern[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
        this.addSlotToContainer(
            this.patternSlotIN = new SlotPattern(
                SlotRestrictedInput.PlacableItemType.BLANK_PATTERN,
                patternInv,
                0,
                220,
                31,
                ip));
        this.addSlotToContainer(
            this.patternSlotOUT = new SlotPattern(
                SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN,
                patternInv,
                1,
                220,
                31 + 43,
                ip));
        this.patternSlotOUT.setStackLimit(1);
        for (int page = 0; page < CRAFTING_GRID_PAGES; page++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                    this.addSlotToContainer(
                        this.craftingSlots[x + y * CRAFTING_GRID_WIDTH
                            + page * CRAFTING_GRID_SLOTS] = new ProcessingSlotPattern(
                                crafting,
                                this,
                                x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS,
                                224,
                                -59,
                                x,
                                y,
                                x + 4));
                }
            }
            for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                    this.addSlotToContainer(
                        this.outputSlots[x * CRAFTING_GRID_HEIGHT + y
                            + page * CRAFTING_GRID_SLOTS] = new ProcessingSlotPattern(
                                output,
                                this,
                                x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS,
                                224 + 97,
                                -59,
                                -x,
                                y,
                                x));
                }
            }
        }
    }

    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            this.container.substitute = this.it.isSubstitution();
            this.container.combine = this.it.shouldCombine();
            this.container.beSubstitute = this.it.canBeSubstitute();
            this.container.prioritize = this.it.isPrioritize();
            if (container.inverted != it.isInverted() || container.activePage != it.getActivePage()) {
                container.inverted = it.isInverted();
                container.activePage = it.getActivePage();
                offsetSlots();
            }
        }
    }

    private void offsetSlots() {
        for (int page = 0; page < CRAFTING_GRID_PAGES; page++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                    this.craftingSlots[x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS]
                        .setHidden(page != container.activePage || x > 0 && container.inverted);
                    this.outputSlots[x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS]
                        .setHidden(page != container.activePage || x > 0 && !container.inverted);
                }
            }
        }
    }

    public void onUpdate(String field, Object oldValue, Object newValue) {
        if (field.equals("inverted") || field.equals("activePage")) {
            offsetSlots();
        }
    }

    protected Slot addSlotToContainer(Slot newSlot) {
        return Ae2Reflect.addSlotToContainer(this.container, newSlot);
    }

    @Override
    public IPatternTerminal getPatternTerminal() {
        return this.it;
    }

    @Override
    public void clear() {
        for (final Slot s : this.craftingSlots) {
            s.putStack(null);
        }
        for (final Slot s : this.outputSlots) {
            s.putStack(null);
        }
        this.detectAndSendChanges();
    }

    @Override
    public void doubleStacks(boolean isShift) {
        if (isShift) {
            if (canDouble(this.craftingSlots, 8) && canDouble(this.outputSlots, 8)) {
                doubleStacksInternal(this.craftingSlots, 8);
                doubleStacksInternal(this.outputSlots, 8);
            }
        } else {
            if (canDouble(this.craftingSlots, 2) && canDouble(this.outputSlots, 2)) {
                doubleStacksInternal(this.craftingSlots, 2);
                doubleStacksInternal(this.outputSlots, 2);
            }
        }
        this.detectAndSendChanges();
    }

    @Override
    public boolean isPatternTerminal() {
        return true;
    }

    @Override
    public boolean hasRefillerUpgrade() {
        return false;
    }

    @Override
    public void refillBlankPatterns(Slot slot) {

    }

    protected static boolean containsFluid(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots)
            .filter(SlotFake::isEnabled)
            .collect(Collectors.toList());
        long fluid = enabledSlots.stream()
            .filter(s -> Util.isFluidPacket(s.getStack()))
            .count();
        return fluid > 0;
    }

    protected static boolean nonNullSlot(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots)
            .filter(SlotFake::isEnabled)
            .collect(Collectors.toList());
        long object = enabledSlots.stream()
            .filter(s -> s.getStack() != null)
            .count();
        return object > 0;
    }

    protected ItemStack[] getInputs() {
        final ArrayList<ItemStack> input = new ArrayList<>();
        for (SlotFake craftingSlot : this.craftingSlots) {
            input.add(craftingSlot.getStack());
        }
        if (input.stream()
            .anyMatch(Objects::nonNull)) {
            return input.toArray(new ItemStack[0]);
        }
        return null;
    }

    protected ItemStack[] getOutputs() {
        final ArrayList<ItemStack> output = new ArrayList<>();
        for (final SlotFake outputSlot : this.outputSlots) {
            output.add(outputSlot.getStack());
        }
        if (output.stream()
            .anyMatch(Objects::nonNull)) {
            return output.toArray(new ItemStack[0]);
        }
        return null;
    }

    protected boolean notPattern(final ItemStack output) {
        if (output == null) {
            return true;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return false;
        }
        final IDefinitions definitions = AEApi.instance()
            .definitions();

        boolean isPattern = definitions.items()
            .encodedPattern()
            .isSameAs(output);
        isPattern |= definitions.materials()
            .blankPattern()
            .isSameAs(output);

        return !isPattern;
    }

    protected boolean checkHasFluidPattern() {
        boolean hasFluid = containsFluid(this.craftingSlots);
        boolean search = nonNullSlot(this.craftingSlots);
        if (!search) { // search=false -> inputs were empty
            return false;
        }
        hasFluid |= containsFluid(this.outputSlots);
        search = nonNullSlot(this.outputSlots);
        return hasFluid && search; // search=false -> outputs were empty
    }

    public void encodeItemPattern() {
        ItemStack output = this.patternSlotOUT.getStack();
        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();

        // if there is no input, this would be silly.
        if (in == null || out == null) {
            return;
        }
        // first check the output slots, should either be null, or a pattern
        if (output != null && this.notPattern(output)) {
            return;
        } // if nothing is there we should snag a new pattern.
        else if (output == null) {
            output = this.patternSlotIN.getStack();
            if (this.notPattern(output)) {
                return; // no blanks.
            }

            // remove one, and clear the input slot.
            output.stackSize--;
            if (output.stackSize == 0) {
                this.patternSlotIN.putStack(null);
            }

            // add a new encoded pattern.
            for (final ItemStack encodedPatternStack : AEApi.instance()
                .definitions()
                .items()
                .encodedPattern()
                .maybeStack(1)
                .asSet()) {
                output = encodedPatternStack;
            }
        } else if (output.getItem() instanceof ItemFluidEncodedPattern) {
            for (final ItemStack encodedPatternStack : AEApi.instance()
                .definitions()
                .items()
                .encodedPattern()
                .maybeStack(1)
                .asSet()) {
                output = encodedPatternStack;
            }
        }

        // encode the slot.
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for (final ItemStack i : in) {
            tagIn.appendTag(this.createItemTag(i));
        }

        for (final ItemStack i : out) {
            tagOut.appendTag(this.createItemTag(i));
        }

        encodedValue.setTag("in", tagIn);
        encodedValue.setTag("out", tagOut);
        encodedValue.setBoolean("crafting", false);
        encodedValue.setBoolean("substitute", this.container.substitute);
        encodedValue.setBoolean("beSubstitute", this.container.beSubstitute);
        encodedValue.setBoolean("prioritize", this.container.prioritize);
        output.setTagCompound(encodedValue);
        stampAuthor(output);
        this.patternSlotOUT.putStack(output);
    }

    protected ItemStack stampAuthor(ItemStack patternStack) {
        if (patternStack.stackTagCompound == null) {
            patternStack.stackTagCompound = new NBTTagCompound();
        }
        patternStack.stackTagCompound.setString("author", this.container.getPlayerInv().player.getCommandSenderName());
        return patternStack;
    }

    protected NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();
        if (i != null) {
            Util.writeItemStackToNBT(i, c);
        }
        return c;
    }

    @Override
    public void encode() {
        if (!checkHasFluidPattern()) {
            encodeItemPattern();
            return;
        }
        ItemStack stack = this.patternSlotOUT.getStack();
        if (stack == null) {
            stack = this.patternSlotIN.getStack();
            if (notPattern(stack)) {
                return;
            }
            if (stack.stackSize == 1) {
                this.patternSlotIN.putStack(null);
            } else {
                stack.stackSize--;
            }
            encodeFluidPattern();
        } else if (!notPattern(stack)) {
            encodeFluidPattern();
        }
    }

    protected static IAEItemStack[] collectInventory(Slot[] slots) {
        IAEItemStack[] stacks = new IAEItemStack[slots.length];
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = slots[i].getStack();
            if (stack != null) {
                if (stack.getItem() instanceof ItemFluidPacket) {
                    IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack));
                    if (dropStack != null) {
                        stacks[i] = dropStack;
                        continue;
                    }
                }
            }
            IAEItemStack aeStack = AEItemStack.create(stack);
            stacks[i] = aeStack;
        }
        return stacks;
    }

    protected void encodeFluidPattern() {
        ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);
        FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(this.craftingSlots));
        pattern.setOutputs(collectInventory(this.outputSlots));
        pattern.setCanBeSubstitute(this.container.beSubstitute ? 1 : 0);
        patternSlotOUT.putStack(stampAuthor(pattern.writeToStack()));
    }

    @Override
    public void encodeAndMoveToInventory() {
        ItemStack output = this.patternSlotOUT.getStack();
        if (output != null) {
            if (!this.container.getPlayerInv()
                .addItemStackToInventory(output)) {
                this.container.getPlayerInv().player.entityDropItem(output, 0);
            }
            this.patternSlotOUT.putStack(null);
        }
    }

    @Override
    public void encodeAllItemAndMoveToInventory() {
        ItemStack output = this.patternSlotOUT.getStack();
        if (output != null) {
            if (this.patternSlotIN.getStack() != null) output.stackSize += this.patternSlotIN.getStack().stackSize;
            if (!this.container.getPlayerInv()
                .addItemStackToInventory(output)) {
                this.container.getPlayerInv().player.entityDropItem(output, 0);
            }
            this.patternSlotOUT.putStack(null);
            this.patternSlotIN.putStack(null);
        }
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        if (idx < 4) // outputs
        {
            return this.container.inverted || idx == 0;
        } else {
            return !this.container.inverted || idx == 4;
        }
    }
}
