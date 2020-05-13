package name.valery1707.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class FillInVariables {
    @Param({"inplace", "manual", "java9"})
    public String mode;

    /**
     * Количество известных переменных
     */
    @Param({"0", "1", "10", "50"})
    public int exists;

    /**
     * Количество не известных переменных
     */
    @Param({"0", "1", "10", "50"})
    public int absent;

    /**
     * Количество констант
     */
    @Param({"0", "1", "10", "50"})
    public int consts;

    private String source;
    private Map<String, String> variables;

    private String inplace(String text, Function<String, String> resolver) {
        while (true) {
            Matcher matcher = Pattern.compile("\\{.*?}").matcher(text);
            if (!matcher.find()) break;
            String varName = matcher.group(0).replaceAll("[{}]", "");
            String variable = resolver.apply(varName);
            if (Objects.nonNull(variable)) {
                text = matcher.replaceFirst(variable);
            } else {
                text = matcher.replaceFirst("...");
            }
        }
        return text;
    }

    private static final Pattern VARIABLE_NAME = Pattern.compile("\\{(.*?)}");

    private String manual(String text, Function<String, String> resolver) {
        Matcher matcher = VARIABLE_NAME.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement;
            String value = resolver.apply(variable);
            if (Objects.nonNull(value)) {
                replacement = value;
            } else {
                replacement = "...";
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String java9(String text, Function<String, String> resolver) {
        return VARIABLE_NAME.matcher(text).replaceAll(match ->
                Optional.ofNullable(match.group(1)).map(resolver).orElse("...")
        );
    }

    @Setup
    public void setup() {
        List<String> all = new ArrayList<>(exists + absent + consts);
        List<String> exists = words(this.exists, "{exists", "}");
        List<String> absent = words(this.absent, "{absent", "}");
        List<String> consts = words(this.consts, "consts", "");

        variables = exists.stream().collect(toUnmodifiableMap(s -> s.substring(1, 1), s -> s.replace('{', '[').replace('}', ']')));

        all.addAll(exists);
        all.addAll(absent);
        all.addAll(consts);
        Collections.shuffle(all);
        source = String.join(" ", all);
    }

    private static List<String> words(int count, String prefix, String suffix) {
        return IntStream.iterate(1, i -> i++).limit(count).mapToObj(i -> prefix + i + suffix).collect(toList());
    }

    private void benchmark(final Blackhole hole) {
        switch (mode) {
            case "inplace":
                hole.consume(inplace(source, variables::get));
                break;
            case "manual":
                hole.consume(manual(source, variables::get));
                break;
            case "java9":
                hole.consume(java9(source, variables::get));
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

//    @Benchmark
//    @OperationsPerInvocation(1_000)
//    public void benchmark_1_000(final Blackhole hole) {
//        benchmark(hole);
//    }
//
//    @Benchmark
//    @OperationsPerInvocation(10_000)
//    public void benchmark_10_000(final Blackhole hole) {
//        benchmark(hole);
//    }
//
//    @Benchmark
//    @OperationsPerInvocation(100_000)
//    public void benchmark_100_000(final Blackhole hole) {
//        benchmark(hole);
//    }
//
//    @Benchmark
//    @OperationsPerInvocation(1_000_000)
//    public void benchmark_1_000_000(final Blackhole hole) {
//        benchmark(hole);
//    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FillInVariables.class.getSimpleName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }
}
