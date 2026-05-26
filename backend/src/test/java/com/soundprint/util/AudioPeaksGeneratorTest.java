package com.soundprint.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudioPeaksGeneratorTest {

    @Test
    void buildCommandUsesArgumentListForPcmFloatOutput() {
        AudioPeaksGenerator generator = new AudioPeaksGenerator();

        List<String> command = generator.buildCommand(new File("D:/soundprint-storage/audio/source file.flac"));

        assertThat(command).containsExactly(
                "ffmpeg",
                "-nostdin",
                "-i", "D:\\soundprint-storage\\audio\\source file.flac",
                "-vn",
                "-f", "f32le",
                "-ac", "1",
                "-ar", "8000",
                "-"
        );
    }

    @Test
    void downsamplePeaksTakesAbsolutePeakPerBucket() {
        AudioPeaksGenerator generator = new AudioPeaksGenerator();

        float[] peaks = generator.downsamplePeaks(List.of(
                0.1f, -0.4f,
                0.3f, -0.2f,
                -0.9f, 0.2f,
                0.0f, 0.7f
        ), 4);

        assertThat(peaks).containsExactly(0.4f, 0.3f, 0.9f, 0.7f);
    }

    @Test
    void downsamplePeaksPadsWhenTargetIsLongerThanInput() {
        AudioPeaksGenerator generator = new AudioPeaksGenerator();

        float[] peaks = generator.downsamplePeaks(List.of(-0.5f, 0.25f), 4);

        assertThat(peaks).containsExactly(0.5f, 0.25f, 0.0f, 0.0f);
    }
}
