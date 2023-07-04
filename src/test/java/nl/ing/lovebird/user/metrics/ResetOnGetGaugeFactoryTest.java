package nl.ing.lovebird.user.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ResetOnGetGaugeFactoryTest {

    private final SimpleMeterRegistry delegateRegistry = new SimpleMeterRegistry();

    @Test
    public void testSuccessAndFailureGauges() {
        // given
        final var gaugeDelegate = ResetOnGetGaugeFactory.createResetOnGetGauge("testGauge", delegateRegistry);

        // when
        gaugeDelegate.set(5);

        // then

        // map gauges by name for easier access
        final Map<String, Gauge> meters = delegateRegistry.getMeters().stream()
                .map(m -> (Gauge) m)
                .collect(Collectors.toMap(m -> m.getId().getName(), Function.identity()));
        final Gauge meter = meters.get("testGauge");
        // read the values
        assertThat(meter.value()).isEqualTo(5.0);
        // now they should have automatically reset
        assertThat(meter.value()).isNaN();
    }
}