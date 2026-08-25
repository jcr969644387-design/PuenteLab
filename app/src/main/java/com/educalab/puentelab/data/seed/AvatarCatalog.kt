package com.educalab.puentelab.data.seed

/** 8 avatares locales (dibujados con Compose Canvas, sin fotos ni assets externos). */
data class AvatarOption(val id: String, val label: String, val primaryHex: String, val accentHex: String)

object AvatarCatalog {
    val all: List<AvatarOption> = listOf(
        AvatarOption("avatar_casco_naranja", "Casco Naranja", "#F2994A", "#0D2A4A"),
        AvatarOption("avatar_casco_azul", "Casco Azul", "#2E6DB4", "#F5C34C"),
        AvatarOption("avatar_casco_verde", "Casco Verde", "#3F8F5D", "#F7F5EF"),
        AvatarOption("avatar_casco_violeta", "Casco Violeta", "#6B5CA5", "#F5C34C"),
        AvatarOption("avatar_casco_rojo", "Casco Rojo", "#C9683D", "#0D2A4A"),
        AvatarOption("avatar_casco_teal", "Casco Turquesa", "#2CAFC0", "#1C2530"),
        AvatarOption("avatar_casco_gris", "Casco Grafito", "#5C6B7A", "#F5C34C"),
        AvatarOption("avatar_casco_dorado", "Casco Dorado", "#D8A93B", "#1C2530")
    )
}
