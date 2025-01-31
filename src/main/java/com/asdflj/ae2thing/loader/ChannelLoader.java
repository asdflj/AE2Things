package com.asdflj.ae2thing.loader;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;

public class ChannelLoader implements Runnable {

    public static final ChannelLoader INSTANCE = new ChannelLoader();

    public static Set<Class<?>> getClasses(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread()
            .getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        Set<Class<?>> classes = new LinkedHashSet<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("jar".equals(resource.getProtocol())) {
                processJarFile(classes, resource, packageName);
            }
        }
        return classes;
    }

    private static void processJarFile(Set<Class<?>> classes, URL jarFileUrl, String packageName) {
        JarFile jarFile = null;
        try {
            JarURLConnection jarURLConnection = (JarURLConnection) jarFileUrl.openConnection();
            if (jarURLConnection != null) {
                jarFile = jarURLConnection.getJarFile();
                if (jarFile != null) {
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry jarEntry = jarEntries.nextElement();
                        String jarEntryName = jarEntry.getName();
                        if (jarEntryName.startsWith(packageName.replace('.', '/') + '/')
                            && jarEntryName.endsWith(".class")) {
                            String className = jarEntryName.substring(0, jarEntryName.lastIndexOf("."))
                                .replaceAll("/", ".");
                            try {
                                classes.add(Class.forName(className));
                            } catch (ClassNotFoundException ignored) {

                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {} finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run() {
        int id = 0;
        AE2ThingNetworkWrapper netHandler = AE2Thing.proxy.netHandler;
        try {
            Set<Class<?>> result = getClasses("com.asdflj.ae2thing.network");
            for (Class<?> aClass : result) {
                if (aClass.getName()
                    .endsWith("Handler")) {
                    Class c = Class.forName(
                        aClass.getName()
                            .replace("$Handler", ""));
                    IMessageHandler cls = (IMessageHandler) aClass.getConstructor()
                        .newInstance();
                    netHandler.registerMessage(
                        cls,
                        c,
                        id++,
                        c.getSimpleName()
                            .startsWith("C") ? Side.SERVER : Side.CLIENT);
                }
            }
        } catch (Exception ignored) {}

    }

    public static void sendPacketToAllPlayers(Packet packet, World world) {
        for (Object player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
            }
        }
    }
}
