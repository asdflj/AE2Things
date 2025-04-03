package com.asdflj.ae2thing.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.network.CPacketInventoryActionExtend;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.Util;

import appeng.util.item.AEItemStack;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeybindLoader implements Runnable {

    public static KeyBinding openBackpackTerminal;
    public static KeyBinding openDualInterfaceTerminal;
    public static KeyBinding openTerminalMenu;

    @Override
    public void run() {
        openBackpackTerminal = new KeyBinding(
            AE2Thing.MODID + ".key.open_backpack_terminal",
            Keyboard.CHAR_NONE,
            "itemGroup." + AE2Thing.MODID);
        ClientRegistry.registerKeyBinding(openBackpackTerminal);
        openDualInterfaceTerminal = new KeyBinding(
            AE2Thing.MODID + ".key.open_dual_interface_terminal",
            Keyboard.CHAR_NONE,
            "itemGroup." + AE2Thing.MODID);
        ClientRegistry.registerKeyBinding(openDualInterfaceTerminal);
        openTerminalMenu = new KeyBinding(
            AE2Thing.MODID + ".key.open_terminal",
            Keyboard.CHAR_NONE,
            "itemGroup." + AE2Thing.MODID);
        ClientRegistry.registerKeyBinding(openTerminalMenu);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
        // middle click
        if (Mouse.isButtonDown(2) && !p.capabilities.isCreativeMode && p.inventory.getCurrentItem() == null) {
            // request item
            ItemStack block = getTargetBlock(p.getEntityWorld(), p);
            if (block != null) {
                if (Util.findItemStack(p, block) == -1) {
                    AE2Thing.proxy.netHandler.sendToServer(
                        new CPacketInventoryActionExtend(
                            InventoryActionExtend.REQUEST_ITEM,
                            p.inventory.currentItem,
                            0,
                            AEItemStack.create(block)));
                }
            }
            return;
        }
        if (!(event instanceof InputEvent.KeyInputEvent) && !(event instanceof InputEvent.MouseInputEvent)) return;
        if (p.openContainer == null) {
            return;
        }
        if (openTerminalMenu.isPressed()) {
            AE2ThingAPI.instance()
                .openTerminalMenu();
        }
        if (openBackpackTerminal.isPressed()) {
            AE2ThingAPI.instance()
                .openBackpackTerminal();
        }
        if (openDualInterfaceTerminal.isPressed()) {
            AE2ThingAPI.instance()
                .openDualinterfaceTerminal();
        }

    }

    private static ItemStack getTargetBlock(World world, EntityPlayer player) {
        Vec3 position = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLookVec();
        Vec3 end = position.addVector(look.xCoord * 5.0, look.yCoord * 5.0, look.zCoord * 5.0);
        MovingObjectPosition hit = world.rayTraceBlocks(position, end);

        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return new BlockPos(hit, world).getPickBlock(hit, world, player);
        }
        return null;
    }
}
