package queue;

import org.openjdk.jmh.runner.RunnerException;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentLinkedQueuePerfTest extends AbstractQueueThroughputOneToOnePerfTest {

    private ConcurrentLinkedQueue<Integer> queue;

    @Override
    public void recreateQueue() {
        queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void addImpl(int x) {
        queue.offer(x);
    }

    @Override
    public int getImpl() {
        Integer val;
        while((val = queue.poll()) == null) {

        }
        return val;
    }

    public static void main(String[] args) throws RunnerException {
        new ConcurrentLinkedQueuePerfTest().run();
    }
}
