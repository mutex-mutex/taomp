package lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class CLHLock {

    private AtomicReference<QNode> tail = new AtomicReference<>(new QNode());
    private ThreadLocal<QNode> myNode = new ThreadLocal<QNode>(){
        @Override
        protected QNode initialValue() {
            return new QNode();
        }
    };
    private ThreadLocal<QNode> myPred = new ThreadLocal<QNode>(){
        @Override
        protected QNode initialValue() {
            return null;
        }
    };

    private class QNode {
        private volatile boolean locked = false;
    }

    public void lock() {
        QNode node = myNode.get();
        node.locked = true;
        QNode oldTail = tail.getAndSet(node);
        myPred.set(oldTail);
        while(oldTail.locked) {}
    }

    public void unlock() {
        QNode node = myNode.get();
        node.locked = false;
        myNode.set(myPred.get());
    }

    private static long counter = 0L;

    /*
    * NOT WORKING
    * */
    public static void main(String[] args) {
        final int threadCount = 10;
        final int opCount = 50000;

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
