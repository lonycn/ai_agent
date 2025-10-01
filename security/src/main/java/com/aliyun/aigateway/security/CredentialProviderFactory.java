package com.aliyun.aigateway.security;

import com.aliyun.credentials.provider.StaticCredentialProvider;
import com.aliyun.credentials.ClientException;
import com.aliyun.credentials.models.CredentialModel;
import org.springframework.stereotype.Component;

@Component
public class CredentialProviderFactory {

    public StaticCredentialProvider from(String accessKeyId, String accessKeySecret, String securityToken) {
        try {
            CredentialModel credentialModel = CredentialModel.builder()
                    .accessKeyId(accessKeyId)
                    .accessKeySecret(accessKeySecret)
                    .securityToken(securityToken)
                    .build();
            return StaticCredentialProvider.create(credentialModel);
        } catch (ClientException ex) {
            throw new CredentialProvisionException("Failed to initialize Alibaba Cloud credentials", ex);
        }
    }
}
