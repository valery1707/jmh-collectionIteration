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
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class NewLineReplace {
    private static final char[] CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '*'};
    private static final Pattern PATTERN = Pattern.compile("(\r\n|\n)");

    @Param({"regexp-raw", "regexp-compiled", "replace"})
    public String mode;

    @Param({"10", "100", "1000"})
    public int length;

    @Param({"1.0", "0.7", "0.5", "0.1"})
    public double percent;

    private String source;

    private String regexpRaw(String source) {
        return source.replaceAll(PATTERN.pattern(), " ");
    }

    private String regexpCompiled(String source) {
        return PATTERN.matcher(source).replaceAll(" ");
    }

    private String replace(String source) {
        return source.replace("\r\n", " ").replace("\n", " ");
    }

    @Setup
    public void setup() {
        source = IntStream
                .generate(() -> ThreadLocalRandom.current().nextInt(CHARS.length))
                .limit(length)
                .mapToObj(i -> CHARS[i])
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                .toString();
        int partSize = (int) (length * percent);
        source = IntStream
                .rangeClosed(0, length / partSize)
                .mapToObj(part -> source.substring(part * partSize, Math.min((part + 1) * partSize, length)))
                .collect(joining("\r\n"));
    }

    private void benchmark(final Blackhole hole) {
        switch (mode) {
            case "regexp-raw":
                hole.consume(regexpRaw(source));
                break;
            case "regexp-compiled":
                hole.consume(regexpCompiled(source));
                break;
            case "replace":
                hole.consume(replace(source));
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
                .include(NewLineReplace.class.getSimpleName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }
}
