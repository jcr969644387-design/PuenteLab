package com.educalab.puentelab.domain.model

enum class AnchorSide { LEFT, RIGHT, NONE }

/**
 * Nodo (nudo) de la estructura. Puede ser una orilla fija del nivel, un apoyo intermedio
 * ya provisto por el escenario, o un nodo libre colocado por el jugador.
 */
data class BridgeNode(
    val id: String,
    val point: GridPoint,
    val anchorSide: AnchorSide = AnchorSide.NONE,
    val isFixedByLevel: Boolean = false,   // nodo provisto por el nivel (orillas, apoyos gratuitos)
    val isUserPier: Boolean = false        // apoyo adicional colocado por el jugador (tiene costo)
) {
    val isAnchor: Boolean get() = anchorSide != AnchorSide.NONE
}

/** Barra que conecta dos nodos usando un material y cumpliendo un rol estructural. */
data class BridgeMember(
    val id: String,
    val nodeAId: String,
    val nodeBId: String,
    val materialId: String,
    val role: MemberRole,
    val structureType: StructureType
)

/** Diseño completo: conjunto de nodos y barras para un desafío concreto. */
data class BridgeDesignSpec(
    val id: String,
    val challengeId: String,
    val name: String,
    val nodes: List<BridgeNode>,
    val members: List<BridgeMember>
)
