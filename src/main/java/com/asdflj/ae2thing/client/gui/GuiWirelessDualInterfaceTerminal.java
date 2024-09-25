package com.asdflj.ae2thing.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.container.ContainerMonitor;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.client.gui.widget.IAEBasePanel;
import com.asdflj.ae2thing.client.gui.widget.IDraggable;
import com.asdflj.ae2thing.client.gui.widget.IGuiMonitor;
import com.asdflj.ae2thing.client.gui.widget.IGuiSelection;
import com.asdflj.ae2thing.client.gui.widget.ItemPanel;
import com.asdflj.ae2thing.client.gui.widget.PatternPanel;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.network.CPacketSwitchGuis;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.localization.GuiText;
import appeng.util.IConfigManagerHost;

public class GuiWirelessDualInterfaceTerminal extends GuiBaseInterfaceWireless
    implements IWidgetGui, IGuiDrawSlot, IGuiMonitorTerminal, ISortSource, IConfigManagerHost, IGuiSelection {

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
        this.panels.add(new PatternPanel(this, container));
        this.itemPanel = new ItemPanel(this, container, this.configSrc, this);
        this.panels.add(this.itemPanel);
        ((ContainerMonitor) this.inventorySlots).setGui(this);
        this.baseXSize = this.xSize;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        for (IAEBasePanel panel : this.panels) {
            panel.drawFG(offsetX, offsetY, mouseX, mouseY);
        }
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        for (IAEBasePanel panel : this.panels) {
            panel.drawBG(offsetX, offsetY, mouseX, mouseY);
        }
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        this.xSize = baseXSize;
        IDraggable.Rectangle rectangle;
        if (dragging) {
            if (activePanel != null && this.mouse != null) {
                activePanel.move(mouseX - mouse.x, mouseY - mouse.y);
            } else {
                for (IAEBasePanel panel : this.panels) {
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
        for (IAEBasePanel panel : this.panels) {
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
        EntityPlayer player = this.mc.thePlayer;
        ItemStack is = player.inventory.getItemStack();
        if (isFilledContainer(is)) {
            Slot s = this.getSlot(mouseX, mouseY);
            if (s instanceof SlotME) {
                List<String> message = new ArrayList<>();
                message.add(
                    "\u00a77" + I18n.format(
                        NameConst.GUI_TERMINAL_STORE_ACTION,
                        I18n.format(NameConst.GUI_TERMINAL_LEFT_CLICK),
                        EnumChatFormatting.WHITE + is.getDisplayName() + EnumChatFormatting.RESET));
                message.add(
                    "\u00a77" + I18n.format(
                        NameConst.GUI_TERMINAL_STORE_ACTION,
                        I18n.format(NameConst.GUI_TERMINAL_RIGHT_CLICK),
                        EnumChatFormatting.WHITE + getContainerDisplayName(is) + EnumChatFormatting.RESET));
                drawContainerActionTooltip(mouseX, mouseY, String.join("\n", message));
            }
        }
        this.xSize = fullXSize;
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        this.panels.forEach(p -> p.mouseClicked(xCoord, yCoord, btn));
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void mouseClickMove(final int x, final int y, final int c, final long d) {
        this.panels.forEach(panel -> panel.mouseClickMove(x, y, c, d));
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
        for (IAEBasePanel panel : this.panels) {
            if (panel.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton)) return;
        }
        if (slotIdx < 0) return;
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    @Override
    protected boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        for (IAEBasePanel panel : this.panels) {
            if (panel.mouseWheelEvent(mouseX, mouseY, wheel)) return true;
        }
        return super.mouseWheelEvent(mouseX, mouseY, wheel);
    }

    @Override
    protected void keyTyped(char character, int key) {
        this.xSize = baseXSize;
        for (IAEBasePanel panel : this.panels) {
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
    public AEBaseGui getGui() {
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
        for (IAEBasePanel panel : this.panels) {
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
            if(obj instanceof SlotPatternFake psp){
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
        this.panels.stream()
            .filter(p -> p instanceof IGuiMonitor)
            .forEach(p -> ((IGuiMonitor) p).postUpdate(list));
    }

    @Override
    public void setScrollBar() {
        this.panels.stream()
            .filter(p -> p instanceof IGuiMonitor)
            .forEach(p -> ((IGuiMonitor) p).setScrollBar());
    }

    @Override
    public void postFluidUpdate(List<IAEFluidStack> list) {
        this.panels.stream()
            .filter(p -> p instanceof IGuiMonitor)
            .forEach(p -> ((IGuiMonitor) p).postFluidUpdate(list));
    }

    @Override
    public void setPlayerInv(ItemStack is) {
        this.container.getPlayerInv()
            .setItemStack(is);
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
        this.panels.stream()
            .filter(p -> p instanceof IConfigManagerHost)
            .forEach(p -> ((IConfigManagerHost) p).updateSetting(manager, settingName, newValue));
    }

    @Override
    public void drawHistorySelection(int x, int y, String text, int width, List<String> searchHistory) {
        final int maxRows = AE2ThingAPI.maxSelectionRows;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        String[] var4 = null;
        final List<String> history = new ArrayList<>(searchHistory);
        Collections.reverse(history);

        if (history.size() > maxRows) {
            for (int i = 1; i < history.size(); i++) {
                if (text.equals(history.get(i))) {
                    int max = Math.min(history.size(), i + maxRows - 1);
                    int min = Math.max(0, max - maxRows);
                    var4 = history.subList(min, max)
                        .toArray(new String[0]);
                    break;
                }
            }
        }
        if (var4 == null) {
            var4 = history.subList(0, Math.min(history.size(), 5))
                .toArray(new String[0]);
        }
        if (var4.length > 0) {
            int var5 = width;
            int var6;
            int var7;

            for (var6 = 0; var6 < var4.length; ++var6) {
                var7 = this.fontRendererObj.getStringWidth(var4[var6]) + 8;

                if (var7 > var5) {
                    var5 = var7;
                }
            }

            var6 = x + 3;
            var7 = y + 15;
            int var9 = 8;

            if (var4.length > 1) {
                var9 += 2 + (var4.length - 1) * 10;
            }

            if (this.guiTop + var7 + var9 + 6 > this.height) {
                var7 = this.height - var9 - this.guiTop - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            final int var10 = -267386864;
            this.drawGradientRect(var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10);
            this.drawGradientRect(var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10);
            this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10);
            this.drawGradientRect(var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10);
            this.drawGradientRect(var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10);
            final int var11 = 1347420415;
            final int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
            this.drawGradientRect(var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12);
            this.drawGradientRect(var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12);
            this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11);
            this.drawGradientRect(var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12);

            for (int var13 = 0; var13 < var4.length; ++var13) {
                String var14 = var4[var13];
                if (var14.equals(text)) {
                    var14 = "> " + var14;
                    var14 = '\u00a7' + Integer.toHexString(15) + var14;
                } else {
                    var14 = "\u00a77" + var14;
                }

                this.fontRendererObj.drawStringWithShadow(var14, var6, var7, -1);

                if (var13 == 0) {
                    var7 += 2;
                }

                var7 += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }
        GL11.glPopAttrib();

    }
}
