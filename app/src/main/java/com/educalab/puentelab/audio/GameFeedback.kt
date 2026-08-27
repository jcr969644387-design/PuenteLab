package com.educalab.puentelab.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

private const val SAMPLE_RATE = 44100

/** Una nota corta: frecuencia (con barrido opcional) y duración, para armar melodías simples. */
private data class Tone(val startHz: Double, val endHz: Double = startHz, val durationMs: Int, val amplitude: Double = 0.5)

/**
 * Paleta de sonidos del juego: cada evento es una mini melodía de 1 a 5 notas sintetizadas en
 * memoria (sin archivos de assets ni red), pensada para sonar corta y agradable para chicos de
 * 10 a 15 años en vez de un simple "beep" de teléfono.
 */
private enum class GameSound(val tones: List<Tone>) {
    TAP(listOf(Tone(1200.0, 1200.0, 28, 0.18))),
    PLACE_PIECE(listOf(Tone(880.0, 880.0, 55, 0.35))),
    CONNECT_NODE(listOf(Tone(660.0, 880.0, 90, 0.4))),
    DELETE_PIECE(listOf(Tone(700.0, 420.0, 90, 0.32))),
    BUILD_OK(listOf(Tone(523.0, 523.0, 70, 0.4), Tone(659.0, 659.0, 100, 0.45))),
    TEST_START(listOf(Tone(320.0, 900.0, 220, 0.4))),
    BRIDGE_SUCCESS(listOf(Tone(523.0, 523.0, 90, 0.5), Tone(659.0, 659.0, 90, 0.5), Tone(784.0, 784.0, 140, 0.55))),
    BRIDGE_FAIL(listOf(Tone(300.0, 220.0, 160, 0.4), Tone(190.0, 150.0, 200, 0.4))),
    STAR_EARNED(listOf(Tone(1046.0, 1568.0, 110, 0.5))),
    MISSION_COMPLETE(listOf(Tone(523.0, 523.0, 85, 0.5), Tone(659.0, 659.0, 85, 0.5), Tone(784.0, 784.0, 85, 0.5), Tone(1046.0, 1046.0, 160, 0.6))),
    MISSION_UNLOCK(listOf(Tone(784.0, 1046.0, 100, 0.4))),
    SCENARIO_UNLOCK(
        listOf(
            Tone(523.0, 523.0, 90, 0.55), Tone(659.0, 659.0, 90, 0.55), Tone(784.0, 784.0, 90, 0.55),
            Tone(1046.0, 1046.0, 90, 0.6), Tone(1319.0, 1319.0, 190, 0.65)
        )
    )
}

/**
 * Motor de sonido y vibración del juego. Sintetiza tonos cortos en memoria (WAV PCM) y los
 * reproduce con SoundPool, y vibra usando el Vibrator del sistema directamente en vez de
 * LocalHapticFeedback: ese API de Compose depende del ajuste "sonido táctil" del sistema y en
 * muchos equipos no vibra nunca, además de necesitar el permiso VIBRATE (ver AndroidManifest).
 */
class GameFeedback(context: Context) {
    private val appContext = context.applicationContext

    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedIds = mutableSetOf<Int>()
    private val soundIdByEvent = mutableMapOf<GameSound, Int>()

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) loadedIds += sampleId }
        runCatching {
            GameSound.values().forEach { sound ->
                val file = File(appContext.cacheDir, "sfx_${sound.name}.wav")
                if (!file.exists() || file.length() < 44) writeWav(file, synthesize(sound.tones))
                soundIdByEvent[sound] = pool.load(file.absolutePath, 1)
            }
        }
    }

    fun release() {
        runCatching { pool.release() }
    }

    // --- Eventos del juego ---
    fun tap() = play(GameSound.TAP)
    fun placePiece() { play(GameSound.PLACE_PIECE); buzz(15) }
    fun connectNode() { play(GameSound.CONNECT_NODE); buzz(18) }
    fun deletePiece() { play(GameSound.DELETE_PIECE); buzz(15) }
    fun buildCorrect() = play(GameSound.BUILD_OK)
    fun testStart() = play(GameSound.TEST_START)
    fun bridgeSuccess() = play(GameSound.BRIDGE_SUCCESS)
    fun bridgeFail() { play(GameSound.BRIDGE_FAIL); buzz(60) }
    fun starEarned() { play(GameSound.STAR_EARNED); buzz(20) }
    fun missionComplete() { play(GameSound.MISSION_COMPLETE); buzz(35) }
    fun missionUnlock() = play(GameSound.MISSION_UNLOCK)
    fun scenarioUnlock() { play(GameSound.SCENARIO_UNLOCK); buzz(45) }

    private fun play(sound: GameSound) {
        if (!soundEnabled) return
        val id = soundIdByEvent[sound] ?: return
        if (id !in loadedIds) return
        runCatching { pool.play(id, 1f, 1f, 1, 0, 1f) }
    }

    private fun buzz(durationMs: Long) {
        if (!hapticEnabled) return
        val v = vibrator ?: return
        runCatching {
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, 160))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        }
    }

    /** Sintetiza una secuencia de tonos (con una pequeñísima pausa entre notas) a PCM de 16 bits. */
    private fun synthesize(tones: List<Tone>): ShortArray {
        val gapSamples = (SAMPLE_RATE * 0.008).toInt()
        val segments = tones.map { tone ->
            val n = (SAMPLE_RATE * tone.durationMs / 1000.0).toInt().coerceAtLeast(1)
            ShortArray(n) { i ->
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / n
                val freq = tone.startHz + (tone.endHz - tone.startHz) * progress
                // envolvente corta de ataque/caída para evitar "clics" al inicio y al final de la nota
                val attack = min(1.0, i / (SAMPLE_RATE * 0.005))
                val release = min(1.0, (n - i) / (SAMPLE_RATE * 0.012))
                val envelope = min(attack, release)
                val sample = sin(2.0 * PI * freq * t) * tone.amplitude * envelope
                (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }.toList()
        }
        val all = ArrayList<Short>(segments.sumOf { it.size } + gapSamples * segments.size)
        segments.forEachIndexed { idx, seg ->
            all += seg
            if (idx != segments.lastIndex) repeat(gapSamples) { all += 0.toShort() }
        }
        return all.toShortArray()
    }

    private fun writeWav(file: File, pcm: ShortArray) {
        val dataBytes = pcm.size * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + dataBytes); put("WAVE".toByteArray())
            put("fmt ".toByteArray()); putInt(16); putShort(1); putShort(1)
            putInt(SAMPLE_RATE); putInt(SAMPLE_RATE * 2); putShort(2); putShort(16)
            put("data".toByteArray()); putInt(dataBytes)
        }.array()
        val body = ByteBuffer.allocate(dataBytes).order(ByteOrder.LITTLE_ENDIAN)
        pcm.forEach { body.putShort(it) }
        RandomAccessFile(file, "rw").use { raf ->
            raf.setLength(0)
            raf.write(header)
            raf.write(body.array())
        }
    }
}

/** Instancia recordada de [GameFeedback], liberada automáticamente cuando la pantalla se cierra. */
@Composable
fun rememberGameFeedback(): GameFeedback {
    val context = LocalContext.current
    val feedback = remember { GameFeedback(context) }
    DisposableEffect(Unit) { onDispose { feedback.release() } }
    return feedback
}
