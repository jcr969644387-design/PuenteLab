package com.educalab.puentelab.domain

import com.educalab.puentelab.domain.logic.BadgeEngine
import com.educalab.puentelab.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class BadgeEngineTest {

    private fun attempt(
        id: String, scenario: ScenarioType = ScenarioType.RIVER, passed: Boolean = true,
        stars: Int = 2, types: Set<StructureType> = setOf(StructureType.BEAM),
        budgetRatio: Double = 0.5, attemptNum: Int = 1
    ) = ChallengeAttempt(id, scenario, passed, stars, types, budgetRatio, attemptNum)

    @Test
    fun `sin intentos no hay insignias`() {
        assertTrue(BadgeEngine.unlockedBadges(emptyList()).isEmpty())
    }

    @Test
    fun `primer desafio aprobado desbloquea PRIMER_PUENTE`() {
        val badges = BadgeEngine.unlockedBadges(listOf(attempt("c1")))
        assertTrue(BadgeId.PRIMER_PUENTE in badges)
    }

    @Test
    fun `un desafio por escenario desbloquea EXPLORADOR`() {
        val attempts = ScenarioType.values().mapIndexed { i, s -> attempt("c$i", scenario = s) }
        assertTrue(BadgeId.EXPLORADOR in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `4 de 5 escenarios no desbloquea EXPLORADOR`() {
        val attempts = ScenarioType.values().take(4).mapIndexed { i, s -> attempt("c$i", scenario = s) }
        assertFalse(BadgeId.EXPLORADOR in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `5 arcos desbloquea MAESTRO_ARCO`() {
        val attempts = (1..5).map { attempt("arch$it", types = setOf(StructureType.ARCH)) }
        assertTrue(BadgeId.MAESTRO_ARCO in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `4 arcos no desbloquea MAESTRO_ARCO`() {
        val attempts = (1..4).map { attempt("arch$it", types = setOf(StructureType.ARCH)) }
        assertFalse(BadgeId.MAESTRO_ARCO in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `5 cerchas desbloquea INGENIERO_CERCHA`() {
        val attempts = (1..5).map { attempt("truss$it", types = setOf(StructureType.TRUSS)) }
        assertTrue(BadgeId.INGENIERO_CERCHA in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `3 suspensiones desbloquea MAESTRO_SUSPENSION`() {
        val attempts = (1..3).map { attempt("susp$it", types = setOf(StructureType.SUSPENSION)) }
        assertTrue(BadgeId.MAESTRO_SUSPENSION in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `5 aprobados baratos desbloquea PRESUPUESTO_DE_ORO`() {
        val attempts = (1..5).map { attempt("g$it", budgetRatio = 0.6) }
        assertTrue(BadgeId.PRESUPUESTO_DE_ORO in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `5 aprobados caros no desbloquea PRESUPUESTO_DE_ORO`() {
        val attempts = (1..5).map { attempt("e$it", budgetRatio = 0.95) }
        assertFalse(BadgeId.PRESUPUESTO_DE_ORO in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `10 aprobados a la primera desbloquea SIN_FALLOS`() {
        val attempts = (1..10).map { attempt("ft$it", attemptNum = 1) }
        assertTrue(BadgeId.SIN_FALLOS in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `un reintento entre diez rompe SIN_FALLOS`() {
        val attempts = (1..9).map { attempt("ft$it", attemptNum = 1) } + attempt("ft10", attemptNum = 2)
        assertFalse(BadgeId.SIN_FALLOS in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `10 desafios con 3 estrellas desbloquea COLECCIONISTA`() {
        val attempts = (1..10).map { attempt("s$it", stars = 3) }
        assertTrue(BadgeId.COLECCIONISTA in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `25 aprobados desbloquea VETERANO`() {
        val attempts = (1..25).map { attempt("v$it") }
        assertTrue(BadgeId.VETERANO in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `24 aprobados no desbloquea VETERANO`() {
        val attempts = (1..24).map { attempt("v$it") }
        assertFalse(BadgeId.VETERANO in BadgeEngine.unlockedBadges(attempts))
    }

    @Test
    fun `catalogo tiene al menos 8 insignias`() {
        assertTrue(BadgeEngine.catalog.size >= 8)
    }

    @Test
    fun `catalogo no tiene ids duplicados`() {
        val ids = BadgeEngine.catalog.map { it.id }
        assertEquals(ids.distinct().size, ids.size)
    }
}
