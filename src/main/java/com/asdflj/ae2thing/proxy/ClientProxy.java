package com.asdflj.ae2thing.proxy;

import com.asdflj.ae2thing.nei.recipes.DefaultExtractorLoader;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import cpw.mods.fml.common.event.FMLLoadCompleteEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        super.onLoadComplete(event);
        if (ModAndClassUtil.NEI) {
            new DefaultExtractorLoader().run();
        }
    }
}
