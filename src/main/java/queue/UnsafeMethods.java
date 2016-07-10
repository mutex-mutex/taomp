package queue;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class UnsafeMethods {

    public static Unsafe u;
    private static long FIELD_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);

            u = (Unsafe) f.get(null);
            FIELD_OFFSET = u.objectFieldOffset(TestEntity.class.getDeclaredField("id"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    };

    private TestEntity testEntity = new TestEntity();

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Benchmark
    public void testVolatilePut() {
        long id = random.nextLong();

        u.putOrderedLong(testEntity, FIELD_OFFSET, id);
    }

    @Benchmark
    public void testVolatileStore() {
        long id = random.nextLong();

        testEntity.setId(id);
    }

    public void run() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + this.getClass().getSimpleName() + ".*")
                .forks(1)
                .warmupTime(TimeValue.seconds(3))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(3))
                .measurementIterations(3)
                .jvmArgsAppend("-Xmx2G")
                .build();

        new Runner(opt).run();
    }

    public static void main(String[] args) throws RunnerException {
        new UnsafeMethods().run();
    }

    public static class TestEntity {

        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}
