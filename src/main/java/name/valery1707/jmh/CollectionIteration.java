package name.valery1707.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class CollectionIteration {
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int size;

    @Param({"java.util.HashSet", "java.util.TreeSet", "java.util.ArrayList", "java.util.LinkedList"})
    public String clazz;

    private Collection<Integer> collection;

    @Setup
    @SuppressWarnings("unchecked")
    public void setup(
            BenchmarkParams params
    ) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<Collection<Integer>> clazz = (Class<Collection<Integer>>) Class.forName(this.clazz);
        collection = clazz.newInstance();
        for (int i = 0; i < size; i++) {
            collection.add(i);
        }
    }

    /**
     * Standard for-each without size cache
     *
     * @param hole Black hole
     */
    @Benchmark
    public void forWithoutCache(final Blackhole hole) {
        for (int i = 0; i < collection.size(); i++) {
            hole.consume(i);
        }
    }

    /**
     * Standard for-each without size cache
     *
     * @param hole Black hole
     */
    @Benchmark
    public void forWithCache(final Blackhole hole) {
        int i, size;
        for (i = 0, size = collection.size(); i < size; i++) {
            hole.consume(i);
        }
    }

    /**
     * Standard for-each without size cache
     *
     * @param hole Black hole
     */
    @Benchmark
    public void forEachJava5(final Blackhole hole) {
        for (Integer i : collection) {
            hole.consume(i);
        }
    }

    /**
     * Standard for-each without size cache
     *
     * @param hole Black hole
     */
    @Benchmark
    @SuppressWarnings("Convert2MethodRef")
    public void forEachJava8(final Blackhole hole) {
        collection.forEach(i -> hole.consume(i));
    }

    /**
     * Standard for-each without size cache
     *
     * @param hole Black hole
     */
    @Benchmark
    @SuppressWarnings("WhileLoopReplaceableByForEach")
    public void iterator(final Blackhole hole) {
        Iterator<Integer> iterator = collection.iterator();
        while (iterator.hasNext()) {
            hole.consume(iterator.next());
        }
    }

    /**
     * ============================== HOW TO RUN THIS TEST: ====================================
     * <p>
     * You will see measureWrong() running on-par with baseline().
     * Both measureRight() are measuring twice the baseline, so the logs are intact.
     * <p>
     * You can run this test:
     * <p>
     * a) Via the command line:
     * $ mvn clean package
     * $ java -jar target/benchmarks.jar '.*CollectionIteration.*'
     * <p>
     * b) Via the Java API:
     *
     * @param args Arguments
     * @throws RunnerException Possible exception
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CollectionIteration.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .build();
        new Runner(opt).run();
    }
}
