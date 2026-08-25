package com.educalab.puentelab.data.local.converters

import androidx.room.TypeConverter
import com.educalab.puentelab.domain.model.*

/**
 * Convertidores de Room. Se guardan enums como texto (nombre) y listas/conjuntos simples
 * como CSV o "x,y;x,y" para evitar dependencias externas de JSON en la capa de datos.
 */
class Converters {

    // ---- Enums simples ----
    @TypeConverter fun fromScenario(v: ScenarioType): String = v.name
    @TypeConverter fun toScenario(v: String): ScenarioType = ScenarioType.valueOf(v)

    @TypeConverter fun fromDemand(v: DemandLevel): String = v.name
    @TypeConverter fun toDemand(v: String): DemandLevel = DemandLevel.valueOf(v)

    @TypeConverter fun fromRole(v: MemberRole): String = v.name
    @TypeConverter fun toRole(v: String): MemberRole = MemberRole.valueOf(v)

    @TypeConverter fun fromStructureType(v: StructureType): String = v.name
    @TypeConverter fun toStructureType(v: String): StructureType = StructureType.valueOf(v)

    @TypeConverter fun fromStructureTypeNullable(v: StructureType?): String? = v?.name
    @TypeConverter fun toStructureTypeNullable(v: String?): StructureType? = v?.let { StructureType.valueOf(it) }

    @TypeConverter fun fromAnchorSide(v: AnchorSide): String = v.name
    @TypeConverter fun toAnchorSide(v: String): AnchorSide = AnchorSide.valueOf(v)

    @TypeConverter fun fromModuleState(v: ModuleState): String = v.name
    @TypeConverter fun toModuleState(v: String): ModuleState = ModuleState.valueOf(v)

    @TypeConverter fun fromBadgeId(v: BadgeId): String = v.name
    @TypeConverter fun toBadgeId(v: String): BadgeId = BadgeId.valueOf(v)

    // ---- Set<MemberRole> como CSV: "DECK,BRACE" ----
    @TypeConverter
    fun fromRoleSet(v: Set<MemberRole>): String = v.joinToString(",") { it.name }

    @TypeConverter
    fun toRoleSet(v: String): Set<MemberRole> =
        if (v.isBlank()) emptySet() else v.split(",").map { MemberRole.valueOf(it) }.toSet()

    // ---- Set<StructureType> como CSV (para registrar qué tipos se usaron en un intento) ----
    @TypeConverter
    fun fromStructureTypeSet(v: Set<StructureType>): String = v.joinToString(",") { it.name }

    @TypeConverter
    fun toStructureTypeSet(v: String): Set<StructureType> =
        if (v.isBlank()) emptySet() else v.split(",").map { StructureType.valueOf(it) }.toSet()

    // ---- List<GridPoint> como "x:y;x:y" (para apoyos fijos de un desafío) ----
    @TypeConverter
    fun fromPointList(v: List<GridPoint>): String = v.joinToString(";") { "${it.x}:${it.y}" }

    @TypeConverter
    fun toPointList(v: String): List<GridPoint> =
        if (v.isBlank()) emptyList() else v.split(";").map {
            val (x, y) = it.split(":")
            GridPoint(x.toDouble(), y.toDouble())
        }
}
