package com.asdflj.ae2thing.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerMonitor;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.client.gui.widget.IAEBasePanel;
import com.asdflj.ae2thing.client.gui.widget.IDraggable;
import com.asdflj.ae2thing.client.gui.widget.IGuiMonitor;
import com.asdflj.ae2thing.client.gui.widget.IGuiSelection;
import com.asdflj.ae2thing.client.gui.widget.ItemPanel;
import com.asdflj.ae2thing.client.gui.widget.PatternPanel;
import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;
import com.asdflj.ae2thing.client.me.AdvItemRepo;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.network.CPacketSwitchGuis;

import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.InternalSlotME;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.localization.GuiText;
import appeng.util.IConfigManagerHost;

public class GuiWirelessDualInterfaceTerminal extends GuiBaseInterfaceWireless implements IWidgetGui, IGuiDrawSlot,
    IGuiMonitorTerminal, ISortSource, IConfigManagerHost, IGuiSelection, IDropToFillTextField {

    public ContainerWirelessDualInterfaceTerminal container;
    private GuiTabButton craftingStatusBtn;
    private final int baseXSize;
    private static final int fullXSize = 1000;
    private final List<IAEBasePanel> panels = new ArrayList<>();
    private IAEBasePanel activePanel = null;
    private Point mouse;
    private boolean dragging = false;
    private final ItemPanel itemPanel;

    public GuiWirelessDualInterfaceTerminal(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        container = (ContainerWirelessDualInterfaceTerminal) this.inventorySlots;
        this.itemPanel = new ItemPanel(this, container, this.configSrc, this);
        this.panels.add(new PatternPanel(this, container));
        this.panels.add(this.itemPanel);
        ((ContainerMonitor) this.inventorySlots).setGui(this);
        this.baseXSize = this.xSize;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
        for (IAEBasePanel panel : this.getActivePanels()) {
            panel.drawFG(offsetX, offsetY, mouseX, mouseY);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        for (IAEBasePanel panel : this.getActivePanels()) {
            panel.drawBG(offsetX, offsetY, mouseX, mouseY);
        }
    }

    private List<IAEBasePanel> getActivePanels() {
        return this.panels.stream()
            .filter(IAEBasePanel::isActive)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isOverTextField(int mousex, int mousey) {
        if (super.isOverTextField(mousex, mousey)) {
            return true;
        }
        return this.itemPanel.isOverTextField(mousex, mousey);
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        super.setTextFieldValue(displayName, mousex, mousey, stack);
        this.itemPanel.setTextFieldValue(displayName, mousex, mousey, stack);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        this.xSize = baseXSize;
        IDraggable.Rectangle rectangle;
        if (dragging) {
            if (activePanel != null && this.mouse != null) {
                activePanel.move(mouseX - mouse.x, mouseY - mouse.y);
            } else {
                for (IAEBasePanel panel : this.getActivePanels()) {
                    if (panel.draggable()) {
                        rectangle = panel.getRectangle();
                        if (mouseX > rectangle.x() && mouseX < rectangle.x() + rectangle.width()
                            && mouseY > rectangle.y()
                            && mouseY < rectangle.y() + rectangle.height()) {
                            this.activePanel = panel;
                            this.mouse = new Point(mouseX - rectangle.x(), mouseY - rectangle.y());
                            break;
                        }
                    }
                }
            }
        }
        for (IAEBasePanel panel : this.getActivePanels()) {
            panel.drawScreen(mouseX, mouseY, btn);
        }
        if (this.itemPanel.getRepo()
            .hasCache()) {
            try {
                this.itemPanel.getRepo()
                    .getLock()
                    .lock();
                super.drawScreen(mouseX, mouseY, btn);
            } finally {
                this.itemPanel.getRepo()
                    .getLock()
                    .unlock();
            }
        } else {
            super.drawScreen(mouseX, mouseY, btn);
        }
        this.xSize = fullXSize;
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        this.getActivePanels()
            .forEach(p -> p.mouseClicked(xCoord, yCoord, btn));
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void mouseClickMove(final int x, final int y, final int c, final long d) {
        this.getActivePanels()
            .forEach(panel -> panel.mouseClickMove(x, y, c, d));
        this.dragging = true;
        super.mouseClickMove(x, y, c, d);
    }

    @Override
    public void handleMouseInput() {
        if (Mouse.getEventButton() != -1) {
            this.activePanel = null;
            this.mouse = null;
            this.dragging = false;
        }
        super.handleMouseInput();
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        for (IAEBasePanel panel : this.getActivePanels()) {
            if (panel.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton)) return;
        }
        if (slotIdx < 0) return;
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    @Override
    protected boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        for (IAEBasePanel panel : this.getActivePanels()) {
            if (panel.mouseWheelEvent(mouseX, mouseY, wheel)) return true;
        }
        return super.mouseWheelEvent(mouseX, mouseY, wheel);
    }

    @Override
    protected void keyTyped(char character, int key) {
        this.xSize = baseXSize;
        for (IAEBasePanel panel : this.getActivePanels()) {
            if (!this.checkHotbarKeys(key) && panel.keyTyped(character, key)) return;
        }
        super.keyTyped(character, key);
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot(s)) super.func_146977_a(s);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.xSize = baseXSize;
        super.initGui();
        for (IAEBasePanel panel : this.panels) {
            panel.initGui();
        }
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
    public BaseMEGui getGui() {
        return this;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.panels.forEach(IAEBasePanel::onGuiClosed);
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean hideItemPanelSlot(int x, int y, int w, int h) {
        for (IAEBasePanel panel : this.getActivePanels()) {
            if (panel.hideItemPanelSlot(x, y, w, h)) return true;
        }
        return false;
    }

    @Override
    public List<GuiButton> getButtonList() {
        return this.buttonList;
    }

    @Override
    public IAEBasePanel getActivePanel() {
        return this.activePanel;
    }

    @Override
    public List<InternalSlotME> getMeSlots() {
        return super.getMeSlots();
    }

    @Override
    public RenderItem getRenderItem() {
        return itemRender;
    }

    @Override
    public Slot getSlot(int mouseX, int mouseY) {
        return super.getSlot(mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (this.craftingStatusBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.CRAFTING_STATUS_ITEM));
        }
        for (IAEBasePanel panel : this.panels) {
            if (panel.actionPerformed(btn)) return;
        }
        super.actionPerformed(btn);
    }

    @Override
    protected void repositionSlots() {
        for (final Object obj : this.inventorySlots.inventorySlots) {
            if(obj instanceof SlotPatternFake s){
                s.yDisplayPosition = this.ySize + s.getY() - this.viewHeight - 78 - 4;
            } else if (obj instanceof SlotRestrictedInput s) {
                s.yDisplayPosition = this.ySize + s.getY() - this.viewHeight - 78 - 4;
            } else if (obj instanceof SlotFakeCraftingMatrix s) {
                s.yDisplayPosition = this.ySize + s.getY() - this.viewHeight - 78 - 4;
            } else if (obj instanceof SlotPatternTerm s) {
                s.yDisplayPosition = this.ySize + s.getY() - this.viewHeight - 78 - 4;
            } else if (obj instanceof final AppEngSlot slot) {
                slot.yDisplayPosition = this.ySize + slot.getY() - 78 - 4;
            }
        }
    }

    @Override
    protected boolean isPowered() {
        return ((ContainerWirelessDualInterfaceTerminal) this.inventorySlots).hasPower;
    }

    @Override
    public AEBaseGui getAEBaseGui() {
        return this;
    }

    @Override
    public float getzLevel() {
        return this.zLevel;
    }

    @Override
    public void postUpdate(List<IAEItemStack> list) {
        this.getActivePanels()
            .stream()
            .filter(p -> p instanceof IGuiMonitor)
            .forEach(p -> ((IGuiMonitor) p).postUpdate(list));
    }

    @Override
    public void setScrollBar() {
        this.getActivePanels()
            .stream()
            .filter(p -> p instanceof IGuiMonitor)
            .forEach(p -> ((IGuiMonitor) p).setScrollBar());
    }

    @Override
    public AdvItemRepo getRepo() {
        return this.itemPanel.getRepo();
    }

    @Override
    public void postFluidUpdate(List<IAEFluidStack> list) {
        this.getActivePanels()
            .stream()
            .filter(p -> p instanceof IGuiMonitor)
            .forEach(p -> ((IGuiMonitor) p).postFluidUpdate(list));
    }

    @Override
    public void setPlayerInv(ItemStack is) {
        this.itemPanel.setPlayerInv(is);

    }

    @Override
    public THGuiTextField getSearchField() {
        return this.itemPanel.getSearchField();
    }

    @Override
    public Enum<?> getSortBy() {
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public Enum<?> getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public Enum<?> getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public Enum<?> getTypeFilter() {
        return this.configSrc.getSetting(Settings.TYPE_FILTER);
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        this.getActivePanels()
            .stream()
            .filter(p -> p instanceof IConfigManagerHost)
            .forEach(p -> ((IConfigManagerHost) p).updateSetting(manager, settingName, newValue));
    }

    @Override
    public void handleKeyboardInput() {
        super.handleKeyboardInput();
        if (this.itemPanel != null) {
            this.itemPanel.handleKeyboardInput();
        }
    }
}
