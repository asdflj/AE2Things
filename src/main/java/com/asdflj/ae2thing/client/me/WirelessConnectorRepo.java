package com.asdflj.ae2thing.client.me;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.client.gui.widget.Component;
import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NeCharUtil;
import com.asdflj.ae2thing.util.Util;

import appeng.api.util.DimensionalCoord;
import appeng.client.gui.widgets.IScrollSource;

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

    @SuppressWarnings("unchecked")
    @Override
    public void updateView() {
        if (Component.activeInfo != null) {
            boolean found = false;
            for (Info info : infos) {
                if (Component.activeInfo.equals(info)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Component.activeInfo = null;
            }
        }

        this.dsp.clear();
        final List<Info> tmp = new ArrayList<>();
        String innerSearch = this.searchString;
        this.dsp.addAll(infos);
        if (!innerSearch.isEmpty()) {
            String[] list = innerSearch.split(" ");
            for (String s : list) {
                ImmutablePair<SearchMode, String> result = SearchMode.searchWhat(s);
                if (result.right.isEmpty()) continue;
                Pattern m;
                try {
                    m = Pattern.compile(result.right.toLowerCase(), Pattern.CASE_INSENSITIVE);
                } catch (final Throwable ignore) {
                    try {
                        m = Pattern.compile(Pattern.quote(result.right.toLowerCase()), Pattern.CASE_INSENSITIVE);
                    } catch (final Throwable __) {
                        return;
                    }
                }
                Pattern finalM = m;
                tmp.addAll(
                    this.dsp.stream()
                        .filter(
                            i -> NeCharUtil.INSTANCE.matcher(
                                finalM,
                                result.left.getDisplayName(i)
                                    .toLowerCase()))
                        .collect(Collectors.toList()));
                this.dsp.clear();
                this.dsp.addAll(tmp);
                tmp.clear();
            }
        }
        this.dsp.sort(Comparator.comparing(Info::getName, String::compareToIgnoreCase));
        if (Component.activeInfo != null) {
            if (!dsp.contains(Component.activeInfo)) {
                dsp.add(0, Component.activeInfo);
            }
            if (Component.activeInfo.link) {
                DimensionalCoord b = Component.activeInfo.b;
                Optional<Info> info = this.infos.stream()
                    .filter(i -> i.a.equals(b))
                    .findFirst();
                if (info.isPresent() && !dsp.contains(info.get())) {
                    if (!this.dsp.isEmpty()) {
                        dsp.add(1, info.get());
                    } else {
                        dsp.add(0, info.get());
                    }
                }
            }

        }
    }

    @Override
    public int size() {
        return this.dsp.size();
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
        return this.searchString;
    }

    @Override
    public void setSearchString(@NotNull String searchString) {
        this.searchString = searchString;
    }

    public enum SearchMode {

        POS("#"),
        COLOR("~"),
        BIND("@b"),
        UNBIND("@u"),
        CLICKED("@c"),
        NAME("");

        private final String prefix;

        SearchMode(String prefix) {
            this.prefix = prefix;
        }

        public String getDisplayName(Info info) {
            if (this == NAME) {
                return info.getName();
            } else if (this == POS) {
                return info.getPosString();
            } else if (this == COLOR) {
                return info.getColor();
            } else if ((this == BIND && info.link) || (this == UNBIND && !info.link)) {
                return this.prefix;
            } else if (this == CLICKED && Component.activeInfo != null
                && (Util.isSameDimensionalCoord(Component.activeInfo.a, info.a)
                    || (Component.activeInfo.link && Util.isSameDimensionalCoord(Component.activeInfo.b, info.a)))) {
                        return this.prefix;
                    } else {
                        return "";
                    }
        }

        public String getPrefix() {
            return this.prefix;
        }

        public static ImmutablePair<SearchMode, String> searchWhat(String text) {
            for (SearchMode mode : SearchMode.values()) {
                if (mode == NAME) continue;
                Pattern m;
                try {
                    m = Pattern.compile("^" + mode.prefix.toLowerCase(), Pattern.CASE_INSENSITIVE);
                } catch (final Throwable ignore) {
                    try {
                        m = Pattern.compile(Pattern.quote("^" + mode.prefix.toLowerCase()), Pattern.CASE_INSENSITIVE);
                    } catch (final Throwable __) {
                        return new ImmutablePair<>(SearchMode.NAME, text);
                    }
                }
                if (m.matcher(text)
                    .find()) {
                    if (mode == UNBIND || mode == BIND || mode == CLICKED) {
                        return ImmutablePair.of(mode, text);
                    }
                    return new ImmutablePair<>(mode, text.substring(mode.prefix.length()));
                }
            }
            return new ImmutablePair<>(SearchMode.NAME, text);
        }
    }
}
