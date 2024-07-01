package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;

public class GuiCraftingTerminal extends GuiMonitor {

    public ContainerCraftingTerminal monitorableContainer;

    public GuiCraftingTerminal(Container container) {
        super(container);
        this.showViewBtn = false;
        this.viewCell = false;
    }

    public GuiCraftingTerminal(InventoryPlayer inventory, ITerminalHost inv) {
        this(new ContainerCraftingTerminal(inventory, inv));
        this.xSize = 195;
        this.ySize = 204;
        this.standardSize = this.xSize;
        (this.monitorableContainer = (ContainerCraftingTerminal) this.inventorySlots).setGui(this);
        this.reservedSpace = 73;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(
            this.clearBtn = new GuiImgButton(
                this.guiLeft + 92,
                this.guiTop + this.ySize - 156,
                Settings.ACTIONS,
                ActionItems.STASH));
        this.clearBtn.setHalfSize(true);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (this.clearBtn == btn) {
            Slot s = null;
            final Container c = this.inventorySlots;
            for (final Object j : c.inventorySlots) {
                if (j instanceof SlotCraftingMatrix) {
                    s = (Slot) j;
                }
            }
            if (s != null) {
                final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, s.slotNumber, 0);
                NetworkHandler.instance.sendToServer(p);
            }
        }
        super.actionPerformed(btn);
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        saveSearchString();

        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    protected void repositionSlot(final AppEngSlot s) {
        s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
            GuiText.CraftingTerminal.getLocal(),
            8,
            this.ySize - 96 + 1 - this.getReservedSpace(),
            GuiColors.CraftingTerminalTitle.getColor());
        this.fontRendererObj.drawString(this.getGuiDisplayName(GuiText.Terminal.getLocal()), 8, 6, 4210752);
    }

    public int getReservedSpace() {
        return this.reservedSpace;
    }

    protected String getBackground() {
        return "gui/crafting.png";
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(this.getBackground());
        final int x_width = 195;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);
        for (int x = 0; x < this.rows; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }
        this.drawTexturedModalRect(
            offsetX,
            offsetY + 16 + this.rows * 18 + this.lowerTextureOffset,
            0,
            106 - 18 - 18,
            x_width,
            99 + this.reservedSpace - this.lowerTextureOffset);
        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    @Override
    public void postUpdate(List<IAEItemStack> list) {
        for (IAEItemStack ias : list) {
            if (ias.getItem() instanceof ItemFluidDrop) {
                ItemStack fluidDrop = ItemFluidDrop.newDisplayStack(ItemFluidDrop.getFluidStack(ias.getItemStack()));
                AEItemStack is = AEItemStack.create(fluidDrop);
                if (is == null) continue;
                is.setStackSize(ias.getStackSize());
                this.repo.postUpdate(is);
            } else {
                this.repo.postUpdate(ias);
            }
        }
        this.repo.updateView();
        this.setScrollBar();
    }

}
