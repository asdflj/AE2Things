package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueName;
import com.asdflj.ae2thing.common.parts.PartInfusionPatternTerminal;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessDualInterfaceTerminalInventory;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.network.CPacketPatternNameSet;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.container.AEBaseContainer;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;

public class GuiPatternValueName extends AEBaseGui implements IDropToFillTextField, IGuiDrawSlot {

    protected MEGuiTextField textField;
    protected GuiTabButton originalGuiBtn;
    protected GuiButton submit;
    protected GuiType originalGui;
    protected ItemStack myIcon;

    public GuiPatternValueName(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(new ContainerPatternValueName(inventoryPlayer, te));

        this.xSize = 216;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();

        this.buttonList
            .add(this.submit = new GuiButton(0, this.guiLeft + 168, this.guiTop + 27, 38, 20, GuiText.Set.getLocal()));

        setOriginGUI(((AEBaseContainer) this.inventorySlots).getTarget());
        if (this.originalGui != null && this.myIcon != null) {
            this.buttonList.add(
                this.originalGuiBtn = new GuiTabButton(
                    this.guiLeft + 191,
                    this.guiTop - 4,
                    this.myIcon,
                    this.myIcon.getDisplayName(),
                    itemRender));
            this.originalGuiBtn.setHideEdge(13);
        }

        textField = new MEGuiTextField(122, 12);
        textField.setMaxStringLength(32);
        textField.x = this.guiLeft + 39;
        textField.y = this.guiTop + 31;
        textField.setFocused(true);
    }

    protected void setOriginGUI(Object target) {
        if (target instanceof PartInfusionPatternTerminal) {
            this.myIcon = ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL.stack();
            this.originalGui = GuiType.INFUSION_PATTERN_TERMINAL;
        } else if (target instanceof WirelessDualInterfaceTerminalInventory) {
            this.myIcon = ItemAndBlockHolder.ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack();
            this.originalGui = GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL;
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj
            .drawString(I18n.format(NameConst.GUI_Pattern_Rename), 12, 8, GuiColors.RenamerTitle.getColor());
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/gui/craftName.png");
        this.mc.getTextureManager()
            .bindTexture(loc);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        textField.drawTextBox();
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        textField.mouseClicked(xCoord, yCoord, btn);
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!textField.textboxKeyTyped(character, key)) {
            super.keyTyped(character, key);
        }
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button == originalGuiBtn) {
            switchGui();
        } else {
            super.actionPerformed(button);
        }

        try {
            if (button == this.submit && button.enabled) {
                CPacketPatternNameSet message = new CPacketPatternNameSet(
                    originalGui,
                    getName(),
                    ((ContainerPatternValueName) this.inventorySlots).getValueIndex());
                AE2Thing.proxy.netHandler.sendToServer(message);
            }
        } catch (final NumberFormatException e) {
            textField.setText("");
        }
    }

    public void switchGui() {
        if (originalGui != null) {
            InventoryHandler.switchGui(originalGui);
        }
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot(s)) super.func_146977_a(s);
    }

    @Override
    public float getzLevel() {
        return this.zLevel;
    }

    @Override
    public AEBaseGui getAEBaseGui() {
        return this;
    }

    @Override
    public boolean isOverTextField(int mousex, int mousey) {
        return textField.isMouseIn(mousex, mousey);
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        textField.setText(displayName);
    }

    public String getName() {
        return textField.getText();
    }

    public void setName(String name) {
        textField.setText(name);
    }
}
