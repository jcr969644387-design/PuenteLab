package com.educalab.puentelab.data.seed

/**
 * 8 avatares locales (dibujados con Compose Canvas, sin fotos ni assets externos): niño o niña
 * con casco y chaleco de ingeniero, con distintos tonos de piel, peinados y colores de casco.
 */
enum class HairStyle { SHORT, PONYTAIL, CURLY, BUZZCUT }

data class AvatarOption(
    val id: String,
    val label: String,
    val helmetHex: String,
    val skinHex: String,
    val hairHex: String,
    val hairStyle: HairStyle
)

object AvatarCatalog {
    val all: List<AvatarOption> = listOf(
        AvatarOption("avatar_casco_naranja", "Casco Naranja", "#F2994A", "#E8B08A", "#3B2A20", HairStyle.SHORT),
        AvatarOption("avatar_casco_azul", "Casco Azul", "#2E6DB4", "#F5CBA0", "#1C1C1C", HairStyle.PONYTAIL),
        AvatarOption("avatar_casco_verde", "Casco Verde", "#3F8F5D", "#8D5A3C", "#241A14", HairStyle.CURLY),
        AvatarOption("avatar_casco_violeta", "Casco Violeta", "#6B5CA5", "#F0C9A0", "#6B4423", HairStyle.PONYTAIL),
        AvatarOption("avatar_casco_rojo", "Casco Rojo", "#C9683D", "#3E2A1F", "#0D0D0D", HairStyle.BUZZCUT),
        AvatarOption("avatar_casco_teal", "Casco Turquesa", "#2CAFC0", "#C68A5B", "#4A2E1A", HairStyle.SHORT),
        AvatarOption("avatar_casco_gris", "Casco Grafito", "#5C6B7A", "#F5D5B8", "#D4A24C", HairStyle.CURLY),
        AvatarOption("avatar_casco_dorado", "Casco Dorado", "#D8A93B", "#6B4226", "#1A1A1A", HairStyle.BUZZCUT)
    )
}
