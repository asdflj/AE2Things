package com.asdflj.ae2thing.client.me;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.util.Ae2ReflectClient;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;

public class AdvItemRepo extends ItemRepo {

    protected final ArrayList<IAEItemStack> view = Ae2ReflectClient.getView(this);
    protected final ArrayList<ItemStack> dsp = Ae2ReflectClient.getDsp(this);

    public AdvItemRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
    }

    private void setAsEmpty(int i) {
        this.view.add(i, null);
        this.dsp.add(i, null);
    }

    @Override
    public void updateView() {
        super.updateView();
        this.setPinItems();
    }

    protected void setPinItems() {
        final List<IAEItemStack> pinItems = AE2ThingAPI.instance()
            .getPinItems();
        if (!pinItems.isEmpty()) {
            for (int i = 0; i < AE2ThingAPI.maxPinSize; i++) {
                if (i >= pinItems.size()) {
                    this.setAsEmpty(i);
                    continue;
                }
                IAEItemStack is = pinItems.get(i);
                int idx = this.view.indexOf(is);
                if (idx != -1) {
                    this.view.remove(idx);
                    this.dsp.remove(idx);
                    this.view.add(i, is);
                    this.dsp.add(i, is.getItemStack());
                    continue;
                }
                this.setAsEmpty(i);
            }
        }
    }

}
