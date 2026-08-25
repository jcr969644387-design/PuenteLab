# PuenteLab

**Bridge Builder Junior** — una app educativa de ingeniería civil para chicos y chicas de 10 a 15 años. Diseñas puentes en cinco escenarios ilustrados (río, cañón, bosque, ciudad y paso de montaña), eligiendo materiales y tipos estructurales reales (viga, cercha, arco, suspensión conceptual), y cada diseño se prueba cruzándolo con un vehículo animado sobre un motor de simulación determinista propio.

- **Paquete:** `com.educalab.puentelab`
- **Versión:** 1.0.0
- **Plataforma:** Android nativo, Kotlin + Jetpack Compose, 100% offline
- **minSdk:** 24 · **targetSdk / compileSdk:** 34 · **JDK:** 17

## Estado de la compilación

Este proyecto se construyó en un entorno de desarrollo **sin Android SDK, sin Gradle y sin acceso de red a Google Maven ni a la distribución de Gradle** (reglas de red del entorno: `dl.google.com`, `maven.google.com` y `services.gradle.org` devuelven `host_not_allowed` / 403). Por lo tanto:

- **La compilación con `./gradlew assembleDebug` NO se pudo verificar en este entorno.** El intento real (con su log) está documentado en `docs/BUILD_REPORT.md` y en `tools/build_logs/gradlew_attempt.log`.
- En cambio, sí se pudo descargar el compilador oficial de Kotlin (`kotlinc` 1.9.24) desde GitHub Releases (host permitido) y usarlo para **compilar y ejecutar de verdad** toda la capa de dominio (`BridgeEngine`, `ProgressEngine`, `BadgeEngine`) contra 56 aserciones reales, sin simular resultados. Ver `docs/BUILD_REPORT.md` para el detalle exacto.
- Se incluye un workflow de GitHub Actions (`.github/workflows/android-build.yml`) que compila la APK real, corre los tests y el lint en cuanto el proyecto se suba a un repositorio de GitHub, donde sí hay acceso completo a internet.

**Para compilar de verdad:** subir este proyecto a GitHub (o abrirlo en Android Studio con conexión a internet normal) y ejecutar:

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

## Estructura del repositorio

```
app/            Código fuente Android (Kotlin + Compose, MVVM, Room)
database/       schema.sql y sample_data.sql (referencia, espejo del esquema Room)
docs/           Documentación completa (memoria, manuales, base de datos, build report) + PDF
tools/          Scripts auxiliares (generación de iconos, logs de build)
deliverables/   Entregables finales (ver docs/BUILD_REPORT.md para qué contiene exactamente)
.github/        Workflow de compilación en GitHub Actions
```

## Módulos de la app

1. **Academia** — panel/hub principal con progreso, nivel y siguiente actividad sugerida por PIVOT.
2. **Escenarios** — 5 escenarios ilustrados x 9 desafíos cada uno (45 en total).
3. **Materiales** — catálogo de 8 materiales con su balance de resistencia/costo/peso.
4. **Constructor táctil** — cuadrícula interactiva: colocar nodos, conectar barras, elegir material y rol estructural.
5. **Presupuesto** — barra de costo en vivo dentro del constructor.
6. **Cargas** — la demanda del desafío y el vehículo elegido determinan la carga real que analiza el motor.
7. **Vehículo de prueba** — animación de cruce sobre el propio diseño.
8. **Simulación/optimización** — resultado detallado por barra, con retroalimentación educativa.
9. **Mis diseños** — hasta 15 puentes guardados, duplicables y editables.
10. **Progreso** — nivel, XP, 9 insignias y 12 sellos coleccionables.

## Motor de simulación (`BridgeEngine`)

Reglas deterministas (sin aleatoriedad) inspiradas en conceptos reales de ingeniería estructural: conectividad de grafo (BFS), ruta de calzada con pendiente máxima transitable, presupuesto real por longitud de barra y material, y capacidad de carga según material/longitud/triangulación (bonos reales de cercha, arco y suspensión). **No es un análisis de elementos finitos ni sustituye cálculo de ingeniería profesional** — así se indica también dentro de la app.

Este motor se verificó de forma independiente con 56 aserciones ejecutadas con `kotlinc` fuera de Gradle (ver `docs/BUILD_REPORT.md`).

## Privacidad

App 100% offline. No se declara el permiso `INTERNET`. No se pide nombre real, correo, teléfono, ubicación ni contactos: solo un apodo elegido libremente y un avatar local. No hay anuncios, analítica, cuentas online ni compras.

## Documentación completa

- `docs/MEMORIA_DESCRIPTIVA.md` / `.pdf`
- `docs/MANUAL_USUARIO.md` / `.pdf`
- `docs/MANUAL_TECNICO.md` / `.pdf`
- `docs/BASE_DE_DATOS.md`
- `docs/BUILD_REPORT.md`
