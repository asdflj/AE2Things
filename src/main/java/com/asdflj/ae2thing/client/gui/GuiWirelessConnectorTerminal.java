package com.asdflj.ae2thing.client.gui;

import java.util.ArrayList;
import java.util.List;

import appeng.client.gui.widgets.GuiScrollbar;
import appeng.core.localization.ButtonToolTips;
import com.asdflj.ae2thing.client.gui.widget.THGuiWirelessConnector;
import com.asdflj.ae2thing.client.me.IDisplayRepo;
import com.asdflj.ae2thing.client.me.WirelessConnectorRepo;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessConnectorTerminal;
import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;
import com.asdflj.ae2thing.util.Info;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;

public class GuiWirelessConnectorTerminal extends AEBaseGui {

//    public static final int
    private final int reservedSpace;
    protected static String memoryText = "";
    private int maxRows;
    private int perRow;
    protected int standardSize;
    private int rows;
    protected THGuiTextField searchField;
    private int offsetY;
    protected final IDisplayRepo repo;
    private final List<THGuiWirelessConnector> components = new ArrayList<>();

    public GuiWirelessConnectorTerminal(InventoryPlayer inventory, ITerminalHost inv) {
        super(new ContainerWirelessConnectorTerminal(inventory, inv));
        this.xSize = 195;
        this.ySize = 204;
        this.standardSize = this.xSize;
        this.reservedSpace = 92;
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.repo = new WirelessConnectorRepo(scrollbar);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj.drawString(this.getGuiDisplayName(GuiText.Terminal.getLocal()), 8, 6, 4210752);
        for (THGuiWirelessConnector com:this.components){
            com.draw();
        }

    }

    public void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/" + file);
        this.mc.getTextureManager()
            .bindTexture(loc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.maxRows = this.getMaxRows();
        this.perRow = AEConfig.instance.getConfigManager()
            .getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL ? 9
                : 9 + ((this.width - this.standardSize) / 42);

        final boolean hasNEI = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI);

        final int NEI = 0;
        int top = hasNEI ? 22 : 0;

        int magicNumber = 0;
        final int extraSpace = this.height - magicNumber - NEI - top - this.reservedSpace;

        this.rows = (int) Math.floor(extraSpace / 42.0);
        if (this.rows > this.maxRows) {
            this.rows = this.maxRows;
        }

        if (hasNEI) {
            this.rows--;
        }

        if (this.rows < 3) {
            this.rows = 3;
        }
        super.initGui();
        this.ySize = magicNumber + this.rows * 42 + this.reservedSpace;
        final int unusedSpace = this.height - this.ySize;
        this.guiTop = unusedSpace < 0 ? 0 : (int) Math.floor(unusedSpace / 4.0f);
        this.offsetY = this.guiTop + 8;

        this.searchField = new THGuiTextField(
            this.fontRendererObj,
            this.guiLeft + 69,
            this.guiTop + 3,
            100,
            12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setVisible(true);
        this.searchField.setMessage(ButtonToolTips.SearchStringTooltip.getLocal());
        this.components.clear();
        for (int i = 0; i < this.rows; i++) {
            this.components.add(new THGuiWirelessConnector(this.repo,i,this.fontRendererObj,28,21));
        }
        setSearchString(memoryText,false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        if (AEConfig.instance.preserveSearchBar && searchField != null)
            handleTooltip(mouseX, mouseY, searchField.getTooltipProvider());
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) {
        this.searchField.mouseClicked(xCoord, yCoord, btn);
        if (btn == 1 && this.searchField.isMouseIn(xCoord, yCoord)) {
            setSearchString("",true);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    public void setSearchString(String memoryText,boolean updateView) {
        this.searchField.setText(memoryText);
        this.setScrollBar();
        if(updateView){
            this.repo.updateView();
        }

    }

    private int getMaxRows() {
        return AEConfig.instance.getConfigManager()
            .getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        memoryText = this.searchField.getText();
    }

    @Override
    protected void keyTyped(final char character, final int key) {
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

    public void setScrollBar() {
        this.getScrollBar()
            .setTop(18)
            .setLeft(175)
            .setHeight(this.rows * 42 - 2);
        this.getScrollBar()
            .setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(this.getBackground());
        final int x_width = 195;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);
        for (int x = 0; x < this.rows; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 42, 0, 18, x_width, 42);
        }
        this.drawTexturedModalRect(offsetX, offsetY + 16 + this.rows * 42, 0, 145, x_width, 8);
        for(THGuiWirelessConnector com:this.components){
            if(com.isMouseIn(mouseX,mouseY)){
                com.drawNameMask();
            }
        }
        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    protected String getBackground() {
        return "gui/wireless_connector.png";
    }

    public void postUpdate(List<Info> list) {
        this.repo.clear();
        for (Info info:list){
            this.repo.postUpdate(info);
        }
        this.repo.updateView();
    }
}
