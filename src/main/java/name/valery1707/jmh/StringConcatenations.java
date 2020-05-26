package name.valery1707.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class StringConcatenations {
    @Param({"plus", "join"})
    public String mode;

    @Param({"0", "1", "10", "50"})
    public int lhsLen;

    @Param({"0", "1", "10", "50"})
    public int rhsLen;

    private String lhs;
    private String rhs;

    private String plus(String lhs, String rhs) {
        return lhs + "_" + rhs;
    }

    private String join(String lhs, String rhs) {
        return String.join("_", lhs, rhs);
    }

    @Setup
    public void setup() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        lhs = generate(random, lhsLen);
        rhs = generate(random, rhsLen);
    }

    private String generate(Random random, int len) {
        return random
                .ints(1, 10)
                .limit(len).mapToObj(String::valueOf)
                .collect(Collectors.joining())
                .substring(0, len);
    }

    private void benchmark(final Blackhole hole) {
        switch (mode) {
            case "plus":
                hole.consume(plus(lhs, rhs));
                break;
            case "join":
                hole.consume(join(lhs, rhs));
                break;
            default:
                throw new IllegalStateException("Unknown mode: " + mode);
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

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringConcatenations.class.getSimpleName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }
}
