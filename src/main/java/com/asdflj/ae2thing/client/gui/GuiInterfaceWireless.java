package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerInterfaceWireless;
import com.asdflj.ae2thing.client.gui.container.slot.ProcessingSlotPattern;
import com.asdflj.ae2thing.client.gui.widget.IAEBasePanel;
import com.asdflj.ae2thing.client.gui.widget.PatternPanel;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.network.CPacketSwitchGuis;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.localization.GuiText;

public class GuiInterfaceWireless extends GuiBaseInterfaceWireless implements IWidgetGui {

    private final IAEBasePanel panel;
    public ContainerInterfaceWireless container;
    private GuiTabButton craftingStatusBtn;

    public GuiInterfaceWireless(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        container = (ContainerInterfaceWireless) this.inventorySlots;
        panel = new PatternPanel(this, container);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        panel.drawFG(offsetX, offsetY, mouseX, mouseY);
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        panel.drawScreen(mouseX, mouseY, btn);
        super.drawScreen(mouseX, mouseY, btn);

    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        panel.drawBG(offsetX, offsetY, mouseX, mouseY);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        if (!panel.mouseClicked(xCoord, yCoord, btn)) {
            super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    @Override
    protected void mouseClickMove(final int x, final int y, final int c, final long d) {
        this.panel.mouseClickMove(x, y, c, d);
        super.mouseClickMove(x, y, c, d);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        if (slotIdx == -999) return;
        panel.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    @Override
    protected boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        if (this.panel.mouseWheelEvent(mouseX, mouseY, wheel)) return true;
        return super.mouseWheelEvent(mouseX, mouseY, wheel);
    }

    @Override
    public void setXSize(int size) {
        this.xSize = size;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.panel.initGui();
        this.buttonList.add(
            this.craftingStatusBtn = new GuiTabButton(
                this.guiLeft + 184,
                this.guiTop - 4,
                2 + 11 * 16,
                GuiText.CraftingStatus.getLocal(),
                itemRender));
        this.craftingStatusBtn.setHideEdge(13); // GuiTabButton implementation //
    }

    @Override
    public AEBaseGui getGui() {
        return this;
    }

    @Override
    public boolean hideItemPanelSlot(int x, int y, int w, int h) {
        return this.panel.hideItemPanelSlot(x, y, w, h);
    }

    @Override
    public List<GuiButton> getButtonList() {
        return this.buttonList;
    }

    @Override
    public IAEBasePanel getPanel() {
        return this.panel;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (this.craftingStatusBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.CRAFTING_STATUS_ITEM));
        }
        this.panel.actionPerformed(btn);
        super.actionPerformed(btn);
    }

    @Override
    protected void repositionSlots() {
        for (final Object obj : this.inventorySlots.inventorySlots) {
            if(obj instanceof ProcessingSlotPattern psp){
                psp.yDisplayPosition = this.ySize + psp.getY() - this.viewHeight - 78 - 4;
            } else if (obj instanceof SlotRestrictedInput sri) {
                sri.yDisplayPosition = this.ySize + sri.getY() - this.viewHeight - 78 - 4;
            } else if (obj instanceof final AppEngSlot slot) {
                slot.yDisplayPosition = this.ySize + slot.getY() - 78 - 4;
            }
        }
    }

    @Override
    protected boolean isPowered() {
        return ((ContainerInterfaceWireless) this.inventorySlots).hasPower;
    }
}
