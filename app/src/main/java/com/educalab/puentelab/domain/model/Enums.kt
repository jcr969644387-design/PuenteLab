package com.educalab.puentelab.domain.model

/** Tipo estructural educativo elegido por el jugador para un tramo del puente. */
enum class StructureType {
    BEAM,        // Viga simple
    TRUSS,       // Cercha (triangulada)
    ARCH,        // Arco
    SUSPENSION;  // Suspensión conceptual

    fun displayName(): String = when (this) {
        BEAM -> "Viga"
        TRUSS -> "Cercha"
        ARCH -> "Arco"
        SUSPENSION -> "Suspensión"
    }
}

/** Nivel de carga exigido por un desafío. loadUnits es un valor arbitrario del motor, no físico real. */
enum class DemandLevel(val loadUnits: Double, val displayName: String) {
    LOW(100.0, "Baja"),
    MEDIUM(220.0, "Media"),
    HIGH(380.0, "Alta")
}

/** Rol funcional de una barra dentro de la estructura. */
enum class MemberRole {
    DECK,   // Forma parte de la calzada por la que circula el vehículo
    BRACE,  // Diagonal/riostra de refuerzo (no transitable)
    CABLE,  // Cable de suspensión
    TOWER   // Torre vertical de anclaje de cables
}

enum class ScenarioType(val displayName: String) {
    RIVER("Río Correntoso"),
    CANYON("Cañón Rojo"),
    FOREST("Bosque Profundo"),
    CITY("Ciudad Elevada"),
    MOUNTAIN("Paso de Montaña")
}

enum class ModuleState { LOCKED, AVAILABLE, STARTED, COMPLETED, MASTERED }

enum class FailureReason(val message: String) {
    DISCONNECTED("Las dos orillas todavía no están conectadas. Añade barras hasta unirlas."),
    NO_VALID_ROUTE("Hay conexión, pero falta una calzada continua para el vehículo. Usa barras de tipo calzada."),
    SLOPE_TOO_STEEP("Alguna sección de la calzada es demasiado inclinada para que el vehículo circule."),
    OVER_BUDGET("El diseño se pasa del presupuesto disponible."),
    OVERLOADED("Alguna barra no soporta la carga exigida y el puente colapsaría.")
}
