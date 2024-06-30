package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerDistillationPatternTerminal;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;

public class GuiDistillationPatternTerminal extends GuiItemMonitor {

    private final ContainerDistillationPatternTerminal container;
    protected final boolean viewCell;
    protected final ItemStack[] myCurrentViewCells = new ItemStack[5];
    protected GuiImgButton encodeBtn;
    protected GuiImgButton doubleBtn;

    public GuiDistillationPatternTerminal(InventoryPlayer inventory, ITerminalHost inv) {
        super(new ContainerDistillationPatternTerminal(inventory, inv));
        this.xSize = 195;
        this.ySize = 204;
        this.standardSize = this.xSize;
        (this.container = (ContainerDistillationPatternTerminal) this.inventorySlots).setGui(this);
        this.reservedSpace = 81;
        this.viewCell = inv instanceof IViewCellStorage;
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
    }

    public int getReservedSpace() {
        return this.reservedSpace;
    }

    protected String getBackground() {
        return "gui/pattern.png";
    }

    @Override
    public void initGui() {
        super.initGui();
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
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTerminalBtns("PatternTerminal.Double", (isShiftKeyDown() ? 1 : 0)));
        }
        super.actionPerformed(btn);
    }

    public boolean hideItemPanelSlot(int tx, int ty, int tw, int th) {
        if (this.viewCell) {
            int rw = 33;
            int rh = 14 + myCurrentViewCells.length * 18;

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

    public void postUpdate(List<IAEItemStack> list) {
        for (IAEItemStack ias : list) {
            this.repo.postUpdate(ias);
        }
        this.repo.updateView();
        this.setScrollBar();
    }
}
