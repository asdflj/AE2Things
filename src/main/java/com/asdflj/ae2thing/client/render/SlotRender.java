package com.asdflj.ae2thing.client.render;

import java.util.Collection;
import java.util.HashMap;

import com.asdflj.ae2thing.util.ModAndClassUtil;

public class SlotRender {

    private static final HashMap<Class<? extends ISlotRender>, ISlotRender> renders = new HashMap<>();
    private static final SlotRender API = new SlotRender();

    private SlotRender() {
        registerSlotRenderHandler(RenderFluidDrop.class, new RenderFluidDrop());
        registerSlotRenderHandler(RenderFluidPacketPatternSlot.class, new RenderFluidPacketPatternSlot());
        if (ModAndClassUtil.THE) {
            registerSlotRenderHandler(RenderItemPhial.class, new RenderItemPhial());
        }
    }

    public static SlotRender instance() {
        return API;
    }

    public void registerSlotRenderHandler(Class<? extends ISlotRender> cls, ISlotRender render) {
        renders.putIfAbsent(cls, render);
    }

    public Collection<ISlotRender> getRenders() {
        return renders.values();
    }

}
