package com.asdflj.ae2thing.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import com.asdflj.ae2thing.util.NameConst;

import appeng.me.Grid;
import appeng.tile.networking.TileController;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;

public class CraftingDebugCardObject {

    private final ItemStack item;
    private final NBTTagCompound data;
    private Mode currentMode;

    public enum Mode {
        Everything,
        Player,
        Machine
    }

    public CraftingDebugCardObject(ItemStack itemStack) {
        this.item = itemStack;
        this.data = Platform.openNbtData(itemStack);
        this.currentMode = Mode.values()[this.data.getByte(Constants.DEBUG_CARD_MODE)];
    }

    public Mode getMode() {
        return currentMode;
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        data.setByte(Constants.DEBUG_CARD_MODE, (byte) mode.ordinal());
    }

    public Mode getNextMode() {
        return Mode.values()[(this.getMode()
            .ordinal() + 1) % Mode.values().length];
    }

    public void sendMessageToPlayer(TileController tc, EntityPlayer player) {
        try {
            Grid grid = (Grid) tc.getProxy()
                .getGrid();
            List<String> text = getHistoryText(grid);
            if (!text.isEmpty()) {
                for (String s : text) {
                    player.addChatComponentMessage(new ChatComponentText(s));
                }
            } else {
                player.addChatComponentMessage(
                    new ChatComponentText(I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_NO_HISTORY)));
            }
        } catch (Exception ignored) {}
    }

    public List<String> getHistoryText(Grid grid) {
        List<String> msg = new ArrayList<>();
        CraftingDebugHelper.LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> history = AE2ThingAPI.instance()
            .getHistory(grid);
        for (CraftingDebugHelper.CraftingInfo info : history) {
            if (this.getMode() == Mode.Player && !info.isPlayer) continue;
            if (this.getMode() == Mode.Machine && info.isPlayer) continue;
            msg.add("------------------------------------------------");
            msg.add(I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_NETWORK_ID) + info.getNetworkID());
            msg.add(
                I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_REQUEST_TYPE) + I18n.format(
                    info.isPlayer ? NameConst.MESSAGE_CRAFTING_DEBUG_CARD_REQUEST_TYPE_PLAYER
                        : NameConst.MESSAGE_CRAFTING_DEBUG_CARD_REQUEST_TYPE_MACHINE));
            if (!info.isPlayer && info.pos != null) {
                msg.add(
                    I18n.format(
                        NameConst.MESSAGE_CRAFTING_DEBUG_CARD_POS,
                        info.pos.getX(),
                        info.pos.getY(),
                        info.pos.getZ()));
            }
            msg.add(I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_NAME) + info.name);
            msg.add(I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_START_TIME) + info.getFormatStartTime());
            if (info.isFinish()) {
                msg.add(I18n.format(NameConst.MESSAGE_CRAFTING_DEBUG_CARD_USAGE_TIME) + info.getUsageTime());
            }
            msg.add(
                I18n.format(
                    NameConst.MESSAGE_CRAFTING_DEBUG_CARD_STACK,
                    info.itemName,
                    ReadableNumberConverter.INSTANCE.toSlimReadableForm(info.requestSize)));
            msg.add("------------------------------------------------");
        }
        return msg;
    }
}
