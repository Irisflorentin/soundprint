package com.soundprint.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FFmpegRunnerTest {

    @Test
    void buildCommandUsesArgumentListForMp3Conversion() {
        FFmpegRunner runner = new FFmpegRunner();

        List<String> command = runner.buildCommand(
                "D:/soundprint-storage/audio/source file.flac",
                "D:/soundprint-storage/conversion/result file.mp3",
                "MP3",
                320,
                44100
        );

        assertThat(command).containsExactly(
                "ffmpeg",
                "-y",
                "-i", "D:/soundprint-storage/audio/source file.flac",
                "-vn",
                "-acodec", "libmp3lame",
                "-b:a", "320k",
                "-ar", "44100",
                "D:/soundprint-storage/conversion/result file.mp3"
        );
    }

    @Test
    void buildCommandRejectsUnsupportedFormat() {
        FFmpegRunner runner = new FFmpegRunner();

        assertThatThrownBy(() -> runner.buildCommand(
                "input.flac",
                "output.xyz",
                "XYZ",
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的格式");
    }
}
