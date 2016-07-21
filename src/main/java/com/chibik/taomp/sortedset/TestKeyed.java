package com.chibik.taomp.sortedset;

public class TestKeyed implements Keyed {

    private final int key;

    public TestKeyed(int key) {
        this.key = key;
    }

    public int key() {
        return key;
    }

    @Override
    public String toString() {
        return String.valueOf(key);
    }
}
