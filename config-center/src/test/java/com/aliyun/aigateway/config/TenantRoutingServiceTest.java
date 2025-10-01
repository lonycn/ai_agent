package com.aliyun.aigateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.aliyun.aigateway.config.AiGatewayProperties.AsrConfig;
import com.aliyun.aigateway.config.AiGatewayProperties.Credentials;
import com.aliyun.aigateway.config.AiGatewayProperties.ModelConfig;
import com.aliyun.aigateway.config.AiGatewayProperties.TenantConfig;
import com.aliyun.aigateway.config.TenantConfigurationException;
import com.aliyun.aigateway.sdk.dto.AsrProcessedRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantRoutingServiceTest {

    private AiGatewayProperties properties;
    private TenantRoutingService routingService;

    @BeforeEach
    void setUp() {
        properties = new AiGatewayProperties();
        TenantConfig tenantConfig = new TenantConfig();
        Credentials credentials = new Credentials();
        credentials.setAccessKeyId("ak");
        credentials.setAccessKeySecret("sk");
        tenantConfig.setCredentials(credentials);
        AsrConfig asrConfig = new AsrConfig();
        asrConfig.setDefaultModelId("paraformer-v2");
        asrConfig.setDefaultVocabularyName("general");
        ModelConfig meeting = new ModelConfig();
        meeting.setModelId("paraformer-meeting");
        meeting.setVocabularyName("meeting-hotwords");
        asrConfig.setModels(Map.of("meeting", meeting));
        tenantConfig.setAsr(asrConfig);
        properties.setTenants(Map.of("tenant-a", tenantConfig));
        routingService = new TenantRoutingService(properties);
    }

    @Test
    void resolvesDefaultModelWhenNoOverridesProvided() {
        AsrProcessedRequest request = new AsrProcessedRequest(
                "tenant-a", new byte[0], 16_000, "zh", null, null);

        TenantRoutingService.ResolvedAsrContext context = routingService.resolveAsr(request);

        assertEquals("paraformer-v2", context.modelId());
        assertEquals("general", context.vocabularyName());
        assertEquals("ak", context.credentials().getAccessKeyId());
    }

    @Test
    void resolvesAliasModelProfile() {
        AsrProcessedRequest request = new AsrProcessedRequest(
                "tenant-a", new byte[0], 16_000, null, null, "meeting");

        TenantRoutingService.ResolvedAsrContext context = routingService.resolveAsr(request);

        assertEquals("paraformer-meeting", context.modelId());
        assertEquals("meeting-hotwords", context.vocabularyName());
    }

    @Test
    void prefersRequestVocabularyOverride() {
        AsrProcessedRequest request = new AsrProcessedRequest(
                "tenant-a", new byte[0], 16_000, null, "custom-vocab", "meeting");

        TenantRoutingService.ResolvedAsrContext context = routingService.resolveAsr(request);

        assertEquals("paraformer-meeting", context.modelId());
        assertEquals("custom-vocab", context.vocabularyName());
    }

    @Test
    void fallsBackToRequestedModelIdWhenAliasMissing() {
        AsrProcessedRequest request = new AsrProcessedRequest(
                "tenant-a", new byte[0], 16_000, null, null, "paraformer-direct");

        TenantRoutingService.ResolvedAsrContext context = routingService.resolveAsr(request);

        assertEquals("paraformer-direct", context.modelId());
        assertEquals("general", context.vocabularyName());
    }

    @Test
    void throwsWhenTenantUnknown() {
        AsrProcessedRequest request = new AsrProcessedRequest(
                "unknown", new byte[0], 16_000, null, null, null);

        assertThrows(TenantConfigurationException.class, () -> routingService.resolveAsr(request));
    }
}
