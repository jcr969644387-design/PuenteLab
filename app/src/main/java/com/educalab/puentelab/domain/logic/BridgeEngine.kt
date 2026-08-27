package com.educalab.puentelab.domain.logic

import com.educalab.puentelab.domain.model.*
import kotlin.math.max

/**
 * Motor de simulación de puentes de PuenteLab.
 *
 * IMPORTANTE (honestidad pedagógica): este motor aplica reglas deterministas inspiradas en
 * conceptos reales de ingeniería estructural (conectividad, presupuesto, pendiente transitable,
 * capacidad de carga según material/longitud/triangulación) pero NO es un análisis de elementos
 * finitos ni sustituye cálculo de ingeniería profesional. Los valores de resistencia y carga son
 * unidades educativas propias de PuenteLab.
 *
 * Todas las funciones son puras (sin efectos secundarios, sin aleatoriedad) para poder testearse
 * de forma determinista.
 */
object BridgeEngine {

    // Penaliza barras "libres" (sin apoyo intermedio) más largas que este umbral.
    private const val SAFE_UNSUPPORTED_SPAN = 3.0
    private const val LENGTH_PENALTY_PER_UNIT = 0.18

    // Bonificadores de capacidad por tipo estructural, solo se aplican si la geometría los justifica.
    private const val TRUSS_BRACE_BONUS = 0.55
    private const val ARCH_BONUS = 0.6
    private const val SUSPENSION_CABLE_RELIEF = 0.8

    // La Torre sostiene directamente el tramo de calzada que toca, y ancla mejor los cables
    // que llegan a ella (además del alivio que ya da SUSPENSION_CABLE_RELIEF).
    private const val TOWER_DECK_SUPPORT_BONUS = 0.35
    private const val TOWER_CABLE_ANCHOR_BONUS = 0.3

    private const val USER_PIER_COST = 35.0

    fun simulate(
        design: BridgeDesignSpec,
        challenge: BridgeChallengeSpec,
        materials: Map<String, MaterialSpec>
    ): SimulationResult {
        val nodesById = design.nodes.associateBy { it.id }
        val reasons = mutableListOf<FailureReason>()

        // ---- 1. Costo y presupuesto (siempre se calcula, incluso si falla lo demás) ----
        val memberCosts = design.members.associate { m ->
            val a = nodesById[m.nodeAId]
            val b = nodesById[m.nodeBId]
            val length = if (a != null && b != null) a.point.distanceTo(b.point) else 0.0
            val cost = length * (materials[m.materialId]?.costPerUnit ?: 0.0)
            m.id to cost
        }
        val pierCost = design.nodes.count { it.isUserPier } * USER_PIER_COST
        val totalCost = memberCosts.values.sum() + pierCost
        val budgetRemaining = challenge.budget - totalCost
        if (totalCost > challenge.budget) reasons += FailureReason.OVER_BUDGET

        // ---- 2. Conectividad general orilla-orilla (con cualquier tipo de barra) ----
        val leftAnchors = design.nodes.filter { it.anchorSide == AnchorSide.LEFT }.map { it.id }.toSet()
        val rightAnchors = design.nodes.filter { it.anchorSide == AnchorSide.RIGHT }.map { it.id }.toSet()

        val fullAdjacency = buildAdjacency(design.members)
        val connected = leftAnchors.isNotEmpty() && rightAnchors.isNotEmpty() &&
            leftAnchors.any { start -> rightAnchors.any { end -> bfsConnected(fullAdjacency, start, end) } }
        if (!connected) reasons += FailureReason.DISCONNECTED

        // ---- 3. Ruta transitable (solo barras DECK, respetando pendiente máxima) ----
        val deckMembers = design.members.filter { it.role == MemberRole.DECK }
        val deckAdjacencyAll = buildAdjacency(deckMembers)
        val deckConnectedIgnoringSlope = leftAnchors.isNotEmpty() && rightAnchors.isNotEmpty() &&
            leftAnchors.any { s -> rightAnchors.any { e -> bfsConnected(deckAdjacencyAll, s, e) } }

        val deckMembersWithinSlope = deckMembers.filter { m ->
            val a = nodesById[m.nodeAId]?.point
            val b = nodesById[m.nodeBId]?.point
            a != null && b != null && a.slopeTo(b) <= challenge.maxSlope
        }
        val deckAdjacencySafe = buildAdjacency(deckMembersWithinSlope)

        var routePath: List<String> = emptyList()
        var routeFound = false
        outer@ for (s in leftAnchors) {
            for (e in rightAnchors) {
                val path = bfsPath(deckAdjacencySafe, s, e)
                if (path != null) {
                    routePath = path
                    routeFound = true
                    break@outer
                }
            }
        }

        if (!routeFound) {
            if (deckConnectedIgnoringSlope) {
                reasons += FailureReason.SLOPE_TOO_STEEP
            } else if (connected) {
                // Hay conexión estructural pero no una calzada continua (p.ej. solo cables/riostras)
                reasons += FailureReason.NO_VALID_ROUTE
            }
            // si ni siquiera hay conexión general, DISCONNECTED ya cubre el caso.
        }

        // ---- 4. Análisis de carga (siempre se calcula por barra para dar retroalimentación útil) ----
        val routeMemberIds: Set<String> = if (routeFound) {
            routePath.zipWithNext().mapNotNull { (a, b) -> findMemberBetween(deckMembersWithinSlope, a, b)?.id }.toSet()
        } else emptySet()

        val braceTouchingNode: Set<String> = design.members
            .filter { it.role == MemberRole.BRACE }
            .flatMap { listOf(it.nodeAId, it.nodeBId) }
            .toSet()

        val cableTouchingNode: Set<String> = design.members
            .filter { it.role == MemberRole.CABLE }
            .flatMap { listOf(it.nodeAId, it.nodeBId) }
            .toSet()

        val towerTouchingNode: Set<String> = design.members
            .filter { it.role == MemberRole.TOWER }
            .flatMap { listOf(it.nodeAId, it.nodeBId) }
            .toSet()

        val archApplies = routeFound && pathFormsArch(routePath, nodesById)

        val baseLoadPerDeckMember = if (routeMemberIds.isNotEmpty()) {
            challenge.demand.loadUnits / routeMemberIds.size
        } else 0.0

        val analyses = mutableListOf<MemberAnalysis>()
        var maxStress = 0.0
        var weakestId: String? = null

        for (m in design.members) {
            val a = nodesById[m.nodeAId]
            val b = nodesById[m.nodeBId]
            val length = if (a != null && b != null) a.point.distanceTo(b.point) else 0.0
            val material = materials[m.materialId]
            val cost = memberCosts[m.id] ?: 0.0

            var bonus = 1.0
            if (m.id in routeMemberIds) {
                val touchesBrace = braceTouchingNode.contains(m.nodeAId) || braceTouchingNode.contains(m.nodeBId)
                if (touchesBrace) bonus += TRUSS_BRACE_BONUS
                if (archApplies) bonus += ARCH_BONUS
                val touchesCable = cableTouchingNode.contains(m.nodeAId) || cableTouchingNode.contains(m.nodeBId)
                if (touchesCable) bonus += SUSPENSION_CABLE_RELIEF
                // la torre sostiene directamente el tramo de calzada que llega a su base
                val touchesTower = towerTouchingNode.contains(m.nodeAId) || towerTouchingNode.contains(m.nodeBId)
                if (touchesTower) bonus += TOWER_DECK_SUPPORT_BONUS
            }
            if (m.role == MemberRole.CABLE) {
                // un cable bien anclado a una torre aguanta más tensión
                val anchoredToTower = towerTouchingNode.contains(m.nodeAId) || towerTouchingNode.contains(m.nodeBId)
                if (anchoredToTower) bonus += TOWER_CABLE_ANCHOR_BONUS
            }

            val lengthFactor = 1.0 + max(0.0, length - SAFE_UNSUPPORTED_SPAN) * LENGTH_PENALTY_PER_UNIT
            val strength = material?.strength ?: 0.0
            val capacity = (strength * bonus) / lengthFactor

            val selfWeight = (material?.weightFactor ?: 0.0) * length
            var demand = selfWeight
            if (m.id in routeMemberIds) {
                demand += baseLoadPerDeckMember
            } else if (m.role == MemberRole.BRACE && (braceTouchingNode.contains(m.nodeAId) || braceTouchingNode.contains(m.nodeBId))) {
                demand += baseLoadPerDeckMember * (TRUSS_BRACE_BONUS / (1 + TRUSS_BRACE_BONUS))
            } else if (m.role == MemberRole.CABLE) {
                demand += baseLoadPerDeckMember * (SUSPENSION_CABLE_RELIEF / (1 + SUSPENSION_CABLE_RELIEF))
            }

            val stress = if (capacity > 0.0) demand / capacity else if (demand > 0.0) Double.POSITIVE_INFINITY else 0.0
            if (stress > maxStress) {
                maxStress = stress
                weakestId = m.id
            }
            analyses += MemberAnalysis(m.id, length, cost, capacity, demand, stress, m.role)
        }

        if (routeFound && maxStress > 1.0) reasons += FailureReason.OVERLOADED

        // ---- 5. Elementos obligatorios del escenario (una Calzada sola nunca alcanza) ----
        val requiredRoles = ScenarioRequirements.requiredRoles[challenge.scenario].orEmpty()
        val presentRoles = design.members.map { it.role }.toSet()
        val missingRoles = (requiredRoles - presentRoles).toList()
        if (missingRoles.isNotEmpty()) reasons += FailureReason.MISSING_ELEMENTS

        // ---- 6. Restricción especial del desafío (si tiene una) ----
        val constraintMessages = checkConstraint(challenge.id, design)
        if (constraintMessages.isNotEmpty()) reasons += FailureReason.CONSTRAINT_VIOLATED

        val passed = reasons.isEmpty()

        val budgetMarginRatio = if (challenge.budget > 0) budgetRemaining / challenge.budget else 0.0
        val stars = when {
            !passed -> 0
            budgetMarginRatio >= challenge.starThresholds.budgetMarginFor3Stars &&
                maxStress <= challenge.starThresholds.maxStressFor3Stars -> 3
            budgetMarginRatio >= challenge.starThresholds.budgetMarginFor2Stars && maxStress <= 0.9 -> 2
            else -> 1
        }

        val feedback = buildFeedback(passed, reasons, totalCost, challenge.budget, weakestId, materials, design, missingRoles, challenge.scenario, constraintMessages)

        return SimulationResult(
            passed = passed,
            failureReasons = reasons.distinct(),
            totalCost = totalCost,
            budget = challenge.budget,
            budgetRemaining = budgetRemaining,
            maxStressRatio = if (maxStress.isFinite()) maxStress else 9.99,
            weakestMemberId = weakestId,
            memberAnalyses = analyses,
            routeNodeIds = routePath,
            stars = stars,
            feedback = feedback
        )
    }

    // ---------- Geometría / grafo ----------

    private fun buildAdjacency(members: List<BridgeMember>): Map<String, List<Pair<String, BridgeMember>>> {
        val adj = mutableMapOf<String, MutableList<Pair<String, BridgeMember>>>()
        for (m in members) {
            adj.getOrPut(m.nodeAId) { mutableListOf() }.add(m.nodeBId to m)
            adj.getOrPut(m.nodeBId) { mutableListOf() }.add(m.nodeAId to m)
        }
        return adj
    }

    private fun bfsConnected(adj: Map<String, List<Pair<String, BridgeMember>>>, start: String, end: String): Boolean {
        return bfsPath(adj, start, end) != null
    }

    /** BFS que devuelve el camino de nodos más corto entre start y end, o null si no existe. */
    private fun bfsPath(adj: Map<String, List<Pair<String, BridgeMember>>>, start: String, end: String): List<String>? {
        if (start == end) return listOf(start)
        val visited = mutableSetOf(start)
        val queue: ArrayDeque<String> = ArrayDeque()
        queue.add(start)
        val prev = mutableMapOf<String, String>()
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            val neighbors = adj[cur] ?: emptyList()
            for ((next, _) in neighbors) {
                if (next !in visited) {
                    visited += next
                    prev[next] = cur
                    if (next == end) {
                        // reconstruir camino
                        val path = mutableListOf(end)
                        var n = end
                        while (n != start) {
                            n = prev[n]!!
                            path.add(n)
                        }
                        return path.reversed()
                    }
                    queue.add(next)
                }
            }
        }
        return null
    }

    private fun findMemberBetween(members: List<BridgeMember>, aId: String, bId: String): BridgeMember? =
        members.firstOrNull { (it.nodeAId == aId && it.nodeBId == bId) || (it.nodeAId == bId && it.nodeBId == aId) }

    /**
     * Determina si el camino de calzada forma un arco: existe un nodo interior cuya altura
     * (y menor = más arriba) es estrictamente mayor que la de ambas orillas del camino.
     */
    private fun pathFormsArch(path: List<String>, nodesById: Map<String, BridgeNode>): Boolean {
        if (path.size < 3) return false
        val ys = path.mapNotNull { nodesById[it]?.point?.y }
        if (ys.size != path.size) return false
        val startY = ys.first()
        val endY = ys.last()
        val interior = ys.subList(1, ys.size - 1)
        return interior.any { it < startY - 0.5 && it < endY - 0.5 }
    }

    /** Revisa la restricción especial del desafío (si tiene una) y devuelve los avisos que aplican. */
    private fun checkConstraint(challengeId: String, design: BridgeDesignSpec): List<String> {
        val constraint = MissionConstraints.byChallengeId[challengeId] ?: return emptyList()
        val msgs = mutableListOf<String>()
        val counts = design.members.groupingBy { it.role }.eachCount()
        if (constraint.maxMembers != null && design.members.size > constraint.maxMembers) {
            msgs += "Te pasaste del límite de barras (máximo ${constraint.maxMembers}). Simplifica el diseño."
        }
        if (constraint.maxCables != null && (counts[MemberRole.CABLE] ?: 0) > constraint.maxCables) {
            msgs += "Usaste más Cables 🪢 de los permitidos (máximo ${constraint.maxCables})."
        }
        if (constraint.maxBraces != null && (counts[MemberRole.BRACE] ?: 0) > constraint.maxBraces) {
            msgs += "Usaste más Riostras 🔺 de las permitidas (máximo ${constraint.maxBraces})."
        }
        if (constraint.maxTowers != null && (counts[MemberRole.TOWER] ?: 0) > constraint.maxTowers) {
            msgs += "Usaste más Torres 🗼 de las permitidas (máximo ${constraint.maxTowers})."
        }
        if (constraint.bannedMaterialIds.isNotEmpty()) {
            val used = design.members.map { it.materialId }.filter { it in constraint.bannedMaterialIds }.toSet()
            if (used.isNotEmpty()) msgs += "Este desafío no permite usar ese material aquí. Prueba con otro."
        }
        return msgs
    }

    private fun buildFeedback(
        passed: Boolean,
        reasons: List<FailureReason>,
        totalCost: Double,
        budget: Double,
        weakestId: String?,
        materials: Map<String, MaterialSpec>,
        design: BridgeDesignSpec,
        missingRoles: List<MemberRole>,
        scenario: ScenarioType,
        constraintMessages: List<String>
    ): List<String> {
        if (passed) {
            val used = "%.0f".format(totalCost)
            val total = "%.0f".format(budget)
            val msgs = mutableListOf("¡El vehículo cruzó sin problemas! Usaste $${used} de $${total} de presupuesto.")
            val seenCombos = mutableSetOf<String>()
            for (m in design.members) {
                val combo = comboBonusMessage(scenario, m.role, m.materialId) ?: continue
                if (seenCombos.add(combo)) msgs += combo
            }
            return msgs
        }
        val msgs = mutableListOf<String>()
        for (r in reasons.distinct()) {
            when (r) {
                FailureReason.OVER_BUDGET -> {
                    val over = "%.0f".format(totalCost - budget)
                    msgs += "${r.message} Te pasaste por $${over}."
                }
                FailureReason.OVERLOADED -> {
                    val weakMember = design.members.firstOrNull { it.id == weakestId }
                    val matName = weakMember?.let { materials[it.materialId]?.name }
                    msgs += overloadedMessage(weakMember?.role, matName)
                }
                FailureReason.MISSING_ELEMENTS -> {
                    missingRoles.forEach { role -> msgs += missingRoleMessage(role) }
                }
                FailureReason.CONSTRAINT_VIOLATED -> {
                    constraintMessages.forEach { msgs += it }
                }
                else -> msgs += r.message
            }
        }
        return msgs
    }

    /** Explica qué parte falta y por qué, en vez de solo decir "faltan cosas". */
    private fun missingRoleMessage(role: MemberRole): String = when (role) {
        MemberRole.TOWER -> "Falta una Torre 🗼: sin ella, nada sostiene bien la estructura."
        MemberRole.CABLE -> "Falta un Cable 🪢: necesitas uno para dar tensión al puente."
        MemberRole.BRACE -> "Falta una Riostra 🔺: agrégala para que el puente no se deforme."
        MemberRole.DECK -> "Falta la Calzada 🛣️: sin ella no hay por dónde cruzar."
    }

    /** Explica cuál pieza colapsó y por qué, según su función en el puente. */
    private fun overloadedMessage(role: MemberRole?, matName: String?): String {
        val mat = matName ?: "material"
        return when (role) {
            MemberRole.CABLE -> "El $mat no soportó tanta tensión. Prueba un cable más resistente."
            MemberRole.TOWER -> "La torre de $mat no pudo sostener tanto peso. Prueba una torre más resistente."
            MemberRole.BRACE -> "La riostra de $mat no evitó que el puente se deformara de más. Prueba una riostra más resistente."
            else -> "La calzada de $mat no aguantó el peso del vehículo. Prueba un material más resistente o añade una riostra de refuerzo."
        }
    }

    /** Mensaje de aliento cuando el jugador usó, para un rol, el material que mejor le queda al escenario. */
    private fun comboBonusMessage(scenario: ScenarioType, role: MemberRole, materialId: String): String? {
        val info = ScenarioEducation.byScenario[scenario] ?: return null
        if (info.recommendedMaterialByRole[role] != materialId) return null
        return when {
            scenario == ScenarioType.RIVER && role == MemberRole.CABLE ->
                "💡 Buena elección: el Cable de Acero le da mucha estabilidad a un puente de río."
            scenario == ScenarioType.RIVER && role == MemberRole.TOWER ->
                "💡 Buena elección: el Hormigón hace que las torres del río sean muy resistentes."
            scenario == ScenarioType.CITY && role == MemberRole.CABLE ->
                "💡 Buena elección: la Fibra de Carbono aligera tu puente sin perder resistencia."
            scenario == ScenarioType.FOREST ->
                "💡 Buena elección: usar Madera en el Bosque Profundo ahorra recursos."
            else -> null
        }
    }
}
