package com.aliyun.aigateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiGatewayProperties.class)
public class ConfigCenterConfiguration {
}
