package com.asdflj.ae2thing.coremod;

import java.util.Map;

import javax.annotation.Nullable;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("AE2ThingCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("com.asdflj.ae2thing.coremod")
public class AE2ThingCore implements IFMLLoadingPlugin {

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
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
