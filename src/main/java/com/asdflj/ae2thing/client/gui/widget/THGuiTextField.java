package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.localization.GuiColors;
import codechicken.nei.FormattedTextField;
import codechicken.nei.SearchField;
import codechicken.nei.SearchTextFormatter;

public class THGuiTextField extends FormattedTextField {

    private static final int PADDING = 2;
    private final FontRenderer _fontRender;
    private final int fontPad;
    private String tooltip;

    public int x;
    public int y;
    private final int _width;
    private final int _height;
    private int _border;
    private int _color;
    private String suggestion;
    private boolean bg = false;
    public boolean forceDrawSuggestion = false;
    private final TooltipProvider tooltipProvider = new TooltipProvider();
    private String rawSuggestion;
    private static final SearchTextFormatter formatter = new SearchTextFormatter(SearchField.searchParser);

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

        this.x = xPos;
        this.y = yPos;
        this._width = width;
        this._height = height;
        this._fontRender = fontRenderer;
        this.tooltip = "";
        this.suggestion = "";
        this.rawSuggestion = "";
        this.fontPad = fontRenderer.getCharWidth('_');
        this.setBackgroundDrawing();
        this.setEnableBackgroundDrawing(false);
        this.setVisible(true);
        this.setMaxStringLength(100);
        this.setFormatter(formatter);
    }

    public THGuiTextField(final int width, final int height, String tooltip) {
        this(Minecraft.getMinecraft().fontRenderer, 0, 0, width, height);
        this.setTextColor(GuiColors.SearchboxText.getColor());
        this.setCursorPositionZero();
        this.setMessage(tooltip);
    }

    public void setSuggestion(final String suggestion) {
        String text = "";
        if (suggestion.startsWith(this.getText())) {
            int pos = suggestion.indexOf(this.getText());
            text = suggestion.substring(
                pos + this.getText()
                    .length());
        }
        this.suggestion = text;
        this.rawSuggestion = suggestion;
    }

    public void updateSuggestion() {
        setSuggestion(this.rawSuggestion);
    }

    public String getSuggestion() {
        return this.suggestion;
    }

    public String getRawSuggestion() {
        return this.rawSuggestion;
    }

    public void setSuggestionToText() {
        this.setText(this.rawSuggestion);
        this.setSuggestion(this.rawSuggestion);
    }

    @Override
    public void drawTextBox() {
        setDimensionsAndColor();
        if (this.getVisible()) {
            if (this.getBorder() > 0) {
                drawRect(
                    this.x - getBorder(),
                    this.y - getBorder(),
                    this.xPosition + this._width + getBorder(),
                    this.yPosition + this._height + getBorder(),
                    this.getColor());
            }
            if (this.bg) {
                GuiTextField.drawRect(
                    this.x + 1,
                    this.y + 1,
                    this.x + this._width - 1,
                    this.y + this._height - 1,
                    isFocused() ? GuiColors.SearchboxFocused.getColor() : GuiColors.SearchboxUnfocused.getColor());
            }
        }
        drawSuggestion();
        try {
            super.drawTextBox();
        } catch (Exception e) {
            // fix crash
            String text = this.getText()
                .replaceAll("[^a-zA-Z0-9\\s]", "");
            this.setText(text);
        }
    }

    public void setBackgroundDrawing() {
        bg = true;

    }

    protected void setDimensionsAndColor() {
        this.xPosition = this.x + PADDING;
        this.yPosition = this.y + PADDING;
        this.width = this._width - PADDING * 2 - this.fontPad;
        this.height = this._height - PADDING * 2;
    }

    public void onTextChange(final String oldText) {}

    private void drawSuggestion() {
        if (this.getVisible() && !this.suggestion.isEmpty()) {
            String drawString = this.suggestion;
            if (this._fontRender.getStringWidth(rawSuggestion) > this._width) {
                int w = this._fontRender.getStringWidth(this.getText());
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < this.suggestion.toCharArray().length; i++) {
                    char s = this.suggestion.charAt(i);
                    int charWidth = this._fontRender.getCharWidth(s);
                    if (w + charWidth * 3 < this._width) {
                        w += charWidth;
                        builder.append(s);
                    } else {
                        break;
                    }
                }
                drawString = builder.toString();
            }
            drawString(
                this._fontRender,
                drawString,
                this.x + 2 + this._fontRender.getStringWidth(this.getText()),
                this.y + 2,
                0xC0C0C0);
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        final String oldText = getText();
        int currentCursorPos = this.getCursorPosition();
        this.setCursorPosition(currentCursorPos);
        onTextChange(oldText);
    }

    @Override
    public boolean textboxKeyTyped(final char keyChar, final int keyID) {
        if (!isFocused()) {
            return false;
        }
        final String oldText = getText();
        boolean handled = super.textboxKeyTyped(keyChar, keyID);

        if (!handled
            && (keyID == Keyboard.KEY_RETURN || keyID == Keyboard.KEY_NUMPADENTER || keyID == Keyboard.KEY_ESCAPE)) {
            setFocused(false);
        }

        if (handled) {
            onTextChange(oldText);
        }

        return handled;
    }

    public void handleTooltip(int mouseX, int mouseY, AEBaseGui gui) {
        this.handleTooltip(mouseX, mouseY, gui, 0);
    }

    public void handleTooltip(int mouseX, int mouseY, AEBaseGui gui, int offsetY) {
        if (isMouseIn(mouseX, mouseY) && this.tooltip != null && !this.tooltip.isEmpty()) {
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
        if (button == 1 && requiresFocus) {
            this.setText("");
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        bg = focused;
    }

    /**
     * Checks if the mouse is within the element
     *
     * @param xCoord current x coord of the mouse
     * @param yCoord current y coord of the mouse
     * @return true if mouse position is within the text field area
     */
    public boolean isMouseIn(final int xCoord, final int yCoord) {
        final boolean withinXRange = this.x <= xCoord && xCoord < this.x + this._width;
        final boolean withinYRange = this.y <= yCoord && yCoord < this.y + this._height;

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
            return x;
        }

        @Override
        public int yPos() {
            return y;
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
