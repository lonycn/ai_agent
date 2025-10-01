package com.aliyun.aigateway.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-gateway")
public class AiGatewayProperties {

    private Map<String, TenantConfig> tenants = Collections.emptyMap();

    public Map<String, TenantConfig> getTenants() {
        return tenants;
    }

    public void setTenants(Map<String, TenantConfig> tenants) {
        this.tenants = tenants;
    }

    public TenantConfig getTenant(String tenantId) {
        if (tenants == null) {
            return null;
        }
        return tenants.get(tenantId);
    }

    public static class TenantConfig {
        private Credentials credentials;
        private AsrConfig asr = new AsrConfig();

        public Credentials getCredentials() {
            return credentials;
        }

        public void setCredentials(Credentials credentials) {
            this.credentials = credentials;
        }

        public AsrConfig getAsr() {
            return asr;
        }

        public void setAsr(AsrConfig asr) {
            this.asr = asr;
        }
    }

    public static class Credentials {
        private String accessKeyId;
        private String accessKeySecret;
        private String securityToken;

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getSecurityToken() {
            return securityToken;
        }

        public void setSecurityToken(String securityToken) {
            this.securityToken = securityToken;
        }
    }

    public static class AsrConfig {
        private String defaultModelId;
        private String defaultVocabularyName;
        private Map<String, ModelConfig> models = Collections.emptyMap();

        public String getDefaultModelId() {
            return defaultModelId;
        }

        public void setDefaultModelId(String defaultModelId) {
            this.defaultModelId = defaultModelId;
        }

        public String getDefaultVocabularyName() {
            return defaultVocabularyName;
        }

        public void setDefaultVocabularyName(String defaultVocabularyName) {
            this.defaultVocabularyName = defaultVocabularyName;
        }

        public Map<String, ModelConfig> getModels() {
            return models;
        }

        public void setModels(Map<String, ModelConfig> models) {
            this.models = models;
        }

        public ModelConfig getModelProfile(String key) {
            if (models == null) {
                return null;
            }
            return models.get(key);
        }
    }

    public static class ModelConfig {
        private String modelId;
        private String vocabularyName;
        private Integer sampleRate;

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getVocabularyName() {
            return vocabularyName;
        }

        public void setVocabularyName(String vocabularyName) {
            this.vocabularyName = vocabularyName;
        }

        public Integer getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(Integer sampleRate) {
            this.sampleRate = sampleRate;
        }
    }
}
