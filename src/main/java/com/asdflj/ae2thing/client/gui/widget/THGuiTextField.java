package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.ITooltip;

public class THGuiTextField extends GuiTextField {

    private static final int PADDING = 2;
    private final FontRenderer _fontRender;
    private String tooltip;

    private final int _xPos;
    private final int _yPos;
    private final int _width;
    private final int _height;
    private int _border;
    private int _color;
    private final TooltipProvider tooltipProvider = new TooltipProvider();

    /**
     * Uses the values to instantiate a padded version of a text field. Pays attention to the '_' caret.
     *
     * @param fontRenderer renderer for the strings
     * @param xPos         absolute left position
     * @param yPos         absolute top position
     * @param width        absolute width
     * @param height       absolute height
     */
    public THGuiTextField(final FontRenderer fontRenderer, final int xPos, final int yPos, final int width,
        final int height) {
        super(
            fontRenderer,
            xPos + PADDING,
            yPos + PADDING,
            width - 2 * PADDING - fontRenderer.getCharWidth('_'),
            height - 2 * PADDING);

        this._xPos = xPos;
        this._yPos = yPos;
        this._width = width;
        this._height = height;
        this._fontRender = fontRenderer;
        this.tooltip = "";
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
        if (this.getBorder() > 0 && this.getVisible()) {
            drawRect(
                this._xPos - getBorder(),
                this._yPos - getBorder(),
                this.xPosition + this.width + getBorder(),
                this.yPosition + this.height + getBorder(),
                this.getColor());
        }
    }

    public void handleTooltip(int mouseX, int mouseY, AEBaseGui gui) {
        this.handleTooltip(mouseX, mouseY, gui, 0);
    }

    public void handleTooltip(int mouseX, int mouseY, AEBaseGui gui, int offsetY) {
        if (isMouseIn(mouseX, mouseY) && this.tooltip != null && !"".equals(this.tooltip)) {
            int length = 0;
            for (String s : this.getMessage()
                .split("\n")) {
                length = Math.max(this._fontRender.getStringWidth(s), length);
            }
            if (mouseY < 15) {
                mouseY = 15;
            }
            if (mouseX + length + 20 >= gui.width) {
                gui.drawTooltip(mouseX - length - 20, mouseY + 4 + offsetY, 0, this.getMessage());
            } else {
                gui.drawTooltip(mouseX, mouseY + 4 + offsetY, 0, this.getMessage());
            }
        }
    }

    public int getBorder() {
        return this._border;
    }

    public void setBorder(int border) {
        this._border = border;
        this._color = 0;
    }

    public void setBorder(int border, int color) {
        this._border = border;
        this._color = color;
    }

    public void setBorder() {
        this._border = 0;
        this._color = 0;
    }

    public int getColor() {
        return this._color;
    }

    public void setColor(int color) {
        this._color = color;
    }

    @Override
    public void mouseClicked(final int xPos, final int yPos, final int button) {
        super.mouseClicked(xPos, yPos, button);

        final boolean requiresFocus = this.isMouseIn(xPos, yPos);

        this.setFocused(requiresFocus);
    }

    /**
     * Checks if the mouse is within the element
     *
     * @param xCoord current x coord of the mouse
     * @param yCoord current y coord of the mouse
     * @return true if mouse position is within the text field area
     */
    public boolean isMouseIn(final int xCoord, final int yCoord) {
        final boolean withinXRange = this._xPos <= xCoord && xCoord < this._xPos + this._width;
        final boolean withinYRange = this._yPos <= yCoord && yCoord < this._yPos + this._height;

        return withinXRange && withinYRange;
    }

    public void setMessage(String t) {
        tooltip = t;
    }

    public String getMessage() {
        return tooltip;
    }

    public TooltipProvider getTooltipProvider() {
        return this.tooltipProvider;
    }

    public class TooltipProvider implements ITooltip {

        @Override
        public String getMessage() {
            return tooltip;
        }

        @Override
        public int xPos() {
            return _xPos;
        }

        @Override
        public int yPos() {
            return _yPos;
        }

        @Override
        public int getHeight() {
            return _height;
        }

        @Override
        public int getWidth() {
            return _width;
        }

        @Override
        public boolean isVisible() {
            return getVisible();
        }
    }
}
