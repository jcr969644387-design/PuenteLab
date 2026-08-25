package com.educalab.puentelab.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Proyección Room: un diseño junto con todos sus nodos y barras, en una sola consulta. */
data class DesignWithStructure(
    @Embedded val design: BridgeDesignEntity,
    @Relation(parentColumn = "id", entityColumn = "designId")
    val nodes: List<BridgeNodeEntity>,
    @Relation(parentColumn = "id", entityColumn = "designId")
    val members: List<BridgeMemberEntity>
)
