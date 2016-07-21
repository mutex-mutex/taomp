package com.chibik.taomp.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


/*
* TODO: NOT WORKING, FIX
* */
public class MCSLock {

    private AtomicReference<QNode> tail;
    private ThreadLocal<QNode> myNode;

    public MCSLock() {
        tail = new AtomicReference<>(null);
        myNode = new ThreadLocal<QNode>() {
            @Override
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    public void lock() {
        QNode qnode = myNode.get();
        QNode pred = tail.getAndSet(qnode);
        if(pred != null) {
            qnode.locked = true;
            pred.next = qnode;

            while(qnode.locked) {}
        }
    }

    public void unlock() {
        QNode node = myNode.get();
        if(node.next == null) {
            if(tail.compareAndSet(node, null)) {
                return;
            }
            while(node.next == null) {}
        }
        node.next.locked = false;
        node.next = null;
    }

    public static class QNode {
        private volatile boolean locked;
        private volatile QNode next;
    }

    public static int counter = 0;

    public static void main(String[] args) {
        final int threadCount = 10;
        final int opCount = 50000;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> list = new ArrayList<>();

        final MCSLock lock = new MCSLock();
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
