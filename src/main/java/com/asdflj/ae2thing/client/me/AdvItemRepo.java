package com.asdflj.ae2thing.client.me;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.GuiMonitor;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.util.Ae2ReflectClient;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;

public class AdvItemRepo extends ItemRepo implements Runnable {

    private static final int SIZE = 1;
    private static final int DELAY = 3;

    private static final BlockingQueue<Runnable> IN = new LinkedBlockingQueue<>(SIZE);
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(
        SIZE,
        SIZE,
        60,
        TimeUnit.SECONDS,
        IN,
        r -> new Thread(r, "AE2 Thing repo sort thread"),
        new RejectedExecutionHandler() {

            private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(SIZE);

            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                scheduledThreadPool.schedule(() -> pool.execute(r), DELAY, TimeUnit.SECONDS);
            }
        });

    protected final ArrayList<IAEItemStack> view = Ae2ReflectClient.getView(this);
    protected final ArrayList<ItemStack> dsp = Ae2ReflectClient.getDsp(this);
    protected AdvItemRepo repo;
    protected final Set<IAEItemStack> cache = Collections.synchronizedSet(new HashSet<IAEItemStack>());
    protected GuiMonitor gui;
    private static final Lock lock = new ReentrantLock();

    public AdvItemRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
    }

    public AdvItemRepo(ISortSource sortSrc) {
        super(null, sortSrc);
    }

    public Lock getLock() {
        return lock;
    }

    public void setCache(GuiMonitor gui) {
        if (Config.updateViewThread) {
            this.repo = new AdvItemRepo(gui);
            this.repo.setPowered(true);
            this.gui = gui;
        }
    }

    public boolean hasCache() {
        return repo != null;
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
            lock.lock();
            this.cache.remove(is);
            this.cache.add(is);
            lock.unlock();
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
            try {
                pool.execute(this);
            } catch (Exception ignored) {

            }
        } else {
            super.updateView();
            this.setPinItems();
        }
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

    @Override
    public void run() {
        try {
            lock.lock();
            for (IAEItemStack is : this.cache) {
                this.repo.postUpdate(is);
            }
            this.cache.clear();
        } finally {
            lock.unlock();
        }
        this.repo.updateView();
        try {
            lock.lock();
            this.view.clear();
            this.dsp.clear();
            this.view.ensureCapacity(this.repo.view.size());
            this.dsp.ensureCapacity(this.repo.dsp.size());
            this.view.addAll(this.repo.view);
            this.dsp.addAll(this.repo.dsp);
            this.gui.setScrollBar();
        } finally {
            lock.unlock();
        }
    }
}
