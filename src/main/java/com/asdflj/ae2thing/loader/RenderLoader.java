package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.client.render.ItemFishBigRender;

public class RenderLoader implements Runnable {

    @Override
    public void run() {
        new ItemFishBigRender();
    }
}
