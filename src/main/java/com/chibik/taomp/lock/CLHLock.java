package com.chibik.taomp.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/*
* TODO: NOT WORKING, FIX
* */
public class CLHLock {

    private AtomicReference<QNode> tail = new AtomicReference<>(new QNode());
    ThreadLocal<QNode> myPred;
    ThreadLocal<QNode> myNode;

    public CLHLock() {
        myNode = new ThreadLocal<QNode>() {
            @Override
            protected QNode initialValue() {
                return new QNode();
            }
        };

        myPred = new ThreadLocal<QNode>() {
            @Override
            protected QNode initialValue() {
                return null;
            }
        };
    }

    public void lock() {
        QNode qnode = myNode.get();
        qnode.locked = true;
        QNode pred = tail.getAndSet(qnode);
        myPred.set(pred);
        while (pred.locked) {}
    }

    public void unlock() {
        QNode qnode = myNode.get();
        qnode.locked = false;
        myNode.set(myPred.get());
    }

    public static class QNode {
        private volatile boolean locked;
        private volatile QNode pred;
    }


    private static long counter = 0L;

    public static void main(String[] args) {
        final int threadCount = 10;
        final int opCount = 1000000;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> list = new ArrayList<>();

        final CLHLock lock = new CLHLock();
        long startMillis = System.currentTimeMillis();

        for(int i = 0; i < threadCount; i++) {
            list.add(
                    executorService.submit(
                            () -> {
                                for(int z = 0; z < opCount; z++) {
                                    lock.lock();
                                    counter++;
                                    lock.unlock();
                                }
                            }
                    )
            );
        }

        list.forEach(x -> {
            try {
                x.get(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });

        long elapsedMillis = System.currentTimeMillis() - startMillis;

        System.out.println("Elapsed " + elapsedMillis + " ms, counter=" + counter);
    }
}
