package com.chibik.taomp.hash;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseGrainedHashSet<T> extends BaseHashSet<T> {

    private Lock lock;

    public CoarseGrainedHashSet(int capacity) {
        super(capacity);
        lock = new ReentrantLock();
    }

    @Override
    protected boolean policy() {
        return (setSize / table.length) > 4;
    }

    @Override
    protected void resize() {
        int oldCapacity = table.length;
        lock.lock();
        try {

            if(oldCapacity != table.length) {
                return;
            }
            int newCapacity = 2 * oldCapacity;
            List<T>[] oldTable = table;
            table = (List<T>[]) new List[newCapacity];
            for(int i = 0; i < newCapacity; i++) {
                table[i] = new ArrayList<>();
            }
            for(List<T> bucket : oldTable) {
                for(T x : bucket) {
                    table[x.hashCode() % table.length].add(x);
                }
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void acquire(T x) {
        lock.lock();
    }

    @Override
    public void release(T x) {
        lock.unlock();
    }

    public static void main(String[] args) throws Exception {
        CoarseGrainedHashSet<Integer> set = new CoarseGrainedHashSet<Integer>(16);

        final int count = 2_000_000;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        long startMillis = System.currentTimeMillis();
        Future<?> f1 = executorService.submit(
                () -> {
                    for(int i = 0; i < count; i++) {
                        set.add(i);
                    }
                }
        );
        Future<?> f2 = executorService.submit(
                () -> {
                    int a = 0;
                    for(int i = count; i >= 0; i--) {
                        a ^= set.contains(i) ? 1 : 13;
                    }
                    System.out.println("result = " + a);
                }
        );
        f1.get();
        f2.get();
        long elapsed = System.currentTimeMillis() - startMillis;

        System.out.println(
                "Time=" + elapsed + "ms, op count = " +
                count + " add, " + count + " contains"
        );
    }
}
