package com.aliyun.aigateway.sdk.dto;

import java.time.Duration;

public record AsrRecognitionResponse(
        String tenantId,
        String text,
        Duration latency,
        Double accuracy,
        String modelId,
        String requestId
) {

    public AsrRecognitionResponse withLatency(Duration newLatency) {
        return new AsrRecognitionResponse(tenantId, text, newLatency, accuracy, modelId, requestId);
    }
}
