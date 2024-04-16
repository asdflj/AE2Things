package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import appeng.api.storage.IMEInventory;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.prioitylist.IPartitionList;

public class Ae2Reflect {

    private static final Field fAEPass_internal;
    private static final Field fAEInv_partitionList;

    static {
        try {
            fAEPass_internal = reflectField(MEPassThrough.class, "internal");
            fAEInv_partitionList = reflectField(MEInventoryHandler.class, "myPartitionList");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static Method reflectMethod(Class<?> owner, String name, Class<?>... paramTypes)
        throws NoSuchMethodException {
        Method m = owner.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m;
    }

    public static Field reflectField(Class<?> owner, String... names) throws NoSuchFieldException {
        Field f = null;
        for (String name : names) {
            try {
                f = owner.getDeclaredField(name);
                // IntelliJ misses that the exception is ignored and thus "f" can indeed be null.
                // noinspection ConstantValue
                if (f != null) break;
            } catch (NoSuchFieldException ignore) {}
        }
        if (f == null) throw new NoSuchFieldException("Can't find field from " + Arrays.toString(names));
        f.setAccessible(true);
        return f;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readField(Object owner, Field field) {
        try {
            return (T) field.get(owner);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read field: " + field);
        }
    }

    public static void writeField(Object owner, Field field, Object value) {
        try {
            field.set(owner, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write field: " + field);
        }
    }

    public static IPartitionList<?> getPartitionList(MEInventoryHandler<?> me) {
        return Ae2Reflect.readField(me, fAEInv_partitionList);
    }

    public static IMEInventory<?> getInternal(MEPassThrough<?> me) {
        return Ae2Reflect.readField(me, fAEPass_internal);
    }

}
