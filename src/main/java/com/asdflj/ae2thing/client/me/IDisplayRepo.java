package com.asdflj.ae2thing.client.me;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.util.Info;

public interface IDisplayRepo {

    void postUpdate(Info info);

    Info getInfo(int idx);

    void updateView();

    int size();

    void clear();

    int getRowSize();

    void setRowSize(int rowSize);

    String getSearchString();

    void setSearchString(@NotNull String searchString);
}
