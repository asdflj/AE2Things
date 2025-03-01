package com.asdflj.ae2thing.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;

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
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
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
}
