package com.asdflj.ae2thing.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.widget.IGuiSelection;
import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.nei.NEI_TH_Config;
import com.asdflj.ae2thing.network.CPacketFluidUpdate;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.HBMAeAddonUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.glodblock.github.hbmaeaddon.util.HBMUtil;
import com.glodblock.github.util.Util;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.me.SlotME;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import codechicken.nei.LayoutManager;
import codechicken.nei.util.TextHistory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;

public abstract class BaseMEGui extends AEBaseMEGui implements IGuiSelection {

    protected IConfigManager configSrc;
    protected TextHistory history;

    public BaseMEGui(Container container) {
        super(container);
        this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        this.history = Ae2ReflectClient.getHistory(LayoutManager.searchField);
    }

    protected boolean isNEISearch() {
        final Enum<?> s = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        return s == SearchBoxMode.NEI_MANUAL_SEARCH || s == SearchBoxMode.NEI_AUTOSEARCH;
    }

    protected String getContainerDisplayName(ItemStack is) {
        if (ModAndClassUtil.THE && AspectUtil.isEssentiaContainer(is)) {
            Aspect aspect = AspectUtil.getAspectFromJar(is);
            return aspect.getName();
        } else if (Util.FluidUtil.isFluidContainer(is)) {
            FluidStack fs = Util.FluidUtil.getFluidFromContainer(is);
            return fs.getLocalizedName();
        } else if (ModAndClassUtil.HBM_AE_ADDON && HBMAeAddonUtil.getItemHasFluidType(is)) {
            return HBMUtil.getFluidType(is)
                .getLocalizedName();
        } else {
            return is.getDisplayName();
        }
    }

    public boolean hasShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    protected boolean isFilledContainer(ItemStack is) {
        if (is == null) return false;
        return (Util.FluidUtil.isFluidContainer(is) && Util.FluidUtil.isFilled(is))
            || (ModAndClassUtil.THE && AspectUtil.isEssentiaContainer(is) && !AspectUtil.isEmptyEssentiaContainer(is))
            || (ModAndClassUtil.HBM_AE_ADDON && HBMAeAddonUtil.getItemHasFluidType(is));
    }

    private boolean isEmptyContainer(ItemStack is, IAEFluidStack fs) {
        if (is == null) return false;
        return Util.FluidUtil.isEmpty(is)
            || (ModAndClassUtil.THE && AspectUtil.isEssentiaContainer(is) && AspectUtil.isEmptyEssentiaContainer(is))
            || (ModAndClassUtil.HBM_AE_ADDON && HBMAeAddonUtil.getItemIsEmptyContainer(is, fs));
    }

    @SideOnly(Side.CLIENT)
    public boolean updateFluidContainer(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (slot instanceof SlotME sme) {
            try {
                ItemStack cs = player.inventory.getItemStack();
                IAEItemStack item = sme.getHasStack() ? sme.getAEStack() : null;
                if (ctrlDown == 0) {
                    if (item != null && item.getItem() != null
                        && item.getItem() instanceof ItemFluidDrop
                        && item.getStackSize() != 0) {
                        if (cs == null || isEmptyContainer(cs, ItemFluidDrop.getAeFluidStack(item))) {
                            IAEFluidStack fluid = ItemFluidDrop.getAeFluidStack(item);
                            AE2Thing.proxy.netHandler.sendToServer(new CPacketFluidUpdate(fluid, isShiftKeyDown()));
                            return true;
                        }
                    }
                } else if (ctrlDown == 1 && isFilledContainer(cs)) {
                    AE2Thing.proxy.netHandler.sendToServer(new CPacketFluidUpdate(null, isShiftKeyDown()));
                    return true;
                }
                if (mouseButton == 3 && player.capabilities.isCreativeMode
                    && item != null
                    && !item.isCraftable()
                    && item.getItem() instanceof ItemFluidDrop) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    protected void drawContainerActionTooltip(int x, int y, String message) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        final String[] lines = message.split("\n");

        if (lines.length > 0) {
            int width = 0;
            int left;
            int top;

            for (left = 0; left < lines.length; ++left) {
                top = this.fontRendererObj.getStringWidth(lines[left]);

                if (top > width) {
                    width = top;
                }
            }

            left = x + 12;
            top = y - 12;
            int height = 8;

            if (lines.length > 1) {
                height += 2 + (lines.length - 1) * 10;
            }

            ScaledResolution scaledresolution = new ScaledResolution(
                this.mc,
                this.mc.displayWidth,
                this.mc.displayHeight);

            if (top + height + 6 > scaledresolution.getScaledHeight()) {
                top = scaledresolution.getScaledHeight() - height - 6;
            }

            if (left + width + 6 > scaledresolution.getScaledWidth()) {
                left = scaledresolution.getScaledWidth() - width - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            final int color1 = 0xF0100010;
            this.drawGradientRect(left - 3, top - 4, left + width + 3, top - 3, color1, color1);
            this.drawGradientRect(left - 3, top + height + 3, left + width + 3, top + height + 4, color1, color1);
            this.drawGradientRect(left - 3, top - 3, left + width + 3, top + height + 3, color1, color1);
            this.drawGradientRect(left - 4, top - 3, left - 3, top + height + 3, color1, color1);
            this.drawGradientRect(left + width + 3, top - 3, left + width + 4, top + height + 3, color1, color1);
            final int color2 = 0x505000FF;
            final int color3 = 0x5028007F; // (color2 & 16711422) >> 1 | color2 & -16777216;
            this.drawGradientRect(left - 3, top - 3 + 1, left - 3 + 1, top + height + 3 - 1, color2, color3);
            this.drawGradientRect(
                left + width + 2,
                top - 3 + 1,
                left + width + 3,
                top + height + 3 - 1,
                color2,
                color3);
            this.drawGradientRect(left - 3, top - 3, left + width + 3, top - 3 + 1, color2, color2);
            this.drawGradientRect(left - 3, top + height + 2, left + width + 3, top + height + 3, color3, color3);

            for (int var13 = 0; var13 < lines.length; ++var13) {
                String var14 = lines[var13];

                this.fontRendererObj.drawStringWithShadow(var14, left, top, -1);

                if (var13 == 0) {
                    top += 2;
                }

                top += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }
        GL11.glPopAttrib();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        this.drawFluidContainerTooltip(mouseX, mouseY);
    }

    public void drawFluidContainerTooltip(int mouseX, int mouseY) {
        EntityPlayer player = this.mc.thePlayer;
        ItemStack is = player.inventory.getItemStack();
        if (isFilledContainer(is)) {
            Slot s = this.getSlot(mouseX, mouseY);
            if (s instanceof SlotME) {
                List<String> message = new ArrayList<>();
                message.add(
                    "\u00a77" + I18n.format(
                        NameConst.GUI_TERMINAL_STORE_ACTION,
                        I18n.format(NameConst.GUI_TERMINAL_LEFT_CLICK),
                        EnumChatFormatting.WHITE + is.getDisplayName() + EnumChatFormatting.RESET));
                message.add(
                    "\u00a77" + I18n.format(
                        NameConst.GUI_TERMINAL_STORE_ACTION,
                        I18n.format(NameConst.GUI_TERMINAL_RIGHT_CLICK),
                        EnumChatFormatting.WHITE + getContainerDisplayName(is) + EnumChatFormatting.RESET));
                drawContainerActionTooltip(mouseX, mouseY, String.join("\n", message));
            }
        }
    }

    @Override
    public void drawHistorySelection(final int x, final int y, String text, int width,
        final List<String> searchHistory) {
        if (!NEI_TH_Config.getConfigValue(ButtonConstants.HISTORY)) return;
        final int maxRows = AE2ThingAPI.maxSelectionRows;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        String[] var4 = null;
        final List<String> history = new ArrayList<>(searchHistory);
        Collections.reverse(history);

        if (history.size() > maxRows) {
            for (int i = 1; i < history.size(); i++) {
                if (text.equals(history.get(i))) {
                    int max = Math.min(history.size(), i + maxRows - 1);
                    int min = Math.max(0, max - maxRows);
                    var4 = history.subList(min, max)
                        .toArray(new String[0]);
                    break;
                }
            }
        }
        if (var4 == null) {
            var4 = history.subList(0, Math.min(history.size(), 5))
                .toArray(new String[0]);
        }
        if (var4.length > 0) {
            int var5 = width;
            int var6;
            int var7;

            for (var6 = 0; var6 < var4.length; ++var6) {
                var7 = this.fontRendererObj.getStringWidth(var4[var6]) + 8;

                if (var7 > var5) {
                    var5 = var7;
                }
            }

            var6 = x + 3;
            var7 = y + 15;
            int var9 = 8;

            if (var4.length > 1) {
                var9 += 2 + (var4.length - 1) * 10;
            }

            if (this.guiTop + var7 + var9 + 6 > this.height) {
                var7 = this.height - var9 - this.guiTop - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            final int var10 = -267386864;
            this.drawGradientRect(var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10);
            this.drawGradientRect(var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10);
            this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10);
            this.drawGradientRect(var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10);
            this.drawGradientRect(var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10);
            final int var11 = 1347420415;
            final int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
            this.drawGradientRect(var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12);
            this.drawGradientRect(var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12);
            this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11);
            this.drawGradientRect(var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12);

            for (int var13 = 0; var13 < var4.length; ++var13) {
                String var14 = var4[var13];
                if (var14.equals(text)) {
                    var14 = "> " + var14;
                    var14 = '\u00a7' + Integer.toHexString(15) + var14;
                } else {
                    var14 = "\u00a77" + var14;
                }

                this.fontRendererObj.drawStringWithShadow(var14, var6, var7, -1);

                if (var13 == 0) {
                    var7 += 2;
                }

                var7 += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }
        GL11.glPopAttrib();
    }

    public abstract int getOffsetY();

    public void initDone() {

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this instanceof IGuiMonitorTerminal gmt
            && CommonHelper.proxy.isActionKey(ActionKey.TOGGLE_FOCUS, keyCode)) {
            gmt.getSearchField()
                .setFocused(
                    !gmt.getSearchField()
                        .isFocused());
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }
}
