package com.chibik.taomp.queue;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sun.misc.Contended;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ArrayBlockingQueueTest {

    private static final int TEST_SIZE = 1000000;

    @Param({"" + TEST_SIZE, "" + (TEST_SIZE/2), "" + (TEST_SIZE/4), "" + (TEST_SIZE/10), "" + 1000, "" + 10})
    private int queueLength;
    @Contended
    private int[] elementsToAdd;
    @Contended
    private int addIndex;
    @Contended
    private ArrayBlockingQueue<Integer> queue;

    @Setup(Level.Iteration)
    public void setUp() {
        queue = new ArrayBlockingQueue<>(queueLength);
        addIndex = 0;
        elementsToAdd = new int[1024];

        Random r = new Random(42);
        for(int i = 0; i < elementsToAdd.length; i++) {
            elementsToAdd[i] = r.nextInt(10240);
        }
    }

    @GroupThreads(1)
    @Group("arrayblockingqueuetest")
    @Benchmark
    @Warmup(iterations = 5, batchSize = 20000)
    @Measurement(iterations = 5, batchSize = TEST_SIZE)
    @BenchmarkMode(Mode.SingleShotTime)
    public void add() throws InterruptedException {
        queue.put(elementsToAdd[++addIndex % 1024]);
    }

    @GroupThreads(1)
    @Group("arrayblockingqueuetest")
    @Benchmark
    @Warmup(iterations = 5, batchSize = 20000)
    @Measurement(iterations = 5, batchSize = TEST_SIZE)
    @BenchmarkMode(Mode.SingleShotTime)
    public int get() throws InterruptedException {
        return queue.take();
    }

    public void run() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + this.getClass().getSimpleName() + ".*")
                .forks(1)
                .jvmArgsAppend("-Xmx4G")
                .build();

        new Runner(opt).run();
    }

    public static void main(String[] args) throws RunnerException {
        new ArrayBlockingQueueTest().run();
    }
}
