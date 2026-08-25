package com.educalab.puentelab.domain.logic

import com.educalab.puentelab.domain.model.ChallengeAttempt
import com.educalab.puentelab.domain.model.LevelInfo

/**
 * Calcula experiencia (XP) y nivel a partir del historial real de intentos.
 * No hay aleatoriedad: el mismo historial siempre produce el mismo resultado.
 */
object ProgressEngine {

    // Umbrales de XP acumulada requeridos para cada nivel (índice = nivel - 1).
    private val LEVEL_THRESHOLDS = listOf(0, 120, 300, 550, 900, 1350, 1900, 2600, 3400, 4400)

    private const val XP_PER_STAR = 50
    private const val FIRST_TRY_BONUS = 20

    fun xpForAttempt(attempt: ChallengeAttempt): Int {
        if (!attempt.passed) return 0
        var xp = attempt.stars * XP_PER_STAR
        if (attempt.attemptNumber == 1) xp += FIRST_TRY_BONUS
        return xp
    }

    /** Solo cuenta el MEJOR intento aprobado por desafío (no se puede "farmear" repitiendo). */
    fun totalXp(attempts: List<ChallengeAttempt>): Int {
        return attempts
            .filter { it.passed }
            .groupBy { it.challengeId }
            .values
            .sumOf { attemptsForChallenge -> attemptsForChallenge.maxOf { xpForAttempt(it) } }
    }

    fun levelInfo(xp: Int): LevelInfo {
        var level = 1
        for (i in LEVEL_THRESHOLDS.indices) {
            if (xp >= LEVEL_THRESHOLDS[i]) level = i + 1
        }
        val currentThreshold = LEVEL_THRESHOLDS[level - 1]
        val nextThreshold = LEVEL_THRESHOLDS.getOrNull(level)
        return LevelInfo(level, xp, currentThreshold, nextThreshold)
    }

    fun maxLevel(): Int = LEVEL_THRESHOLDS.size
}
