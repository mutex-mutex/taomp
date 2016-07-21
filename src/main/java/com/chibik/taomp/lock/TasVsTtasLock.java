package com.chibik.taomp.lock;

import org.openjdk.jmh.annotations.*;
import com.chibik.taomp.support.RunBenchmark;

import java.util.concurrent.atomic.AtomicBoolean;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class TasVsTtasLock {

    public static final int THREAD_COUNT = 10;

    private AtomicBoolean state;

    private long counter = 0L;

    @Setup
    public void setUp() {
        state = new AtomicBoolean(false);
        counter = 0L;
    }

    public void lockTTAS() {
        while(true) {
            while(state.get()) {}
            if(!state.getAndSet(true)) {
                return;
            }
        }
    }

    public void unlockTTAS() {
        state.set(false);
    }

    @Benchmark
    @Group(value = "ttas")
    @GroupThreads(value = THREAD_COUNT)
    public void incrementTtas() {
        lockTTAS();
        try {

            counter++;
        } finally {
            unlockTTAS();
        }
    }

    public void lockTAS() {
        while(state.getAndSet(true)) {}
    }

    public void unlockTAS() {
        state.set(false);
    }

    @Benchmark
    @Group(value = "tas")
    @GroupThreads(value = THREAD_COUNT)
    public void incrementTas() {
        lockTAS();
        try {

            counter++;
        } finally {
            unlockTAS();
        }
    }

    public static void main(String[] args) {
        RunBenchmark.runSimple(TasVsTtasLock.class);
    }
}
