package com.asdflj.ae2thing.client.gui;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternModifier;
import com.asdflj.ae2thing.client.gui.container.slot.SlotEncodedPatternInput;
import com.asdflj.ae2thing.client.gui.container.slot.SlotReplaceFake;
import com.asdflj.ae2thing.network.CPacketInventoryAction;
import com.asdflj.ae2thing.network.CPacketInventoryActionExtend;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIDragClick;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiPatternModifier extends AEBaseGui implements INEIGuiHandler {

    protected GuiButton replace;
    protected GuiButton clear;
    protected ContainerPatternModifier container;

    public GuiPatternModifier(InventoryPlayer inventory, ITerminalHost inv) {
        super(new ContainerPatternModifier(inventory, inv));
        this.container = (ContainerPatternModifier) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 207;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_PATTERN_MODIFIER)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }

    public void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/" + file);
        this.mc.getTextureManager()
            .bindTexture(loc);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(
            this.replace = new GuiButton(
                0,
                this.guiLeft + 70,
                this.guiTop + 92,
                50,
                20,
                I18n.format(NameConst.GUI_PATTERN_MODIFIER_REPLACE)));
        this.buttonList.add(
            this.clear = new GuiButton(
                0,
                this.guiLeft + 70 + 50 + 2,
                this.guiTop + 92,
                50,
                20,
                I18n.format(NameConst.GUI_PATTERN_MODIFIER_CLEAR)));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(this.getBackground());
        drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
    }

    protected String getBackground() {
        return "gui/pattern_modifier.png";
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == this.replace) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketInventoryActionExtend(InventoryActionExtend.REPLACE_PATTERN));
        } else if (button == this.clear) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketInventoryActionExtend(InventoryActionExtend.CLEAR_PATTERN));
        }
        super.actionPerformed(button);
    }

    @Override
    public void func_146977_a(final Slot s) {
        drawSlot(s);
        super.func_146977_a(s);
    }

    private void drawSlot(final Slot s) {
        if (s instanceof SlotEncodedPatternInput p && p.getHasStack()
            && p.getStack()
                .getItem() != null
            && p.getStack()
                .getItem() instanceof ICraftingPatternItem cr
            && this.container.getSource() != null) {
            ItemStack item = this.container.getSource();
            ICraftingPatternDetails details = cr
                .getPatternForItem(s.getStack(), this.container.getInventoryPlayer().player.worldObj);
            if (details != null) {
                if (drawSlotBG(details.getInputs(), item, s)) return;
                if (drawSlotBG(details.getOutputs(), item, s)) return;
                this.zLevel = 100.0F;
                itemRender.zLevel = 100.0F;
                GL11.glDisable(GL11.GL_LIGHTING);
                drawRect(
                    s.xDisplayPosition,
                    s.yDisplayPosition,
                    16 + s.xDisplayPosition,
                    16 + s.yDisplayPosition,
                    GuiColors.ItemSlotOverlayUnpowered.getColor());
                GL11.glEnable(GL11.GL_LIGHTING);
                this.zLevel = 0.0F;
                itemRender.zLevel = 0.0F;
            }
        }
    }

    private boolean drawSlotBG(IAEItemStack[] list, ItemStack item, Slot s) {
        for (IAEItemStack is : list) {
            if (is == null) continue;
            ItemStack i = is.getItemStack();
            if (Platform.isSameItemPrecise(i, item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return Collections.emptyList();
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        if (draggedStack == null) {
            return false;
        }
        Slot slotAtPosition = this.getSlotAtPosition(mouseX, mouseY);
        if (slotAtPosition == null) {
            return false;
        }
        Slot target;
        if (this.container.getSourceSlot()
            .equals(slotAtPosition)) {
            target = this.container.getSourceSlot();
        } else if (this.container.getTargetSlot()
            .equals(slotAtPosition)) {
                target = this.container.getTargetSlot();
            } else {
                target = null;
            }
        if (target != null) {
            target.putStack(draggedStack.copy());
            NetworkHandler.instance.sendToServer(new PacketNEIDragClick(draggedStack, target.slotNumber));
            return true;
        }

        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return false;
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        if (mouseButton == 3 && slot instanceof SlotReplaceFake && slot.getHasStack()) {
            IAEItemStack stack = AEItemStack.create(slot.getStack());
            this.container.setTargetStack(stack);
            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
                if (slot.equals(this.inventorySlots.inventorySlots.get(i))) {
                    InventoryAction action = InventoryAction.SET_PATTERN_VALUE;
                    AE2Thing.proxy.netHandler.sendToServer(new CPacketInventoryAction(action, i, 0, stack));
                    return;
                }
            }
        }

        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }
}
