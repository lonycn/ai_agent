package com.aliyun.aigateway.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AudioValidationServiceTest {

    private final AudioValidationService validationService = new AudioValidationService();

    @Test
    void validatesPcmWaveHeaders() {
        byte[] wav = TestAudioFactory.sineWaveWav(16_000, 1, 16);

        WaveformMetadata metadata = validationService.validateWave(wav, 16_000);

        assertEquals(16_000, metadata.sampleRate());
        assertEquals(1, metadata.channels());
        assertEquals(16, metadata.bitsPerSample());
        assertEquals(wav.length - 44, metadata.dataLength());
    }

    @Test
    void rejectsMismatchedSampleRate() {
        byte[] wav = TestAudioFactory.sineWaveWav(16_000, 1, 16);

        assertThrows(InvalidAudioException.class, () -> validationService.validateWave(wav, 8_000));
    }

    @Test
    void rejectsNonWavePayload() {
        byte[] bogus = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
                .put("FORM".getBytes(StandardCharsets.US_ASCII))
                .array();

        assertThrows(InvalidAudioException.class, () -> validationService.validateWave(bogus, 16_000));
    }
}
