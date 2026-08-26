package com.educalab.puentelab.data.seed

import com.educalab.puentelab.data.local.entity.MaterialEntity
import com.educalab.puentelab.domain.model.MemberRole

/**
 * Catálogo de materiales. Los valores de resistencia/costo/peso son unidades educativas de
 * PuenteLab (no valores de ingeniería profesional real) pero mantienen relaciones coherentes
 * entre sí: más resistencia y menos peso siempre cuestan más.
 */
object SeedMaterials {
    val all: List<MaterialEntity> = listOf(
        MaterialEntity(
            id = "wood", name = "Madera de Obra",
            description = "Ligera y económica, con resistencia media. Sirve para calzadas, riostras y torres sencillas.",
            strength = 40.0, costPerUnit = 3.0, weightFactor = 0.15,
            allowedRoles = setOf(MemberRole.DECK, MemberRole.BRACE, MemberRole.TOWER),
            colorHex = "#B4783C", iconKey = "material_wood", unlockLevel = 1
        ),
        MaterialEntity(
            id = "rope", name = "Cuerda Reforzada",
            description = "Muy ligera y barata, pero aguanta poco. Útil como cable simple en estructuras pequeñas.",
            strength = 20.0, costPerUnit = 2.0, weightFactor = 0.05,
            allowedRoles = setOf(MemberRole.CABLE, MemberRole.BRACE),
            colorHex = "#D8C08A", iconKey = "material_rope", unlockLevel = 1
        ),
        MaterialEntity(
            id = "stone", name = "Piedra Tallada",
            description = "Muy resistente cuando la aplastas, pero pesada y poco flexible. Ideal para torres y soportes.",
            strength = 100.0, costPerUnit = 5.0, weightFactor = 0.5,
            allowedRoles = setOf(MemberRole.TOWER, MemberRole.BRACE),
            colorHex = "#8C8C82", iconKey = "material_stone", unlockLevel = 1
        ),
        MaterialEntity(
            id = "steel", name = "Acero Estructural",
            description = "Muy resistente y el más versátil de todos: sirve para calzadas, riostras y torres.",
            strength = 90.0, costPerUnit = 6.0, weightFactor = 0.25,
            allowedRoles = setOf(MemberRole.DECK, MemberRole.BRACE, MemberRole.TOWER),
            colorHex = "#5C7A99", iconKey = "material_steel", unlockLevel = 1
        ),
        MaterialEntity(
            id = "steel_cable", name = "Cable de Acero",
            description = "Aguanta muchísimo cuando lo estiras. El cable ideal para cargas grandes.",
            strength = 70.0, costPerUnit = 8.0, weightFactor = 0.10,
            allowedRoles = setOf(MemberRole.CABLE),
            colorHex = "#3E5266", iconKey = "material_cable", unlockLevel = 2
        ),
        MaterialEntity(
            id = "concrete", name = "Hormigón Armado",
            description = "Muy resistente pero pesado, aguanta cargas grandes. Ideal para calzadas firmes y torres.",
            strength = 110.0, costPerUnit = 9.0, weightFactor = 0.40,
            allowedRoles = setOf(MemberRole.DECK, MemberRole.TOWER),
            colorHex = "#9AA0A6", iconKey = "material_concrete", unlockLevel = 3
        ),
        MaterialEntity(
            id = "aluminum", name = "Aluminio Aeronáutico",
            description = "Ligero y resistente, aunque caro. Bueno para riostras cuando quieres bajar el peso del puente.",
            strength = 65.0, costPerUnit = 7.0, weightFactor = 0.12,
            allowedRoles = setOf(MemberRole.BRACE, MemberRole.TOWER),
            colorHex = "#C7CDD6", iconKey = "material_aluminum", unlockLevel = 4
        ),
        MaterialEntity(
            id = "carbon_fiber", name = "Fibra de Carbono",
            description = "Muy ligera y muy resistente, pero cara. Perfecta para cables y riostras exigentes.",
            strength = 130.0, costPerUnit = 14.0, weightFactor = 0.08,
            allowedRoles = setOf(MemberRole.CABLE, MemberRole.BRACE),
            colorHex = "#2B2E33", iconKey = "material_carbon", unlockLevel = 6
        )
    )
}
