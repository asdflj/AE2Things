package com.asdflj.ae2thing.client.gui.widget;

import static com.asdflj.ae2thing.api.Constants.MODE_CRAFTING;
import static com.asdflj.ae2thing.api.Constants.MODE_PROCESSING;
import static net.minecraft.client.gui.GuiScreen.isCtrlKeyDown;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.IWidgetGui;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.network.CPacketInventoryActionExtend;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.InventoryActionExtend;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.client.gui.GuiFCImgButton;

import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.PatternBeSubstitution;
import appeng.api.config.PatternSlotConfig;
import appeng.api.config.Settings;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternTerm;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;

public class PatternPanel implements IAEBasePanel {

    private final AEBaseGui parent;
    private final IWidgetGui gui;
    private final ContainerWirelessDualInterfaceTerminal container;
    protected GuiImgButton encodeBtn;
    protected GuiImgButton substitutionsEnabledBtn;
    protected GuiImgButton substitutionsDisabledBtn;
    protected GuiFCImgButton fluidPrioritizedEnabledBtn;
    protected GuiFCImgButton fluidPrioritizedDisabledBtn;
    protected GuiImgButton invertBtn;
    protected GuiImgButton clearBtn;
    protected GuiImgButton doubleBtn;
    protected GuiImgButton beSubstitutionsEnabledBtn;
    protected GuiImgButton beSubstitutionsDisabledBtn;
    protected GuiFCImgButton combineEnableBtn;
    protected GuiFCImgButton combineDisableBtn;
    protected GuiTabButton tabProcessButton;
    protected GuiTabButton tabCraftButton;
    protected final GuiScrollbar processingScrollBar = new GuiScrollbar();
    private final AEBaseContainer inventorySlots;
    protected final Set<Slot> drag_click;
    private final int w;
    private final int h;

    public PatternPanel(IWidgetGui gui, ContainerWirelessDualInterfaceTerminal container) {
        this.gui = gui;
        this.container = container;
        this.parent = gui.getGui();
        this.inventorySlots = this.container;
        this.drag_click = Ae2ReflectClient.getDragClick(this.parent);
        processingScrollBar.setHeight(70)
            .setWidth(7)
            .setLeft(6)
            .setRange(0, 1, 1);
        processingScrollBar.setTexture(AE2Thing.MODID, getBackground(), 242, 0);
        this.w = 110;
        this.h = 92;
    }

    @Override
    public String getBackground() {
        if (this.container.isCraftingMode()) {
            return "gui/widget/pattern3.png";
        } else {
            return "gui/widget/pattern.png";
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        updateButton(this.substitutionsEnabledBtn, this.container.substitute);
        updateButton(this.substitutionsDisabledBtn, !this.container.substitute);
        updateButton(this.combineEnableBtn, this.container.combine);
        updateButton(this.combineDisableBtn, !this.container.combine);
        updateButton(this.beSubstitutionsEnabledBtn, this.container.beSubstitute);
        updateButton(this.beSubstitutionsDisabledBtn, !this.container.beSubstitute);
        updateButton(this.fluidPrioritizedEnabledBtn, this.container.prioritize);
        updateButton(this.fluidPrioritizedDisabledBtn, !this.container.prioritize);
        updateButton(this.tabCraftButton, this.container.isCraftingMode());
        updateButton(this.tabProcessButton, !this.container.isCraftingMode());
        this.processingScrollBar.setVisible(!this.container.isCraftingMode());
        this.processingScrollBar.draw(this.parent);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(getBackground());
        if (this.container.isCraftingMode() || this.container.inverted) {
            this.parent.drawTexturedModalRect(offsetX + 209, offsetY, 0, 0, 133, 93);
        } else {
            this.parent.drawTexturedModalRect(offsetX + 209, offsetY, 0, 93, 133, 93);
        }
        this.parent.drawTexturedModalRect(offsetX + 209, offsetY + 93, 133, 0, 40, 77);
        this.parent.drawTexturedModalRect(offsetX + 209, offsetY + 93 + 77, 173, 0, 32, 32);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        if (this.container.isCraftingMode()) {
            substitutionsEnabledBtn.xPosition = this.parent.getGuiLeft() + 291;
            substitutionsEnabledBtn.yPosition = this.parent.getGuiTop() + 14;

            substitutionsDisabledBtn.xPosition = this.parent.getGuiLeft() + 291;
            substitutionsDisabledBtn.yPosition = this.parent.getGuiTop() + 14;

            beSubstitutionsEnabledBtn.xPosition = this.parent.getGuiLeft() + 291;
            beSubstitutionsEnabledBtn.yPosition = this.parent.getGuiTop() + 24;

            beSubstitutionsDisabledBtn.xPosition = this.parent.getGuiLeft() + 291;
            beSubstitutionsDisabledBtn.yPosition = this.parent.getGuiTop() + 24;

            fluidPrioritizedEnabledBtn.xPosition = -9000;
            fluidPrioritizedEnabledBtn.yPosition = -9000;

            fluidPrioritizedDisabledBtn.xPosition = -9000;
            fluidPrioritizedDisabledBtn.yPosition = -9000;

            doubleBtn.xPosition = -9000;
            doubleBtn.yPosition = -9000;

            clearBtn.xPosition = this.parent.getGuiLeft() + 281;
            clearBtn.yPosition = this.parent.getGuiTop() + 14;

            invertBtn.xPosition = -9000;
            invertBtn.yPosition = -9000;

            combineEnableBtn.xPosition = -9000;
            combineEnableBtn.yPosition = -9000;

            combineDisableBtn.xPosition = -9000;
            combineDisableBtn.yPosition = -9000;

        } else {
            final int offset = container.inverted ? 18 * -3 : 0;
            substitutionsEnabledBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            substitutionsEnabledBtn.yPosition = this.parent.getGuiTop() + 10;

            substitutionsDisabledBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            substitutionsDisabledBtn.yPosition = this.parent.getGuiTop() + 10;

            beSubstitutionsEnabledBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            beSubstitutionsEnabledBtn.yPosition = this.parent.getGuiTop() + 69;

            beSubstitutionsDisabledBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            beSubstitutionsDisabledBtn.yPosition = this.parent.getGuiTop() + 69;

            fluidPrioritizedEnabledBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            fluidPrioritizedEnabledBtn.yPosition = this.parent.getGuiTop() + 59;

            fluidPrioritizedDisabledBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            fluidPrioritizedDisabledBtn.yPosition = this.parent.getGuiTop() + 59;

            doubleBtn.xPosition = this.parent.getGuiLeft() + 306 + offset;
            doubleBtn.yPosition = this.parent.getGuiTop() + 20;

            clearBtn.xPosition = this.parent.getGuiLeft() + 296 + offset;
            clearBtn.yPosition = this.parent.getGuiTop() + 10;

            invertBtn.xPosition = this.parent.getGuiLeft() + 296 + offset;
            invertBtn.yPosition = this.parent.getGuiTop() + 20;

            combineEnableBtn.xPosition = this.parent.getGuiLeft() + 296 + offset;
            combineEnableBtn.yPosition = this.parent.getGuiTop() + 59;

            combineDisableBtn.xPosition = this.parent.getGuiLeft() + 296 + offset;
            combineDisableBtn.yPosition = this.parent.getGuiTop() + 59;
            processingScrollBar.setCurrentScroll(container.activePage);
        }
    }

    @Override
    public void initGui() {
        this.gui.getButtonList()
            .add(
                this.encodeBtn = new GuiImgButton(
                    this.parent.getGuiLeft() + 220,
                    this.parent.getGuiTop() + 118,
                    Settings.ACTIONS,
                    ActionItems.ENCODE));
        this.gui.getButtonList()
            .add(
                this.tabProcessButton = new GuiTabButton(
                    this.parent.getGuiLeft() + 248,
                    this.parent.getGuiTop() + 93,
                    new ItemStack(Blocks.furnace),
                    GuiText.ProcessingPattern.getLocal(),
                    this.gui.getRenderItem()));
        this.gui.getButtonList()
            .add(
                this.tabCraftButton = new GuiTabButton(
                    this.parent.getGuiLeft() + 248,
                    this.parent.getGuiTop() + 93,
                    new ItemStack(Blocks.crafting_table),
                    GuiText.CraftingPattern.getLocal(),
                    this.gui.getRenderItem()));
        this.substitutionsEnabledBtn = new GuiImgButton(
            this.parent.getGuiLeft() + 306,
            this.parent.getGuiTop() + 10,
            Settings.ACTIONS,
            ItemSubstitution.ENABLED);
        this.substitutionsEnabledBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new GuiImgButton(
            this.parent.getGuiLeft() + 306,
            this.parent.getGuiTop() + 10,
            Settings.ACTIONS,
            ItemSubstitution.DISABLED);
        this.substitutionsDisabledBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.substitutionsDisabledBtn);

        this.fluidPrioritizedEnabledBtn = new GuiFCImgButton(
            this.parent.getGuiLeft() + 306,
            this.parent.getGuiTop() + 59,
            "FORCE_PRIO",
            "DO_PRIO");
        this.fluidPrioritizedEnabledBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.fluidPrioritizedEnabledBtn);

        this.fluidPrioritizedDisabledBtn = new GuiFCImgButton(
            this.parent.getGuiLeft() + 306,
            this.parent.getGuiTop() + 59,
            "NOT_PRIO",
            "DONT_PRIO");
        this.fluidPrioritizedDisabledBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.fluidPrioritizedDisabledBtn);

        this.invertBtn = new GuiImgButton(
            this.parent.getGuiLeft() + 296,
            this.parent.getGuiTop() + 20,
            Settings.ACTIONS,
            container.inverted ? PatternSlotConfig.C_4_16 : PatternSlotConfig.C_16_4);
        this.invertBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.invertBtn);

        this.clearBtn = new GuiImgButton(
            this.parent.getGuiLeft() + 296,
            this.parent.getGuiTop() + 10,
            Settings.ACTIONS,
            ActionItems.CLOSE);
        this.clearBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.clearBtn);

        if (ModAndClassUtil.isDoubleButton) {
            this.doubleBtn = new GuiImgButton(
                this.parent.getGuiLeft() + 306,
                this.parent.getGuiTop() + 20,
                Settings.ACTIONS,
                ActionItems.DOUBLE);
            this.doubleBtn.setHalfSize(true);
            this.gui.getButtonList()
                .add(this.doubleBtn);
        }

        this.combineEnableBtn = new GuiFCImgButton(
            this.parent.getGuiLeft() + 296,
            this.parent.getGuiTop() + 59,
            "FORCE_COMBINE",
            "DO_COMBINE");
        this.combineEnableBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.combineEnableBtn);

        this.combineDisableBtn = new GuiFCImgButton(
            this.parent.getGuiLeft() + 296,
            this.parent.getGuiTop() + 59,
            "NOT_COMBINE",
            "DONT_COMBINE");
        this.combineDisableBtn.setHalfSize(true);
        this.gui.getButtonList()
            .add(this.combineDisableBtn);
        if (ModAndClassUtil.isBeSubstitutionsButton) {
            this.beSubstitutionsEnabledBtn = new GuiImgButton(
                this.parent.getGuiLeft() + 306,
                this.parent.getGuiTop() + 69,
                Settings.ACTIONS,
                PatternBeSubstitution.ENABLED);
            this.beSubstitutionsEnabledBtn.setHalfSize(true);
            this.gui.getButtonList()
                .add(this.beSubstitutionsEnabledBtn);

            this.beSubstitutionsDisabledBtn = new GuiImgButton(
                this.parent.getGuiLeft() + 306,
                this.parent.getGuiTop() + 69,
                Settings.ACTIONS,
                PatternBeSubstitution.DISABLED);
            this.beSubstitutionsDisabledBtn.setHalfSize(true);
            this.gui.getButtonList()
                .add(this.beSubstitutionsDisabledBtn);
        }
        processingScrollBar.setTop(9);
        processingScrollBar.setLeft(215);
    }

    protected void updateButton(GuiButton button, boolean vis) {
        if (button != null) {
            button.visible = vis;
        }
    }

    @Override
    public boolean hideItemPanelSlot(int tx, int ty, int tw, int th) {
        int rw = this.w;
        int rh = this.h;
        if (tw <= 0 || th <= 0) {
            return false;
        }

        int rx = this.parent.getGuiLeft() + this.parent.getXSize();
        int ry = this.parent.getGuiTop();

        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;

        // overflow || intersect
        return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
    }

    @Override
    public void mouseClicked(int xCoord, int yCoord, int btn) {
        if (this.container.isCraftingMode()) return;
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar
            .click(this.parent, xCoord - this.parent.getGuiLeft(), yCoord - this.parent.getGuiTop());
        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    protected void changeActivePage() {
        AE2Thing.proxy.netHandler.sendToServer(
            new CPacketTerminalBtns("PatternTerminal.ActivePage", this.processingScrollBar.getCurrentScroll()));
    }

    @Override
    public boolean handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        if (!((slot instanceof SlotPatternFake) || (slot instanceof SlotFakeCraftingMatrix)
            || (slot instanceof SlotPatternTerm))) {
            return false;
        }
        if (mouseButton == 3 && !this.container.isCraftingMode()) {
            if (slot.getHasStack()) {
                IAEItemStack stack = AEItemStack.create(slot.getStack());
                this.inventorySlots.setTargetStack(stack);
                for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
                    if (slot.equals(this.inventorySlots.inventorySlots.get(i))) {
                        if (isCtrlKeyDown() && !Util.isFluidPacket(stack.getItemStack())) {
                            InventoryActionExtend action = InventoryActionExtend.SET_PATTERN_NAME;
                            AE2Thing.proxy.netHandler
                                .sendToServer(new CPacketInventoryActionExtend(action, i, 0, stack));
                        } else {
                            InventoryAction action = InventoryAction.SET_PATTERN_VALUE;
                            AE2Thing.proxy.netHandler.sendToServer(new CPacketInventoryAction(action, i, 0, stack));
                        }
                    }
                }
                return true;
            }
        }

        InventoryAction action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
            : InventoryAction.PICKUP_OR_SET_DOWN;
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
                action = InventoryAction.MOVE_REGION;
            } else {
                action = InventoryAction.PICKUP_SINGLE;
            }
        }
        if (Ae2ReflectClient.getDragClick(this.parent)
            .size() > 1) {
            return false;
        }
        final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, -1);
        NetworkHandler.instance.sendToServer(p);
        return true;
    }

    @Override
    public boolean actionPerformed(GuiButton btn) {
        if (this.tabCraftButton == btn || this.tabProcessButton == btn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns(
                    "PatternTerminal.CraftMode",
                    this.tabProcessButton == btn ? MODE_CRAFTING : MODE_PROCESSING));
            return true;
        } else if (this.encodeBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns(
                    "PatternTerminal.Encode",
                    (isCtrlKeyDown() ? 1 : 0) << 1 | (isShiftKeyDown() ? 1 : 0)));
            return true;
        } else if (this.clearBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("PatternTerminal.Clear", 1));
            return true;
        } else if (this.substitutionsEnabledBtn == btn || this.substitutionsDisabledBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns("PatternTerminal.Substitute", this.substitutionsEnabledBtn == btn ? 0 : 1));
            return true;
        } else if (this.fluidPrioritizedEnabledBtn == btn || this.fluidPrioritizedDisabledBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns(
                    "PatternTerminal.Prioritize",
                    isShiftKeyDown() ? 2 : (container.prioritize ? 0 : 1)));
            return true;
        } else if (this.invertBtn == btn) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTerminalBtns("PatternTerminal.Invert", container.inverted ? 0 : 1));
            return true;
        } else if (this.combineDisableBtn == btn || this.combineEnableBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTerminalBtns("PatternTerminal.Combine", this.combineDisableBtn == btn ? 1 : 0));
            return true;
        } else if (com.glodblock.github.util.ModAndClassUtil.isDoubleButton && doubleBtn == btn) {
            final boolean backwards = Mouse.isButtonDown(1);
            int val = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1 : 0;
            if (backwards) val |= 0b10;
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("PatternTerminal.Double", val));
            return true;
        } else if (ModAndClassUtil.isBeSubstitutionsButton && beSubstitutionsDisabledBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("PatternTerminal.beSubstitute", 1));
            return true;
        } else if (ModAndClassUtil.isBeSubstitutionsButton && beSubstitutionsEnabledBtn == btn) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("PatternTerminal.beSubstitute", 0));
            return true;
        }
        return false;
    }

    @Override
    public void mouseClickMove(int x, int y, int c, long d) {
        final Slot slot = this.gui.getSlot(x, y);
        final ItemStack itemstack = this.parent.mc.thePlayer.inventory.getItemStack();

        if (slot instanceof SlotFake && itemstack != null) {
            this.drag_click.add(slot);
            if (this.drag_click.size() > 1) {
                for (final Slot dr : this.drag_click) {
                    final PacketInventoryAction p = new PacketInventoryAction(
                        c == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE,
                        dr.slotNumber,
                        -1);
                    NetworkHandler.instance.sendToServer(p);
                }
            }
        }
        if (this.container.isCraftingMode()) return;
        final int currentScroll = this.processingScrollBar.getCurrentScroll();
        this.processingScrollBar.click(this.parent, x - this.parent.getGuiLeft(), y - this.parent.getGuiTop());
        if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    public boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        if (this.container.isCraftingMode()) return false;
        if (this.processingScrollBar.contains(mouseX - this.parent.getGuiLeft(), mouseY - this.parent.getGuiTop())) {
            final int currentScroll = this.processingScrollBar.getCurrentScroll();
            this.processingScrollBar.wheel(wheel);
            if (currentScroll != this.processingScrollBar.getCurrentScroll()) {
                changeActivePage();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character, int key) {
        return false;
    }

    @Override
    public boolean draggable() {
        return false;
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(
            this.parent.getGuiLeft() + this.parent.getXSize(),
            this.parent.getGuiTop(),
            this.w,
            this.h);
    }

    @Override
    public void setRectangle(int x, int y) {}
}
