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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class MatcherReplace {
    private static final char[] CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '*'};
    private static final Pattern PATTERN = Pattern.compile("([\\d*]{4})");

    @Param({"string", "function1", "function2"})
    public String mode;

    @Param({"8", "10", "12", "24", "48", "1024"})
    public int length;

    private String source;

    private String string(String source) {
        return PATTERN.matcher(source).replaceAll("$1 ").trim();
    }

    private String function1(String source) {
        return PATTERN.matcher(source).replaceAll(match -> match.group(1).concat(" ")).trim();
    }

    private static final Function<MatchResult, String> REPLACE = match -> match.group(1).concat(" ");

    private String function2(String source) {
        return PATTERN.matcher(source).replaceAll(REPLACE).trim();
    }

    @Setup
    public void setup() {
        source = IntStream
                .generate(() -> ThreadLocalRandom.current().nextInt(CHARS.length))
                .limit(length)
                .mapToObj(i -> CHARS[i])
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private void benchmark(final Blackhole hole) {
        switch (mode) {
            case "string":
                hole.consume(string(source));
                break;
            case "function1":
                hole.consume(function1(source));
                break;
            case "function2":
                hole.consume(function2(source));
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
                .include(MatcherReplace.class.getSimpleName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }
}
