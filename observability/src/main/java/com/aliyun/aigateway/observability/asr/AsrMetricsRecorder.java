package com.aliyun.aigateway.observability.asr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class AsrMetricsRecorder {

    private final MeterRegistry meterRegistry;

    public AsrMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordSuccess(String tenantId, Duration latency, Double accuracy, int transcriptLength) {
        Duration safeLatency = latency != null ? latency : Duration.ZERO;
        recordLatency(tenantId, "success", safeLatency);
        Counter.builder("asr.recognition.success")
                .tag("tenantId", tenantId)
                .register(meterRegistry)
                .increment();
        if (accuracy != null) {
            DistributionSummary.builder("asr.recognition.accuracy")
                    .baseUnit("ratio")
                    .minimumExpectedValue(0.0)
                    .maximumExpectedValue(1.0)
                    .tag("tenantId", tenantId)
                    .register(meterRegistry)
                    .record(accuracy);
        }
        DistributionSummary.builder("asr.recognition.transcript.length")
                .baseUnit("characters")
                .tag("tenantId", tenantId)
                .register(meterRegistry)
                .record(Math.max(0, transcriptLength));
    }

    public void recordFailure(String tenantId, Duration latency, Throwable error) {
        Duration safeLatency = latency != null ? latency : Duration.ZERO;
        recordLatency(tenantId, "failure", safeLatency);
        Counter.builder("asr.recognition.failure")
                .tag("tenantId", tenantId)
                .tag("exception", error.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();
    }

    private void recordLatency(String tenantId, String outcome, Duration latency) {
        Timer.builder("asr.recognition.latency")
                .tag("tenantId", tenantId)
                .tag("outcome", outcome)
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(meterRegistry)
                .record(latency);
    }
}
