package com.asdflj.ae2thing.client.me;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;

import appeng.api.AEApi;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.IItemDisplayRegistry;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.item.OreHelper;
import appeng.util.item.OreReference;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class AdvItemRepo implements IDisplayRepo {

    protected final IItemList<IAEItemStack> list = AEApi.instance()
        .storage()
        .createItemList();
    protected final ArrayList<IAEItemStack> view = new ArrayList<>();
    protected final ArrayList<ItemStack> dsp = new ArrayList<>();
    protected final ArrayList<IAEItemStack> cache = new ArrayList<>();
    protected final Map<IAEItemStack, Integer> itemInView = new HashMap<>();
    protected final IScrollSource src;
    protected final ISortSource sortSrc;

    protected int rowSize = 9;

    protected String searchString = "";
    protected String lastSearchString = "";
    protected String NEIWord = null;
    protected boolean hasPower;

    public AdvItemRepo(IScrollSource src, ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    private void setAsEmpty(int i) {
        this.view.add(i, null);
        this.dsp.add(i, null);
    }

    @Override
    public IAEItemStack getReferenceItem(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    @Override
    public ItemStack getItem(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.dsp.size()) {
            return null;
        }
        return this.dsp.get(idx);
    }

    @Override
    public void postUpdate(final IAEItemStack is) {
        final IAEItemStack st = this.list.findPrecise(is);
        if (st != null) {
            st.reset();
            st.add(is);
            if (isShiftKeyDown()) {
                Optional<Integer> idx = Optional.ofNullable(this.itemInView.get(st));
                idx.ifPresent(
                    i -> this.view.get(i)
                        .setStackSize(st.getStackSize()));
            }
        } else {
            if (isShiftKeyDown()) this.cache.add(is);
            this.list.add(is);
        }
        if (AE2ThingAPI.instance()
            .getPinItems()
            .isEmpty()) return;
        List<IAEItemStack> tmp = this.view.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        this.view.clear();
        this.view.addAll(tmp);
    }

    @Override
    public void setViewCell(final ItemStack[] list) {
        this.updateView();
    }

    protected boolean needUpdateView() {
        return !isShiftKeyDown() || !this.lastSearchString.equals(this.searchString);
    }

    @Override
    public void updateView() {
        if (needUpdateView()) this.view.clear();
        this.dsp.clear();

        this.view.ensureCapacity(this.list.size());
        this.dsp.ensureCapacity(this.list.size());

        final Enum viewMode = this.sortSrc.getSortDisplay();
        final Enum searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        final Enum typeFilter = this.sortSrc.getTypeFilter();
        if (searchMode == SearchBoxMode.NEI_AUTOSEARCH || searchMode == SearchBoxMode.NEI_MANUAL_SEARCH) {
            this.updateNEI(this.searchString);
        }

        String innerSearch = this.searchString;
        // final boolean terminalSearchToolTips =
        // AEConfig.instance.settings.getSetting(Settings.SEARCH_TOOLTIPS) != YesNo.NO;
        // boolean terminalSearchMods = Configuration.INSTANCE.settings.getSetting( Settings.SEARCH_MODS ) != YesNo.NO;
        final SearchMode searchWhat;
        if (innerSearch.length() == 0) {
            searchWhat = SearchMode.ITEM;
        } else {
            searchWhat = switch (innerSearch.substring(0, 1)) {
                case "#" -> SearchMode.TOOLTIPS;
                case "@" -> SearchMode.MOD;
                case "$" -> SearchMode.ORE;
                default -> SearchMode.ITEM;
            };
            if (searchWhat != SearchMode.ITEM) innerSearch = innerSearch.substring(1);
        }
        Pattern m = null;
        try {
            m = Pattern.compile(innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
        } catch (final Throwable ignore) {
            try {
                m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
            } catch (final Throwable __) {
                return;
            }
        }
        IItemDisplayRegistry registry = AEApi.instance()
            .registries()
            .itemDisplay();

        boolean notDone = false;
        for (IAEItemStack is : needUpdateView() ? this.list : this.cache) {
            // filter AEStack type
            IAEItemStack finalIs = is;
            if (registry.isBlacklisted(finalIs.getItem()) || registry.isBlacklisted(
                finalIs.getItem()
                    .getClass())) {
                continue;
            }

            if (viewMode == ViewItems.STORED && is.getStackSize() == 0) {
                continue;
            }
            String dspName = null;
            switch (searchWhat) {
                case MOD -> dspName = Platform.getModId(is);
                case ORE -> {
                    OreReference ore = OreHelper.INSTANCE.isOre(is.getItemStack());
                    if (ore != null) {
                        dspName = String.join(" ", ore.getEquivalents());
                    }
                }
                case TOOLTIPS -> dspName = String.join(" ", ((List<String>) Platform.getTooltip(is)));
                default -> dspName = Platform.getItemDisplayName(is);
            }

            if (dspName == null) continue;

            notDone = true;
            if (m.matcher(dspName.toLowerCase())
                .find()) {
                this.view.add(is);
                notDone = false;
            }

            if (notDone && searchWhat == SearchMode.ITEM) {
                for (final Object lp : Platform.getTooltip(is)) {
                    if (lp instanceof String && m.matcher((CharSequence) lp)
                        .find()) {
                        this.view.add(is);
                        notDone = false;
                        break;
                    }
                }
            }
        }
        if (needUpdateView()) {
            final Enum SortBy = this.sortSrc.getSortBy();
            final Enum SortDir = this.sortSrc.getSortDir();

            ItemSorters.setDirection((appeng.api.config.SortDir) SortDir);
            ItemSorters.init();

            if (SortBy == SortOrder.MOD) {
                this.view.sort(ItemSorters.CONFIG_BASED_SORT_BY_MOD);
            } else if (SortBy == SortOrder.AMOUNT) {
                this.view.sort(ItemSorters.CONFIG_BASED_SORT_BY_SIZE);
            } else if (SortBy == SortOrder.INVTWEAKS) {
                this.view.sort(ItemSorters.CONFIG_BASED_SORT_BY_INV_TWEAKS);
            } else {
                this.view.sort(ItemSorters.CONFIG_BASED_SORT_BY_NAME);
            }
        } else {
            this.cache.clear();
        }

        this.itemInView.clear();

        for (int i = 0; i < this.view.size(); i++) {
            final IAEItemStack is = this.view.get(i);
            this.dsp.add(is.getItemStack());
            this.itemInView.put(is, i);
        }

        this.lastSearchString = this.searchString;
        this.setPinItems();
    }

    protected void setPinItems() {
        final List<IAEItemStack> pinItems = AE2ThingAPI.instance()
            .getPinItems();
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

    protected void updateNEI(final String filter) {
        try {
            if (this.NEIWord == null || !this.NEIWord.equals(filter)) {
                final Class c = ReflectionHelper.getClass(
                    this.getClass()
                        .getClassLoader(),
                    "codechicken.nei.LayoutManager");
                final Field fldSearchField = c.getField("searchField");
                final Object searchField = fldSearchField.get(c);

                final Method a = searchField.getClass()
                    .getMethod("setText", String.class);
                final Method b = searchField.getClass()
                    .getMethod("onTextChange", String.class);

                this.NEIWord = filter;
                a.invoke(searchField, filter);
                b.invoke(searchField, "");
            }
        } catch (final Throwable ignore) {

        }
    }

    @Override
    public int size() {
        return this.view.size();
    }

    @Override
    public void clear() {
        this.list.resetStatus();
    }

    @Override
    public boolean hasPower() {
        return this.hasPower;
    }

    @Override
    public void setPowered(final boolean hasPower) {
        this.hasPower = hasPower;
    }

    @Override
    public int getRowSize() {
        return this.rowSize;
    }

    @Override
    public void setRowSize(final int rowSize) {
        this.rowSize = rowSize;
    }

    @Override
    public String getSearchString() {
        return this.searchString;
    }

    @Override
    public void setSearchString(@Nonnull final String searchString) {
        this.searchString = searchString;
    }

    protected enum SearchMode {
        MOD,
        TOOLTIPS,
        ORE,
        ITEM
    }
}
