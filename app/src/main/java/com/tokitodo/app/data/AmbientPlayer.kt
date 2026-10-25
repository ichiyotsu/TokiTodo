package com.tokitodo.app.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** 计时页的背景音：用 AudioTrack 现场合成柔和噪声，不依赖音频文件。 */
enum class AmbientMode(val label: String) {
    Rain("雨声"),
    Waves("海浪"),
    Noise("白噪音"),
}

object AmbientPlayer {

    @Volatile
    private var running = false
    private var worker: Thread? = null
    private var track: AudioTrack? = null

    val isPlaying: Boolean
        get() = running

    fun start(mode: AmbientMode) {
        stop()
        running = true
        worker = Thread({ loop(mode) }, "TokiAmbient").apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    @Synchronized
    fun stop() {
        running = false
        worker?.let { thread ->
            runCatching { thread.join(300) }
        }
        worker = null
        runCatching { track?.stop() }
        runCatching { track?.release() }
        track = null
    }

    private fun loop(mode: AmbientMode) {
        val sampleRate = 44_100
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(4096)
        val bufferSize = maxOf(minBuffer, 16_384)
        val audioTrack = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull() ?: return
        track = audioTrack
        runCatching { audioTrack.play() }

        val random = Random(System.currentTimeMillis())
        var lowPass = 0f
        var phase = 0.0
        val chunk = ShortArray(2048)
        while (running) {
            for (i in chunk.indices) {
                val white = random.nextFloat() * 2f - 1f
                lowPass += (white - lowPass) * when (mode) {
                    AmbientMode.Rain -> 0.34f
                    AmbientMode.Waves -> 0.05f
                    AmbientMode.Noise -> 0.22f
                }
                val t = (phase + i.toDouble() / sampleRate)
                val envelope = when (mode) {
                    AmbientMode.Rain -> 1f
                    AmbientMode.Waves -> 0.45f + 0.4f * sin(2 * PI * t / 7.0).toFloat()
                    AmbientMode.Noise -> 0.8f
                }
                val volume = when (mode) {
                    AmbientMode.Rain -> 0.10f
                    AmbientMode.Waves -> 0.16f
                    AmbientMode.Noise -> 0.05f
                }
                val sample = (lowPass * envelope * volume * Short.MAX_VALUE)
                    .coerceIn(-Short.MAX_VALUE.toFloat(), Short.MAX_VALUE.toFloat())
                chunk[i] = sample.toInt().toShort()
            }
            phase += chunk.size.toDouble() / sampleRate
            if (phase > 3600) phase = 0.0
            val written = runCatching {
                audioTrack.write(chunk, 0, chunk.size, AudioTrack.WRITE_BLOCKING)
            }.getOrDefault(0)
            if (written < 0) break
        }
        runCatching { audioTrack.stop() }
        runCatching { audioTrack.release() }
        if (track === audioTrack) track = null
    }
}
