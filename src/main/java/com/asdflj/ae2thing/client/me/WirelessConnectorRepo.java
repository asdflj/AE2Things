package com.asdflj.ae2thing.client.me;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.me.ItemRepo;
import appeng.util.Platform;
import appeng.util.item.OreHelper;
import appeng.util.item.OreReference;
import com.asdflj.ae2thing.util.Info;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WirelessConnectorRepo implements IDisplayRepo {
    private final IScrollSource src;
    private final List<Info> infos = new ArrayList<>();
    private final List<Info> dsp = new ArrayList<>();
    private int rowSize = 9;
    private String searchString = "";

    public WirelessConnectorRepo(final IScrollSource src) {
        this.src = src;
    }

    @Override
    public void postUpdate(Info info) {
        this.infos.add(info);
    }

    @Override
    public Info getInfo(int idx) {
        idx += this.src.getCurrentScroll();
        if (idx >= this.dsp.size()) {
            return null;
        }
        return this.dsp.get(idx);
    }

    @Override
    public void updateView() {
        this.dsp.clear();
        String innerSearch = this.searchString;
        final SearchMode searchWhat;
        if (innerSearch.isEmpty()) {
            searchWhat = SearchMode.NAME;
        } else {
            searchWhat = switch (innerSearch.substring(0, 1)) {
                case "#" -> SearchMode.POS;
                case "$" -> SearchMode.COLOR;
                default -> SearchMode.NAME;
            };
            if (searchWhat != SearchMode.NAME) innerSearch = innerSearch.substring(1);
        }
        Pattern m;
        try {
            m = Pattern.compile(innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
        } catch (final Throwable ignore) {
            try {
                m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
            } catch (final Throwable __) {
                return;
            }
        }
        for (final Info info : this.infos) {
            String dspName;
            switch (searchWhat) {
                case POS -> dspName = info.getPosString();
                case COLOR -> dspName = info.getColor();
                default -> dspName = info.getName();
            }
            if (dspName == null) continue;
            if (m.matcher(dspName.toLowerCase()).find()) {
                this.dsp.add(info);
            }
        }

    }

    @Override
    public int size() {
        return this.infos.size();
    }

    @Override
    public void clear() {
        this.infos.clear();
    }

    @Override
    public int getRowSize() {
        return this.rowSize;
    }

    @Override
    public void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    @Override
    public String getSearchString() {
        return "";
    }

    @Override
    public void setSearchString(@NotNull String searchString) {
        this.searchString = searchString;
    }
    enum SearchMode {
        NAME,POS,COLOR
    }
}
