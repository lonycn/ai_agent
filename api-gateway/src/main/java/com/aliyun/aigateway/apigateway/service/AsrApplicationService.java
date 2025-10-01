package com.aliyun.aigateway.apigateway.service;

import com.aliyun.aigateway.audio.AudioProcessingService;
import com.aliyun.aigateway.audio.ProcessedAudio;
import com.aliyun.aigateway.observability.asr.AsrMetricsRecorder;
import com.aliyun.aigateway.sdk.AsrClient;
import com.aliyun.aigateway.sdk.dto.AsrRecognitionRequest;
import com.aliyun.aigateway.sdk.dto.AsrRecognitionResponse;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class AsrApplicationService {

    private final AsrClient asrClient;
    private final AudioProcessingService audioProcessingService;
    private final AsrMetricsRecorder metricsRecorder;

    public AsrApplicationService(
            AsrClient asrClient,
            AudioProcessingService audioProcessingService,
            AsrMetricsRecorder metricsRecorder) {
        this.asrClient = asrClient;
        this.audioProcessingService = audioProcessingService;
        this.metricsRecorder = metricsRecorder;
    }

    public AsrRecognitionResponse recognize(AsrRecognitionRequest request) {
        long start = System.nanoTime();
        ProcessedAudio processedAudio =
                audioProcessingService.prepareForRecognition(request.audioBase64(), request.sampleRate());
        try {
            AsrRecognitionResponse response = asrClient.recognize(
                    request.toProcessedRequest(processedAudio.pcmData(), processedAudio.sampleRate()));
            Duration measuredLatency = response.latency() != null
                    ? response.latency()
                    : Duration.ofNanos(System.nanoTime() - start);
            AsrRecognitionResponse normalizedResponse =
                    response.latency() != null ? response : response.withLatency(measuredLatency);
            metricsRecorder.recordSuccess(
                    request.tenantId(),
                    measuredLatency,
                    normalizedResponse.accuracy(),
                    normalizedResponse.text() != null ? normalizedResponse.text().length() : 0);
            return normalizedResponse;
        } catch (RuntimeException ex) {
            Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
            metricsRecorder.recordFailure(request.tenantId(), elapsed, ex);
            throw ex;
        }
    }
}
