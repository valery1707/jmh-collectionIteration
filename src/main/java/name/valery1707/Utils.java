package name.valery1707;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("WeakerAccess")
public class Utils {
    public static <T> Stream<T> toStream(Iterator<? extends T> iterator) {
        return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public static <D, T> Iterator<T> toIterator(final D delegate, final Predicate<? super D> hasNext, final Function<? super D, ? extends T> next) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return hasNext.test(delegate);
            }

            @Override
            public T next() {
                return next.apply(delegate);
            }
        };
    }

    public static <D, T> Stream<T> toStream(final D delegate, final Predicate<? super D> hasNext, final Function<? super D, ? extends T> next) {
        return toStream(toIterator(delegate, hasNext, next));
    }

    @SuppressWarnings("ConstantConditions")
    public static <D, T> Stream<T> toStream(final D delegate, final Function<? super D, Optional<? extends T>> reader) {
        AtomicReference<Optional<? extends T>> next = new AtomicReference<>();
        return toStream(toIterator(delegate,
                d -> {
                    if (next.get() == null) {
                        next.set(reader.apply(d));
                    }
                    return next.get().isPresent();
                },
                d -> next.getAndSet(null).get()
        ));
    }
}
