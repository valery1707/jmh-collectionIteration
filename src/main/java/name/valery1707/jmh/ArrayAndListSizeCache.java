package name.valery1707.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class ArrayAndListSizeCache {
    @Param({"forWithoutCache", "forWithCache"})
    public String mode;

    @Param({"array", "list"})
    public String type;

    private Integer[] array;
    private List<Integer> list;

    @Setup
    public void setup(
            BenchmarkParams params
    ) {
        int size = Integer.parseInt(params
                .getBenchmark()
                .substring(ArrayAndListSizeCache.class.getName().length())
                .replaceAll("[^\\d]", "")
        );
        array = new Integer[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        list = Arrays.asList(array);
    }

    private void listForWithoutCache(Blackhole hole) {
        for (int i = 0; i < list.size(); i++) {
            hole.consume(i);
        }
    }

    private void arrayForWithoutCache(Blackhole hole) {
        for (int i = 0; i < array.length; i++) {
            hole.consume(i);
        }
    }

    private void listForWithCache(Blackhole hole) {
        int i, size;
        for (i = 0, size = list.size(); i < size; i++) {
            hole.consume(i);
        }
    }

    private void arrayForWithCache(Blackhole hole) {
        int i, size;
        for (i = 0, size = array.length; i < size; i++) {
            hole.consume(i);
        }
    }

    private void benchmark(final Blackhole hole) {
        switch (mode) {
            case "forWithoutCache":
                switch (type) {
                    case "array":
                        arrayForWithoutCache(hole);
                        break;
                    case "list":
                        listForWithoutCache(hole);
                        break;
                }
                break;
            case "forWithCache":
                switch (type) {
                    case "array":
                        arrayForWithCache(hole);
                        break;
                    case "list":
                        listForWithCache(hole);
                        break;
                }
                break;
            default:
        }
    }

    @Benchmark
    @OperationsPerInvocation(1)
    public void benchmark_1(final Blackhole hole) {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void benchmark_10(final Blackhole hole) {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(100)
    public void benchmark_100(final Blackhole hole) {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(1_000)
    public void benchmark_1_000(final Blackhole hole) {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(10_000)
    public void benchmark_10_000(final Blackhole hole) {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void benchmark_100_000(final Blackhole hole) {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(1_000_000)
    public void benchmark_1_000_000(final Blackhole hole) {
        benchmark(hole);
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
     * $ java -jar target/benchmarks.jar '.*ArrayAndListSizeCache.*'
     * <p>
     * b) Via the Java API:
     *
     * @param args Arguments
     * @throws RunnerException Possible exception
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ArrayAndListSizeCache.class.getSimpleName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }
}
