package queue;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LinkedBlockingQueueTest {

    private static final int TEST_SIZE = 1000000;

    private int[] elementsToAdd;
    private int addIndex;
    private LinkedBlockingQueue<Integer> queue;

    @Setup(Level.Iteration)
    public void setUp() {
        queue = new LinkedBlockingQueue<>();
        addIndex = 0;
        elementsToAdd = new int[1024];

        Random r = new Random(42);
        for(int i = 0; i < elementsToAdd.length; i++) {
            elementsToAdd[i] = r.nextInt(10240);
        }
    }

    @GroupThreads(1)
    @Group("linkedblockingqueuetest")
    @Benchmark
    @Warmup(iterations = 5, batchSize = 20000)
    @Measurement(iterations = 10, batchSize = TEST_SIZE)
    @BenchmarkMode(Mode.SingleShotTime)
    public void add() throws InterruptedException {
        queue.put(elementsToAdd[++addIndex % 1024]);
    }

    @GroupThreads(1)
    @Group("linkedblockingqueuetest")
    @Benchmark
    @Warmup(iterations = 5, batchSize = 20000)
    @Measurement(iterations = 10, batchSize = TEST_SIZE)
    @BenchmarkMode(Mode.SingleShotTime)
    public int get() throws InterruptedException {
        return queue.take();
    }

    //todo: logic to base class
    public void run() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + this.getClass().getSimpleName() + ".*")
                .forks(1)
                .jvmArgsAppend("-Xmx2G")
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        for(RunResult result : results) {
            double througputSampleTime = result.getPrimaryResult().getScore();
            System.out.println("speed=" + (1000.0/througputSampleTime)*TEST_SIZE + " per sec");
        }
    }

    public static void main(String[] args) throws RunnerException {
        new LinkedBlockingQueueTest().run();
    }
}
