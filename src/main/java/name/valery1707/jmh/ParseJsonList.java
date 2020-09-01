package name.valery1707.jmh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("DefaultAnnotationParam")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class ParseJsonList {
    @Param({"arrayToStream", "arrayToListToStream", "listToStream"})
    public String mode;

    @Param({"8", "10", "12", "24", "48", "1024"})
    public int count;

    private ObjectMapper mapper;

    private String source;

    private Map<String, DemoDo> arrayToStream(String source) throws JsonProcessingException {
        return Stream.of(mapper.readValue(source, DemoDo[].class))
                .collect(Collectors.toUnmodifiableMap(DemoDo::getCode, Function.identity()));
    }

    private Map<String, DemoDo> arrayToListToStream(String source) throws JsonProcessingException {
        return Arrays.asList(mapper.readValue(source, DemoDo[].class))
                .stream()
                .collect(Collectors.toUnmodifiableMap(DemoDo::getCode, Function.identity()));
    }

    private Map<String, DemoDo> listToStream(String source) throws JsonProcessingException {
        return mapper.readValue(source, new TypeReference<List<DemoDo>>() {})
                .stream()
                .collect(Collectors.toUnmodifiableMap(DemoDo::getCode, Function.identity()));
    }

    @Setup
    public void setup() throws JsonProcessingException {
        mapper = new ObjectMapper();

        List<DemoDo> list = IntStream
                .generate(() -> ThreadLocalRandom.current().nextInt(count * 1000))
                .distinct()
                .limit(count)
                .mapToObj(String::valueOf)
                .map(DemoDo::new)
                .collect(toList());
        source = mapper.writeValueAsString(list);
    }

    private void benchmark(final Blackhole hole) throws JsonProcessingException {
        switch (mode) {
            case "arrayToStream":
                hole.consume(arrayToStream(source));
                break;
            case "arrayToListToStream":
                hole.consume(arrayToListToStream(source));
                break;
            case "listToStream":
                hole.consume(listToStream(source));
                break;
            default:
                throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    @Benchmark
    @OperationsPerInvocation(1)
    public void benchmark_1(final Blackhole hole) throws JsonProcessingException {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void benchmark_10(final Blackhole hole) throws JsonProcessingException {
        benchmark(hole);
    }

    @Benchmark
    @OperationsPerInvocation(100)
    public void benchmark_100(final Blackhole hole) throws JsonProcessingException {
        benchmark(hole);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParseJsonList.class.getSimpleName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class DemoDo {
        private String code;

        public DemoDo() {
        }

        public DemoDo(String code) {
            this();
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public DemoDo setCode(String code) {
            this.code = code;
            return this;
        }
    }
}
