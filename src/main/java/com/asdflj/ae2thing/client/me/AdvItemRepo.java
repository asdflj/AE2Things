package com.asdflj.ae2thing.client.me;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.util.Ae2ReflectClient;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;

public class AdvItemRepo extends ItemRepo {

    public static List<IAEItemStack> pinItems = new ArrayList<>();
    protected final ArrayList<IAEItemStack> view;
    private final ArrayList<ItemStack> dsp;

    public AdvItemRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
        this.dsp = Ae2ReflectClient.getDsp(this);
        this.view = Ae2ReflectClient.getView(this);
    }

    private void setAsEmpty(int i) {
        this.view.add(i, null);
        this.dsp.add(i, null);
    }

    public static void addPinItems(IAEItemStack is) {
        if (!pinItems.remove(is)) {
            pinItems.add(is);
            if (pinItems.size() > 9) {
                pinItems = pinItems.subList(pinItems.size() - 9, pinItems.size());
            }
        }
    }

    @Override
    public void postUpdate(IAEItemStack is) {
        super.postUpdate(is);
        List<IAEItemStack> tmp = this.view.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        this.view.clear();
        this.view.addAll(tmp);
    }

    @Override
    public void updateView() {
        super.updateView();
        if (!pinItems.isEmpty()) {
            for (int i = 0; i < 9; i++) {
                if (i >= pinItems.size()) {
                    this.setAsEmpty(i);
                    continue;
                }
                IAEItemStack is = pinItems.get(i);
                int idx = this.view.indexOf(is);
                if (idx != -1) {
                    this.view.remove(is);
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
