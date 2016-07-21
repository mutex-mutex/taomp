package com.chibik.taomp.queue.impl;

import org.openjdk.jmh.runner.RunnerException;
import com.chibik.taomp.queue.AbstractQueueThroughputOneToOnePerfTest;
import sun.misc.Contended;

/*
* v1: 4609441, 4412290, 4408378, 4648851, 4583774, 4338387, 4235743, 4446673
* v2:
* */
public class SimpleNonblockingQueuePerfTest extends AbstractQueueThroughputOneToOnePerfTest {

    @Contended
    private SimpleNonblockingQueue<Integer> queue;

    @Override
    public void recreateQueue() {
        queue = new SimpleNonblockingQueue<>();
    }

    @Override
    public void addImpl(int x) {
        queue.enqueue(x);
    }

    @Override
    public int getImpl() {
        Integer val;
        while((val = queue.dequeue()) == null) {}
        return val;
    }

    public static void main(String[] args) throws RunnerException {
        new SimpleNonblockingQueuePerfTest().run();
    }
}
