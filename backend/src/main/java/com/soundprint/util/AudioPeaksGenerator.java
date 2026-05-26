package com.soundprint.util;

import com.soundprint.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 使用 FFmpeg 把任意音频统一转为单声道 f32le PCM，再降采样为波形峰值。
 */
@Slf4j
@Component
public class AudioPeaksGenerator {

    private static final int WAVEFORM_SAMPLE_RATE = 8000;

    public List<Float> readPcmSamples(File audioFile) throws IOException, InterruptedException {
        List<String> command = buildCommand(audioFile);
        log.info("FFmpeg peaks 命令: {}", String.join(" ", command));

        Process process = new ProcessBuilder(command).start();
        CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(
                () -> readStreamAsText(process.getErrorStream()));

        List<Float> samples = new ArrayList<>();
        try (InputStream stdout = process.getInputStream()) {
            readLittleEndianFloats(stdout, samples);
        } catch (IOException e) {
            process.destroyForcibly();
            throw e;
        }

        int exitCode = process.waitFor();
        String stderr = stderrFuture.join();
        if (exitCode != 0) {
            throw new IOException("FFmpeg peaks 失败，退出码 " + exitCode + ": " + stderr);
        }
        return samples;
    }

    List<String> buildCommand(File audioFile) {
        return List.of(
                "ffmpeg",
                "-nostdin",
                "-i", audioFile.getAbsolutePath(),
                "-vn",
                "-f", "f32le",
                "-ac", "1",
                "-ar", String.valueOf(WAVEFORM_SAMPLE_RATE),
                "-"
        );
    }

    float[] downsamplePeaks(List<Float> samples, int targetCount) {
        if (targetCount <= 0) {
            throw new BusinessException("波形采样数量必须大于 0");
        }

        float[] peaks = new float[targetCount];
        if (samples == null || samples.isEmpty()) {
            return peaks;
        }

        int bucketSize = Math.max(1, (int) Math.ceil(samples.size() / (double) targetCount));
        for (int i = 0; i < targetCount; i++) {
            int start = i * bucketSize;
            if (start >= samples.size()) {
                break;
            }
            int end = Math.min(start + bucketSize, samples.size());
            float max = 0f;
            for (int j = start; j < end; j++) {
                float value = samples.get(j);
                if (Float.isFinite(value)) {
                    max = Math.max(max, Math.abs(value));
                }
            }
            peaks[i] = Math.min(1f, max);
        }
        return peaks;
    }

    public float[] generate(File audioFile, int targetCount) throws IOException, InterruptedException {
        return downsamplePeaks(readPcmSamples(audioFile), targetCount);
    }

    private void readLittleEndianFloats(InputStream input, List<Float> samples) throws IOException {
        byte[] buffer = new byte[8192];
        byte[] pending = new byte[4];
        int pendingCount = 0;
        int read;

        while ((read = input.read(buffer)) != -1) {
            int offset = 0;
            if (pendingCount > 0) {
                while (pendingCount < 4 && offset < read) {
                    pending[pendingCount++] = buffer[offset++];
                }
                if (pendingCount == 4) {
                    samples.add(decodeLittleEndianFloat(pending, 0));
                    pendingCount = 0;
                }
            }

            while (offset + 4 <= read) {
                samples.add(decodeLittleEndianFloat(buffer, offset));
                offset += 4;
            }

            while (offset < read) {
                pending[pendingCount++] = buffer[offset++];
            }
        }
    }

    private float decodeLittleEndianFloat(byte[] bytes, int offset) {
        int bits = (bytes[offset] & 0xff)
                | ((bytes[offset + 1] & 0xff) << 8)
                | ((bytes[offset + 2] & 0xff) << 16)
                | ((bytes[offset + 3] & 0xff) << 24);
        return Float.intBitsToFloat(bits);
    }

    private String readStreamAsText(InputStream input) {
        try (input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            input.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
