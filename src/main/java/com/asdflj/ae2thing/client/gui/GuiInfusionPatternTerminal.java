package com.asdflj.ae2thing.client.gui;

import static com.asdflj.ae2thing.api.Constants.MODE_CRAFTING;
import static com.asdflj.ae2thing.api.Constants.MODE_PROCESSING;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.client.gui.GuiFCImgButton;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;

public class GuiInfusionPatternTerminal extends GuiMonitor implements IGuiMonitorTerminal {

    private final ContainerInfusionPatternTerminal container;
    protected GuiTabButton tabCraftButton;
    protected GuiTabButton tabProcessButton;
    protected GuiFCImgButton combineEnableBtn;
    protected GuiFCImgButton combineDisableBtn;
    protected final boolean viewCell;
    protected final ItemStack[] myCurrentViewCells = new ItemStack[5];
    protected GuiImgButton encodeBtn;
    protected GuiImgButton doubleBtn;
    protected final GuiScrollbar processingScrollBar = new GuiScrollbar();

    public GuiInfusionPatternTerminal(InventoryPlayer inventory, ITerminalHost inv) {
        super(new ContainerInfusionPatternTerminal(inventory, inv));
        this.xSize = 195;
        this.ySize = 204;
        this.standardSize = this.xSize;
        (this.container = (ContainerInfusionPatternTerminal) this.inventorySlots).setGui(this);
        this.reservedSpace = 81;
        this.viewCell = inv instanceof IViewCellStorage;
        this.repo.setCache(this);
        processingScrollBar.setHeight(70)
            .setWidth(7)
            .setLeft(6)
            .setRange(0, 1, 1);
        processingScrollBar.setTexture(AE2Thing.MODID, "gui/pattern.png", 242, 0);
    }

    protected void repositionSlot(final AppEngSlot s) {
        if (s instanceof SlotDisabled || s.isPlayerSide()) {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
        } else {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 3;
        }
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        if (mouseButton == 3) {
            if (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix) {
                if (slot.getHasStack()) {
                    InventoryAction action = InventoryAction.SET_PATTERN_VALUE;
                    IAEItemStack stack = AEItemStack.create(slot.getStack());
                    ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                    for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
                        if (slot.equals(this.inventorySlots.inventorySlots.get(i))) {
                            AE2Thing.proxy.netHandler.sendToServer(new CPacketInventoryAction(action, i, 0, stack));
                        }
                    }
                    return;
                }
            }
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);

    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
            GuiText.PatternTerminalEx.getLocal(),
            8,
            this.ySize - 96 + 1 - this.getReservedSpace(),
            GuiColors.PatternTerminalEx.getColor());
        this.fontRendererObj.drawString(this.getGuiDisplayName(GuiText.Terminal.getLocal()), 8, 6, 4210752);
        updateButton(this.tabCraftButton, this.container.isCraftingMode());
        updateButton(this.tabProcessButton, !this.container.isCraftingMode());
        updateButton(this.combineEnableBtn, this.container.combine);
        updateButton(this.combineDisableBtn, !this.container.combine);
        this.processingScrollBar.draw(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        processingScrollBar.setCurrentScroll(container.activePage);
        super.drawScreen(mouseX, mouseY, btn);
    }

    protected void updateButton(GuiButton button, boolean vis) {
        if (button != null) {
            button.visible = vis;
        }
    }

    public int getReservedSpace() {
        return this.reservedSpace;
    }

    protected String getBackground() {
        if (this.container.isCraftingMode()) {
            return "gui/pattern1.png";
        } else {
            return "gui/pattern.png";
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(
            this.tabCraftButton = new GuiTabButton(
                this.guiLeft + 173,
                this.guiTop + this.ySize - 177,
                new ItemStack(Blocks.crafting_table),
                GuiText.CraftingPattern.getLocal(),
                itemRender));
        this.buttonList.add(
            this.tabProcessButton = new GuiTabButton(
                this.guiLeft + 173,
                this.guiTop + this.ySize - 177,
                new ItemStack(Blocks.furnace),
                GuiText.ProcessingPattern.getLocal(),
                itemRender));
        this.buttonList.add(
            this.clearBtn = new GuiImgButton(
                this.guiLeft + 87 + 18 * -3,
                this.guiTop + this.ySize - 156,
                Settings.ACTIONS,
                ActionItems.CLOSE));
        this.clearBtn.setHalfSize(true);
        this.encodeBtn = new GuiImgButton(
            this.guiLeft + 147,
            this.guiTop + this.ySize - 142,
            Settings.ACTIONS,
            ActionItems.ENCODE);
        this.buttonList.add(this.encodeBtn);
        if (ModAndClassUtil.isDoubleButton) {
            this.doubleBtn = new GuiImgButton(
                this.guiLeft + 97 + 18 * -3,
                this.guiTop + this.ySize - 156,
                Settings.ACTIONS,
                ActionItems.DOUBLE);
            this.doubleBtn.setHalfSize(true);
            this.buttonList.add(this.doubleBtn);
        }

        this.combineEnableBtn = new GuiFCImgButton(
            this.guiLeft + 87 + 18 * -3,
            this.guiTop + this.ySize - 146,
            "FORCE_COMBINE",
            "DO_COMBINE");
        this.combineEnableBtn.setHalfSize(true);
        this.buttonList.add(this.combineEnableBtn);

        this.combineDisableBtn = new GuiFCImgButton(
            this.guiLeft + 87 + 18 * -3,
            this.guiTop + this.ySize - 146,
            "NOT_COMBINE",
            "DONT_COMBINE");
        this.combineDisableBtn.setHalfSize(true);
        this.buttonList.add(this.combineDisableBtn);

        processingScrollBar.setTop(this.ySize - 164);
        processingScrollBar.setLeft(this.xSize - 64);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn == encodeBtn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns(
                    "PatternTerminal.Encode",
                    (isCtrlKeyDown() ? 1 : 0) << 1 | (isShiftKeyDown() ? 1 : 0)));
        } else if (btn == clearBtn) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("PatternTerminal.Clear", 1));
        } else if (btn == doubleBtn) {
            final boolean backwards = Mouse.isButtonDown(1);
            int val = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1 : 0;
            if (backwards) val |= 0b10;
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("PatternTerminal.Double", val));
        } else if (this.tabCraftButton == btn || this.tabProcessButton == btn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns(
                    "PatternTerminal.CraftMode",
                    this.tabProcessButton == btn ? MODE_CRAFTING : MODE_PROCESSING));
        } else if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTerminalBtns("PatternTerminal.Combine", this.combineDisableBtn == btn));
        }
        super.actionPerformed(btn);
    }

    public boolean hideItemPanelSlot(int tx, int ty, int tw, int th) {
        if (this.viewCell) {
            int rw = 33;
            int rh = 14 + myCurrentViewCells.length * 18;
            if (this.container.isPatternTerminal()) rh += 18;
            if (tw <= 0 || th <= 0) {
                return false;
            }

            int rx = this.guiLeft + this.xSize;
            int ry = this.guiTop;

            rw += rx;
            rh += ry;
            tw += tx;
            th += ty;

            // overflow || intersect
            return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
        }
        return false;
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(this.getBackground());
        final int x_width = 195;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);
        if (this.viewCell) {
            this.drawTexturedModalRect(offsetX + x_width, offsetY, x_width, 0, 46, 128);
        }
        for (int x = 0; x < this.rows; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }
        this.drawTexturedModalRect(
            offsetX,
            offsetY + 16 + this.rows * 18 + this.lowerTextureOffset,
            0,
            106 - 18 - 18,
            x_width,
            99 + this.reservedSpace - this.lowerTextureOffset);
        if (this.viewCell) {
            boolean update = false;
            for (int i = 0; i < 5; i++) {
                if (this.myCurrentViewCells[i] != this.container.getCellViewSlot(i)
                    .getStack()) {
                    update = true;
                    this.myCurrentViewCells[i] = this.container.getCellViewSlot(i)
                        .getStack();
                }
            }
            if (update) {
                this.repo.setViewCell(this.myCurrentViewCells);
            }
        }
        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    @Override
    public void postUpdate(List<IAEItemStack> list) {
        for (IAEItemStack ias : list) {
            if (ias.getItem() instanceof ItemFluidDrop) continue;
            this.repo.postUpdate(ias);
        }
        this.repo.updateView();
        if (!this.repo.hasCache()) {
            this.setScrollBar();
        }
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this, xCoord - this.guiLeft, yCoord - this.guiTop);
        super.mouseClicked(xCoord, yCoord, btn);

        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        final int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight;

            if (this.processingScrollBar.contains(x - this.guiLeft, y - this.guiTop)) {
                final int currentScroll = this.processingScrollBar.getCurrentScroll();
                this.processingScrollBar.wheel(wheel);

                if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
                    changeActivePage();
                }
            }
        }
    }

    private void changeActivePage() {
        AE2Thing.proxy.netHandler.sendToServer(
            new CPacketTerminalBtns("PatternTerminal.ActivePage", this.processingScrollBar.getCurrentScroll()));
    }

    @Override
    protected void mouseClickMove(final int x, final int y, final int c, final long d) {
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this, x - this.guiLeft, y - this.guiTop);
        super.mouseClickMove(x, y, c, d);

        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    public void postFluidUpdate(List<IAEFluidStack> list) {
        for (IAEFluidStack is : list) {
            IAEItemStack stack = AEItemStack.create(ItemFluidDrop.newDisplayStack(is.getFluidStack()));
            stack.setStackSize(is.getStackSize());
            stack.setCraftable(is.isCraftable());
            this.repo.postUpdate(stack);
        }
        this.repo.updateView();
        if (!this.repo.hasCache()) {
            this.setScrollBar();
        }

    }

    @Override
    protected boolean isPowered() {
        return this.container.hasPower;
    }
}
