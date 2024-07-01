package com.asdflj.ae2thing.client.gui;

import static com.asdflj.ae2thing.util.NameConst.TT_FLUID_TERMINAL_AMOUNT;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.container.ContainerMonitor;
import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;
import com.asdflj.ae2thing.client.me.AdvItemRepo;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.network.CPacketFluidUpdate;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.util.Util;

import appeng.api.config.CraftingStatus;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalFontSize;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
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
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import codechicken.nei.LayoutManager;
import codechicken.nei.util.TextHistory;

public abstract class GuiMonitor extends AEBaseMEGui
    implements IConfigManagerHost, ISortSource, IDropToFillTextField, IGuiDrawSlot {

    protected GuiImgButton clearBtn;
    public static int craftingGridOffsetX;
    public static int craftingGridOffsetY;
    protected static String memoryText = "";
    protected final int offsetX = 9;
    protected final int lowerTextureOffset = 0;
    public IConfigManager configSrc;
    protected IDisplayRepo repo;
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
    protected TextHistory history;
    protected boolean showViewBtn = true;
    protected boolean viewCell = true;
    protected final ContainerMonitor container;

    public GuiMonitor(Container container) {
        super(container);
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        this.repo = new AdvItemRepo(getScrollBar(), this);
        this.repo.setPowered(true);
        this.history = Ae2ReflectClient.getHistory(LayoutManager.searchField);
        this.container = (ContainerMonitor) container;
    }

    protected boolean isNEISearch() {
        final Enum<?> s = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        return s == SearchBoxMode.NEI_MANUAL_SEARCH || s == SearchBoxMode.NEI_AUTOSEARCH;
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
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (slot instanceof SlotME sme) {
            ItemStack cs = player.inventory.getItemStack();
            if (ctrlDown == 0 && sme.getHasStack()
                && sme.getStack()
                    .getItem() instanceof ItemFluidDrop
                && sme.getAEStack()
                    .getStackSize() != 0) {
                IAEFluidStack fluid = ItemFluidDrop.getAeFluidStack(sme.getAEStack());
                AE2Thing.proxy.netHandler.sendToServer(new CPacketFluidUpdate(fluid, isShiftKeyDown()));
                return;
            } else if (ctrlDown == 1 && (Util.FluidUtil.isFilled(cs) || !AspectUtil.isEmptyEssentiaContainer(cs))) {
                AE2Thing.proxy.netHandler.sendToServer(new CPacketFluidUpdate(null, isShiftKeyDown()));
                return;
            }
            if (mouseButton == 3 && player.capabilities.isCreativeMode
                && sme.getHasStack()
                && !sme.getAEStack()
                    .isCraftable()
                && sme.getStack()
                    .getItem() instanceof ItemFluidDrop) {
                return;
            }
        }

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
                    } else if (stack != null) {
                        AE2ThingAPI.instance()
                            .togglePinItems(stack);
                        this.repo.updateView();
                        return;
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

    protected void setScrollBar() {
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
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setVisible(true);
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
        if (ModAndClassUtil.NEI && key == Keyboard.KEY_DELETE) {
            String next = this.history.getNext(this.searchField.getText())
                .orElse("");
            Ae2ReflectClient.getHistoryList(this.history)
                .removeIf(s -> s.equals(this.searchField.getText()));
            setSearchString(next, true);
            return;
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
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    public void setSearchString(String memoryText, boolean updateView) {
        this.searchField.setText(memoryText);
        this.repo.setSearchString(memoryText);
        if (updateView) {
            this.repo.updateView();
            this.setScrollBar();
        }
    }

    public void setSearchString(String memoryText, boolean updateView, int pos) {
        this.setSearchString(memoryText, updateView);
        this.searchField.setCursorPosition(pos);
    }

    protected void drawHistorySelection(final int x, final int y, String text, int width,
        final List<String> searchHistory) {
        final int maxRows = 5;
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
        super.drawScreen(mouseX, mouseY, btn);
        if (AEConfig.instance.preserveSearchBar && searchField != null)
            handleTooltip(mouseX, mouseY, searchField.getTooltipProvider());
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
        if (AspectUtil.getAspectFromJar(stack) != null) {
            setSearchString(
                Objects.requireNonNull(AspectUtil.getAspectFromJar(stack))
                    .getName(),
                true);
        } else {
            setSearchString(displayName, true);
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
    public List<String> handleItemTooltip(final ItemStack stack, final int mouseX, final int mouseY,
        final List<String> currentToolTip) {
        if (stack != null && stack.getItem() instanceof ItemFluidDrop) {
            if (isShiftKeyDown()) return currentToolTip;
            final Slot s = this.getSlot(mouseX, mouseY);
            if (s instanceof SlotME || s instanceof SlotFake) {
                final int BigNumber = AEConfig.instance.getTerminalFontSize() == TerminalFontSize.SMALL ? 9999 : 999;

                IAEItemStack myStack = null;

                try {
                    myStack = Platform.getAEStackInSlot(s);
                } catch (final Throwable ignore) {}

                if (myStack != null) {
                    if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1)) {
                        final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getStackSize());
                        final String format = I18n.format(TT_FLUID_TERMINAL_AMOUNT, formattedAmount);
                        currentToolTip.add("\u00a77" + format);
                    }
                }
            }
            return currentToolTip;
        } else {
            return super.handleItemTooltip(stack, mouseX, mouseY, currentToolTip);
        }
    }

}
