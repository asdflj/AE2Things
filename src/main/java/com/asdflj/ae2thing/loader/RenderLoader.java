package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.client.render.ItemDollRender;

public class RenderLoader implements Runnable {

    @Override
    public void run() {
        new ItemDollRender(ItemAndBlockHolder.BLOCK_FISHBIG);
        new ItemDollRender(ItemAndBlockHolder.BLOCK_MDDyue);
    }
}
