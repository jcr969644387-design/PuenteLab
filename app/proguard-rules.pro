# PuenteLab - reglas ProGuard/R8
# minifyEnabled está desactivado en debug y release por defecto en este proyecto educativo.
# Reglas conservadoras por si se activa minificación en el futuro:
-keep class com.educalab.puentelab.data.local.entity.** { *; }
-keep class com.educalab.puentelab.domain.model.** { *; }
-dontwarn kotlinx.coroutines.**
