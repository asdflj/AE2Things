package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerFluidPacketEncoder;
import com.asdflj.ae2thing.common.tile.TileFluidPacketEncoder;
import com.asdflj.ae2thing.network.CPacketValueConfig;
import com.asdflj.ae2thing.util.NameConst;

import appeng.client.gui.AEBaseGui;
import appeng.core.AEConfig;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;

public class GuiFluidPacketEncoder extends AEBaseGui {

    private GuiTextField level;
    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;
    private GuiButton submit;
    private boolean isMul = false;
    protected final ContainerFluidPacketEncoder cvb;
    protected final TileFluidPacketEncoder te;

    public GuiFluidPacketEncoder(InventoryPlayer ip, TileFluidPacketEncoder tile) {
        super(new ContainerFluidPacketEncoder(ip, tile));
        cvb = (ContainerFluidPacketEncoder) this.inventorySlots;
        te = tile;
        this.ySize = 184;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.addButtons();
        this.level = new GuiTextField(this.fontRendererObj, 24, 43, 79, this.fontRendererObj.FONT_HEIGHT);
        this.level.setEnableBackgroundDrawing(false);
        this.level.setMaxStringLength(16);
        this.level.setTextColor(GuiColors.LevelEmitterValue.getColor());
        this.level.setVisible(true);
        this.level.setFocused(true);
        ((ContainerFluidPacketEncoder) this.inventorySlots).setTextField(this.level);
    }

    public static int y1 = 38;
    public static int x1 = 144;

    protected void addButtons() {
        final int a = AEConfig.instance.levelByStackAmounts(0);
        final int b = AEConfig.instance.levelByStackAmounts(1);
        final int c = AEConfig.instance.levelByStackAmounts(2);
        final int d = AEConfig.instance.levelByStackAmounts(3);

        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d));
        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d));

        this.buttonList
            .add(this.submit = new GuiButton(0, this.guiLeft + x1, this.guiTop + y1, 26, 20, GuiText.Set.getLocal()));

    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
            getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_PACKET_ENCODER)),
            8,
            6,
            GuiColors.UpgradableTitle.getColor());
        this.fontRendererObj
            .drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, GuiColors.UpgradableInventory.getColor());
        this.level.drawTextBox();
        if (isShiftKeyDown() && !isMul) {
            for (Object btn : this.buttonList) {
                if (btn instanceof GuiButton && btn != submit) {
                    ((GuiButton) btn).displayString += "000";
                }
            }
            isMul = true;
        } else if (!isShiftKeyDown() && isMul) {
            for (Object btn : this.buttonList) {
                if (btn instanceof GuiButton && btn != submit) {
                    ((GuiButton) btn).displayString = ((GuiButton) btn).displayString
                        .substring(0, ((GuiButton) btn).displayString.lastIndexOf("000"));
                }
            }
            isMul = false;
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture(this.getBackground());
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, 211 - 34, this.ySize);
        try {
            this.submit.enabled = getAmount() > 0;
        } catch (final NumberFormatException e) {
            this.submit.enabled = false;
        }
    }

    protected String getBackground() {
        return "guis/lvlemitter.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
        final boolean isMinus = btn == this.minus1 || btn == this.minus10
            || btn == this.minus100
            || btn == this.minus1000;

        if (isPlus || isMinus) {
            this.addQty(this.getQty(btn));
        }
        if (btn == this.submit) {
            this.addQty(0);
        }
    }

    protected int getAmount() {
        try {
            String out = this.level.getText();
            double result = Calculator.conversion(out);
            if (result <= 0 || Double.isNaN(result)) {
                return 0;
            } else {
                return (int) ArithHelper.round(result, 0);
            }
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private void addQty(final long i) {
        try {
            long resultI = getAmount();
            if (resultI == 1 && i > 1) {
                resultI = 0;
            }
            resultI += i;
            if (resultI < 1) {
                resultI = 1;
            }
            String out = Long.toString(resultI);
            this.level.setText(out);
            AE2Thing.proxy.netHandler.sendToServer(new CPacketValueConfig(resultI, 0));
        } catch (final NumberFormatException ignore) {}
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            if (key == Keyboard.KEY_RETURN) {
                addQty(0);
            } else {
                this.level.textboxKeyTyped(character, key);
                super.keyTyped(character, key);
            }

        }
    }
}
