package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.client.ClientHelper;

public class ListenerLoader implements Runnable {

    @Override
    public void run() {
        ClientHelper.register();
    }
}
