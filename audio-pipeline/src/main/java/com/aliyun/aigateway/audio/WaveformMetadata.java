package com.aliyun.aigateway.audio;

public record WaveformMetadata(
        int sampleRate,
        int channels,
        int bitsPerSample,
        int dataOffset,
        int dataLength
) {

    public boolean isMonoPcm16() {
        return channels == 1 && bitsPerSample == 16;
    }
}
