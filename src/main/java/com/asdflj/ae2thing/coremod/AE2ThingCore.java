package com.asdflj.ae2thing.coremod;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("AE2ThingCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("com.asdflj.ae2thing.coremod")
public class AE2ThingCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static boolean DEV_ENVIRONMENT;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { AE2ThingCore.class.getPackage()
            .getName() + ".ClassTransformer" };
    }

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
        DEV_ENVIRONMENT = !(boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.ae2thing.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Lists.newArrayList();
    }

    public static boolean isDevEnv() {
        return DEV_ENVIRONMENT;
    }
}
