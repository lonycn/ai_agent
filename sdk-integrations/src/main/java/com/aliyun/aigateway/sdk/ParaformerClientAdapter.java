package com.aliyun.aigateway.sdk;

import com.aliyun.aigateway.sdk.dto.AsrProcessedRequest;
import com.aliyun.credentials.provider.StaticCredentialProvider;
import org.springframework.stereotype.Component;

@Component
public class ParaformerClientAdapter {

    public RecognitionResult recognize(
            StaticCredentialProvider credentialProvider,
            String modelId,
            String vocabularyName,
            AsrProcessedRequest request) {
        // TODO: Integrate Alibaba Model Studio SDK invocation here.
        return new RecognitionResult("", modelId, "pending-request-id", 0L, null);
    }

    public record RecognitionResult(
            String text, String modelId, String requestId, long latencyMillis, Double accuracy) {
    }
}
