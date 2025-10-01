package com.aliyun.aigateway.audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AudioProcessingServiceTest {

    private AudioProcessingService processingService;

    @BeforeEach
    void setUp() {
        processingService = new AudioProcessingService(new AudioValidationService());
    }

    @Test
    void returnsMonoPcmForPreNormalizedAudio() {
        byte[] wav = TestAudioFactory.sineWaveWav(16_000, 1, 16);
        String base64 = Base64.getEncoder().encodeToString(wav);

        ProcessedAudio processed = processingService.prepareForRecognition(base64, 16_000);

        assertEquals(16_000, processed.sampleRate());
        assertEquals(wav.length - 44, processed.pcmData().length);
        byte[] expected = new byte[wav.length - 44];
        System.arraycopy(wav, 44, expected, 0, expected.length);
        assertArrayEquals(expected, processed.pcmData());
    }

    @Test
    void upSamplesToTargetRate() {
        byte[] wav = TestAudioFactory.sineWaveWav(8_000, 1, 16);
        String base64 = Base64.getEncoder().encodeToString(wav);

        ProcessedAudio processed = processingService.prepareForRecognition(base64, 8_000);

        assertEquals(16_000, processed.sampleRate());
        assertEquals(16_000 * 2, processed.pcmData().length); // 16-bit mono, 1 second
    }

    @Test
    void rejectsInvalidBase64() {
        assertThrows(InvalidAudioException.class, () -> processingService.prepareForRecognition("not-base64", 16_000));
    }
}
