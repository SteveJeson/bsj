package com.zdzc.collector.simulator.pool;

import java.util.concurrent.atomic.AtomicInteger;

public class IntegerFactory {
    private static class SingletonHolder {
        private static final AtomicInteger INSTANCE = new AtomicInteger();
    }

    private IntegerFactory(){}

    public static final AtomicInteger getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
