package lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class MCSLock {

    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myNode;

    public MCSLock() {
        tail = new AtomicReference<>();
        myNode = new ThreadLocal<QNode>() {
            @Override
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    public void lock() {
        QNode node = myNode.get();
        QNode pred = tail.getAndSet(node);
        if(pred != null) {
            node.locked = true;
            pred.next = node;
            while(node.locked) {}
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

    private class QNode {
        volatile boolean locked = false;
        volatile QNode next = null;
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
