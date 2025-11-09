package com.asdflj.ae2thing.client.gui.widget;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.BaseMEGui;
import com.asdflj.ae2thing.client.gui.IGuiMonitorTerminal;
import com.asdflj.ae2thing.client.gui.IWidgetGui;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.me.AdvItemRepo;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotPatternTerm;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.LayoutManager;
import codechicken.nei.util.TextHistory;

public class ItemPanel implements IAEBasePanel, IGuiMonitorTerminal, IConfigManagerHost, IDropToFillTextField {

    private final BaseMEGui parent;
    private final IWidgetGui gui;
    private final ContainerWirelessDualInterfaceTerminal container;
    private final int perRow;
    private final int rows;
    protected THGuiTextField searchField;
    private final AEBaseContainer inventorySlots;
    private final AdvItemRepo repo;
    private final IConfigManager configSrc;
    private final GuiScrollbar scrollbar;
    private int absX;
    private int absY;
    private final int w;
    private final int h;
    private int offsetY;
    private final boolean showViewBtn = true;
    private GuiImgButton SortByBox;
    private GuiImgButton ViewBox;
    private GuiImgButton typeFilter;
    private GuiImgButton SortDirBox;
    private GuiImgButton searchBoxSettings;
    private static String memoryText = "";
    private final TextHistory history;
    private int lastClickTime = 0;

    public ItemPanel(IWidgetGui gui, ContainerWirelessDualInterfaceTerminal container, IConfigManager configSrc,
        ISortSource source) {
        this.gui = gui;
        this.container = container;
        this.parent = gui.getGui();
        this.inventorySlots = this.container;
        this.configSrc = configSrc;
        this.scrollbar = new GuiScrollbar();
        this.repo = new AdvItemRepo(scrollbar, source);
        this.repo.setCache(this);
        this.repo.setPowered(true);
        this.w = 101;
        this.h = 96;
        this.repo.setRowSize(4);
        this.rows = 4;
        this.perRow = 4;
        this.history = Ae2ReflectClient.getHistory(LayoutManager.searchField);
    }

    public void saveSearchString() {
        if (ModAndClassUtil.NEI && isNEISearch()
            && !this.searchField.getText()
                .isEmpty()) {
            this.history.add(this.searchField.getText());
        }
    }

    protected boolean isNEISearch() {
        final Enum<?> s = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        return s == SearchBoxMode.NEI_MANUAL_SEARCH || s == SearchBoxMode.NEI_AUTOSEARCH;
    }

    @Override
    public String getBackground() {
        return "gui/widget/items.png";
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        GL11.glTranslatef(0f, 0f, 100f);
        this.scrollbar.draw(this.parent);
        GL11.glTranslatef(0f, 0f, -100f);
        if (AEConfig.instance.preserveSearchBar && searchField != null && searchField.isMouseIn(mouseX, mouseY))
            this.parent.drawTooltip(this.absX - offsetX, this.absY - 20, 0, searchField.getMessage());
        if (ModAndClassUtil.NEI && searchField != null
            && this.searchField.isMouseIn(mouseX, mouseY)
            && this.isNEISearch()
            && this.parent != null) {
            // draw selection
            List<String> list = Ae2ReflectClient.getHistoryList(this.history);
            ((IGuiSelection) this.parent).drawHistorySelection(
                searchField.xPosition - offsetX,
                searchField.yPosition - this.parent.getGuiTop(),
                searchField.getText(),
                searchField.width,
                list);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(getBackground());
        this.parent.drawTexturedModalRect(absX, absY, 0, 0, 101, 96);
        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {}

    @Override
    public void initGui() {
        this.absX = this.parent.getGuiLeft() - 101;
        this.absY = this.parent.getGuiTop() + this.parent.getYSize() - 96;
        this.searchField = new THGuiTextField(this.parent.getFontRenderer(), absX + 3, absY + 4, 72, 12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setVisible(true);
        this.searchField.setMessage(ButtonToolTips.SearchStringTooltip.getLocal());
        this.gui.getMeSlots()
            .clear();
        final List<Slot> slots = this.getInventorySlots();
        slots.removeIf(slot -> slot instanceof SlotME);
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                final InternalSlotME s = new InternalSlotME(
                    this.repo,
                    x + y * this.perRow,
                    (this.absX - this.parent.getGuiLeft() + 5) + x * 18,
                    (this.absY + 18 - this.parent.getGuiTop()) + y * 18);
                this.gui.getMeSlots()
                    .add(s);
                this.getInventorySlots()
                    .add(new SlotME(s));
            }
        }
        this.offsetY = this.absY;
        this.gui.getButtonList()
            .add(
                this.SortByBox = new GuiImgButton(
                    this.absX - 18,
                    this.offsetY,
                    Settings.SORT_BY,
                    this.configSrc.getSetting(Settings.SORT_BY)));
        this.offsetY += 20;

        if (this.showViewBtn) {
            this.gui.getButtonList()
                .add(
                    this.ViewBox = new GuiImgButton(
                        this.absX - 18,
                        this.offsetY,
                        Settings.VIEW_MODE,
                        this.configSrc.getSetting(Settings.VIEW_MODE)));
            this.offsetY += 20;
        }

        if (ModAndClassUtil.isTypeFilter) {
            this.gui.getButtonList()
                .add(
                    this.typeFilter = new GuiImgButton(
                        this.absX - 18,
                        this.offsetY,
                        Settings.TYPE_FILTER,
                        this.configSrc.getSetting(Settings.TYPE_FILTER)));
            this.offsetY += 20;
        }

        this.gui.getButtonList()
            .add(
                this.SortDirBox = new GuiImgButton(
                    this.absX - 18,
                    this.offsetY,
                    Settings.SORT_DIRECTION,
                    this.configSrc.getSetting(Settings.SORT_DIRECTION)));
        this.offsetY += 20;

        this.gui.getButtonList()
            .add(
                this.searchBoxSettings = new GuiImgButton(
                    this.absX - 18,
                    this.offsetY,
                    Settings.SEARCH_MODE,
                    AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE)));
        this.offsetY += 20;
        final Enum<?> searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);

        if (searchMode == SearchBoxMode.AUTOSEARCH || searchMode == SearchBoxMode.NEI_AUTOSEARCH) {
            this.searchField.setFocused(true);
        }
        if ((AEConfig.instance.preserveSearchBar || this.parent.isSubGui())) {
            setSearchString(memoryText, false);
        }
        if (this.parent.isSubGui()) {
            this.repo.updateView();
        }
        this.setScrollBar();
    }

    private List<Slot> getInventorySlots() {
        return this.parent.inventorySlots.inventorySlots;
    }

    @Override
    public boolean hideItemPanelSlot(int tx, int ty, int tw, int th) {
        int rw = 101;
        int rh = 96;
        if (tw <= 0 || th <= 0) {
            return false;
        }

        int rx = this.absX;
        int ry = this.absY;

        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;

        // overflow || intersect
        return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
    }

    @Override
    public void mouseClicked(int xCoord, int yCoord, int btn) {
        this.saveSearchString();
        this.searchField.mouseClicked(xCoord, yCoord, btn);
        if (btn == 1 && this.searchField.isMouseIn(xCoord, yCoord)) {
            setSearchString("", true);
        }
        this.scrollbar.click(this.parent, xCoord - this.parent.getGuiLeft(), yCoord - this.parent.getGuiTop());
        // remove
        // if (ModAndClassUtil.CORE_MOD && GTUtil.compareVersion(GTUtil.CoreModVersion) == 1) {
        // return;
        // }
        boolean flag = btn == this.parent.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100;
        Slot slot = this.getSlotAtPosition(xCoord, yCoord);
        if (slot != null && (btn == 0 || btn == 1 || flag)) {
            if (btn == this.parent.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
                this.mouseClick(slot, slot.getSlotIndex(), btn, 3);
            } else if (isShiftKeyDown()) {
                this.mouseClick(slot, slot.getSlotIndex(), btn, 1);
            } else {
                this.mouseClick(slot, slot.getSlotIndex(), btn, 0);
            }
        }
    }

    private Slot getSlotAtPosition(int mouseX, int mouseY) {
        Optional<Slot> slot = this.getInventorySlots()
            .stream()
            .filter(s -> s instanceof SlotME)
            .filter(x -> isMouseOverSlot(x, mouseX, mouseY))
            .findFirst();
        return slot.orElse(null);
    }

    private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
        return this.func_146978_c(slotIn.xDisplayPosition, slotIn.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    private boolean func_146978_c(int left, int top, int right, int bottom, int pointX, int pointY) {
        int k1 = this.parent.getGuiLeft();
        int l1 = this.parent.getGuiTop();
        pointX -= k1;
        pointY -= l1;
        return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
    }

    public void setSearchString(String memoryText, boolean updateView) {
        this.searchField.setText(memoryText);
        this.repo.setSearchString(memoryText);
        if (updateView) {
            this.repo.updateView();
            this.setScrollBar();
        }
        updateSuggestion();
    }

    @Override
    public void setScrollBar() {
        this.scrollbar.setTop(this.absY - this.parent.getGuiTop() + 18)
            .setLeft(this.absX - this.parent.getGuiLeft() + this.w - 20)
            .setHeight(this.rows * 18 - 2);
        this.scrollbar
            .setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    public boolean isOverTextField(int mousex, int mousey) {
        return searchField.isMouseIn(mousex, mousey);
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        if (!searchField.isMouseIn(mousex, mousey)) return;
        if (ModAndClassUtil.THE && AspectUtil.getAspectFromJar(stack) != null) {
            setSearchString(
                Objects.requireNonNull(AspectUtil.getAspectFromJar(stack))
                    .getName(),
                true);
        } else {
            setSearchString(displayName, true);
        }
        this.saveSearchString();
    }

    private boolean mouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        if (slotIdx < 0) return false;
        // Temporary solution
        if (lastClickTime == Minecraft.getMinecraft().thePlayer.ticksExisted) {
            return false;
        }
        lastClickTime = Minecraft.getMinecraft().thePlayer.ticksExisted;
        saveSearchString();
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (this.parent.updateFluidContainer(slot, slotIdx, ctrlDown, mouseButton)) return true;

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            if (!(slot instanceof SlotPatternTerm)) {
                IAEItemStack stack = null;
                if (slot instanceof SlotME) {
                    stack = ((SlotME) slot).getAEStack();
                }
                int slotNum = Ae2ReflectClient.getInventorySlots(this.parent)
                    .size();
                if (!(slot instanceof SlotME) && slot != null) {
                    slotNum = slot.slotNumber;
                }
                this.inventorySlots.setTargetStack(stack);
                final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, -2);
                NetworkHandler.instance.sendToServer(p);
                return true;
            }
        }
        if (slot instanceof SlotME) {
            InventoryAction action = null;
            IAEItemStack stack = null;
            switch (mouseButton) {
                case 0: // pickup / set-down.
                    action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN
                        && stack.getStackSize() == 0
                        && player.inventory.getItemStack() == null) {
                        action = InventoryAction.AUTO_CRAFT;
                    }
                    break;
                case 1:
                    action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    stack = ((SlotME) slot).getAEStack();
                    break;
                case 3: // creative dupe:
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && stack.isCraftable()) {
                        action = InventoryAction.AUTO_CRAFT;
                    } else if (player.capabilities.isCreativeMode) {
                        final IAEItemStack slotItem = ((SlotME) slot).getAEStack();
                        if (slotItem != null) {
                            action = InventoryAction.CREATIVE_DUPLICATE;
                        }
                    } else break;
                default:
                case 4: // drop item:
                case 6:
            }
            if (action == InventoryAction.AUTO_CRAFT) {
                this.inventorySlots.setTargetStack(stack);
                AE2Thing.proxy.netHandler.sendToServer(
                    new CPacketInventoryAction(
                        action,
                        Ae2ReflectClient.getInventorySlots(this.parent)
                            .size(),
                        -2,
                        stack));
            } else if (action != null) {
                if (stack != null && stack.getItem() instanceof ItemFluidDrop) stack = null;
                this.inventorySlots.setTargetStack(stack);
                final PacketInventoryAction p = new PacketInventoryAction(
                    action,
                    Ae2ReflectClient.getInventorySlots(this.parent)
                        .size(),
                    -2);
                NetworkHandler.instance.sendToServer(p);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        return slot instanceof SlotME;
    }

    @Override
    public boolean actionPerformed(GuiButton btn) {
        if (btn instanceof final GuiImgButton iBtn) {
            final boolean backwards = Mouse.isButtonDown(1);
            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum<?> cv = iBtn.getCurrentValue();
                final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());
                if (btn == this.searchBoxSettings) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
                } else if (btn == this.SortByBox || btn == this.SortDirBox || btn == this.ViewBox || btn == this.typeFilter) {
                    try {
                        NetworkHandler.instance
                            .sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
                    } catch (final IOException e) {
                        AELog.debug(e);
                    }
                    iBtn.set(next);
                    if (next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class) {
                        this.reInitalize();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void reInitalize() {
        this.gui.getButtonList()
            .clear();
        this.initGui();
    }

    @Override
    public void mouseClickMove(int x, int y, int c, long d) {
        this.scrollbar.click(this.parent, x - this.parent.getGuiLeft(), y - this.parent.getGuiTop());
    }

    @Override
    public boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        if (ModAndClassUtil.NEI && this.searchField.isMouseIn(mouseX, mouseY) && isNEISearch()) {
            TextHistory.Direction direction;
            switch (wheel) {
                case -1:
                    direction = TextHistory.Direction.PREVIOUS;
                    break;
                case 1:
                    direction = TextHistory.Direction.NEXT;
                    break;
                default:
                    return false;
            }
            this.history.get(direction, this.searchField.getText())
                .ifPresent(t -> setSearchString(t, true));
            return true;
        }
        if (this.scrollbar.contains(mouseX - this.parent.getGuiLeft(), mouseY - this.parent.getGuiTop())
            || (mouseX > this.absX && mouseX < this.absX + this.w
                && mouseY > this.absY + 18
                && mouseY < this.absY + this.h)) {
            this.saveSearchString();
            final int currentScroll = this.scrollbar.getCurrentScroll();
            this.scrollbar.wheel(wheel);
            return currentScroll != this.scrollbar.getCurrentScroll();
        }
        return mouseX > this.absX && mouseX < this.absX + this.w && mouseY > this.absY && mouseY < this.absY + this.h;
    }

    private void updateSuggestion() {
        if (ModAndClassUtil.NEI && this.isNEISearch()) {
            if (this.searchField.getText()
                .isEmpty()) {
                this.setSuggestion("");
                return;
            }
            Optional<String> history = Ae2ReflectClient.getHistoryList(this.history)
                .stream()
                .filter(s -> s.startsWith(this.searchField.getText()))
                .findFirst();
            if (history.isPresent()) {
                this.setSuggestion(history.get());
            } else {
                this.setSuggestion("");
            }
        }
    }

    private void setSuggestion(String suggestion) {
        this.searchField.setSuggestion(suggestion);
    }

    @Override
    public boolean keyTyped(char character, int key) {
        if (ModAndClassUtil.NEI && this.isNEISearch()) {
            if (key == Keyboard.KEY_TAB && this.searchField.isFocused()) {
                Optional<String> history = Ae2ReflectClient.getHistoryList(this.history)
                    .stream()
                    .filter(s -> s.startsWith(this.searchField.getText()))
                    .findFirst();
                history.ifPresent(s -> setSearchString(s, true));
                return true;
            } else if (key == Keyboard.KEY_DELETE) {
                String next = this.history.getNext(this.searchField.getText())
                    .orElse("");
                Ae2ReflectClient.getHistoryList(this.history)
                    .removeIf(s -> s.equals(this.searchField.getText()));
                setSearchString(next, true);
                return true;
            }
        }
        if (this.searchField.isFocused()) {
            if (character == ' ' && this.searchField.getText()
                .isEmpty()) {
                return false;
            }

            if (this.searchField.textboxKeyTyped(character, key)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.setScrollBar();
                this.updateSuggestion();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean draggable() {
        return false;
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(this.absX, this.absY, w, h);
    }

    @Override
    public void setRectangle(int x, int y) {
        this.absX = x;
        this.absY = y;
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
    public void onGuiClosed() {
        memoryText = this.searchField.getText();
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.SortByBox != null) {
            this.SortByBox.set(this.configSrc.getSetting(Settings.SORT_BY));
        }
        if (this.SortDirBox != null) {
            this.SortDirBox.set(this.configSrc.getSetting(Settings.SORT_DIRECTION));
        }
        if (this.ViewBox != null) {
            this.ViewBox.set(this.configSrc.getSetting(Settings.VIEW_MODE));
        }
        if (this.typeFilter != null) {
            this.typeFilter.set(this.configSrc.getSetting(Settings.TYPE_FILTER));
        }

        this.repo.updateView();
    }

    public AdvItemRepo getRepo() {
        return repo;
    }

    @Override
    public void handleKeyboardInput() {
        this.getRepo()
            .setPaused(this.parent.hasShiftDown());
    }

    @Override
    public void setPlayerInv(ItemStack is) {
        this.container.getPlayerInv()
            .setItemStack(is);
    }

    @Override
    public THGuiTextField getSearchField() {
        return this.searchField;
    }
}
