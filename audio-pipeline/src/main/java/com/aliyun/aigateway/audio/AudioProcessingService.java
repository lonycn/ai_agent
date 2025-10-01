package com.aliyun.aigateway.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.springframework.stereotype.Service;

@Service
public class AudioProcessingService {

    private static final int TARGET_SAMPLE_RATE = 16_000;

    private final AudioValidationService validationService;

    public AudioProcessingService(AudioValidationService validationService) {
        this.validationService = validationService;
    }

    public ProcessedAudio prepareForRecognition(String audioBase64, Integer declaredSampleRate) {
        if (audioBase64 == null || audioBase64.isBlank()) {
            throw new InvalidAudioException("Audio payload must be provided");
        }
        byte[] audioBytes;
        try {
            audioBytes = Base64.getDecoder().decode(audioBase64);
        } catch (IllegalArgumentException ex) {
            throw new InvalidAudioException("Audio payload is not valid Base64", ex);
        }

        WaveformMetadata metadata = validationService.validateWave(audioBytes, declaredSampleRate);
        if (metadata.isMonoPcm16() && metadata.sampleRate() == TARGET_SAMPLE_RATE) {
            return new ProcessedAudio(stripWaveHeader(audioBytes, metadata), TARGET_SAMPLE_RATE);
        }

        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioBytes))) {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    TARGET_SAMPLE_RATE,
                    16,
                    1,
                    2,
                    TARGET_SAMPLE_RATE,
                    false);
            AudioInputStream pcmSource = ensurePcm(inputStream);
            try (AudioInputStream converted = AudioSystem.getAudioInputStream(targetFormat, pcmSource)) {
                return new ProcessedAudio(readAllBytes(converted), TARGET_SAMPLE_RATE);
            }
        } catch (UnsupportedAudioFileException | IOException | IllegalArgumentException ex) {
            throw new InvalidAudioException("Failed to normalize audio payload", ex);
        }
    }

    private AudioInputStream ensurePcm(AudioInputStream inputStream) {
        AudioFormat format = inputStream.getFormat();
        if (AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                && format.getSampleSizeInBits() == 16) {
            return inputStream;
        }
        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(pcmFormat, inputStream);
    }

    private byte[] readAllBytes(AudioInputStream stream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private byte[] stripWaveHeader(byte[] audioBytes, WaveformMetadata metadata) {
        byte[] pcm = new byte[metadata.dataLength()];
        System.arraycopy(audioBytes, metadata.dataOffset(), pcm, 0, metadata.dataLength());
        return pcm;
    }
}
