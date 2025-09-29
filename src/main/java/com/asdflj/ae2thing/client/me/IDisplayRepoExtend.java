package com.asdflj.ae2thing.client.me;

import appeng.api.storage.data.IAEItemStack;

public interface IDisplayRepoExtend {

    void addEntriesToView(Iterable<IAEItemStack> entries);

    void setAdvRepoPause(boolean pause);
}
