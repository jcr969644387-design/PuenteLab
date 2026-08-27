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

/** Rol funcional de una barra dentro de la estructura, con explicación simple para chicos. */
enum class MemberRole(val emoji: String, val displayName: String, val shortDescription: String) {
    DECK("🛣️", "Calzada", "Es el camino del puente. Por aquí pasan los vehículos y las personas."),
    BRACE("🔺", "Riostra", "Refuerza el puente y ayuda a que no se mueva ni se deforme demasiado."),
    CABLE("🪢", "Cable", "Ayuda a sostener el puente. Trabaja tirando de las partes que sostiene."),
    TOWER("🗼", "Torre", "Sostiene los cables y ayuda a llevar el peso del puente hacia el suelo.")
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
    NO_VALID_ROUTE("Hay conexión, pero falta una Calzada 🛣️ completa para que pase el vehículo."),
    SLOPE_TOO_STEEP("Alguna parte de la Calzada 🛣️ está muy empinada: el vehículo no puede subir por ahí."),
    OVER_BUDGET("El diseño se pasa del presupuesto disponible."),
    OVERLOADED("El puente no pudo soportar la carga. Revisa los materiales o agrega refuerzos."),
    MISSING_ELEMENTS("A este puente le faltan partes importantes."),
    CONSTRAINT_VIOLATED("Este desafío tiene una restricción especial que tu diseño no respeta.")
}
