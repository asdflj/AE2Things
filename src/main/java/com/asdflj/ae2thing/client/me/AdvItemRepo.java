package com.asdflj.ae2thing.client.me;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.util.Ae2ReflectClient;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;

public class AdvItemRepo extends ItemRepo {

    private static final BlockingQueue<AdvItemRepo> IN = new LinkedBlockingQueue<>(1);
    private static final BlockingQueue<Boolean> OUT = new LinkedBlockingQueue<>(1);

    protected final ArrayList<IAEItemStack> view = Ae2ReflectClient.getView(this);
    protected final ArrayList<ItemStack> dsp = Ae2ReflectClient.getDsp(this);
    protected AdvItemRepo repo;

    public AdvItemRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
    }

    public AdvItemRepo(ISortSource sortSrc) {
        super(null, sortSrc);
    }

    public void setCache(AdvItemRepo repo) {
        this.repo = repo;
        this.repo.setPowered(true);
    }

    public boolean hasCache() {
        return repo != null;
    }

    public boolean flush() {
        if (hasCache() && OUT.isEmpty()) {
            OUT.poll();
            this.view.clear();
            this.dsp.clear();
            this.view.ensureCapacity(this.repo.view.size());
            this.dsp.ensureCapacity(this.repo.dsp.size());
            this.view.addAll(this.repo.view);
            this.dsp.addAll(this.repo.dsp);
            return true;
        }
        return false;
    }

    @Override
    public void setSearchString(@Nonnull String searchString) {
        if (this.hasCache()) {
            repo.setSearchString(searchString);
        }
        super.setSearchString(searchString);
    }

    @Override
    public void setViewCell(ItemStack[] list) {
        if (this.hasCache()) {
            repo.setViewCell(list);
        }
        super.setViewCell(list);
    }

    @Override
    public void postUpdate(IAEItemStack is) {
        if (this.hasCache()) {
            this.repo.postUpdate(is);
        }
        super.postUpdate(is);
    }

    private void setAsEmpty(int i) {
        this.view.add(i, null);
        this.dsp.add(i, null);
    }

    @Override
    public void updateView() {
        if (this.hasCache()) {
            IN.offer(this.repo);
        } else {
            super.updateView();
            this.setPinItems();
        }
    }

    private static final Thread updateViewThread = new Thread(() -> {
        while (true) {
            if (!IN.isEmpty()) {
                AdvItemRepo advItemRepo = IN.poll();
                advItemRepo.updateView();
                OUT.offer(true);
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }, "updateViewThread");

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

    static {
        updateViewThread.start();
    }
}
