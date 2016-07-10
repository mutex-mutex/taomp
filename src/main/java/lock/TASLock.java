package lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class TASLock {

    private AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while(state.getAndSet(true)) {}
    }

    public void unlock() {
        state.set(false);
    }

    private static long counter = 0L;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> list = new ArrayList<>();

        final TASLock lock = new TASLock();

        for(int i = 0; i < 10; i++) {
            list.add(
                    executorService.submit(
                            () -> {
                                for(int z = 0; z < 1000000; z++) {
                                    lock.lock();
                                    counter++;
                                    lock.unlock();
                                }
                            }
                    )
            );
        }
    }
}
