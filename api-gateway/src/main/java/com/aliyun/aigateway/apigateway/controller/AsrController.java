package com.aliyun.aigateway.apigateway.controller;

import com.aliyun.aigateway.apigateway.service.AsrApplicationService;
import com.aliyun.aigateway.sdk.dto.AsrRecognitionRequest;
import com.aliyun.aigateway.sdk.dto.AsrRecognitionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asr")
public class AsrController {

    private final AsrApplicationService asrApplicationService;

    public AsrController(AsrApplicationService asrApplicationService) {
        this.asrApplicationService = asrApplicationService;
    }

    @PostMapping("/recognize")
    public ResponseEntity<AsrRecognitionResponse> recognize(@Valid @RequestBody AsrRecognitionRequest request) {
        AsrRecognitionResponse response = asrApplicationService.recognize(request);
        return ResponseEntity.ok(response);
    }
}
