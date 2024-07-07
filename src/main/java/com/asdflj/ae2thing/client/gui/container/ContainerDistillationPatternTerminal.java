package com.asdflj.ae2thing.client.gui.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.parts.PartDistillationPatternTerminal;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.IDefinitions;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotRestrictedInput;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ScanManager;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.items.ItemEnum;

public class ContainerDistillationPatternTerminal extends ContainerMonitor
    implements IOptionalSlotHost, IPatternContainer {

    private static final int CRAFTING_GRID_WIDTH = 4;
    private static final int CRAFTING_GRID_HEIGHT = 4;
    private static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;
    protected IGridNode networkNode;
    protected final SlotRestrictedInput[] cellView = new SlotRestrictedInput[5];
    protected SlotFakeCraftingMatrix[] craftingSlots = new SlotFakeCraftingMatrix[1];
    protected SlotPatternOutputs[] outputSlots = new SlotPatternOutputs[CRAFTING_GRID_SLOTS];
    protected final SlotRestrictedInput patternSlotIN;
    protected final SlotRestrictedInput patternSlotOUT;
    protected SlotRestrictedInput patternRefiller;

    protected final IInventory crafting;
    protected final IInventory output;
    protected final IInventory patternInv;

    @GuiSync(98)
    public boolean hasPower = false;
    @GuiSync(99)
    public boolean canAccessViewCells;

    @GuiSync(100)
    public boolean craftingMode = true;
    private final PartDistillationPatternTerminal it;
    private ItemStack lastScanItem = null;

    public ContainerDistillationPatternTerminal(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.it = (PartDistillationPatternTerminal) monitorable;
        this.canAccessViewCells = false;
        this.crafting = this.it.getInventoryByName("crafting");
        this.output = this.it.getInventoryByName("output");
        this.patternInv = this.it.getInventoryByName("pattern");

        if (monitorable instanceof IViewCellStorage) {
            for (int y = 0; y < 5; y++) {
                this.cellView[y] = new SlotRestrictedInput(
                    SlotRestrictedInput.PlacableItemType.VIEW_CELL,
                    ((IViewCellStorage) monitorable).getViewCellStorage(),
                    y,
                    206,
                    y * 18 + 8,
                    this.getInventoryPlayer());
                this.cellView[y].setAllowEdit(this.canAccessViewCells);
                this.addSlotToContainer(this.cellView[y]);
            }
        }

        this.addSlotToContainer(
            this.patternSlotIN = new SlotRestrictedInput(
                SlotRestrictedInput.PlacableItemType.BLANK_PATTERN,
                patternInv,
                0,
                147,
                -72 - 9,
                this.getInventoryPlayer()));
        this.addSlotToContainer(
            this.patternSlotOUT = new SlotRestrictedInput(
                SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN,
                patternInv,
                1,
                147,
                -72 + 34,
                this.getInventoryPlayer()));
        this.patternSlotOUT.setStackLimit(1);

        this.addSlotToContainer(this.craftingSlots[0] = new SlotFakeCraftingMatrix(this.crafting, 0, 12, -56));

        for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                this.addSlotToContainer(
                    this.outputSlots[x * CRAFTING_GRID_HEIGHT
                        + y] = new SlotPatternOutputs(output, this, x * CRAFTING_GRID_HEIGHT + y, 58, -83, x, y, 1));
                this.outputSlots[x * CRAFTING_GRID_HEIGHT + y].setRenderDisabled(false);
            }
        }
        if (this.isPatternTerminal()) {
            this.addSlotToContainer(
                this.patternRefiller = new SlotRestrictedInput(
                    SlotRestrictedInput.PlacableItemType.UPGRADES,
                    ((IUpgradeableHost) monitorable).getInventoryByName("upgrades"),
                    0,
                    206,
                    5 * 18 + 11,
                    this.getInventoryPlayer()));
        }

        if (this.hasRefillerUpgrade()) {
            refillBlankPatterns(patternSlotIN);
        }

        this.bindPlayerInventory(ip, 0, 0);
    }

    public void refillBlankPatterns(Slot slot) {
        if (Platform.isServer()) {
            ItemStack blanks = slot.getStack();
            int blanksToRefill = 64;
            if (blanks != null) blanksToRefill -= blanks.stackSize;
            if (blanksToRefill <= 0) return;
            final AEItemStack request = AEItemStack.create(
                AEApi.instance()
                    .definitions()
                    .materials()
                    .blankPattern()
                    .maybeStack(blanksToRefill)
                    .get());
            final IAEItemStack extracted = Platform
                .poweredExtraction(this.getPowerSource(), this.getCellInventory(), request, this.getActionSource());
            if (extracted != null) {
                if (blanks != null) blanks.stackSize += extracted.getStackSize();
                else {
                    blanks = extracted.getItemStack();
                }
                slot.putStack(blanks);
            }
        }
    }

    @Override
    public void onSlotChange(Slot s) {
        if (s == this.patternSlotOUT || s == this.craftingSlots[0] && Platform.isServer()) {
            for (final Object crafter : this.crafters) {
                final ICrafting icrafting = (ICrafting) crafter;

                for (final Object g : this.inventorySlots) {
                    if (g instanceof OptionalSlotFake || g instanceof SlotFakeCraftingMatrix) {
                        final Slot sri = (Slot) g;
                        icrafting.sendSlotContents(this, sri.slotNumber, sri.getStack());
                        if (g instanceof SlotFakeCraftingMatrix && !this.isCraftingMode()) {
                            this.scanSourceItem();
                        }
                    }
                }
                ((EntityPlayerMP) icrafting).isChangingQuantityOnly = false;
            }
            this.detectAndSendChanges();
        }
    }

    public PartDistillationPatternTerminal getPatternTerminal() {
        return this.it;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            if (this.isCraftingMode() != this.getPatternTerminal()
                .isCraftingRecipe()) {
                this.setCraftingMode(
                    this.getPatternTerminal()
                        .isCraftingRecipe());
                this.updateOrderOfOutputSlots();
            }
            this.updatePowerStatus();
            final boolean oldAccessible = this.canAccessViewCells;
            this.canAccessViewCells = this.hasAccess(SecurityPermissions.BUILD, false);
            if (this.canAccessViewCells != oldAccessible) {
                for (int y = 0; y < 5; y++) {
                    if (this.cellView[y] != null) {
                        this.cellView[y].setAllowEdit(this.canAccessViewCells);
                    }
                }
            }
        }
    }

    private void updateOrderOfOutputSlots() {
        for (final Slot s : this.outputSlots) {
            s.putStack(null);
        }
        this.lastScanItem = null;
    }

    private void setCraftingMode(boolean craftingRecipe) {
        this.craftingMode = craftingRecipe;
    }

    public void scanSourceItem() {
        ItemStack is = this.craftingSlots[0].getStack();
        if (this.lastScanItem != null && Platform.isSameItemPrecise(is, lastScanItem)) return;
        this.clearAspectSlots();
        if (is == null) return;
        this.lastScanItem = is;
        AspectList itemAspects = ThaumcraftApiHelper.getObjectAspects(is);
        itemAspects = ThaumcraftApiHelper.getBonusObjectTags(is, itemAspects);
        Aspect[] sortedAspects = null;
        // Does the item have any aspects?
        if ((itemAspects == null) || (itemAspects.size() == 0)) {
            // Done
            return;
        }

        // Generate hash
        int hash = ScanManager.generateItemHash(is.getItem(), is.getItemDamage());

        // Get the list of scanned objects
        List<String> list = Thaumcraft.proxy.getScannedObjects()
            .get(this.getPlayerInv().player.getCommandSenderName());

        // Assume all slot will have an aspect
        int numOfAspects = this.outputSlots.length;

        // Has the player scanned the item?
        boolean playerScanned = ((list != null) && ((list.contains("@" + hash)) || (list.contains("#" + hash))));
        if (playerScanned) {
            // Get sorted
            sortedAspects = itemAspects.getAspectsSortedAmount();

            // Set number to display
            numOfAspects = Math.min(numOfAspects, sortedAspects.length);
        }

        // Add each aspect
        Aspect aspect;
        for (int i = 0; i < numOfAspects; ++i) {
            // Create an itemstack
            ItemStack aspectItem = ItemEnum.CRAFTING_ASPECT.getStack();

            if (sortedAspects != null) {
                // Get the aspect
                aspect = sortedAspects[i];

                // Set the aspect
                ItemCraftingAspect.setAspect(aspectItem, aspect);

                // Set the size
                aspectItem.stackSize = itemAspects.getAmount(aspect);
            }
            // Put into slot
            this.outputSlots[i].putStack(aspectItem);
        }
        if (canDouble(this.outputSlots, is.stackSize)) {
            doubleStacksInternal(this.outputSlots, is.stackSize);
        }
    }

    private void clearAspectSlots() {
        for (Slot slot : this.outputSlots) {
            slot.putStack(null);
        }
    }

    @Override
    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("player")) {
            return this.getInventoryPlayer();
        }
        return this.getPatternTerminal()
            .getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    @Override
    public ItemStack[] getViewCells() {
        final ItemStack[] list = new ItemStack[this.cellView.length];
        for (int x = 0; x < this.cellView.length; x++) {
            list[x] = this.cellView[x].getStack();
        }
        return list;
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("canAccessViewCells")) {
            for (int y = 0; y < 5; y++) {
                if (this.cellView[y] != null) {
                    this.cellView[y].setAllowEdit(this.canAccessViewCells);
                }
            }
        }
        super.onUpdate(field, oldValue, newValue);
    }

    protected void updatePowerStatus() {
        try {
            if (this.networkNode != null) {
                this.setPowered(this.networkNode.isActive());
            } else if (this.getPowerSource() instanceof IEnergyGrid) {
                this.setPowered(((IEnergyGrid) this.getPowerSource()).isNetworkPowered());
            } else {
                this.setPowered(
                    this.getPowerSource()
                        .extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Throwable ignore) {}
    }

    protected void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    @Override
    public void processItemList() {
        super.processItemList();
        this.fluidMonitor.processItemList();
    }

    @Override
    public void removeCraftingFromCrafters(ICrafting c) {
        super.removeCraftingFromCrafters(c);
        this.fluidMonitor.removeCraftingFromCrafters(c);
    }

    @Override
    public void addCraftingToCrafters(ICrafting c) {
        super.addCraftingToCrafters(c);
        this.fluidMonitor.queueInventory(c);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.fluidMonitor.getMonitor() != null) this.fluidMonitor.removeListener();
    }

    @Override
    void setMonitor() {
        if (this.host instanceof IGridHost) {
            final IGridNode node = ((IGridHost) this.host).getGridNode(ForgeDirection.UNKNOWN);
            if (node != null) {
                this.networkNode = node;
                final IGrid g = node.getGrid();
                if (g != null) {
                    this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                    IStorageGrid storageGrid = g.getCache(IStorageGrid.class);
                    this.monitor.setMonitor(storageGrid.getItemInventory());
                    this.fluidMonitor.setMonitor(storageGrid.getFluidInventory(), storageGrid.getItemInventory());
                    this.monitor.setFluidMonitorObject(this.fluidMonitor);
                    if (this.monitor.getMonitor() == null) {
                        this.setValidContainer(false);
                    } else {
                        this.monitor.addListener();
                        this.fluidMonitor.addListener();
                        this.setCellInventory(this.monitor.getMonitor());
                    }
                }
            } else {
                this.setValidContainer(false);
            }
        }
    }

    public SlotRestrictedInput getCellViewSlot(final int index) {
        return this.cellView[index];
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
        ItemStack newStack) {

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

    protected NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();
        if (i != null) {
            Util.writeItemStackToNBT(i, c);
        }
        return c;
    }

    public void encode() {
        if (this.hasRefillerUpgrade()) refillBlankPatterns(this.patternSlotIN);
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
        output.setTagCompound(encodedValue);
        stampAuthor(output);
        this.patternSlotOUT.putStack(output);
    }

    protected ItemStack stampAuthor(ItemStack patternStack) {
        if (patternStack.stackTagCompound == null) {
            patternStack.stackTagCompound = new NBTTagCompound();
        }
        patternStack.stackTagCompound.setString("author", getPlayerInv().player.getCommandSenderName());
        return patternStack;
    }

    public void clear() {
        for (final Slot s : this.craftingSlots) {
            s.putStack(null);
        }
        for (final Slot s : this.outputSlots) {
            s.putStack(null);
        }
        this.lastScanItem = null;
        this.detectAndSendChanges();
    }

    public void doubleStacks(boolean isShift) {
        if (isCraftingMode()) return;
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

    private static void doubleStacksInternal(SlotFake[] slots, int mult) {
        List<SlotFake> enabledSlots = Arrays.stream(slots)
            .filter(SlotFake::isEnabled)
            .collect(Collectors.toList());
        for (final Slot s : enabledSlots) {
            ItemStack st = s.getStack();
            if (st != null) {
                st.stackSize *= mult;
            }
        }
    }

    private static boolean canDouble(SlotFake[] slots, int mult) {
        for (Slot s : slots) {
            ItemStack st = s.getStack();
            if (st != null) {
                long result = (long) s.getStack().stackSize * mult;
                if (result > Integer.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    public void encodeAndMoveToInventory() {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if (output != null) {
            if (!getPlayerInv().addItemStackToInventory(output)) {
                this.dropItem(output);
            }
            this.patternSlotOUT.putStack(null);
        }
        if (this.hasRefillerUpgrade()) refillBlankPatterns(patternSlotIN);
    }

    public void encodeAllItemAndMoveToInventory() {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if (output != null) {
            if (this.patternSlotIN.getStack() != null) output.stackSize += this.patternSlotIN.getStack().stackSize;
            if (!getPlayerInv().addItemStackToInventory(output)) {
                this.dropItem(output);
            }
            this.patternSlotOUT.putStack(null);
            this.patternSlotIN.putStack(null);
        }
        if (this.hasRefillerUpgrade()) refillBlankPatterns(patternSlotIN);
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }

    @Override
    public boolean isPatternTerminal() {
        return true;
    }

    @Override
    public boolean hasRefillerUpgrade() {
        return this.getPatternTerminal()
            .hasRefillerUpgrade();
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }
}
