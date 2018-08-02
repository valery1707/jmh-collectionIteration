package name.valery1707.jmh;

import name.valery1707.Utils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class CollectionIteration {
    @Param({"forWithoutCache", "forWithCache", "forEachJava5", "forEachJava8", "iterator"})
    public String mode;

    @Param({"java.util.HashSet", "java.util.TreeSet", "java.util.ArrayList", "java.util.LinkedList"})
    public String clazz;

    private Collection<Integer> collection;

    @Setup
    @SuppressWarnings("unchecked")
    public void setup(
            BenchmarkParams params
    ) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        int size = Integer.parseInt(params
                .getBenchmark()
                .substring(CollectionIteration.class.getName().length())
                .replaceAll("[^\\d]", "")
        );
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
    public void forWithoutCache(final Blackhole hole) {
        for (int i = 0; i < collection.size(); i++) {
            hole.consume(i);
        }
    }

    /**
     * Standard for-each with size cache
     *
     * @param hole Black hole
     */
    public void forWithCache(final Blackhole hole) {
        int i, size;
        for (i = 0, size = collection.size(); i < size; i++) {
            hole.consume(i);
        }
    }

    /**
     * Extended for-each from Java 1.5
     *
     * @param hole Black hole
     */
    public void forEachJava5(final Blackhole hole) {
        for (Integer i : collection) {
            hole.consume(i);
        }
    }

    /**
     * Lambda for-each from Java 1.8
     *
     * @param hole Black hole
     */
    @SuppressWarnings("Convert2MethodRef")
    public void forEachJava8(final Blackhole hole) {
        collection.forEach(i -> hole.consume(i));
    }

    /**
     * Loop over collection with iterator
     *
     * @param hole Black hole
     */
    @SuppressWarnings("WhileLoopReplaceableByForEach")
    public void iterator(final Blackhole hole) {
        Iterator<Integer> iterator = collection.iterator();
        while (iterator.hasNext()) {
            hole.consume(iterator.next());
        }
    }

    private void benchmark(final Blackhole hole) {
        switch (mode) {
            case "forWithoutCache":
                forWithoutCache(hole);
                break;
            case "forWithCache":
                forWithCache(hole);
                break;
            case "forEachJava5":
                forEachJava5(hole);
                break;
            case "forEachJava8":
                forEachJava8(hole);
                break;
            case "iterator":
                iterator(hole);
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
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }

    public static class MarkdownFormatter {
        /**
         * <pre><code>
         * CollectionIteration.benchmark_1             java.util.HashSet  forWithoutCache  avgt    5  14,517 ± 1,909  ns/op
         * </code></pre>
         */
        private static final Pattern DATA_LINE_PATTERN = Pattern.compile(""
                + "^"
                + "CollectionIteration.benchmark_([\\d_]+)"         //size
                + "\\s+"
                + "([\\w.]+)"                                       //clazz
                + "\\s+"
                + "([\\w.]+)"                                       //mode
                + "\\s+"
                + "([\\w.]+)"                                       //benchmarkMode
                + "\\s+"
                + "([\\d]+)"                                        //measureIterations
                + "\\s+"
                + "(([\\d,.]+)\\s+(.)\\s+([\\d,.]+)\\s+([\\w/]+))"    //value
                + "$"
        );

        public static void main(String[] args) throws IOException {
            URI uri = URI.create("https://gist.githubusercontent.com/valery1707/d9e5b7db6bcdbf1a273f7ab76732cf92/raw/b6c31dca06734e9bec12f0f8d3bdfa45921438de/Result_8721f00_2018-08-01.txt");
            try (
                    InputStream inputStream = uri.toURL().openStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
            ) {
                Map<String, Map<String, Map<Integer, String>>> valueBySizeByModeByClazz = Utils
                        .toStream(reader, MarkdownFormatter::readLine)
                        .map(DATA_LINE_PATTERN::matcher)
                        .filter(Matcher::matches)
                        .collect(
                                LinkedHashMap::new,
                                (map, matcher) -> map
                                        .computeIfAbsent(matcher.group(2), __ -> new LinkedHashMap<>())
                                        .computeIfAbsent(matcher.group(3), __ -> new TreeMap<>())
                                        .computeIfAbsent(Integer.valueOf(matcher.group(1).replaceAll("_", "")), __ ->
                                                matcher.group(7) + matcher.group(8) + matcher.group(9)
                                        ),
                                (mapL, mapR) -> {
                                }
                        );
                Set<Integer> sizes = valueBySizeByModeByClazz
                        .values().iterator().next()
                        .values().iterator().next()
                        .keySet();
                String header = sizes
                        .stream()
                        .map(Object::toString)
                        .collect(joining(" | ", "| Collection | Iteration | ", " |"));
                String split = Collections.nCopies(sizes.size() + 2, "-------------").stream().collect(joining(" | ", "| ", " |"));
                System.out.println(header);
                System.out.println(split);
                valueBySizeByModeByClazz.forEach((clazz, valueBySizeByMode) ->
                        valueBySizeByMode.forEach((mode, valueBySize) -> {
                            System.out.print(String.format("| %s | %s | ", clazz, mode));
                            valueBySize.forEach((size, value) ->
                                    System.out.print(String.format("%s |", value))
                            );
                            System.out.println();
                        }));
            }
        }

        private static Optional<String> readLine(BufferedReader reader) {
            try {
                return Optional.ofNullable(reader.readLine());
            } catch (IOException e) {
                throw new IllegalStateException("IO exception in readLine", e);
            }
        }
    }
}
