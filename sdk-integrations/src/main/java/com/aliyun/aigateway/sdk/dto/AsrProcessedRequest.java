package com.aliyun.aigateway.sdk.dto;

public record AsrProcessedRequest(
        String tenantId,
        byte[] audioBytes,
        int sampleRate,
        String language,
        String vocabularyName,
        String modelId
) {
}
