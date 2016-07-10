package sortedset;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SortedSetTester {

    private final ISortedSet<TestKeyed> sortedSet;
    private final ExecutorService readerExecutor;
    private final int opCount;

    public SortedSetTester(ISortedSet<TestKeyed> sortedSet,
                           int readerCount,
                           int opCount) {
        this.sortedSet = sortedSet;
        this.readerExecutor = Executors.newFixedThreadPool(readerCount);
        this.opCount = opCount;
    }

    public void run() {
        for(int i = 0; i < 50_000; i++) {
            sortedSet.add(new TestKeyed(i));
        }
        for(int i = 0; i < 50_000; i++) {
            if(!sortedSet.remove(new TestKeyed(i))) {
                throw new IllegalStateException(
                        "Could not remove " + i
                );
            }
        }

        for(int i = 0; i < 50_000; i++) {
            sortedSet.add(new TestKeyed(i));
        }
    }

    public static class Worker implements Runnable {

        private final ISortedSet<TestKeyed> sortedSet;
        private final int opCount;
        private final boolean add;
        private final CountDownLatch endLatch;

        public Worker(ISortedSet<TestKeyed> sortedSet,
                      int opCount,
                      boolean add,
                      CountDownLatch endLatch) {
            this.sortedSet = sortedSet;
            this.opCount = opCount;
            this.add = add;
            this.endLatch = endLatch;
        }

        @Override
        public void run() {

            for(int i = 0; i < opCount; i++) {
                if(add) {
                    sortedSet.add(new TestKeyed(i));
                } else {
                    sortedSet.remove(new TestKeyed(i));
                }
            }

            endLatch.countDown();
        }
    }

    public static void main(String[] args) {
        SortedSetTester set = new SortedSetTester(
                new CoarseGrainedList<>(), 10, 1_000_000
        );
        set.run();
    }
}
