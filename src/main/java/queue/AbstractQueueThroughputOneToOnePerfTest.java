package queue;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sun.misc.Contended;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public abstract class AbstractQueueThroughputOneToOnePerfTest {

    protected static final int TEST_SIZE = 1000000;

    public abstract void recreateQueue();

    public abstract void addImpl(int x);

    public abstract int getImpl();

    @Contended
    private int[] elementsToAdd;
    @Contended
    private int addIndex;

    @Setup(Level.Iteration)
    public void setUp() {
        recreateQueue();
        addIndex = 0;
        elementsToAdd = new int[1024];

        Random r = new Random(42);
        for(int i = 0; i < elementsToAdd.length; i++) {
            elementsToAdd[i] = r.nextInt(10240);
        }

        System.gc();
        System.gc();
    }

    @GroupThreads
    @Group("queuetest")
    @Benchmark
    @Warmup(iterations = 5, batchSize = 20000)
    @Measurement(iterations = 10, batchSize = TEST_SIZE)
    @BenchmarkMode(Mode.SingleShotTime)
    public void put() throws InterruptedException {
        addImpl(elementsToAdd[++addIndex % 1024]);
    }

    @GroupThreads
    @Group("queuetest")
    @Benchmark
    @Warmup(iterations = 5, batchSize = 20000)
    @Measurement(iterations = 10, batchSize = TEST_SIZE)
    @BenchmarkMode(Mode.SingleShotTime)
    public int get() throws InterruptedException {
        return getImpl();
    }

    public void run() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + this.getClass().getSimpleName() + ".*")
                .forks(1)
                .jvmArgsAppend("-Xmx3G")
                .jvmArgsAppend("-server")
                .jvmArgs("-XX:-TieredCompilation")
                .jvmArgsAppend("-XX:-RestrictContended")
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        for(RunResult result : results) {
            double througputSampleTime = result.getPrimaryResult().getScore();
            System.out.println("speed=" + (1000.0/througputSampleTime)*TEST_SIZE + " per sec");
        }
    }
}
