package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.network.CPacketFluidUpdate;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.util.Util;
import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

import appeng.api.AEApi;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.me.SlotME;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;

public class GuiCraftingTerminal extends GuiItemMonitor {

    public ContainerCraftingTerminal monitorableContainer;
    protected ContainerCraftingTerminal inv;

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
        this.inv = (ContainerCraftingTerminal) this.inventorySlots;
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
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (slot instanceof SlotME sme) {
            ItemStack cs = player.inventory.getItemStack();
            if (Util.FluidUtil.isFluidContainer(cs)) {
                if (ctrlDown == 0 && sme.getHasStack()
                    && sme.getStack()
                        .getItem() instanceof ItemFluidDrop
                    && Util.FluidUtil.isEmpty(cs)) {
                    IAEFluidStack fluid = ItemFluidDrop.getAeFluidStack(sme.getAEStack());
                    AE2Thing.proxy.netHandler.sendToServer(new CPacketFluidUpdate(fluid, isShiftKeyDown()));
                    return;
                } else if (ctrlDown == 1 && Util.FluidUtil.isFilled(cs)) {
                    AE2Thing.proxy.netHandler.sendToServer(new CPacketFluidUpdate(null, isShiftKeyDown()));
                    return;
                }
            }
            if (mouseButton == 3 && player.capabilities.isCreativeMode
                && sme.getHasStack()
                && sme.getStack()
                    .getItem() instanceof ItemFluidDrop) {
                return;
            }
        }
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

    public void postUpdate(List<IAEItemStack> list) {
        for (IAEItemStack ias : list) {
            if (ias.getItem() instanceof ItemFluidDrop) {
                ItemStack fluidDrop = ItemFluidDrop.newDisplayStack(ItemFluidDrop.getFluidStack(ias.getItemStack()));
                this.repo.postUpdate(
                    AEApi.instance()
                        .storage()
                        .createItemStack(fluidDrop));
            } else {
                this.repo.postUpdate(ias);
            }
        }
        this.repo.updateView();
        this.setScrollBar();
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawFluidSlot(s)) super.func_146977_a(s);
    }

    public boolean drawFluidSlot(Slot slot) {
        if (slot instanceof SlotME) {
            IAEItemStack stack = ((SlotME) slot).getAEStack();
            if (stack == null || stack.getItem() == null || !(stack.getItem() instanceof ItemFluidDrop)) return true;
            FluidStack fluidStack = ItemFluidDrop.getFluidStack(slot.getStack());
            this.drawWidget(slot.xDisplayPosition, slot.yDisplayPosition, fluidStack.getFluid());
            aeRenderItem.setAeStack(stack);
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            aeRenderItem.renderItemOverlayIntoGUI(
                fontRendererObj,
                mc.getTextureManager(),
                stack.getItemStack(),
                slot.xDisplayPosition,
                slot.yDisplayPosition);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
            return false;
        }
        return true;
    }

    private void drawWidget(int posX, int posY, Fluid fluid) {
        if (fluid == null) return;
        IIcon icon = fluid.getIcon();
        if (icon == null) return;

        if (ModAndClassUtil.HODGEPODGE && icon instanceof IPatchedTextureAtlasSprite) {
            ((IPatchedTextureAtlasSprite) icon).markNeedsAnimationUpdate();
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(
            (fluid.getColor() >> 16 & 0xFF) / 255.0F,
            (fluid.getColor() >> 8 & 0xFF) / 255.0F,
            (fluid.getColor() & 0xFF) / 255.0F);
        drawTexturedModelRectFromIcon(posX, posY, fluid.getIcon(), 16, 16);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3f(1, 1, 1);
    }

    public void setPlayerInv(ItemStack is) {
        this.inv.getPlayerInv()
            .setItemStack(is);
    }
}
