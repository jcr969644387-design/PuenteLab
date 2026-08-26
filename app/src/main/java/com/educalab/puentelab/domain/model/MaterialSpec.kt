package com.educalab.puentelab.domain.model

/**
 * Ficha técnica (simplificada, no profesional) de un material de construcción.
 *
 * @param strength capacidad de carga en "unidades de ingeniería PuenteLab" (arbitrarias, no MPa reales).
 * @param costPerUnit costo por unidad de longitud de barra.
 * @param weightFactor peso propio por unidad de longitud; genera carga muerta adicional real en el motor.
 * @param allowedRoles roles estructurales en los que este material puede usarse (p. ej. el cable solo en CABLE).
 */
data class MaterialSpec(
    val id: String,
    val name: String,
    val description: String,
    val strength: Double,
    val costPerUnit: Double,
    val weightFactor: Double,
    val allowedRoles: Set<MemberRole>,
    val unlockLevel: Int = 1
)
