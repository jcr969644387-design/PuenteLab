package com.educalab.puentelab.data.seed

/**
 * 8 avatares locales (dibujados con Compose Canvas, sin fotos ni assets externos): 4 chicos y
 * 4 chicas con casco y chaleco de ingeniero, diferenciados claramente por peinado, rasgos y
 * expresión propios — nunca solo por el color de la ropa.
 */
enum class HairStyle {
    // chicos: corto, ondulado, rizado, rapado
    SHORT, WAVY, CURLY, BUZZCUT,
    // chicas: melena larga, coletas, trenza, media melena
    LONG, TWIN_TAILS, BRAID, BOB
}

/** Expresión del rostro: varía cejas y sonrisa para que cada avatar tenga personalidad propia. */
enum class Expression { SMILE, GRIN, CHEERFUL, SMIRK }

data class AvatarOption(
    val id: String,
    val label: String,
    val helmetHex: String,
    val skinHex: String,
    val hairHex: String,
    val hairStyle: HairStyle,
    val expression: Expression = Expression.SMILE
)

object AvatarCatalog {
    /** Primero los 4 chicos y después las 4 chicas: en una grilla de 4 columnas quedan en dos filas. */
    val all: List<AvatarOption> = listOf(
        // Chicos
        AvatarOption("avatar_casco_naranja", "Chico 1", "#F2994A", "#E8B08A", "#3B2A20", HairStyle.SHORT, Expression.SMILE),
        AvatarOption("avatar_casco_azul", "Chico 2", "#2E6DB4", "#F5CBA0", "#1C1C1C", HairStyle.WAVY, Expression.GRIN),
        AvatarOption("avatar_casco_verde", "Chico 3", "#3F8F5D", "#8D5A3C", "#241A14", HairStyle.CURLY, Expression.CHEERFUL),
        AvatarOption("avatar_casco_rojo", "Chico 4", "#C9683D", "#3E2A1F", "#0D0D0D", HairStyle.BUZZCUT, Expression.SMIRK),
        // Chicas
        AvatarOption("avatar_casco_teal", "Chica 1", "#2CAFC0", "#F0C9A0", "#6B4423", HairStyle.LONG, Expression.SMILE),
        AvatarOption("avatar_casco_violeta", "Chica 2", "#6B5CA5", "#C68A5B", "#4A2E1A", HairStyle.TWIN_TAILS, Expression.CHEERFUL),
        AvatarOption("avatar_casco_dorado", "Chica 3", "#D8A93B", "#F5D5B8", "#D4A24C", HairStyle.BRAID, Expression.SMIRK),
        AvatarOption("avatar_casco_gris", "Chica 4", "#5C6B7A", "#6B4226", "#1A1A1A", HairStyle.BOB, Expression.GRIN)
    )
}
