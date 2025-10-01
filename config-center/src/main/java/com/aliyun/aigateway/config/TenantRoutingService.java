package com.aliyun.aigateway.config;

import com.aliyun.aigateway.config.AiGatewayProperties.AsrConfig;
import com.aliyun.aigateway.config.AiGatewayProperties.Credentials;
import com.aliyun.aigateway.config.AiGatewayProperties.ModelConfig;
import com.aliyun.aigateway.config.AiGatewayProperties.TenantConfig;
import com.aliyun.aigateway.sdk.dto.AsrProcessedRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TenantRoutingService {

    private final AiGatewayProperties properties;

    public TenantRoutingService(AiGatewayProperties properties) {
        this.properties = properties;
    }

    public ResolvedAsrContext resolveAsr(AsrProcessedRequest request) {
        TenantConfig tenantConfig = properties.getTenant(request.tenantId());
        if (tenantConfig == null) {
            throw new TenantConfigurationException("Unknown tenant: " + request.tenantId());
        }
        Credentials credentials = tenantConfig.getCredentials();
        if (credentials == null) {
            throw new TenantConfigurationException(
                    "Missing credentials for tenant: " + request.tenantId());
        }
        AsrConfig asrConfig = tenantConfig.getAsr();
        if (asrConfig == null) {
            throw new TenantConfigurationException(
                    "ASR configuration is not defined for tenant: " + request.tenantId());
        }

        ModelConfig modelProfile = null;
        if (StringUtils.hasText(request.modelId())) {
            modelProfile = asrConfig.getModelProfile(request.modelId());
        }

        String resolvedModelId = determineModelId(request, asrConfig, modelProfile);
        String resolvedVocabulary = determineVocabulary(request, asrConfig, modelProfile);

        return new ResolvedAsrContext(credentials, resolvedModelId, resolvedVocabulary);
    }

    private String determineModelId(
            AsrProcessedRequest request, AsrConfig asrConfig, ModelConfig modelProfile) {
        if (modelProfile != null && StringUtils.hasText(modelProfile.getModelId())) {
            return modelProfile.getModelId();
        }
        if (StringUtils.hasText(request.modelId())) {
            return request.modelId();
        }
        if (StringUtils.hasText(asrConfig.getDefaultModelId())) {
            return asrConfig.getDefaultModelId();
        }
        throw new TenantConfigurationException(
                "Default model is not configured for tenant: " + request.tenantId());
    }

    private String determineVocabulary(
            AsrProcessedRequest request, AsrConfig asrConfig, ModelConfig modelProfile) {
        if (StringUtils.hasText(request.vocabularyName())) {
            return request.vocabularyName();
        }
        if (modelProfile != null && StringUtils.hasText(modelProfile.getVocabularyName())) {
            return modelProfile.getVocabularyName();
        }
        return asrConfig.getDefaultVocabularyName();
    }

    public record ResolvedAsrContext(Credentials credentials, String modelId, String vocabularyName) {}
}
