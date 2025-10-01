package com.aliyun.aigateway.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

final class TestAudioFactory {

    private TestAudioFactory() {
    }

    static byte[] sineWaveWav(int sampleRate, int channels, int bitsPerSample) {
        double frequency = 440.0; // A4 reference tone
        int durationSeconds = 1;
        int totalSamples = sampleRate * durationSeconds;
        int bytesPerSample = bitsPerSample / 8;
        ByteBuffer pcmBuffer = ByteBuffer.allocate(totalSamples * channels * bytesPerSample)
                .order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < totalSamples; i++) {
            double angle = 2 * Math.PI * frequency * i / sampleRate;
            short value = (short) (Math.sin(angle) * Short.MAX_VALUE);
            for (int channel = 0; channel < channels; channel++) {
                if (bitsPerSample == 16) {
                    pcmBuffer.putShort(value);
                } else {
                    pcmBuffer.put((byte) (value >> 8));
                }
            }
        }

        byte[] pcm = pcmBuffer.array();
        ByteBuffer container = ByteBuffer.allocate(44 + pcm.length).order(ByteOrder.LITTLE_ENDIAN);
        container.put("RIFF".getBytes(StandardCharsets.US_ASCII));
        container.putInt(36 + pcm.length);
        container.put("WAVE".getBytes(StandardCharsets.US_ASCII));
        container.put("fmt ".getBytes(StandardCharsets.US_ASCII));
        container.putInt(16);
        container.putShort((short) 1); // PCM
        container.putShort((short) channels);
        container.putInt(sampleRate);
        int byteRate = sampleRate * channels * bytesPerSample;
        container.putInt(byteRate);
        container.putShort((short) (channels * bytesPerSample));
        container.putShort((short) bitsPerSample);
        container.put("data".getBytes(StandardCharsets.US_ASCII));
        container.putInt(pcm.length);
        container.put(pcm);
        return container.array();
    }
}
