package nl.ing.lovebird.user.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class ResetOnGetGaugeFactory {

    /**
     * Registers a {@link Gauge} that resets to {@code null}/{@link Double#NaN} every time after it's probed.
     * The gauge is backed by the {@link AtomicReference} (to a {@link Number}) which is returned.
     *
     * @param name the gauge name.
     * @return the backing instance of {@link AtomicReference}
     */
    public static AtomicReference<Number> createResetOnGetGauge(final String name, final MeterRegistry meterRegistry) {
        final AtomicReference<Number> delegate = new AtomicReference<>();
        Gauge.builder(name, () -> delegate.getAndSet(null)).register(meterRegistry);
        return delegate;
    }
}
