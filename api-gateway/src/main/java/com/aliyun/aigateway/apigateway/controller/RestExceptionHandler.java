package com.aliyun.aigateway.apigateway.controller;

import com.aliyun.aigateway.audio.InvalidAudioException;
import com.aliyun.aigateway.config.TenantConfigurationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(InvalidAudioException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAudio(InvalidAudioException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_audio",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(TenantConfigurationException.class)
    public ResponseEntity<Map<String, Object>> handleTenantConfig(TenantConfigurationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", "tenant_configuration",
                "message", ex.getMessage()
        ));
    }
}
