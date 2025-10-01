package com.aliyun.aigateway.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;

public class ObservabilityTagsCustomizer implements MeterFilter {

    private final MeterRegistry meterRegistry;

    public ObservabilityTagsCustomizer(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.meterRegistry.config().meterFilter(this);
    }

    @Override
    public Meter.Id map(Meter.Id id) {
        return id.withTag(Tag.of("service", "ai-gateway-api"));
    }
}
