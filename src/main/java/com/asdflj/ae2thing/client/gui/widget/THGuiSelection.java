package com.asdflj.ae2thing.client.gui.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.util.AEColor;

public class THGuiSelection extends BaseGuiButton implements IScrollable {

    private boolean display = false;

    public THGuiSelection(int xPos, int yPos, int width, int height, int offsetX, int offsetY, Component component,
        String packetName) {
        super(xPos, yPos, width, height, offsetX, offsetY, component, packetName);
        this.visible = true;
    }

    public boolean isDisplay() {
        return display;
    }

    @Override
    public void onClick() {
        if (this.display) {
            unfocused();
        } else {
            this.display = true;
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        Info info = component.getInfo();
        if (this.display && info != null && this.visible) {
            List<String> list = Arrays.stream(AEColor.values())
                .map(AEColor::toString)
                .collect(Collectors.toList());
            drawHistorySelection(
                this.component.getX() + this.component.getRender()
                    .getStringWidth(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_COLOR) + ": "),
                this.component.getY() - 1,
                info.getColor(),
                50,
                list);
        }
    }

    @Override
    public int getIndex() {
        return 2;
    }

    @Override
    public void unfocused() {
        if (this.display) {
            this.display = false;
            Info info = this.component.getInfo();
            if (info == null) return;
            updateColor(info);
        }
    }

    private NBTTagCompound writeToNBT(Info info) {
        NBTTagCompound data = new NBTTagCompound();
        info.a.writeToNBT(data);
        data.setShort(
            Constants.COLOR,
            (short) info.getAEColor()
                .ordinal());
        return data;
    }

    private void updateColor(Info info) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("#0", writeToNBT(info));
        AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns(packetName, info.getName(), tag));
    }

    private void drawHistorySelection(final int x, final int y, String text, int width,
        final List<String> searchHistory) {
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
                var7 = this.component.getGui()
                    .getFontRenderer()
                    .getStringWidth(var4[var6]) + 8;

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

            if (this.component.getGui()
                .getGuiTop() + var7 + var9 + 6 > this.component.getGui().height) {
                var7 = this.component.getGui().height - var9
                    - this.component.getGui()
                        .getGuiTop()
                    - 6;
            }

            this.zLevel = 300.0F;
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

                this.component.getGui()
                    .getFontRenderer()
                    .drawStringWithShadow(var14, var6, var7, -1);

                if (var13 == 0) {
                    var7 += 2;
                }

                var7 += 10;
            }

            this.zLevel = 0.0F;
        }
        GL11.glPopAttrib();
    }

    @Override
    public boolean scroll(int x, int y, int wheel) {
        Info info = this.component.getInfo();
        if (!isDisplay() || info == null) return false;
        AEColor color = switch (wheel) {
            case -1 -> AEColor.values()[Math.max(
                0,
                info.getAEColor()
                    .ordinal() - 1)];
            case 1 -> AEColor.values()[Math.min(
                AEColor.values().length - 1,
                info.getAEColor()
                    .ordinal() + 1)];
            default -> null;
        };
        if (color != null) {
            info.setAEColor(color);
        }

        return true;
    }

}
