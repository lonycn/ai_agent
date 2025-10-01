package com.aliyun.aigateway.sdk;

import com.aliyun.aigateway.sdk.dto.AsrProcessedRequest;
import com.aliyun.aigateway.sdk.dto.AsrRecognitionResponse;

public interface AsrClient {

    AsrRecognitionResponse recognize(AsrProcessedRequest request);
}
