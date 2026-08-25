package com.educalab.puentelab.domain

import com.educalab.puentelab.domain.logic.ProgressEngine
import com.educalab.puentelab.domain.model.ChallengeAttempt
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.domain.model.StructureType
import org.junit.Assert.*
import org.junit.Test

class ProgressEngineTest {

    private fun attempt(
        challengeId: String, passed: Boolean = true, stars: Int = 2,
        types: Set<StructureType> = setOf(StructureType.BEAM), budgetRatio: Double = 0.5, attemptNum: Int = 1
    ) = ChallengeAttempt(challengeId, ScenarioType.RIVER, passed, stars, types, budgetRatio, attemptNum)

    @Test
    fun `sin intentos xp es cero`() {
        assertEquals(0, ProgressEngine.totalXp(emptyList()))
    }

    @Test
    fun `3 estrellas a la primera da 170 xp`() {
        val a = attempt("c1", stars = 3, attemptNum = 1)
        assertEquals(170, ProgressEngine.totalXp(listOf(a)))
    }

    @Test
    fun `solo se cuenta el mejor intento por desafio`() {
        val best = attempt("c1", stars = 3, attemptNum = 1)
        val worse = attempt("c1", stars = 1, attemptNum = 2)
        assertEquals(170, ProgressEngine.totalXp(listOf(best, worse)))
    }

    @Test
    fun `segundo intento no tiene bono`() {
        val a = attempt("c2", stars = 2, attemptNum = 3)
        assertEquals(100, ProgressEngine.xpForAttempt(a))
    }

    @Test
    fun `dos desafios distintos suman xp`() {
        val a1 = attempt("c1", stars = 3, attemptNum = 1)
        val a3 = attempt("c2", stars = 2, attemptNum = 3)
        assertEquals(270, ProgressEngine.totalXp(listOf(a1, a3)))
    }

    @Test
    fun `intento fallido no otorga xp`() {
        val failed = attempt("c3", passed = false)
        assertEquals(0, ProgressEngine.xpForAttempt(failed))
    }

    @Test
    fun `cero xp es nivel 1`() {
        assertEquals(1, ProgressEngine.levelInfo(0).level)
    }

    @Test
    fun `320 xp es nivel 3`() {
        assertEquals(3, ProgressEngine.levelInfo(320).level)
    }

    @Test
    fun `xp enorme se limita al nivel maximo`() {
        val info = ProgressEngine.levelInfo(999_999)
        assertEquals(ProgressEngine.maxLevel(), info.level)
    }

    @Test
    fun `nivel maximo no tiene siguiente umbral`() {
        val info = ProgressEngine.levelInfo(999_999)
        assertNull(info.xpForNextLevel)
    }

    @Test
    fun `progreso al siguiente nivel es 1 en nivel maximo`() {
        val info = ProgressEngine.levelInfo(999_999)
        assertEquals(1f, info.progressToNextLevel, 0.001f)
    }
}
