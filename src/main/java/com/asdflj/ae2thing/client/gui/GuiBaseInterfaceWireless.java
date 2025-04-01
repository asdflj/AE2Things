package com.asdflj.ae2thing.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;
import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.asdflj.ae2thing.common.item.ItemPatternModifier;
import com.asdflj.ae2thing.network.CPacketRenamer;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.GTUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.NeCharUtil;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.client.gui.GuiFCImgButton;

import appeng.api.AEApi;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.IInterfaceTerminalPostUpdate;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInterfaceTerminalUpdate;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.helpers.PatternHelper;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Loader;

public class GuiBaseInterfaceWireless extends BaseMEGui implements IDropToFillTextField, IInterfaceTerminalPostUpdate {

    public Minecraft mc = Minecraft.getMinecraft();
    public static final int HEADER_HEIGHT = 52;
    public static final int INV_HEIGHT = 98;
    public static final int VIEW_WIDTH = 174;
    public static final int VIEW_LEFT = 10;
    protected static final ResourceLocation BACKGROUND = new ResourceLocation(
        AppEng.MOD_ID,
        "textures/guis/newinterfaceterminal.png");

    private final InterfaceWirelessList masterList = new InterfaceWirelessList();
    private final MEGuiTextField searchFieldOutputs;
    private final MEGuiTextField searchFieldInputs;
    private final THGuiTextField searchFieldNames;
    private final GuiImgButton guiButtonHideFull;
    private final GuiImgButton guiButtonAssemblersOnly;
    private final GuiImgButton guiButtonBrokenRecipes;
    private final GuiImgButton terminalStyleBox;
    private final GuiImgButton searchStringSave;
    private boolean onlyMolecularAssemblers = false;
    private boolean onlyBrokenRecipes = false;
    private boolean online;
    /** The height of the viewport. */
    protected int viewHeight;
    private final List<String> extraOptionsText;
    private ItemStack tooltipStack;
    private final boolean neiPresent;
    protected static String searchFieldInputsText = "";
    protected static String searchFieldOutputsText = "";
    protected static String searchFieldNamesText = "";
    /*
     * Z-level Map (FLOATS) 0.0 - BACKGROUND 1.0 - ItemStacks 2.0 - Slot color overlays 20.0 - ItemStack overlays 21.0 -
     * Slot mouse hover overlay 200.0 - Tooltips
     */
    private static final float ITEM_STACK_Z = 100.0f;
    private static final float SLOT_Z = 0.5f;
    private static final float ITEM_STACK_OVERLAY_Z = 200.0f;
    private static final float SLOT_HOVER_Z = 310.0f;
    private static final float TOOLTIP_Z = 410.0f;
    private static final float STEP_Z = 10.0f;
    private static final float MAGIC_RENDER_ITEM_Z = 50.0f;

    protected int offsetY;

    public GuiBaseInterfaceWireless(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(new ContainerWirelessDualInterfaceTerminal(inventoryPlayer, te));
        this.setScrollBar(new GuiScrollbar());
        this.xSize = 240;
        this.ySize = 255;
        this.neiPresent = Loader.isModLoaded("NotEnoughItems");

        searchFieldInputs = new MEGuiTextField(86, 12, ButtonToolTips.SearchFieldInputs.getLocal()) {

            @Override
            public void onTextChange(final String oldText) {
                masterList.markDirty();
            }
        };

        searchFieldOutputs = new MEGuiTextField(86, 12, ButtonToolTips.SearchFieldOutputs.getLocal()) {

            @Override
            public void onTextChange(final String oldText) {
                masterList.markDirty();
            }
        };

        searchFieldNames = new THGuiTextField(71, 12, ButtonToolTips.SearchFieldNames.getLocal()) {

            @Override
            public void onTextChange(final String oldText) {
                updateSuggestion();
                masterList.markDirty();
            }
        };

        searchStringSave = new GuiImgButton(
            0,
            0,
            Settings.SAVE_SEARCH,
            AEConfig.instance.preserveSearchBar ? YesNo.YES : YesNo.NO);
        guiButtonAssemblersOnly = new GuiImgButton(0, 0, Settings.ACTIONS, null);
        guiButtonHideFull = new GuiImgButton(0, 0, Settings.ACTIONS, null);
        guiButtonBrokenRecipes = new GuiImgButton(0, 0, Settings.ACTIONS, null);

        terminalStyleBox = new GuiImgButton(0, 0, Settings.TERMINAL_STYLE, null);

        this.extraOptionsText = new ArrayList<>(2);
        extraOptionsText.add(ButtonToolTips.HighlightInterface.getLocal());
    }

    @Override
    public int getOffsetY() {
        return this.offsetY;
    }

    public void setOffsetY(int y) {
        this.offsetY = y;
    }

    public void setInterfaceScrollBar() {
        int maxScroll = this.masterList.getHeight() - this.viewHeight - 1;
        if (maxScroll <= 0) {
            this.getScrollBar()
                .setTop(52)
                .setLeft(189)
                .setHeight(this.viewHeight)
                .setRange(0, 0, 1);
        } else {
            this.getScrollBar()
                .setTop(52)
                .setLeft(189)
                .setHeight(this.viewHeight)
                .setRange(0, maxScroll, 12);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();
        this.viewHeight = calculateViewHeight();
        this.ySize = HEADER_HEIGHT + INV_HEIGHT + this.viewHeight;

        final int unusedSpace = this.height - this.ySize;
        this.guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));

        searchFieldInputs.x = guiLeft + Math.max(32, VIEW_LEFT);
        searchFieldInputs.y = guiTop + 25;

        searchFieldOutputs.x = guiLeft + Math.max(32, VIEW_LEFT);
        searchFieldOutputs.y = guiTop + 38;

        searchFieldNames.x = guiLeft + Math.max(32, VIEW_LEFT) + 99;
        searchFieldNames.y = guiTop + 38;

        terminalStyleBox.xPosition = guiLeft - 18;
        terminalStyleBox.yPosition = guiTop + 8;

        searchStringSave.xPosition = guiLeft - 18;
        searchStringSave.yPosition = terminalStyleBox.yPosition + 18;

        guiButtonBrokenRecipes.xPosition = guiLeft - 18;
        guiButtonBrokenRecipes.yPosition = searchStringSave.yPosition + 18;

        guiButtonHideFull.xPosition = guiLeft - 18;
        guiButtonHideFull.yPosition = guiButtonBrokenRecipes.yPosition + 18;

        guiButtonAssemblersOnly.xPosition = guiLeft - 18;
        guiButtonAssemblersOnly.yPosition = guiButtonHideFull.yPosition + 18;

        offsetY = guiButtonAssemblersOnly.yPosition + 18;

        if (AEConfig.instance.preserveSearchBar || isSubGui()) {
            setSearchString();
        }

        this.setInterfaceScrollBar();
        this.repositionSlots();

        buttonList.add(guiButtonAssemblersOnly);
        buttonList.add(guiButtonHideFull);
        buttonList.add(guiButtonBrokenRecipes);
        buttonList.add(searchStringSave);
        buttonList.add(terminalStyleBox);
    }

    protected void repositionSlots() {
        for (final Object obj : this.inventorySlots.inventorySlots) {
            if (obj instanceof final AppEngSlot slot) {
                slot.yDisplayPosition = this.ySize + slot.getY() - 78 - 4;
            }
        }
    }

    protected int calculateViewHeight() {
        final int maxViewHeight = this.getMaxViewHeight();
        final boolean hasNEI = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI);
        final int NEIPadding = hasNEI ? 22 /* input */ + 18 /* top panel */ : 0;
        final int availableSpace = this.height - HEADER_HEIGHT - INV_HEIGHT - NEIPadding;

        // screen should use 95% of the space it can, 5% margins
        return Math.min((int) (availableSpace * 0.95), maxViewHeight);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        fontRendererObj.drawString(
            getGuiDisplayName(GuiText.InterfaceTerminal.getLocal()),
            8,
            6,
            GuiColors.InterfaceTerminalTitle.getColor());
        fontRendererObj.drawString(
            GuiText.inventory.getLocal(),
            VIEW_LEFT + 2,
            this.ySize - 96,
            GuiColors.InterfaceTerminalInventory.getColor());
        if (!neiPresent && tooltipStack != null) {
            renderToolTip(tooltipStack, mouseX, mouseY);
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        guiButtonAssemblersOnly.set(
            onlyMolecularAssemblers ? ActionItems.MOLECULAR_ASSEMBLEERS_ON : ActionItems.MOLECULAR_ASSEMBLEERS_OFF);
        guiButtonHideFull.set(
            AEConfig.instance.showOnlyInterfacesWithFreeSlotsInInterfaceTerminal
                ? ActionItems.TOGGLE_SHOW_FULL_INTERFACES_OFF
                : ActionItems.TOGGLE_SHOW_FULL_INTERFACES_ON);
        guiButtonBrokenRecipes.set(
            onlyBrokenRecipes ? ActionItems.TOGGLE_SHOW_ONLY_INVALID_PATTERN_OFF
                : ActionItems.TOGGLE_SHOW_ONLY_INVALID_PATTERN_ON);

        terminalStyleBox.set(AEConfig.instance.settings.getSetting(Settings.TERMINAL_STYLE));

        super.drawScreen(mouseX, mouseY, btn);

        handleTooltip(mouseX, mouseY, searchFieldInputs);
        handleTooltip(mouseX, mouseY, searchFieldOutputs);
        handleTooltip(mouseX, mouseY, searchFieldNames.getTooltipProvider());
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        searchFieldInputs.mouseClicked(xCoord, yCoord, btn);
        searchFieldOutputs.mouseClicked(xCoord, yCoord, btn);
        searchFieldNames.mouseClicked(xCoord, yCoord, btn);

        if (masterList.mouseClicked(xCoord - guiLeft - VIEW_LEFT, yCoord - guiTop - HEADER_HEIGHT, btn)) {
            return;
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    public void setSearchFieldSuggestion(String text) {
        searchFieldNames.setSuggestion(text);
    }

    public void setSearchFieldText(String text) {
        searchFieldNames.setText(text);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == guiButtonAssemblersOnly) {
            onlyMolecularAssemblers = !onlyMolecularAssemblers;
            masterList.markDirty();
        } else if (btn == guiButtonHideFull) {
            AEConfig.instance.showOnlyInterfacesWithFreeSlotsInInterfaceTerminal = !AEConfig.instance.showOnlyInterfacesWithFreeSlotsInInterfaceTerminal;
            masterList.markDirty();
        } else if (btn == guiButtonBrokenRecipes) {
            onlyBrokenRecipes = !onlyBrokenRecipes;
            masterList.markDirty();
        } else if (btn instanceof GuiImgButton iBtn) {
            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum<?> cv = iBtn.getCurrentValue();
                final boolean backwards = Mouse.isButtonDown(1);
                final Enum<?> next = Platform.rotateEnum(
                    cv,
                    backwards,
                    iBtn.getSetting()
                        .getPossibleValues());

                if (btn == this.terminalStyleBox) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
                    initGui();
                } else if (btn == searchStringSave) {
                    AEConfig.instance.preserveSearchBar = next == YesNo.YES;
                }

                iBtn.set(next);
            }
        } else {
            super.actionPerformed(btn);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        searchFieldInputsText = searchFieldInputs.getText();
        searchFieldOutputsText = searchFieldOutputs.getText();
        searchFieldNamesText = searchFieldNames.getText();
    }

    public void setSearchString() {
        searchFieldInputs.setText(searchFieldInputsText);
        searchFieldOutputs.setText(searchFieldOutputsText);
        searchFieldNames.setText(searchFieldNamesText);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture(BACKGROUND);
        /* Draws the top part. */
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, HEADER_HEIGHT);
        /* Draws the middle part. */
        Tessellator.instance.startDrawingQuads();
        addTexturedRectToTesselator(
            offsetX,
            offsetY + HEADER_HEIGHT,
            offsetX + xSize,
            offsetY + HEADER_HEIGHT + viewHeight + 1,
            0.0f,
            0.0f,
            (HEADER_HEIGHT + InterfaceWirelessSection.TITLE_HEIGHT + 1.0f) / 256.0f,
            this.xSize / 256.0f,
            (HEADER_HEIGHT + 106.0f) / 256.0f);
        Tessellator.instance.draw();
        /* Draw the bottom part */
        this.drawTexturedModalRect(offsetX, offsetY + HEADER_HEIGHT + viewHeight, 0, 158, xSize, INV_HEIGHT);
        if (online) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            /* (0,0) => viewPort's (0,0) */
            GL11.glPushMatrix();
            GL11.glTranslatef(offsetX + VIEW_LEFT, offsetY + HEADER_HEIGHT, 0);
            tooltipStack = null;
            masterList.hoveredEntry = null;
            drawViewport(mouseX - offsetX - VIEW_LEFT, mouseY - offsetY - HEADER_HEIGHT - 1);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
        searchFieldInputs.drawTextBox();
        searchFieldOutputs.drawTextBox();
        searchFieldNames.drawTextBox();
    }

    /**
     * Draws the viewport area
     */
    private void drawViewport(int relMouseX, int relMouseY) {
        /* Viewport Magic */
        final int scroll = this.getScrollBar()
            .getCurrentScroll();
        int viewY = -scroll; // current y in viewport coordinates
        int entryIdx = 0;
        List<InterfaceWirelessSection> visibleSections = this.masterList.getVisibleSections();

        final float guiScaleX = (float) mc.displayWidth / width;
        final float guiScaleY = (float) mc.displayHeight / height;
        GL11.glScissor(
            (int) ((guiLeft + VIEW_LEFT) * guiScaleX),
            (int) ((height - (guiTop + HEADER_HEIGHT + viewHeight)) * guiScaleY),
            (int) (VIEW_WIDTH * guiScaleX),
            (int) (this.viewHeight * guiScaleY));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        /*
         * Render each section
         */
        while (viewY < this.viewHeight && entryIdx < visibleSections.size()) {
            InterfaceWirelessSection section = visibleSections.get(entryIdx);
            int sectionHeight = section.getHeight();

            /* Is it viewable/in the viewport at all? */
            if (viewY + sectionHeight < 0) {
                entryIdx++;
                viewY += sectionHeight;
                section.visible = false;
                continue;
            }

            section.visible = true;
            int advanceY = drawSection(section, viewY, relMouseX, relMouseY);
            viewY += advanceY;
            entryIdx++;
        }
    }

    /**
     * Render the section (if it is visible)
     *
     * @param section   the section to render
     * @param viewY     current y coordinate relative to gui
     * @param relMouseX transformed mouse coords relative to viewport
     * @param relMouseY transformed mouse coords relative to viewport
     * @return the height of the section rendered in viewport coordinates, max of viewHeight.
     */
    private int drawSection(InterfaceWirelessSection section, int viewY, int relMouseX, int relMouseY) {
        int title;
        int renderY = 0;
        final int sectionBottom = viewY + section.getHeight() - 1;
        final int fontColor = GuiColors.InterfaceTerminalInventory.getColor();
        /*
         * Render title
         */
        bindTexture(BACKGROUND);
        GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z + ITEM_STACK_Z + STEP_Z);
        if (sectionBottom > 0 && sectionBottom < InterfaceWirelessSection.TITLE_HEIGHT) {
            /* Transition draw */
            title = sectionBottom;
        } else if (viewY < 0) {
            /* Hidden title draw */
            title = 0;
        } else {
            /* Normal title draw */
            title = 0;
        }
        GL11.glTranslatef(0.0f, 0.0f, -(ITEM_STACK_OVERLAY_Z + ITEM_STACK_Z + STEP_Z));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        Iterator<InterfaceWirelessEntry> visible = section.getVisible();
        while (visible.hasNext()) {
            InterfaceWirelessEntry entry = visible.next();
            if (viewY + renderY + entry.rows * 18 + 1 > 0 && viewY + renderY < viewHeight) {
                renderY += drawEntry(
                    entry,
                    viewY + InterfaceWirelessSection.TITLE_HEIGHT + renderY,
                    title,
                    relMouseX,
                    relMouseY);
            } else {
                entry.dispY = -9999;
                entry.optionsButton.yPosition = -1;
                renderY += entry.rows * 18 + 1;
            }
        }
        bindTexture(BACKGROUND);
        GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z + ITEM_STACK_Z + STEP_Z);
        if (sectionBottom > 0 && sectionBottom < InterfaceWirelessSection.TITLE_HEIGHT) {
            /* Transition draw */
            drawTexturedModalRect(
                0,
                0,
                VIEW_LEFT,
                HEADER_HEIGHT + InterfaceWirelessSection.TITLE_HEIGHT - sectionBottom,
                VIEW_WIDTH,
                sectionBottom);
            fontRendererObj
                .drawString(section.name, 2, sectionBottom - InterfaceWirelessSection.TITLE_HEIGHT + 2, fontColor);
        } else if (viewY < 0) {
            /* Hidden title draw */
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glTranslatef(0.0f, 0.0f, 100f);
            drawTexturedModalRect(0, 0, VIEW_LEFT, HEADER_HEIGHT, VIEW_WIDTH, InterfaceWirelessSection.TITLE_HEIGHT);
            fontRendererObj.drawString(section.name, 2, 2, fontColor);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            /* Normal title draw */
            drawTexturedModalRect(
                0,
                viewY,
                VIEW_LEFT,
                HEADER_HEIGHT,
                VIEW_WIDTH,
                InterfaceWirelessSection.TITLE_HEIGHT);
            fontRendererObj.drawString(section.name, 2, viewY + 2, fontColor);
        }
        GL11.glTranslatef(0.0f, 0.0f, -(ITEM_STACK_OVERLAY_Z + ITEM_STACK_Z + STEP_Z));

        return InterfaceWirelessSection.TITLE_HEIGHT + renderY;
    }

    /**
     * Draws the entry. In practice it just draws the slots + items.
     *
     * @param viewY the gui coordinate z
     */
    private int drawEntry(InterfaceWirelessEntry entry, int viewY, int titleBottom, int relMouseX, int relMouseY) {
        bindTexture(BACKGROUND);
        Tessellator.instance.startDrawingQuads();
        int relY = 0;
        final int slotLeftMargin = (VIEW_WIDTH - entry.rowSize * 18);

        entry.dispY = viewY;
        /* PASS 1: BG */
        for (int row = 0; row < entry.rows; ++row) {
            final int rowYTop = row * 18;
            final int rowYBot = rowYTop + 18;

            relY += 18;
            /* Is the slot row in view? */
            if (viewY + rowYBot <= titleBottom) {
                continue;
            }
            for (int col = 0; col < entry.rowSize; ++col) {
                addTexturedRectToTesselator(
                    col * 18 + slotLeftMargin,
                    viewY + rowYTop,
                    18 * col + 18 + slotLeftMargin,
                    viewY + rowYBot,
                    0,
                    21 / 256f,
                    173 / 256f,
                    (21 + 18) / 256f,
                    (173 + 18) / 256f);
            }
        }
        Tessellator.instance.draw();
        /* Draw button */
        if (viewY + entry.optionsButton.height > 0 && viewY < viewHeight) {
            entry.optionsButton.yPosition = viewY;
            entry.renameButton.yPosition = viewY;
            entry.doubleButton.yPosition = viewY + 8;
            GuiFCImgButton toRender;
            if (isShiftKeyDown()) {
                toRender = entry.renameButton;
            } else {
                toRender = entry.optionsButton;
            }
            toRender.drawButton(mc, relMouseX, relMouseY);
            entry.doubleButton.drawButton(mc, relMouseX, relMouseY);
            List<String> tooltips = new ArrayList<>();
            if (toRender.getMouseIn()
                && relMouseY >= Math.max(InterfaceWirelessSection.TITLE_HEIGHT, entry.optionsButton.yPosition)) {
                tooltips.add(toRender.getMessage());
            } else if (entry.doubleButton.getMouseIn()
                && relMouseY >= Math.max(InterfaceWirelessSection.TITLE_HEIGHT, entry.optionsButton.yPosition)) {
                    tooltips.addAll(
                        Arrays.stream(
                            entry.doubleButton.getMessage()
                                .split("\\n"))
                            .collect(Collectors.toList()));
                }
            if (!tooltips.isEmpty()) {
                // draw a tooltip
                GL11.glTranslatef(0f, 0f, TOOLTIP_Z);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                drawHoveringText(tooltips, relMouseX, relMouseY);
                GL11.glTranslatef(0f, 0f, -TOOLTIP_Z);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);

            }
        } else {
            entry.optionsButton.yPosition = -1;
            entry.renameButton.yPosition = -1;
            entry.doubleButton.yPosition = -1;
        }
        /* PASS 2: Items */
        for (int row = 0; row < entry.rows; ++row) {
            final int rowYTop = row * 18;
            final int rowYBot = rowYTop + 18;
            /* Is the slot row in view? */
            if (viewY + rowYBot <= titleBottom) {
                continue;
            }
            AppEngInternalInventory inv = entry.getInventory();

            for (int col = 0; col < entry.rowSize; ++col) {
                final int colLeft = col * 18 + slotLeftMargin + 1;
                final int colRight = colLeft + 18 + 1;
                final int slotIdx = row * entry.rowSize + col;
                ItemStack stack = inv.getStackInSlot(slotIdx);

                boolean tooltip = relMouseX > colLeft - 1 && relMouseX < colRight - 1
                    && relMouseY >= Math.max(viewY + rowYTop, InterfaceWirelessSection.TITLE_HEIGHT)
                    && relMouseY < Math.min(viewY + rowYBot, viewHeight);
                if (stack != null) {
                    final ItemEncodedPattern iep = (ItemEncodedPattern) stack.getItem();
                    final ItemStack toRender = iep.getOutput(stack);

                    GL11.glPushMatrix();
                    GL11.glTranslatef(colLeft, viewY + rowYTop + 1, ITEM_STACK_Z);
                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    RenderHelper.enableGUIStandardItemLighting();
                    translatedRenderItem.zLevel = ITEM_STACK_Z - MAGIC_RENDER_ITEM_Z;
                    translatedRenderItem
                        .renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), toRender, 0, 0);
                    GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z);
                    aeRenderItem.setAeStack(AEItemStack.create(toRender));
                    aeRenderItem.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), toRender, 0, 0);
                    aeRenderItem.zLevel = 0.0f;
                    RenderHelper.disableStandardItemLighting();
                    if (!tooltip) {
                        if (entry.slotIsBroken(slotIdx)) {
                            GL11.glTranslatef(0.0f, 0.0f, SLOT_Z - ITEM_STACK_OVERLAY_Z);
                            drawRect(0, 0, 16, 16, GuiColors.ItemSlotOverlayInvalid.getColor());
                        } else if (entry.filteredRecipes[slotIdx]) {
                            GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z);
                            drawRect(0, 0, 16, 16, GuiColors.ItemSlotOverlayUnpowered.getColor());
                        }
                    } else {
                        tooltipStack = stack;
                    }
                    GL11.glPopMatrix();
                } else if (entry.filteredRecipes[slotIdx]) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(colLeft, viewY + rowYTop + 1, ITEM_STACK_OVERLAY_Z);
                    drawRect(0, 0, 16, 16, GuiColors.ItemSlotOverlayUnpowered.getColor());
                    GL11.glPopMatrix();
                }
                if (tooltip) {
                    // overlay highlight
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glTranslatef(0.0f, 0.0f, SLOT_HOVER_Z);
                    drawRect(colLeft, viewY + 1 + rowYTop, -2 + colRight, viewY - 1 + rowYBot, 0x77FFFFFF);
                    GL11.glTranslatef(0.0f, 0.0f, -SLOT_HOVER_Z);
                    masterList.hoveredEntry = entry;
                    entry.hoveredSlotIdx = slotIdx;
                }
                GL11.glDisable(GL11.GL_LIGHTING);
            }
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        return relY + 1;
    }

    @Override
    public List<String> handleItemTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip) {
        return currentToolTip;
    }

    @Override
    public ItemStack getHoveredStack() {
        return tooltipStack;
    }

    /**
     * A copy of super method, but modified to allow for depth testing.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void drawHoveringText(List textLines, int x, int y, FontRenderer font) {
        if (!textLines.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            int maxStrWidth = 0;

            // is this more efficient than doing 1 pass, then doing a translate before drawing the text?
            for (String s : (List<String>) textLines) {
                int width = font.getStringWidth(s);

                if (width > maxStrWidth) {
                    maxStrWidth = width;
                }
            }

            // top left corner
            int curX = x + 12;
            int curY = y - 12;
            int totalHeight = 8;

            if (textLines.size() > 1) {
                totalHeight += 2 + (textLines.size() - 1) * 10;
            }

            /* String is too long? Display on the left side */
            if (curX + maxStrWidth > this.width) {
                curX -= 28 + maxStrWidth;
            }

            /* String is too tall? move it up */
            if (curY + totalHeight + 6 > this.height) {
                curY = this.height - totalHeight - 6;
            }

            int borderColor = -267386864;
            // drawing the border...
            this.drawGradientRect(curX - 3, curY - 4, curX + maxStrWidth + 3, curY - 3, borderColor, borderColor);
            this.drawGradientRect(
                curX - 3,
                curY + totalHeight + 3,
                curX + maxStrWidth + 3,
                curY + totalHeight + 4,
                borderColor,
                borderColor);
            this.drawGradientRect(
                curX - 3,
                curY - 3,
                curX + maxStrWidth + 3,
                curY + totalHeight + 3,
                borderColor,
                borderColor);
            this.drawGradientRect(curX - 4, curY - 3, curX - 3, curY + totalHeight + 3, borderColor, borderColor);
            this.drawGradientRect(
                curX + maxStrWidth + 3,
                curY - 3,
                curX + maxStrWidth + 4,
                curY + totalHeight + 3,
                borderColor,
                borderColor);
            int color1 = 1347420415;
            int color2 = (color1 & 16711422) >> 1 | color1 & -16777216;
            this.drawGradientRect(curX - 3, curY - 3 + 1, curX - 3 + 1, curY + totalHeight + 3 - 1, color1, color2);
            this.drawGradientRect(
                curX + maxStrWidth + 2,
                curY - 3 + 1,
                curX + maxStrWidth + 3,
                curY + totalHeight + 3 - 1,
                color1,
                color2);
            this.drawGradientRect(curX - 3, curY - 3, curX + maxStrWidth + 3, curY - 3 + 1, color1, color1);
            this.drawGradientRect(
                curX - 3,
                curY + totalHeight + 2,
                curX + maxStrWidth + 3,
                curY + totalHeight + 3,
                color2,
                color2);

            for (int i = 0; i < textLines.size(); ++i) {
                String line = (String) textLines.get(i);
                font.drawStringWithShadow(line, curX, curY, -1);

                if (i == 0) {
                    // gap between name and lore text
                    curY += 2;
                }

                curY += 10;
            }

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (key == Keyboard.KEY_TAB) {
            this.searchFieldNames.setSuggestionToText();
        } else if (!checkHotbarKeys(key)) {
            if (character == ' ') {
                if ((searchFieldInputs.getText()
                    .isEmpty() && searchFieldInputs.isFocused())
                    || (searchFieldOutputs.getText()
                        .isEmpty() && searchFieldOutputs.isFocused())
                    || (searchFieldNames.getText()
                        .isEmpty() && searchFieldNames.isFocused()))
                    return;
            } else if (character == '\t' && handleTab()) {
                return;
            }
            if (searchFieldInputs.textboxKeyTyped(character, key) || searchFieldOutputs.textboxKeyTyped(character, key)
                || searchFieldNames.textboxKeyTyped(character, key)) {
                return;
            }
            super.keyTyped(character, key);
        }
    }

    @Override
    protected boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        boolean isMouseInViewport = isMouseInViewport(mouseX, mouseY);
        GuiScrollbar scrollbar = getScrollBar();
        if (isMouseInViewport && isCtrlKeyDown()) {
            if (wheel < 0) {
                scrollbar.setCurrentScroll(masterList.getHeight());
            } else {
                getScrollBar().setCurrentScroll(0);
            }
            return true;
        } else if (isMouseInViewport && isShiftKeyDown()) {
            // advance to the next section
            return masterList.scrollNextSection(wheel > 0);
        } else {
            return super.mouseWheelEvent(mouseX, mouseY, wheel);
        }
    }

    private boolean isMouseInViewport(int mouseX, int mouseY) {
        return mouseX > guiLeft + VIEW_LEFT && mouseX < guiLeft + VIEW_LEFT + VIEW_WIDTH
            && mouseY > guiTop + HEADER_HEIGHT
            && mouseY < guiTop + HEADER_HEIGHT + viewHeight;
    }

    private boolean handleTab() {
        if (searchFieldInputs.isFocused()) {
            searchFieldInputs.setFocused(false);
            if (isShiftKeyDown()) searchFieldNames.setFocused(true);
            else searchFieldOutputs.setFocused(true);
            return true;
        } else if (searchFieldOutputs.isFocused()) {
            searchFieldOutputs.setFocused(false);
            if (isShiftKeyDown()) searchFieldInputs.setFocused(true);
            else searchFieldNames.setFocused(true);
            return true;
        } else if (searchFieldNames.isFocused()) {
            searchFieldNames.setFocused(false);
            if (isShiftKeyDown()) searchFieldOutputs.setFocused(true);
            else searchFieldInputs.setFocused(true);
            return true;
        }
        return false;
    }

    public void postUpdate(List<PacketInterfaceTerminalUpdate.PacketEntry> updates, int statusFlags) {
        if ((statusFlags & PacketInterfaceTerminalUpdate.CLEAR_ALL_BIT)
            == PacketInterfaceTerminalUpdate.CLEAR_ALL_BIT) {
            /* Should clear all client entries. */
            this.masterList.list.clear();
        }
        /* Should indicate disconnected, so the terminal turns dark. */
        this.online = (statusFlags & PacketInterfaceTerminalUpdate.DISCONNECT_BIT)
            != PacketInterfaceTerminalUpdate.DISCONNECT_BIT;

        for (PacketInterfaceTerminalUpdate.PacketEntry cmd : updates) {
            parsePacketCmd(cmd);
        }
        this.masterList.markDirty();

    }

    private void parsePacketCmd(PacketInterfaceTerminalUpdate.PacketEntry cmd) {
        long id = cmd.entryId;
        if (cmd instanceof PacketInterfaceTerminalUpdate.PacketAdd addCmd) {
            InterfaceWirelessEntry entry = new InterfaceWirelessEntry(
                id,
                addCmd.name,
                addCmd.rows,
                addCmd.rowSize,
                addCmd.online).setLocation(addCmd.x, addCmd.y, addCmd.z, addCmd.dim, addCmd.side)
                    .setIcons(addCmd.selfRep, addCmd.dispRep)
                    .setItems(addCmd.items);
            masterList.addEntry(entry);
        } else if (cmd instanceof PacketInterfaceTerminalUpdate.PacketRemove) {
            masterList.removeEntry(id);
        } else if (cmd instanceof PacketInterfaceTerminalUpdate.PacketOverwrite owCmd) {
            InterfaceWirelessEntry entry = masterList.list.get(id);

            if (entry == null) {
                return;
            }

            if (owCmd.onlineValid) {
                entry.online = owCmd.online;
            }

            if (owCmd.itemsValid) {
                if (owCmd.allItemUpdate) {
                    entry.fullItemUpdate(owCmd.items, owCmd.validIndices.length);
                } else {
                    entry.partialItemUpdate(owCmd.items, owCmd.validIndices);
                }
            }
            masterList.isDirty = true;
        } else if (cmd instanceof PacketInterfaceTerminalUpdate.PacketRename renameCmd) {
            InterfaceWirelessEntry entry = masterList.list.get(id);

            if (entry != null) {
                if (StatCollector.canTranslate(renameCmd.newName)) {
                    entry.dispName = StatCollector.translateToLocal(renameCmd.newName);
                } else {
                    entry.dispName = StatCollector.translateToFallback(renameCmd.newName);
                }
            }
            masterList.isDirty = true;
        }
    }

    private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm, boolean in) {
        if (itemStack == null) {
            return false;
        }

        final NBTTagCompound encodedValue = itemStack.getTagCompound();

        if (encodedValue == null) {
            return false;
        }

        final NBTTagList tags = encodedValue.getTagList(in ? "in" : "out", Constants.NBT.TAG_COMPOUND);
        final boolean containsInvalidDisplayName = GuiText.UnknownItem.getLocal()
            .toLowerCase()
            .contains(searchTerm);

        for (int i = 0; i < tags.tagCount(); i++) {
            final NBTTagCompound tag = tags.getCompoundTagAt(i);
            final ItemStack parsedItemStack = ItemStack.loadItemStackFromNBT(tag);

            if (parsedItemStack != null) {
                final String displayName = Platform.getItemDisplayName(
                    AEApi.instance()
                        .storage()
                        .createItemStack(parsedItemStack))
                    .toLowerCase();
                if (NeCharUtil.INSTANCE.contains(searchTerm, displayName)) {
                    return true;
                }
            } else if (containsInvalidDisplayName && !tag.hasNoTags()) {
                return true;
            }
        }

        return false;

    }

    private boolean recipeIsBroken(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        final NBTTagCompound encodedValue = itemStack.getTagCompound();
        if (encodedValue == null) {
            return true;
        }

        final World w = CommonHelper.proxy.getWorld();
        if (w == null) {
            return false;
        }

        try {
            new PatternHelper(itemStack, w);
            return false;
        } catch (final Throwable t) {
            return true;
        }
    }

    private int getMaxViewHeight() {
        return AEConfig.instance.getConfigManager()
            .getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL
                ? AEConfig.instance.InterfaceTerminalSmallSize * 18
                : Integer.MAX_VALUE;
    }

    public boolean isOverTextField(final int mousex, final int mousey) {
        return searchFieldInputs.isMouseIn(mousex, mousey) || searchFieldOutputs.isMouseIn(mousex, mousey)
            || searchFieldNames.isMouseIn(mousex, mousey);
    }

    public void setTextFieldValue(final String displayName, final int mousex, final int mousey, final ItemStack stack) {
        if (searchFieldInputs.isMouseIn(mousex, mousey)) {
            searchFieldInputs.setText(displayName);
        } else if (searchFieldOutputs.isMouseIn(mousex, mousey)) {
            searchFieldOutputs.setText(displayName);
        } else if (searchFieldNames.isMouseIn(mousex, mousey)) {
            searchFieldNames.setText(displayName);
        }
    }

    /**
     * Tracks the list of entries.
     */
    private class InterfaceWirelessList {

        private final Map<Long, InterfaceWirelessEntry> list = new HashMap<>();
        private final Map<String, InterfaceWirelessSection> sections = new TreeMap<>();
        private final List<InterfaceWirelessSection> visibleSections = new ArrayList<>();
        private boolean isDirty;
        private int height;
        private InterfaceWirelessEntry hoveredEntry;

        InterfaceWirelessList() {
            this.isDirty = true;
        }

        /**
         * Performs a full update.
         */
        private void update() {
            height = 0;
            visibleSections.clear();
            String[] list = GuiBaseInterfaceWireless.this.searchFieldNames.getText()
                .split(" ");
            out: for (InterfaceWirelessSection section : sections.values()) {
                for (String query : list) {
                    if (!NeCharUtil.INSTANCE.contains(query.toLowerCase(), section.name.toLowerCase())) {
                        continue out;
                    }
                }

                section.isDirty = true;
                if (section.getVisible()
                    .hasNext()) {
                    height += section.getHeight();
                    visibleSections.add(section);
                }
            }
            isDirty = false;
        }

        public void markDirty() {
            this.isDirty = true;
            setInterfaceScrollBar();
        }

        public int getHeight() {
            if (isDirty) {
                update();
            }
            return height;
        }

        /**
         * Jump between sections.
         */
        private boolean scrollNextSection(boolean up) {
            GuiScrollbar scrollbar = getScrollBar();
            int viewY = scrollbar.getCurrentScroll();
            var sections = getVisibleSections();
            boolean result = false;

            if (up) {
                int y = masterList.getHeight();
                int i = sections.size() - 1;

                while (y > 0 && i >= 0) {
                    y -= sections.get(i)
                        .getHeight();
                    i -= 1;
                    if (y < viewY) {
                        result = true;
                        scrollbar.setCurrentScroll(y);
                        break;
                    }
                }
            } else {
                int y = 0;

                for (InterfaceWirelessSection section : sections) {
                    if (y > viewY) {
                        result = true;
                        scrollbar.setCurrentScroll(y);
                        break;
                    }
                    y += section.getHeight();
                }
            }
            return result;
        }

        public void addEntry(InterfaceWirelessEntry entry) {
            InterfaceWirelessSection section = sections.get(entry.dispName);

            if (section == null) {
                section = new InterfaceWirelessSection(entry.dispName);
                sections.put(entry.dispName, section);
            }
            section.addEntry(entry);
            list.put(entry.id, entry);
            isDirty = true;
        }

        public void removeEntry(long id) {
            InterfaceWirelessEntry entry = list.remove(id);

            if (entry != null) {
                entry.section.removeEntry(entry);
            }
        }

        public List<InterfaceWirelessSection> getVisibleSections() {
            if (isDirty) {
                update();
            }
            return visibleSections;
        }

        /**
         * Mouse button click.
         *
         * @param relMouseX viewport coords mouse X
         * @param relMouseY viewport coords mouse Y
         * @param btn       button code
         */
        public boolean mouseClicked(int relMouseX, int relMouseY, int btn) {
            if (relMouseX < 0 || relMouseX >= VIEW_WIDTH || relMouseY < 0 || relMouseY >= viewHeight) {
                return false;
            }
            for (InterfaceWirelessSection section : getVisibleSections()) {
                if (section.mouseClicked(relMouseX, relMouseY, btn)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A section holds all the interface entries with the same name.
     */
    private class InterfaceWirelessSection {

        public static final int TITLE_HEIGHT = 12;

        String name;
        List<InterfaceWirelessEntry> entries = new ArrayList<>();
        Set<InterfaceWirelessEntry> visibleEntries = new TreeSet<>(Comparator.comparing(e -> {
            if (e.dispRep != null) {
                return e.dispRep.getDisplayName() + e.id;
            } else {
                return String.valueOf(e.id);
            }
        }));
        int height;
        private boolean isDirty = true;
        boolean visible = false;

        InterfaceWirelessSection(String name) {
            this.name = name;
        }

        /**
         * Gets the height. Includes title.
         */
        public int getHeight() {
            if (isDirty) {
                update();
            }
            return height;
        }

        private void update() {
            refreshVisible();
            if (visibleEntries.isEmpty()) {
                height = 0;
            } else {
                height = TITLE_HEIGHT;
                for (InterfaceWirelessEntry entry : visibleEntries) {
                    height += entry.guiHeight;
                }
            }
            isDirty = false;
        }

        public void refreshVisible() {
            visibleEntries.clear();
            String input = GuiBaseInterfaceWireless.this.searchFieldInputs.getText()
                .toLowerCase();
            String output = GuiBaseInterfaceWireless.this.searchFieldOutputs.getText()
                .toLowerCase();

            for (InterfaceWirelessEntry entry : entries) {
                var moleAss = AEApi.instance()
                    .definitions()
                    .blocks()
                    .molecularAssembler()
                    .maybeStack(1);
                entry.dispY = -9999;
                if (onlyMolecularAssemblers
                    && (!moleAss.isPresent() || !Platform.isSameItem(moleAss.get(), entry.dispRep))) {
                    continue;
                }
                if (AEConfig.instance.showOnlyInterfacesWithFreeSlotsInInterfaceTerminal
                    && entry.numItems == entry.rows * entry.rowSize) {
                    continue;
                }
                if (onlyBrokenRecipes && !entry.hasBrokenSlot()) {
                    continue;
                }
                // Find search terms
                if (!input.isEmpty() || !output.isEmpty()) {
                    AppEngInternalInventory inv = entry.inv;
                    boolean shouldAdd = false;

                    for (int i = 0; i < inv.getSizeInventory(); ++i) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (itemStackMatchesSearchTerm(stack, input, true)
                            && itemStackMatchesSearchTerm(stack, output, false)) {
                            shouldAdd = true;
                            entry.filteredRecipes[i] = false;
                        } else {
                            entry.filteredRecipes[i] = true;
                        }
                    }
                    if (!shouldAdd) {
                        continue;
                    }
                } else {
                    Arrays.fill(entry.filteredRecipes, false);
                }
                visibleEntries.add(entry);
            }
        }

        public void addEntry(InterfaceWirelessEntry entry) {
            this.entries.add(entry);
            entry.section = this;
            this.isDirty = true;
        }

        public void removeEntry(InterfaceWirelessEntry entry) {
            this.entries.remove(entry);
            entry.section = null;
            this.isDirty = true;
        }

        public Iterator<InterfaceWirelessEntry> getVisible() {
            if (isDirty) {
                update();
            }
            return visibleEntries.iterator();
        }

        public boolean mouseClicked(int relMouseX, int relMouseY, int btn) {
            Iterator<InterfaceWirelessEntry> it = getVisible();
            boolean ret = false;

            while (it.hasNext() && !ret) {
                ret = it.next()
                    .mouseClicked(relMouseX, relMouseY, btn);
            }

            return ret;
        }
    }

    /**
     * This class keeps track of an entry and its widgets.
     */
    private class InterfaceWirelessEntry {

        String dispName;
        AppEngInternalInventory inv;
        GuiFCImgButton optionsButton;
        GuiFCImgButton renameButton;
        GuiImgButton doubleButton;

        /** Nullable - icon that represents the interface */
        ItemStack selfRep;
        /** Nullable - icon that represents the interface's "target" */
        ItemStack dispRep;
        InterfaceWirelessSection section;
        long id;
        int x, y, z, dim, side;
        int rows, rowSize;
        int guiHeight;
        int dispY = -9999;
        boolean online;
        private Boolean[] brokenRecipes;
        int numItems = 0;
        /** Should recipe be filtered out/grayed out? */
        boolean[] filteredRecipes;
        private int hoveredSlotIdx = -1;

        InterfaceWirelessEntry(long id, String name, int rows, int rowSize, boolean online) {
            this.id = id;
            if (StatCollector.canTranslate(name)) {
                this.dispName = StatCollector.translateToLocal(name);
            } else {
                String fallback = name + ".name"; // its whatever. save some bytes on network but looks ugly
                if (StatCollector.canTranslate(fallback)) {
                    this.dispName = StatCollector.translateToLocal(fallback);
                } else {
                    this.dispName = StatCollector.translateToFallback(name);
                }
            }
            this.inv = new AppEngInternalInventory(null, rows * rowSize, 1);
            this.rows = rows;
            this.rowSize = rowSize;
            this.online = online;
            this.optionsButton = new GuiFCImgButton(2, 0, "HIGHLIGHT", "YES");
            this.optionsButton.setHalfSize(true);
            this.renameButton = new GuiFCImgButton(2, 0, "EDIT", "YES");
            this.renameButton.setHalfSize(true);
            if (ModAndClassUtil.isDoubleButton) {
                this.doubleButton = new GuiImgButton(2, 0, Settings.ACTIONS, ActionItems.DOUBLE);
                this.doubleButton.setHalfSize(true);
            }

            this.guiHeight = 18 * rows + 1;
            this.brokenRecipes = new Boolean[rows * rowSize];
            this.filteredRecipes = new boolean[rows * rowSize];
        }

        InterfaceWirelessEntry setLocation(int x, int y, int z, int dim, int side) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            this.side = side;

            return this;
        }

        InterfaceWirelessEntry setIcons(ItemStack selfRep, ItemStack dispRep) {
            // Kotlin would make this pretty easy :(
            this.selfRep = selfRep;
            this.dispRep = dispRep;

            return this;
        }

        public void fullItemUpdate(NBTTagList items, int newSize) {
            inv = new AppEngInternalInventory(null, newSize);
            rows = newSize / rowSize;
            brokenRecipes = new Boolean[newSize];
            numItems = 0;

            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                setItemInSlot(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i)), i);
            }
            this.guiHeight = 18 * rows + 4;
        }

        InterfaceWirelessEntry setItems(NBTTagList items) {
            assert items.tagCount() == inv.getSizeInventory();

            for (int i = 0; i < items.tagCount(); ++i) {
                setItemInSlot(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i)), i);
            }
            return this;
        }

        public void partialItemUpdate(NBTTagList items, int[] validIndices) {
            for (int i = 0; i < validIndices.length; ++i) {
                setItemInSlot(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i)), validIndices[i]);
            }
        }

        private void setItemInSlot(ItemStack stack, int idx) {
            try {
                final int oldHasItem = inv.getStackInSlot(idx) != null ? 1 : 0;
                final int newHasItem = stack != null ? 1 : 0;

                inv.setInventorySlotContents(idx, stack);
                // Update item count
                numItems += newHasItem - oldHasItem;
                assert numItems >= 0;
            } catch (Exception e) {
                AELog.error(e);
            }
        }

        public boolean hasBrokenSlot() {
            boolean existsUnknown = false;

            for (int idx = 0; idx < brokenRecipes.length; idx++) {
                if (brokenRecipes[idx] == null) {
                    existsUnknown = true;
                } else if (brokenRecipes[idx]) {
                    return true;
                }
            }

            if (existsUnknown) {
                for (int idx = 0; idx < brokenRecipes.length; idx++) {
                    if (slotIsBroken(idx)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean slotIsBroken(int idx) {

            if (brokenRecipes[idx] == null) {
                brokenRecipes[idx] = recipeIsBroken(inv.getStackInSlot(idx));
            }

            return brokenRecipes[idx];
        }

        public AppEngInternalInventory getInventory() {
            return inv;
        }

        private NBTTagCompound getDimensionalCoordSide() {
            Util.DimensionalCoordSide blockPos = new Util.DimensionalCoordSide(
                x,
                y,
                z,
                dim,
                ForgeDirection.getOrientation(side),
                this.dispName);
            NBTTagCompound data = new NBTTagCompound();
            blockPos.writeToNBT(data);
            return data;
        }

        public boolean mouseClicked(int mouseX, int mouseY, int btn) {
            if (!section.visible || btn < 0 || btn > 2) {
                return false;
            }
            if (mouseX >= optionsButton.xPosition && mouseX < 2 + optionsButton.width
                && mouseY > Math.max(optionsButton.yPosition, InterfaceWirelessSection.TITLE_HEIGHT)
                && mouseY <= Math.min(optionsButton.yPosition + optionsButton.height, viewHeight)) {
                optionsButton.func_146113_a(mc.getSoundHandler());
                DimensionalCoord blockPos = new DimensionalCoord(x, y, z, dim);

                if (isShiftKeyDown()) {
                    AE2Thing.proxy.netHandler.sendToServer(
                        new CPacketRenamer(
                            blockPos.x,
                            blockPos.y,
                            blockPos.z,
                            blockPos.getDimension(),
                            ForgeDirection.getOrientation(side)));
                } else {
                    /* View in world */
                    BlockPosHighlighter.highlightBlocks(
                        mc.thePlayer,
                        Collections.singletonList(blockPos),
                        PlayerMessages.InterfaceHighlighted.getName(),
                        PlayerMessages.InterfaceInOtherDim.getName());
                    mc.thePlayer.closeScreen();
                }
                return true;
            } else if (mouseX >= doubleButton.xPosition && mouseX < 2 + doubleButton.width
                && mouseY > Math.max(doubleButton.yPosition, InterfaceWirelessSection.TITLE_HEIGHT)
                && mouseY <= Math.min(doubleButton.yPosition + doubleButton.height, viewHeight)) {
                    doubleButton.func_146113_a(mc.getSoundHandler());
                    final boolean backwards = Mouse.isButtonDown(1);
                    int val = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1 : 0;
                    if (backwards) val |= 0b10;
                    AE2Thing.proxy.netHandler.sendToServer(
                        new CPacketTerminalBtns(
                            "InterfaceTerminal.Double",
                            String.valueOf(val),
                            getDimensionalCoordSide()));
                }

            int offsetY = mouseY - dispY - 1;
            int offsetX = mouseX - (VIEW_WIDTH - rowSize * 18) - 1;
            if (offsetX >= 0 && offsetX < (rowSize * 18)
                && mouseY > Math.max(dispY, InterfaceWirelessSection.TITLE_HEIGHT)
                && offsetY < Math.min(viewHeight - dispY, guiHeight - 1)) {
                final int col = offsetX / 18;
                final int row = offsetY / 18;
                final int slotIdx = row * rowSize + col;

                // send packet to server, request an update
                // TODO: Client prediction.
                PacketInventoryAction packet = null;
                ItemStack currentItem = mc.thePlayer.inventory.getItemStack();
                if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                    packet = new PacketInventoryAction(InventoryAction.MOVE_REGION, 0, id);
                } else if (isShiftKeyDown() && (btn == 0 || btn == 1)
                    && !(currentItem != null && currentItem.getItem() instanceof ItemPatternModifier)) {
                        packet = new PacketInventoryAction(InventoryAction.SHIFT_CLICK, slotIdx, id);
                    } else if (btn == 0 || btn == 1) {
                        if (currentItem != null && currentItem.getItem() instanceof ItemPatternModifier) {
                            int val = slotIdx << 2;
                            final boolean backwards = Mouse.isButtonDown(1);
                            val |= Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1 : 0;
                            if (backwards) val |= 0b10;
                            AE2Thing.proxy.netHandler.sendToServer(
                                new CPacketTerminalBtns(
                                    "InterfaceTerminal.PatternModifier",
                                    String.valueOf(val),
                                    getDimensionalCoordSide()));
                        } else if ((ModAndClassUtil.GT5 || ModAndClassUtil.GT5NH) && GTUtil.isDataStick()) {
                            AE2Thing.proxy.netHandler.sendToServer(
                                new CPacketTerminalBtns("InterfaceTerminal.SetStick", "1", getDimensionalCoordSide()));
                        } else {
                            packet = new PacketInventoryAction(InventoryAction.PICKUP_OR_SET_DOWN, slotIdx, id);
                        }

                    } else {
                        packet = new PacketInventoryAction(InventoryAction.CREATIVE_DUPLICATE, slotIdx, id);
                    }
                if (packet != null) {
                    NetworkHandler.instance.sendToServer(packet);
                }
                return true;
            }

            return false;
        }
    }

}
