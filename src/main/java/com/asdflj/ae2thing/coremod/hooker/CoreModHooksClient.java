package com.asdflj.ae2thing.coremod.hooker;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.items.ItemCraftingAspect;

@SideOnly(Side.CLIENT)
public class CoreModHooksClient {

    private static class ItemInfo {

        public String name;
        public String modId;
        public List<String> tooltip;

        public ItemInfo(String modId, String name, List<String> tooltip) {
            this.name = name;
            this.modId = modId;
            this.tooltip = tooltip;
        }
    }

    private static final HashMap<IAEItemStack, ItemInfo> cache = new HashMap<>();

    public static String getModId(final IAEItemStack is) {
        if (cache.containsKey(is) && cache.get(is).modId != null) {
            return cache.get(is).modId;
        } else if (is.getItem() instanceof ItemFluidDrop) {
            String id = Util.getModId(is);
            putCache(is, id, null, null);
            return id;
        } else {
            return Platform.getModId(is);
        }
    }

    public static String getItemDisplayName(final Object o) {
        if (o instanceof IAEItemStack is) {
            if (cache.containsKey(is) && cache.get(is).name != null) {
                return cache.get(is).name;
            } else if (is.getItem() instanceof ItemFluidDrop) {
                String name = Util.getDisplayName(is);
                putCache(is, null, name, null);
                return name;
            }
        }
        return Platform.getItemDisplayName(o);
    }

    private static void putCache(IAEItemStack is, String modId, String name, List<String> tooltip) {
        if (!cache.containsKey(is)) {
            cache.putIfAbsent(is, new ItemInfo(modId, name, tooltip));
        } else {
            ItemInfo info = cache.get(is);
            if (info.modId == null) info.modId = modId;
            if (info.name == null) info.name = name;
            if (info.tooltip == null) info.tooltip = tooltip;
        }
    }

    public static List<String> getTooltip(final Object o) {
        if (o instanceof IAEItemStack is) {
            if (cache.containsKey(is) && cache.get(is).tooltip != null) {
                return cache.get(is).tooltip;
            } else if (is.getItem() instanceof ItemFluidDrop) {
                FluidStack fs = ItemFluidDrop.getFluidStack(is.getItemStack());
                if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fs)) {
                    Aspect aspect = AspectUtil.getAspectFromGas(fs);
                    List<String> tooltip = ItemCraftingAspect.createStackForAspect(aspect, 1)
                        .getTooltip(Minecraft.getMinecraft().thePlayer, false);
                    putCache(is, null, null, tooltip);
                    return tooltip;
                }
            }
        }

        return Platform.getTooltip(o);
    }
}
