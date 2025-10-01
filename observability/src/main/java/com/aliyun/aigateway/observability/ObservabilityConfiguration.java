package com.aliyun.aigateway.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import brave.Tracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfiguration {

    @Bean
    public Tracer tracer(Tracing tracing) {
        return BraveTracer.fromBrave(tracing);
    }

    @Bean
    public ObservabilityTagsCustomizer observabilityTagsCustomizer(MeterRegistry meterRegistry) {
        return new ObservabilityTagsCustomizer(meterRegistry);
    }
}
