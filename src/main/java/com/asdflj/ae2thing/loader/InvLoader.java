package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.InvUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;

public class InvLoader implements Runnable {

    @Override
    public void run() {
        InvUtil.INVENTORY.add(player -> player.inventory);
        if (ModAndClassUtil.BAUBLES) {
            InvUtil.INVENTORY.add(BaublesUtil::getBaublesInv);
        }
    }
}
