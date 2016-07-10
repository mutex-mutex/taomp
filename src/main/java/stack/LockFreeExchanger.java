package stack;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class LockFreeExchanger<T> {

    static final int EMPTY = 0, WAITING = 1, BUSY = 2;
    AtomicStampedReference<T> slot = new AtomicStampedReference<>(null, EMPTY);

    public T exchange(T myItem, long timeout, TimeUnit unit) throws TimeoutException {
        long nanos = unit.toNanos(timeout);
        long timeBound = System.nanoTime() + nanos;
        int[] stampHolder = {EMPTY};
        while(true) {
            if(System.nanoTime() > timeBound) {
                throw new TimeoutException();
            }
            T yrItem = slot.get(stampHolder);
            int stamp = stampHolder[0];
            switch(stamp) {
                case EMPTY:
                    if(slot.compareAndSet(yrItem, myItem, EMPTY, WAITING)) {
                        while (System.nanoTime() < timeBound) {
                            yrItem = slot.get(stampHolder);
                            if (stampHolder[0] == BUSY) {
                                slot.set(null, EMPTY);
                                return yrItem;
                            }
                        }
                        if (slot.compareAndSet(myItem, null, WAITING, EMPTY)) {
                            throw new TimeoutException();
                        } else {
                            yrItem = slot.get(stampHolder);
                            slot.set(null, EMPTY);
                            return yrItem;
                        }
                    }
                case WAITING:
                    if(slot.compareAndSet(yrItem, myItem, WAITING, BUSY)) {
                        return yrItem;
                    }
                case BUSY:
                    break;
                default:
                    //NOP
            }
        }
    }

    public static void main(String[] args) {
        LockFreeExchanger<Integer> exchanger = new LockFreeExchanger<>();

        Thread t = new Thread(
                () -> {
                    for(int i = 0; i < 100; i++) {
                        try {
                            int exchanged = exchanger.exchange(i, 1, TimeUnit.MINUTES);
                            System.out.println(
                                    "t1:Exchanged " + i + " to " + exchanged
                            );
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        t.start();


        Thread t2 = new Thread(
                () -> {
                    for(int i = 200; i < 300; i++) {
                        try {
                            int exchanged = exchanger.exchange(i, 1, TimeUnit.MINUTES);
                            System.out.println(
                                    "t2:Echanged " + i + " to " + exchanged
                            );
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        t2.start();
    }
}
