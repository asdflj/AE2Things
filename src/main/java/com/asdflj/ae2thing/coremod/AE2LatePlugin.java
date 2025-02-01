package com.asdflj.ae2thing.coremod;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class AE2LatePlugin implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.ae2thing.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Lists.newArrayList("MixinAEBaseGui", "MixinGuiCraftConfirm", "MixinItemRepo");
    }

}
