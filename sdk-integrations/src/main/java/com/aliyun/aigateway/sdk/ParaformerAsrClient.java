package com.aliyun.aigateway.sdk;

import com.aliyun.aigateway.config.AiGatewayProperties.Credentials;
import com.aliyun.aigateway.config.TenantRoutingService;
import com.aliyun.aigateway.config.TenantRoutingService.ResolvedAsrContext;
import com.aliyun.aigateway.sdk.dto.AsrProcessedRequest;
import com.aliyun.aigateway.sdk.dto.AsrRecognitionResponse;
import com.aliyun.aigateway.security.CredentialProviderFactory;
import com.aliyun.credentials.provider.StaticCredentialProvider;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ParaformerAsrClient implements AsrClient {

    private final CredentialProviderFactory credentialProviderFactory;
    private final TenantRoutingService tenantRoutingService;
    private final ParaformerClientAdapter clientAdapter;

    public ParaformerAsrClient(
            CredentialProviderFactory credentialProviderFactory,
            TenantRoutingService tenantRoutingService,
            ParaformerClientAdapter clientAdapter) {
        this.credentialProviderFactory = credentialProviderFactory;
        this.tenantRoutingService = tenantRoutingService;
        this.clientAdapter = clientAdapter;
    }

    @Override
    public AsrRecognitionResponse recognize(AsrProcessedRequest request) {
        ResolvedAsrContext context = tenantRoutingService.resolveAsr(request);
        Credentials credentials = context.credentials();
        StaticCredentialProvider credentialProvider = credentialProviderFactory.from(
                credentials.getAccessKeyId(),
                credentials.getAccessKeySecret(),
                credentials.getSecurityToken());
        ParaformerClientAdapter.RecognitionResult result = clientAdapter.recognize(
                credentialProvider,
                context.modelId(),
                context.vocabularyName(),
                request);
        String resolvedModelId = (result.modelId() != null && !result.modelId().isBlank())
                ? result.modelId()
                : context.modelId();
        return new AsrRecognitionResponse(
                request.tenantId(),
                result.text(),
                Duration.ofMillis(result.latencyMillis()),
                result.accuracy(),
                resolvedModelId,
                result.requestId());
    }
}
