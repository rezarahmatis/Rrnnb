package com.example.audio

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.example.model.AudioSpectrum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln1p
import kotlin.math.sqrt

/**
 * Thread-safe real-time 3-band audio analyzer (bass, mid, treble, overall energy, gentle beat pulse).
 * Fed directly by [PelagiaAudioProcessor] inside ExoPlayer's PCM pipeline — requiring zero microphone permission.
 */
class AudioAnalyzer {

    @Volatile private var rawBass = 0f
    @Volatile private var rawMid = 0f
    @Volatile private var rawTreble = 0f
    @Volatile private var lastUpdateMs = 0L

    private var smoothBass = 0f
    private var smoothMid = 0f
    private var smoothTreble = 0f
    private var smoothEnergy = 0f
    private var beatPulse = 0f
    private var prevBass = 0f

    private val _spectrumFlow = MutableStateFlow(AudioSpectrum())
    val spectrumFlow: StateFlow<AudioSpectrum> = _spectrumFlow.asStateFlow()

    fun submitBands(bass: Float, mid: Float, treble: Float) {
        rawBass = bass.coerceIn(0f, 1f)
        rawMid = mid.coerceIn(0f, 1f)
        rawTreble = treble.coerceIn(0f, 1f)
        lastUpdateMs = System.currentTimeMillis()
    }

    fun reset() {
        rawBass = 0f
        rawMid = 0f
        rawTreble = 0f
    }

    /**
     * Called each render frame on the GL thread to retrieve smoothly interpolated bands
     * that decay gracefully to zero when playback pauses or goes silent.
     */
    fun getLatestSpectrum(): AudioSpectrum {
        val now = System.currentTimeMillis()
        val isActive = (now - lastUpdateMs) < 350L

        val targetB = if (isActive) rawBass else 0f
        val targetM = if (isActive) rawMid else 0f
        val targetT = if (isActive) rawTreble else 0f

        // Smooth attack / slower organic release so the aquatic world breathes gently
        smoothBass += (targetB - smoothBass) * (if (targetB > smoothBass) 0.18f else 0.06f)
        smoothMid += (targetM - smoothMid) * (if (targetM > smoothMid) 0.16f else 0.06f)
        smoothTreble += (targetT - smoothTreble) * (if (targetT > smoothTreble) 0.20f else 0.07f)

        val energy = (smoothBass * 0.48f + smoothMid * 0.34f + smoothTreble * 0.18f).coerceIn(0f, 1f)
        smoothEnergy += (energy - smoothEnergy) * 0.12f

        val bassDelta = (smoothBass - prevBass).coerceAtLeast(0f)
        prevBass = smoothBass
        if (bassDelta > 0.045f) {
            beatPulse = (beatPulse + bassDelta * 3.2f).coerceAtMost(1f)
        } else {
            beatPulse *= 0.90f
        }

        val snapshot = AudioSpectrum(
            bass = smoothBass,
            mid = smoothMid,
            treble = smoothTreble,
            energy = smoothEnergy,
            beatPulse = beatPulse,
            isAnalyzing = isActive && smoothEnergy > 0.01f
        )
        _spectrumFlow.value = snapshot
        return snapshot
    }
}

/**
 * Pass-through Media3 [BaseAudioProcessor] that performs lightweight real-time 3-band crossover
 * analysis directly on ExoPlayer's 16-bit PCM stream without modifying the audio output.
 */
@OptIn(UnstableApi::class)
class PelagiaAudioProcessor(
    private val analyzer: AudioAnalyzer
) : BaseAudioProcessor() {

    private var sampleRate = 44100
    private var channelCount = 2

    // 1-pole IIR crossover state for Bass (< 200Hz), Mid (200Hz - 2200Hz), Treble (> 2200Hz)
    private var lpLowAlpha = 0.03f
    private var lpHighAlpha = 0.25f
    private var lpLowState = 0f
    private var lpHighState = 0f

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        sampleRate = inputAudioFormat.sampleRate.coerceAtLeast(8000)
        channelCount = inputAudioFormat.channelCount.coerceAtLeast(1)
        lpLowAlpha = (1.0 - exp(-2.0 * PI * 200.0 / sampleRate)).toFloat().coerceIn(0.005f, 0.5f)
        lpHighAlpha = (1.0 - exp(-2.0 * PI * 2200.0 / sampleRate)).toFloat().coerceIn(0.05f, 0.9f)
        lpLowState = 0f
        lpHighState = 0f
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        // Analyze PCM samples in-place before copying unchanged bytes to output
        val startPos = inputBuffer.position()
        val endPos = inputBuffer.limit()
        val frameBytes = channelCount * 2

        if (frameBytes > 0 && remaining >= frameBytes) {
            var bassAcc = 0.0
            var midAcc = 0.0
            var trebleAcc = 0.0
            var framesCounted = 0

            // Stride by 2 frames on high sample rates to keep CPU usage ultra-low
            val stepBytes = frameBytes * 2
            var pos = startPos
            while (pos + frameBytes <= endPos) {
                // Mix channels to mono float in [-1, 1]
                var mono = 0f
                for (ch in 0 until channelCount) {
                    val sample = inputBuffer.getShort(pos + ch * 2)
                    mono += sample / 32768f
                }
                mono /= channelCount

                lpLowState += lpLowAlpha * (mono - lpLowState)
                lpHighState += lpHighAlpha * (mono - lpHighState)

                val bass = lpLowState
                val mid = lpHighState - lpLowState
                val treble = mono - lpHighState

                bassAcc += bass * bass
                midAcc += mid * mid
                trebleAcc += treble * treble
                framesCounted++
                pos += stepBytes
            }

            if (framesCounted > 0) {
                val rmsBass = sqrt(bassAcc / framesCounted).toFloat()
                val rmsMid = sqrt(midAcc / framesCounted).toFloat()
                val rmsTreble = sqrt(trebleAcc / framesCounted).toFloat()

                // Logarithmic perceptual scaling so quiet & loud tracks both feel refined
                val normBass = (ln1p(rmsBass * 18.0) / ln1p(18.0)).toFloat().coerceIn(0f, 1f)
                val normMid = (ln1p(rmsMid * 22.0) / ln1p(22.0)).toFloat().coerceIn(0f, 1f)
                val normTreble = (ln1p(rmsTreble * 32.0) / ln1p(32.0)).toFloat().coerceIn(0f, 1f)

                analyzer.submitBands(normBass, normMid, normTreble)
            }
        }

        // Copy untouched audio directly to output buffer
        val out = replaceOutputBuffer(remaining)
        out.put(inputBuffer)
        out.flip()
    }

    override fun onFlush() {
        lpLowState = 0f
        lpHighState = 0f
    }

    override fun onReset() {
        lpLowState = 0f
        lpHighState = 0f
        analyzer.reset()
    }
}
