package queue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchQueue<T> {

    private boolean enq = false;
    private Lock lock = new ReentrantLock(false);
    private T item = null;
    private Condition condition = lock.newCondition();

    public void enq(T x) {
        lock.lock();
        try {
            while(enq) {
                condition.await();
            }
            enq = true;
            item = x;
            condition.signalAll();

            while(item != null) {
                condition.await();
            }
            enq = false;
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public T deq() {
        lock.lock();
        try {
            while(item == null) {
                condition.await();
            }

            T val = item;
            item = null;

            condition.signalAll();

            return val;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SynchQueue<Integer> queue = new SynchQueue<>();

        final long startMillis = System.currentTimeMillis();

        Thread t = new Thread(
                () -> {
                    for(int i = 0; i < 10; i++) {
                        queue.enq(i);
                        System.out.println(startMillis - System.currentTimeMillis() + ";" + "enq " + i);
                    }
                }
        );
        t.start();

        Thread t2 = new Thread(
                () -> {
                    for(int i = 0; i < 10; i++) {
                        ThreadLocalRandom r = ThreadLocalRandom.current();
                        int nextWait = r.nextInt(3000);

                        try {
                            Thread.sleep(nextWait);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Integer val = queue.deq();

                        System.out.println(startMillis - System.currentTimeMillis() + ";" + "deq " + val);
                    }
                }
        );
        t2.start();

        Thread.sleep(10*2000L);
    }
}
