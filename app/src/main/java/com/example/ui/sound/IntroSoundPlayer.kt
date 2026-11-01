package com.example.ui.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Synthesizes a beautiful, deeply emotional acoustic musical piece
 * using Karplus-Strong physical modeling (plucked acoustic strings / sitar / dotara)
 * and soft wooden flute harmonics, playing an evocative traditional Bengali raga melody.
 */
class IntroSoundPlayer {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    @Volatile
    private var isMuted: Boolean = false
    @Volatile
    private var isPlaying: Boolean = false

    private class PluckedString(val frequency: Double, val sampleRate: Int) {
        private val bufferSize = maxOf(2, (sampleRate / frequency).toInt())
        private val ringBuffer = FloatArray(bufferSize)
        private var head = 0
        var active = false
        private var decayFactor = 0.993f

        fun pluck(gain: Float = 1.0f) {
            val random = Random(System.nanoTime())
            for (i in 0 until bufferSize) {
                // Initial burst of acoustic noise filtered with low pass for a warm finger pluck
                ringBuffer[i] = (random.nextFloat() * 2.0f - 1.0f) * gain
            }
            head = 0
            active = true
        }

        fun nextSample(): Float {
            if (!active) return 0f
            val current = ringBuffer[head]
            val nextIndex = (head + 1) % bufferSize
            val next = ringBuffer[nextIndex]

            // Karplus-Strong string physics: low-pass averaging filter simulating string dampening
            val newSample = ((current + next) * 0.5f) * decayFactor
            ringBuffer[head] = newSample
            head = nextIndex
            return current
        }
    }

    fun start(scope: CoroutineScope, muted: Boolean = false) {
        if (isPlaying) return
        this.isMuted = muted
        this.isPlaying = true

        playbackJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, 4096)

            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                // Notes in Hz (Bhairavi / Yaman heritage notes in D):
                // D3: 146.83, F3: 174.61, A3: 220.0, C4: 261.63, D4: 293.66, E4: 329.63, F4: 349.23, G4: 392.0, A4: 440.0
                val stringD3 = PluckedString(146.83, sampleRate)
                val stringA3 = PluckedString(220.00, sampleRate)
                val stringD4 = PluckedString(293.66, sampleRate)
                val stringE4 = PluckedString(329.63, sampleRate)
                val stringF4 = PluckedString(349.23, sampleRate)
                val stringG4 = PluckedString(392.00, sampleRate)
                val stringA4 = PluckedString(440.00, sampleRate)
                val stringC4 = PluckedString(261.63, sampleRate)

                val activeStrings = listOf(stringD3, stringA3, stringD4, stringE4, stringF4, stringG4, stringA4, stringC4)

                // Melody score: pairs of (delayInMilliseconds, Action to pluck)
                val melodyScore = listOf(
                    0L to { stringD3.pluck(0.85f); stringA3.pluck(0.6f) },
                    400L to { stringD4.pluck(0.7f) },
                    1100L to { stringF4.pluck(0.75f) },
                    1800L to { stringE4.pluck(0.65f) },
                    2400L to { stringD4.pluck(0.7f) },
                    3100L to { stringA3.pluck(0.8f); stringD3.pluck(0.6f) },
                    3800L to { stringC4.pluck(0.65f) },
                    4400L to { stringD4.pluck(0.8f) },
                    5200L to { stringF4.pluck(0.75f) },
                    5900L to { stringG4.pluck(0.7f) },
                    6500L to { stringA4.pluck(0.8f) },
                    7400L to { stringF4.pluck(0.65f) },
                    8100L to { stringD4.pluck(0.85f); stringD3.pluck(0.7f) }
                )

                val buffer = ShortArray(bufferSize / 2)
                var currentSample = 0L
                val startTimeMs = System.currentTimeMillis()
                var scoreIndex = 0

                var flutePhase = 0.0
                val fluteFreq = 293.66 // D4 soft flute tone
                val twoPi = 2.0 * PI

                while (isActive && isPlaying) {
                    val currentMuted = isMuted
                    val elapsedMs = System.currentTimeMillis() - startTimeMs

                    // Trigger scheduled melody notes
                    while (scoreIndex < melodyScore.size && elapsedMs >= melodyScore[scoreIndex].first) {
                        melodyScore[scoreIndex].second.invoke()
                        scoreIndex++
                    }

                    for (i in buffer.indices) {
                        if (currentMuted) {
                            buffer[i] = 0
                        } else {
                            // Sum plucked string instruments
                            var stringMix = 0f
                            for (s in activeStrings) {
                                stringMix += s.nextSample()
                            }

                            // Gentle, warm wooden flute background tone (sine with warm 2nd harmonic and soft vibrato)
                            val vibrato = sin(twoPi * 4.5 * (currentSample / sampleRate.toDouble())) * 1.5
                            val actualFluteFreq = fluteFreq + vibrato
                            val fluteSample = sin(flutePhase) * 0.12 + sin(flutePhase * 2.0) * 0.04

                            flutePhase += twoPi * actualFluteFreq / sampleRate
                            if (flutePhase > twoPi) flutePhase -= twoPi

                            val mixed = (stringMix.toDouble() * 0.70 + fluteSample).coerceIn(-1.0, 1.0)
                            val sampleShort = (mixed * 30000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                            buffer[i] = sampleShort
                        }

                        currentSample++
                    }

                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cleanUpTrack()
            }
        }
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        cleanUpTrack()
    }

    private fun cleanUpTrack() {
        try {
            audioTrack?.let {
                if (it.state == AudioTrack.STATE_INITIALIZED) {
                    it.stop()
                    it.release()
                }
            }
        } catch (e: Exception) {
            // Ignore
        } finally {
            audioTrack = null
        }
    }
}
