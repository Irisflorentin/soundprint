package com.soundprint.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FFmpeg 命令调用封装。
 *
 * 这里始终使用 ProcessBuilder 的参数数组形式，不拼接 shell 字符串，
 * 避免文件名包含空格时出错，也避免命令注入风险。
 */
@Slf4j
@Component
public class FFmpegRunner {

    private static final Pattern DURATION_PATTERN =
            Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
    private static final Pattern TIME_PATTERN =
            Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");

    public void convert(
            String inputPath,
            String outputPath,
            String targetFormat,
            Integer targetBitrate,
            Integer targetSampleRate,
            IntConsumer onProgress
    ) throws IOException, InterruptedException {
        List<String> command = buildCommand(inputPath, outputPath, targetFormat, targetBitrate, targetSampleRate);
        log.info("FFmpeg 命令: {}", String.join(" ", command));

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            double totalSeconds = 0;
            while ((line = reader.readLine()) != null) {
                if (totalSeconds == 0) {
                    Matcher duration = DURATION_PATTERN.matcher(line);
                    if (duration.find()) {
                        totalSeconds = parseTime(duration);
                    }
                }

                Matcher currentTime = TIME_PATTERN.matcher(line);
                if (currentTime.find() && totalSeconds > 0) {
                    double currentSeconds = parseTime(currentTime);
                    int percent = Math.min(99, (int) Math.round(currentSeconds / totalSeconds * 100));
                    onProgress.accept(percent);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 退出码非 0: " + exitCode);
        }
        onProgress.accept(100);
    }

    List<String> buildCommand(
            String inputPath,
            String outputPath,
            String targetFormat,
            Integer targetBitrate,
            Integer targetSampleRate
    ) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(inputPath);
        command.add("-vn");

        switch (targetFormat.toUpperCase()) {
            case "MP3" -> {
                command.add("-acodec");
                command.add("libmp3lame");
                addBitrate(command, targetBitrate);
            }
            case "AAC" -> {
                command.add("-acodec");
                command.add("aac");
                addBitrate(command, targetBitrate);
            }
            case "FLAC" -> {
                command.add("-acodec");
                command.add("flac");
                command.add("-compression_level");
                command.add("8");
            }
            case "WAV" -> {
                command.add("-acodec");
                command.add("pcm_s16le");
            }
            default -> throw new IllegalArgumentException("不支持的格式: " + targetFormat);
        }

        if (targetSampleRate != null) {
            command.add("-ar");
            command.add(String.valueOf(targetSampleRate));
        }

        command.add(outputPath);
        return command;
    }

    private void addBitrate(List<String> command, Integer targetBitrate) {
        if (targetBitrate != null) {
            command.add("-b:a");
            command.add(targetBitrate + "k");
        }
    }

    private double parseTime(Matcher matcher) {
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        int seconds = Integer.parseInt(matcher.group(3));
        int centiseconds = Integer.parseInt(matcher.group(4));
        return hours * 3600 + minutes * 60 + seconds + centiseconds / 100.0;
    }
}
