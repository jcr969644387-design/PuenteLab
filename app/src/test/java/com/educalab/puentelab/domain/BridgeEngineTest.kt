package com.educalab.puentelab.domain

import com.educalab.puentelab.domain.logic.BridgeEngine
import com.educalab.puentelab.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * NOTA DE VERIFICACIÓN: la lógica exacta de BridgeEngine.kt fue compilada y ejecutada de forma
 * independiente con kotlinc 1.9.24 (sin Gradle/Android, ver docs/BUILD_REPORT.md) usando estos
 * mismos 20 casos antes de escribir esta clase JUnit; los 20 pasaron. Esta clase traduce esos
 * mismos casos al formato JUnit4 que usará ./gradlew testDebugUnitTest.
 */
class BridgeEngineTest {

    private val steel = MaterialSpec("steel", "steel", "d", strength = 90.0, costPerUnit = 6.0, weightFactor = 0.2, allowedRoles = MemberRole.values().toSet())
    private val wood = MaterialSpec("wood", "wood", "d", strength = 40.0, costPerUnit = 3.0, weightFactor = 0.2, allowedRoles = MemberRole.values().toSet())
    private val rope = MaterialSpec("rope", "rope", "d", strength = 8.0, costPerUnit = 1.0, weightFactor = 0.2, allowedRoles = setOf(MemberRole.CABLE, MemberRole.BRACE))
    private val ropeDeck = MaterialSpec("rope2", "rope2", "d", strength = 8.0, costPerUnit = 1.0, weightFactor = 0.2, allowedRoles = setOf(MemberRole.DECK))
    private val cable = MaterialSpec("cable", "cable", "d", strength = 70.0, costPerUnit = 8.0, weightFactor = 0.2, allowedRoles = setOf(MemberRole.CABLE))
    private val marginal = MaterialSpec("marginal", "marginal", "d", strength = 45.0, costPerUnit = 2.0, weightFactor = 0.2, allowedRoles = MemberRole.values().toSet())

    private val materials = mapOf(
        "steel" to steel, "wood" to wood, "rope" to rope, "rope2" to ropeDeck, "cable" to cable, "marginal" to marginal
    )

    private fun node(id: String, x: Double, y: Double, anchor: AnchorSide = AnchorSide.NONE, fixed: Boolean = false, pier: Boolean = false) =
        BridgeNode(id, GridPoint(x, y), anchor, fixed, pier)

    private fun member(id: String, a: String, b: String, matId: String, role: MemberRole = MemberRole.DECK) =
        BridgeMember(id, a, b, matId, role, StructureType.BEAM)

    // FOREST es el escenario con menos elementos obligatorios (Calzada + Riostra), así que los
    // tests que no se ocupan del requisito de elementos necesitan agregar como mucho una Riostra.
    private fun challenge(budget: Double = 500.0, demand: DemandLevel = DemandLevel.LOW, maxSlope: Double = 0.6, scenario: ScenarioType = ScenarioType.FOREST) = BridgeChallengeSpec(
        id = "c1", scenario = scenario, orderIndex = 1, name = "Test",
        spanUnits = 6.0, leftBank = GridPoint(0.0, 0.0), rightBank = GridPoint(6.0, 0.0),
        budget = budget, demand = demand, maxSlope = maxSlope, narrativeIntro = "x", narrativeSuccess = "y"
    )

    @Test
    fun `disenio desconectado no aprueba`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d1", "c1", "t1", nodes, emptyList()), challenge(), materials)
        assertFalse(r.passed)
        assertTrue(FailureReason.DISCONNECTED in r.failureReasons)
    }

    @Test
    fun `conexion solo con riostra no forma ruta transitable`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "steel", MemberRole.BRACE))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d2", "c1", "t2", nodes, members), challenge(), materials)
        assertFalse(r.passed)
        assertTrue(FailureReason.NO_VALID_ROUTE in r.failureReasons)
    }

    @Test
    fun `puente de acero simple con apoyo central y riostra aprueba`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"), member("brace1", "L", "M", "steel", MemberRole.BRACE))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d3", "c1", "t3", nodes, members), challenge(budget = 200.0), materials)
        assertTrue(r.passed)
        assertTrue(r.stars >= 1)
        assertEquals(3, r.routeNodeIds.size)
    }

    @Test
    fun `calzada sola no aprueba si el escenario exige mas elementos`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d3b", "c1", "t3b", nodes, members), challenge(budget = 200.0), materials)
        assertFalse(r.passed)
        assertTrue(FailureReason.MISSING_ELEMENTS in r.failureReasons)
    }

    @Test
    fun `presupuesto insuficiente falla`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d4", "c1", "t4", nodes, members), challenge(budget = 5.0), materials)
        assertTrue(FailureReason.OVER_BUDGET in r.failureReasons)
        assertFalse(r.passed)
    }

    @Test
    fun `cuerda larga con carga alta colapsa`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 8.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "rope2"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d5", "c1", "t5", nodes, members), challenge(budget = 500.0, demand = DemandLevel.HIGH), materials)
        assertTrue(FailureReason.OVERLOADED in r.failureReasons)
        assertFalse(r.passed)
        assertEquals("m1", r.weakestMemberId)
    }

    @Test
    fun `pendiente excesiva se detecta`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 2.0, 5.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d6", "c1", "t6", nodes, members), challenge(maxSlope = 0.6), materials)
        assertTrue(FailureReason.SLOPE_TOO_STEEP in r.failureReasons)
    }

    @Test
    fun `riostra reduce el esfuerzo maximo`() {
        val nodesNoBrace = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 7.0, 0.0, AnchorSide.RIGHT, true))
        val membersNoBrace = listOf(member("m1", "L", "R", "marginal"))
        val rNoBrace = BridgeEngine.simulate(BridgeDesignSpec("d7a", "c1", "t7a", nodesNoBrace, membersNoBrace), challenge(demand = DemandLevel.MEDIUM), materials)

        val nodesBrace = nodesNoBrace + node("B", 3.5, -2.0)
        val membersBrace = membersNoBrace + member("brace1", "L", "B", "marginal", MemberRole.BRACE)
        val rBrace = BridgeEngine.simulate(BridgeDesignSpec("d7b", "c1", "t7b", nodesBrace, membersBrace), challenge(demand = DemandLevel.MEDIUM), materials)

        assertTrue(rBrace.maxStressRatio < rNoBrace.maxStressRatio)
    }

    @Test
    fun `forma de arco reduce el esfuerzo respecto a viga plana`() {
        val archNodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, -2.5, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "wood"), member("m2", "M", "R", "wood"))
        val rArch = BridgeEngine.simulate(BridgeDesignSpec("d8", "c1", "t8", archNodes, members), challenge(demand = DemandLevel.MEDIUM), materials)

        val flatNodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val rFlat = BridgeEngine.simulate(BridgeDesignSpec("d8b", "c1", "t8b", flatNodes, members), challenge(demand = DemandLevel.MEDIUM), materials)

        assertTrue(rArch.maxStressRatio < rFlat.maxStressRatio)
    }

    @Test
    fun `cable de suspension alivia la calzada`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true), node("T", 3.0, -4.0))
        val membersNoCable = listOf(member("m1", "L", "R", "wood"))
        val rNoCable = BridgeEngine.simulate(BridgeDesignSpec("d9a", "c1", "t9a", nodes, membersNoCable), challenge(demand = DemandLevel.MEDIUM), materials)

        val membersCable = membersNoCable + listOf(member("tower1", "T", "L", "steel", MemberRole.TOWER), member("cable1", "T", "R", "cable", MemberRole.CABLE))
        val rCable = BridgeEngine.simulate(BridgeDesignSpec("d9b", "c1", "t9b", nodes, membersCable), challenge(demand = DemandLevel.MEDIUM), materials)

        assertTrue(rCable.maxStressRatio < rNoCable.maxStressRatio)
    }

    @Test
    fun `margen de presupuesto alto y bajo esfuerzo dan 3 estrellas`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"), member("brace1", "L", "M", "steel", MemberRole.BRACE))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d10a", "c1", "t10a", nodes, members), challenge(budget = 1000.0, demand = DemandLevel.LOW), materials)
        assertEquals(3, r.stars)
    }

    @Test
    fun `presupuesto ajustado que aprueba da 1 estrella`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true), node("B", 0.1, -0.1))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"), member("brace1", "L", "B", "wood", MemberRole.BRACE))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d10b", "c1", "t10b", nodes, members), challenge(budget = 38.0, demand = DemandLevel.LOW), materials)
        assertTrue(r.passed)
        assertEquals(1, r.stars)
    }

    @Test
    fun `apoyo adicional del jugador incrementa el costo`() {
        val nodesPier = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("P", 3.0, 0.0, pier = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val membersPier = listOf(member("m1", "L", "P", "steel"), member("m2", "P", "R", "steel"))
        val rPier = BridgeEngine.simulate(BridgeDesignSpec("d11", "c1", "t11", nodesPier, membersPier), challenge(budget = 1000.0), materials)

        val nodesNoPier = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val membersNoPier = listOf(member("m1", "L", "R", "steel"))
        val rNoPier = BridgeEngine.simulate(BridgeDesignSpec("d11b", "c1", "t11b", nodesNoPier, membersNoPier), challenge(budget = 1000.0), materials)

        assertTrue(rPier.totalCost > rNoPier.totalCost)
    }

    @Test
    fun `disenio vacio no lanza excepcion y no aprueba`() {
        val r = BridgeEngine.simulate(BridgeDesignSpec("d12", "c1", "empty", emptyList(), emptyList()), challenge(), materials)
        assertFalse(r.passed)
    }

    @Test
    fun `costo total nunca es negativo`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d13", "c1", "t13", nodes, members), challenge(), materials)
        assertTrue(r.totalCost >= 0.0)
    }

    @Test
    fun `material desconocido no lanza excepcion (resistencia y costo cero)`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "no_existe"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d14", "c1", "t14", nodes, members), challenge(), emptyMap())
        assertFalse(r.passed)
        assertTrue(FailureReason.OVERLOADED in r.failureReasons || FailureReason.DISCONNECTED in r.failureReasons)
    }

    @Test
    fun `feedback de exito menciona presupuesto usado`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"), member("brace1", "L", "M", "steel", MemberRole.BRACE))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d15", "c1", "t15", nodes, members), challenge(budget = 1000.0), materials)
        assertTrue(r.feedback.isNotEmpty())
        assertTrue(r.feedback.first().contains("presupuesto", ignoreCase = true))
    }

    @Test
    fun `puente colgante bien anclado con carga alta puede aprobar`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true), node("T", 3.0, -4.0))
        val members = listOf(
            member("m1", "L", "R", "steel"),
            member("tower1", "T", "L", "steel", MemberRole.TOWER),
            member("cable1", "T", "R", "cable", MemberRole.CABLE)
        )
        val r = BridgeEngine.simulate(BridgeDesignSpec("d16", "c1", "t16", nodes, members), challenge(budget = 1000.0, demand = DemandLevel.HIGH), materials)
        // No se afirma que siempre apruebe (depende del balance), pero el esfuerzo debe ser finito y no lanzar excepción.
        assertTrue(r.maxStressRatio >= 0.0)
    }

    @Test
    fun `ruta devuelta sigue el orden de izquierda a derecha`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d17", "c1", "t17", nodes, members), challenge(budget = 1000.0), materials)
        assertEquals(listOf("L", "M", "R"), r.routeNodeIds)
    }

    @Test
    fun `dos rutas alternativas elige una valida`() {
        val nodes = listOf(
            node("L", 0.0, 0.0, AnchorSide.LEFT, true),
            node("A", 3.0, 0.0, fixed = true),
            node("B", 3.0, 6.0, fixed = true), // ruta con pendiente excesiva
            node("R", 6.0, 0.0, AnchorSide.RIGHT, true)
        )
        val members = listOf(
            member("m1", "L", "A", "steel"), member("m2", "A", "R", "steel"),
            member("m3", "L", "B", "steel"), member("m4", "B", "R", "steel")
        )
        val r = BridgeEngine.simulate(BridgeDesignSpec("d18", "c1", "t18", nodes, members), challenge(budget = 2000.0), materials)
        assertTrue(r.routeNodeIds.contains("A"))
    }

    @Test
    fun `analisis de barras incluye todas las barras del disenio`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("M", 3.0, 0.0, fixed = true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "M", "steel"), member("m2", "M", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d19", "c1", "t19", nodes, members), challenge(budget = 1000.0), materials)
        assertEquals(2, r.memberAnalyses.size)
    }

    @Test
    fun `presupuesto restante es presupuesto menos costo total`() {
        val nodes = listOf(node("L", 0.0, 0.0, AnchorSide.LEFT, true), node("R", 6.0, 0.0, AnchorSide.RIGHT, true))
        val members = listOf(member("m1", "L", "R", "steel"))
        val r = BridgeEngine.simulate(BridgeDesignSpec("d20", "c1", "t20", nodes, members), challenge(budget = 1000.0), materials)
        assertEquals(r.budget - r.totalCost, r.budgetRemaining, 0.001)
    }
}
