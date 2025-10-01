package com.aliyun.aigateway.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsrRecognitionRequest(
        @NotBlank String tenantId,
        @NotBlank String audioBase64,
        @NotNull @Positive Integer sampleRate,
        String language,
        String vocabularyName,
        String modelId
) {

    @JsonIgnore
    public AsrProcessedRequest toProcessedRequest(byte[] audioBytes, int normalizedSampleRate) {
        return new AsrProcessedRequest(
                tenantId, audioBytes, normalizedSampleRate, language, vocabularyName, modelId);
    }
}
