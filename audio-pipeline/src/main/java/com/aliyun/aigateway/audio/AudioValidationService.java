package com.aliyun.aigateway.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class AudioValidationService {

    private static final byte[] RIFF = new byte[] {'R', 'I', 'F', 'F'};
    private static final byte[] WAVE = new byte[] {'W', 'A', 'V', 'E'};
    private static final byte[] FMT = new byte[] {'f', 'm', 't', ' '};
    private static final byte[] DATA = new byte[] {'d', 'a', 't', 'a'};

    public WaveformMetadata validateWave(byte[] audioBytes, Integer declaredSampleRate) {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new InvalidAudioException("Audio payload is empty");
        }
        if (declaredSampleRate == null || declaredSampleRate <= 0) {
            throw new InvalidAudioException("Sample rate must be provided");
        }
        if (audioBytes.length < 44) {
            throw new InvalidAudioException("Audio payload is smaller than a valid WAV header");
        }

        ByteBuffer buffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN);
        byte[] header = new byte[4];
        buffer.get(header);
        if (!Arrays.equals(header, RIFF)) {
            throw new InvalidAudioException("Audio payload is not a RIFF container");
        }
        buffer.getInt();
        buffer.get(header);
        if (!Arrays.equals(header, WAVE)) {
            throw new InvalidAudioException("Audio payload is not in WAVE format");
        }

        Integer sampleRate = null;
        Integer channels = null;
        Integer bitsPerSample = null;
        Integer dataOffset = null;
        Integer dataLength = null;

        while (buffer.remaining() >= 8 && (sampleRate == null || dataOffset == null)) {
            buffer.get(header);
            int chunkSize = buffer.getInt();
            if (Arrays.equals(header, FMT)) {
                if (chunkSize < 16 || buffer.remaining() < chunkSize) {
                    throw new InvalidAudioException("Invalid fmt chunk in WAV payload");
                }
                int audioFormat = Short.toUnsignedInt(buffer.getShort());
                channels = Short.toUnsignedInt(buffer.getShort());
                sampleRate = buffer.getInt();
                buffer.getInt(); // byte rate
                buffer.getShort(); // block align
                bitsPerSample = Short.toUnsignedInt(buffer.getShort());
                if (chunkSize > 16) {
                    buffer.position(buffer.position() + chunkSize - 16);
                }
                if (audioFormat != 1) {
                    throw new InvalidAudioException("Only PCM encoded WAV audio is supported");
                }
            } else if (Arrays.equals(header, DATA)) {
                if (buffer.remaining() < chunkSize) {
                    throw new InvalidAudioException("Malformed WAV data chunk length");
                }
                dataOffset = buffer.position();
                dataLength = chunkSize;
                buffer.position(buffer.position() + chunkSize);
            } else {
                if (buffer.remaining() < chunkSize) {
                    throw new InvalidAudioException(
                            "Malformed WAV chunk: " + new String(header, StandardCharsets.US_ASCII));
                }
                buffer.position(buffer.position() + chunkSize);
            }
            if ((chunkSize & 1) == 1 && buffer.hasRemaining()) {
                buffer.get();
            }
        }

        if (sampleRate == null || channels == null || bitsPerSample == null) {
            throw new InvalidAudioException("WAV payload is missing fmt chunk");
        }
        if (dataOffset == null || dataLength == null) {
            throw new InvalidAudioException("WAV payload is missing data chunk");
        }
        if (!sampleRate.equals(declaredSampleRate)) {
            throw new InvalidAudioException(
                    "Declared sample rate " + declaredSampleRate + " does not match WAV header " + sampleRate);
        }
        if (channels <= 0) {
            throw new InvalidAudioException("WAV channel count must be positive");
        }
        if (bitsPerSample != 16 && bitsPerSample != 8) {
            throw new InvalidAudioException("Only 8-bit or 16-bit PCM WAV is supported");
        }

        return new WaveformMetadata(sampleRate, channels, bitsPerSample, dataOffset, dataLength);
    }
}
