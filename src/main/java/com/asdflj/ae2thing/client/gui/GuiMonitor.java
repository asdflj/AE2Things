package com.asdflj.ae2thing.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerMonitor;
import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;
import com.asdflj.ae2thing.client.me.AdvItemRepo;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.config.CraftingStatus;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotDisconnected;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternTerm;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.InventoryAction;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.modules.NEI;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import codechicken.nei.util.TextHistory;

public abstract class GuiMonitor extends BaseMEGui
    implements IConfigManagerHost, ISortSource, IDropToFillTextField, IGuiDrawSlot, IGuiMonitorTerminal {

    protected GuiImgButton clearBtn;
    public static int craftingGridOffsetX;
    public static int craftingGridOffsetY;
    protected static String memoryText = "";
    protected final int offsetX = 9;
    protected final int lowerTextureOffset = 0;
    protected AdvItemRepo repo;
    protected THGuiTextField searchField;
    protected int perRow = 9;
    protected int reservedSpace = 0;
    protected int rows = 0;
    protected int maxRows = Integer.MAX_VALUE;
    protected int standardSize;
    protected int offsetY;
    protected GuiTabButton craftingStatusBtn;
    protected GuiImgButton craftingStatusImgBtn;
    protected GuiImgButton SortByBox;
    protected GuiImgButton SortDirBox;
    protected GuiImgButton searchBoxSettings;
    protected GuiImgButton terminalStyleBox;
    protected GuiImgButton searchStringSave;
    protected GuiImgButton ViewBox;
    protected GuiImgButton typeFilter;
    protected boolean showViewBtn = true;
    protected boolean viewCell = true;
    protected final ContainerMonitor container;

    public GuiMonitor(Container container) {
        super(container);
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.container = (ContainerMonitor) container;
        this.repo = new AdvItemRepo(getScrollBar(), this);
        this.repo.setPowered(true);
    }

    protected void saveSearchString() {
        if (ModAndClassUtil.NEI && isNEISearch()
            && !this.searchField.getText()
                .isEmpty()) {
            this.history.add(this.searchField.getText());
        }
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        saveSearchString();
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (updateFluidContainer(slot, slotIdx, ctrlDown, mouseButton)) return;

        if (slot instanceof SlotFake) {
            InventoryAction action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                : InventoryAction.PICKUP_OR_SET_DOWN;
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
                if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
                    action = InventoryAction.MOVE_REGION;
                } else {
                    action = InventoryAction.PICKUP_SINGLE;
                }
            }
            if (Ae2ReflectClient.getDragClick(this)
                .size() > 1) {
                return;
            }
            final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
            NetworkHandler.instance.sendToServer(p);
            return;
        }

        if (slot instanceof SlotPatternTerm) {
            if (mouseButton == 6) {
                return; // prevents weird double clicks
            }
            try {
                NetworkHandler.instance.sendToServer(((SlotPatternTerm) slot).getRequest(isShiftKeyDown()));
            } catch (final IOException e) {
                AELog.debug(e);
            }
        } else if (slot instanceof SlotCraftingTerm) {
            if (mouseButton == 6) {
                return; // prevents weird double clicks
            }
            InventoryAction action;
            if (isShiftKeyDown()) {
                action = InventoryAction.CRAFT_SHIFT;
            } else {
                // Craft a stack on right-click, craft a single one on left-click
                action = (mouseButton == 1) ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
            }
            final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
            NetworkHandler.instance.sendToServer(p);
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            if (this.enableSpaceClicking() && !(slot instanceof SlotPatternTerm)) {
                IAEItemStack stack = null;
                if (slot instanceof SlotME) {
                    stack = ((SlotME) slot).getAEStack();
                }
                int slotNum = Ae2ReflectClient.getInventorySlots(this)
                    .size();
                if (!(slot instanceof SlotME) && slot != null) {
                    slotNum = slot.slotNumber;
                }
                ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, 0);
                NetworkHandler.instance.sendToServer(p);
                return;
            }
        }

        if (slot instanceof SlotDisconnected) {
            if (Ae2ReflectClient.getDragClick(this)
                .size() > 1) {
                return;
            }
            InventoryAction action = null;
            switch (mouseButton) {
                case 0: // pickup / set-down.
                {
                    ItemStack heldStack = player.inventory.getItemStack();
                    if (slot.getStack() == null && heldStack != null) action = InventoryAction.SPLIT_OR_PLACE_SINGLE;
                    else if (slot.getStack() != null && (heldStack == null || heldStack.stackSize <= 1))
                        action = InventoryAction.PICKUP_OR_SET_DOWN;
                }
                    break;
                case 1:
                    action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;
                case 3: // creative dupe:
                    if (player.capabilities.isCreativeMode) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }
                    break;
                default:
                case 4: // drop item:
                case 6:
            }
            if (action != null) {
                final PacketInventoryAction p = new PacketInventoryAction(
                    action,
                    slot.getSlotIndex(),
                    ((SlotDisconnected) slot).getSlot()
                        .getId());
                NetworkHandler.instance.sendToServer(p);
            }
            return;
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
                    stack = transformItem(stack); // for fluid terminal
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
                ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                AE2Thing.proxy.netHandler.sendToServer(
                    new CPacketInventoryAction(
                        action,
                        Ae2ReflectClient.getInventorySlots(this)
                            .size(),
                        0,
                        stack));
            } else if (action != null) {
                if (stack != null && stack.getItem() instanceof ItemFluidDrop) stack = null;
                ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                final PacketInventoryAction p = new PacketInventoryAction(
                    action,
                    Ae2ReflectClient.getInventorySlots(this)
                        .size(),
                    0);
                NetworkHandler.instance.sendToServer(p);
            }
            return;
        }

        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    protected IAEItemStack transformItem(IAEItemStack stack) {
        return stack;
    }

    protected int getMaxRows() {
        return AEConfig.instance.getConfigManager()
            .getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
    }

    public void setScrollBar() {
        this.getScrollBar()
            .setTop(18)
            .setLeft(175)
            .setHeight(this.rows * 18 - 2);
        this.getScrollBar()
            .setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.maxRows = this.getMaxRows();
        this.perRow = AEConfig.instance.getConfigManager()
            .getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL ? 9
                : 9 + ((this.width - this.standardSize) / 18);

        final boolean hasNEI = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI);

        final int NEI = 0;
        int top = hasNEI ? 22 : 0;

        final int magicNumber = 114 + 1;
        final int extraSpace = this.height - magicNumber - NEI - top - this.reservedSpace;

        this.rows = (int) Math.floor(extraSpace / 18.0);
        if (this.rows > this.maxRows) {
            this.rows = this.maxRows;
        }

        if (hasNEI) {
            this.rows--;
        }

        if (this.rows < 3) {
            this.rows = 3;
        }

        this.getMeSlots()
            .clear();
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                this.getMeSlots()
                    .add(new InternalSlotME(this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18));
            }
        }

        if (AEConfig.instance.getConfigManager()
            .getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL) {
            this.xSize = this.standardSize + ((this.perRow - 9) * 18);
        } else {
            this.xSize = this.standardSize;
        }

        super.initGui();
        // full size : 204
        // extra slots : 72
        // slot 18

        this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
        final int unusedSpace = this.height - this.ySize;
        this.guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));

        this.offsetY = this.guiTop + 8;

        this.buttonList.add(
            this.SortByBox = new GuiImgButton(
                this.guiLeft - 18,
                this.offsetY,
                Settings.SORT_BY,
                this.configSrc.getSetting(Settings.SORT_BY)));
        this.offsetY += 20;

        if (this.showViewBtn) {
            this.buttonList.add(
                this.ViewBox = new GuiImgButton(
                    this.guiLeft - 18,
                    this.offsetY,
                    Settings.VIEW_MODE,
                    this.configSrc.getSetting(Settings.VIEW_MODE)));
            this.offsetY += 20;
        }

        if (ModAndClassUtil.isTypeFilter) {
            this.buttonList.add(
                this.typeFilter = new GuiImgButton(
                    this.guiLeft - 18,
                    this.offsetY,
                    Settings.TYPE_FILTER,
                    this.configSrc.getSetting(Settings.TYPE_FILTER)));
            this.offsetY += 20;
        }

        this.buttonList.add(
            this.SortDirBox = new GuiImgButton(
                this.guiLeft - 18,
                this.offsetY,
                Settings.SORT_DIRECTION,
                this.configSrc.getSetting(Settings.SORT_DIRECTION)));
        this.offsetY += 20;

        this.buttonList.add(
            this.searchBoxSettings = new GuiImgButton(
                this.guiLeft - 18,
                this.offsetY,
                Settings.SEARCH_MODE,
                AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE)));
        this.offsetY += 20;

        this.buttonList.add(
            this.searchStringSave = new GuiImgButton(
                this.guiLeft - 18,
                this.offsetY,
                Settings.SAVE_SEARCH,
                AEConfig.instance.preserveSearchBar ? YesNo.YES : YesNo.NO));
        this.offsetY += 20;

        this.buttonList.add(
            this.terminalStyleBox = new GuiImgButton(
                this.guiLeft - 18,
                this.offsetY,
                Settings.TERMINAL_STYLE,
                AEConfig.instance.settings.getSetting(Settings.TERMINAL_STYLE)));
        this.offsetY += 20;

        // Right now 80 > offsetX, but that can be changed later.
        // noinspection DataFlowIssue
        this.searchField = new THGuiTextField(
            this.fontRendererObj,
            this.guiLeft + Math.max(80, this.offsetX),
            this.guiTop + 4,
            90,
            12);
        this.searchField.setMessage(ButtonToolTips.SearchStringTooltip.getLocal());
        if (this.viewCell) {
            if (ModAndClassUtil.isCraftStatus && AEConfig.instance.getConfigManager()
                .getSetting(Settings.CRAFTING_STATUS)
                .equals(CraftingStatus.BUTTON)) {
                this.buttonList.add(
                    this.craftingStatusImgBtn = new GuiImgButton(
                        this.guiLeft - 18,
                        this.offsetY,
                        Settings.CRAFTING_STATUS,
                        AEConfig.instance.settings.getSetting(Settings.CRAFTING_STATUS)));
                this.offsetY += 20;
            } else {
                this.buttonList.add(
                    this.craftingStatusBtn = new GuiTabButton(
                        this.guiLeft + 170,
                        this.guiTop - 4,
                        2 + 11 * 16,
                        GuiText.CraftingStatus.getLocal(),
                        itemRender));
                this.craftingStatusBtn.setHideEdge(13); // GuiTabButton implementation //
            }
        }

        final Enum<?> setting = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        this.searchField.setFocused(SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting);

        if ((AEConfig.instance.preserveSearchBar || this.isSubGui())) {
            setSearchString(memoryText, false);
        }
        if (this.isSubGui()) {
            this.repo.updateView();
            this.setScrollBar();
        }

        craftingGridOffsetX = Integer.MAX_VALUE;
        craftingGridOffsetY = Integer.MAX_VALUE;

        for (final Object s : this.inventorySlots.inventorySlots) {
            if (s instanceof AppEngSlot) {
                if (((Slot) s).xDisplayPosition < 195 || s instanceof SlotDisabled) {
                    this.repositionSlot((AppEngSlot) s);
                }
            }

            if (s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix) {
                final Slot g = (Slot) s;
                if (g.xDisplayPosition > 0 && g.yDisplayPosition > 0) {
                    craftingGridOffsetX = Math.min(craftingGridOffsetX, g.xDisplayPosition);
                    craftingGridOffsetY = Math.min(craftingGridOffsetY, g.yDisplayPosition);
                }
            }
        }

        craftingGridOffsetX -= 25;
        craftingGridOffsetY -= 6;
    }

    protected void repositionSlot(AppEngSlot s) {}

    @Override
    protected void keyTyped(final char character, final int key) {
        if (ModAndClassUtil.NEI && this.isNEISearch()) {
            if (key == Keyboard.KEY_TAB) {
                Optional<String> history = Ae2ReflectClient.getHistoryList(this.history)
                    .stream()
                    .filter(s -> s.startsWith(this.searchField.getText()))
                    .findFirst();
                history.ifPresent(s -> setSearchString(s, true));
                return;
            } else if (key == Keyboard.KEY_DELETE) {
                String next = this.history.getNext(this.searchField.getText())
                    .orElse("");
                Ae2ReflectClient.getHistoryList(this.history)
                    .removeIf(s -> s.equals(this.searchField.getText()));
                setSearchString(next, true);
                return;
            }
        }

        if (!this.checkHotbarKeys(key)) {
            if (character == ' ' && this.searchField.getText()
                .isEmpty()) {
                return;
            }

            if (this.searchField.textboxKeyTyped(character, key)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.setScrollBar();
                this.updateSuggestion();
            } else {
                super.keyTyped(character, key);
            }
        }
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

    public void setSearchString(String memoryText, boolean updateView) {
        this.searchField.setText(memoryText);
        this.repo.setSearchString(memoryText);
        if (updateView) {
            this.repo.updateView();
            this.setScrollBar();
        }
        updateSuggestion();
    }

    public void setSearchString(String memoryText, boolean updateView, int pos) {
        this.setSearchString(memoryText, updateView);
        this.searchField.setCursorPosition(pos);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
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

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        final Enum<?> searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        if (searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.NEI_AUTOSEARCH) {
            this.searchField.mouseClicked(xCoord, yCoord, btn);
        }
        if (btn == 1 && this.searchField.isMouseIn(xCoord, yCoord)) {
            setSearchString("", true);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == this.craftingStatusBtn || btn == this.craftingStatusImgBtn) {
            InventoryHandler.switchGui(GuiType.CRAFTING_STATUS);
        }
        if (btn instanceof final GuiImgButton iBtn) {
            final boolean backwards = Mouse.isButtonDown(1);
            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum<?> cv = iBtn.getCurrentValue();
                final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());
                if (btn == this.terminalStyleBox) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
                } else if (btn == this.searchBoxSettings) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
                } else if (btn == this.searchStringSave) {
                    AEConfig.instance.preserveSearchBar = next == YesNo.YES;
                } else {
                    try {
                        NetworkHandler.instance
                            .sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
                    } catch (final IOException e) {
                        AELog.debug(e);
                    }
                }
                iBtn.set(next);
                if (next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class) {
                    this.reInitalize();
                }
            }
        }
        super.actionPerformed(btn);
    }

    protected void reInitalize() {
        this.buttonList.clear();
        this.initGui();
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
    public void drawScreen(int mouseX, int mouseY, float btn) {
        if (this.repo.hasCache()) {
            try {
                this.repo.getLock()
                    .lock();
                super.drawScreen(mouseX, mouseY, btn);
            } finally {
                this.repo.getLock()
                    .unlock();
            }
        } else {
            super.drawScreen(mouseX, mouseY, btn);
        }
        if (searchField == null) return;
        if (AEConfig.instance.preserveSearchBar) handleTooltip(mouseX, mouseY, searchField.getTooltipProvider());
        if (ModAndClassUtil.NEI && this.searchField.isMouseIn(mouseX, mouseY) && this.isNEISearch()) {
            // draw selection
            List<String> list = Ae2ReflectClient.getHistoryList(this.history);
            drawHistorySelection(
                searchField.xPosition,
                searchField.yPosition,
                searchField.getText(),
                searchField.width,
                list);
        }
    }

    public void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/" + file);
        this.mc.getTextureManager()
            .bindTexture(loc);
    }

    @Override
    public boolean isOverTextField(int mousex, int mousey) {
        return searchField.isMouseIn(mousex, mousey);
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        if (ModAndClassUtil.THE && AspectUtil.getAspectFromJar(stack) != null) {
            setSearchString(
                Objects.requireNonNull(AspectUtil.getAspectFromJar(stack))
                    .getName(),
                true);
        } else {
            setSearchString(NEI.searchField.getEscapedSearchText(displayName), true);
        }
        this.saveSearchString();
    }

    @Override
    protected boolean mouseWheelEvent(int x, int y, int wheel) {
        if (ModAndClassUtil.NEI && this.searchField.isMouseIn(x, y) && isNEISearch()) {
            TextHistory.Direction direction;
            switch (wheel) {
                case -1:
                    direction = TextHistory.Direction.PREVIOUS;
                    break;
                case 1:
                    direction = TextHistory.Direction.NEXT;
                    break;
                default:
                    return super.mouseWheelEvent(x, y, wheel);
            }
            this.history.get(direction, this.searchField.getText())
                .ifPresent(t -> setSearchString(t, true));

        }
        return super.mouseWheelEvent(x, y, wheel);
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot(s)) super.func_146977_a(s);
    }

    @Override
    public float getzLevel() {
        return this.zLevel;
    }

    public abstract void postUpdate(List<IAEItemStack> list);

    public void setPlayerInv(ItemStack is) {
        this.container.getPlayerInv()
            .setItemStack(is);
    }

    @Override
    public AEBaseGui getAEBaseGui() {
        return this;
    }

    @Override
    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public AdvItemRepo getRepo() {
        return repo;
    }

    @Override
    public void handleKeyboardInput() {
        super.handleKeyboardInput();
        this.getRepo()
            .setPaused(hasShiftDown());
    }

    @Override
    public THGuiTextField getSearchField() {
        return searchField;
    }
}
