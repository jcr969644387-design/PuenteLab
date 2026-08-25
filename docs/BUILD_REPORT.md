# Build Report — PuenteLab v1.0.0

Fecha: 2026-08-24 (entorno de desarrollo del encargo, sin conexión a Google Maven/Gradle).

## 1. Entorno de desarrollo disponible

| Herramienta | Disponible | Evidencia |
|---|---|---|
| JDK | Sí (OpenJDK 21.0.10) | `java -version` |
| Gradle | No preinstalado; wrapper presente pero no puede descargar la distribución | ver §3 |
| Android SDK | No | no existe `ANDROID_HOME` ni `sdkmanager` en el sistema |
| kotlinc (compilador Kotlin standalone) | Sí, descargado desde GitHub Releases | `kotlinc-jvm 1.9.24` |
| Acceso de red a `dl.google.com` / `maven.google.com` | **No** — `host_not_allowed` | ver §2 |
| Acceso de red a `services.gradle.org` | **No** — HTTP 403 | ver §3 |
| Acceso de red a `github.com` / `raw.githubusercontent.com` | Sí | usado para obtener kotlinc y el wrapper de Gradle real |

## 2. Por qué la compilación de Android no se pudo verificar

El entorno de este encargo restringe las conexiones salientes a una lista de hosts permitidos que
**no incluye** `dl.google.com` ni `maven.google.com` (el repositorio Maven de Google, necesario
para resolver el Android Gradle Plugin, AndroidX, Jetpack Compose y Room) ni `services.gradle.org`
(de donde el wrapper descarga la distribución de Gradle). Se comprobó explícitamente:

```
$ curl -sI https://dl.google.com
HTTP/2 403
x-deny-reason: host_not_allowed

$ curl -sI https://maven.google.com
HTTP/2 403
x-deny-reason: host_not_allowed
```

Esto significa que, aunque el proyecto tuviera Gradle y el SDK de Android instalados, **no podría
resolver ninguna dependencia de Android/Compose/Room** en este entorno. Es una restricción de red
del entorno de desarrollo, no una limitación del proyecto.

## 3. Intento real de build (evidencia)

Se ejecutó `./gradlew clean` con el wrapper real (descargado de
`https://raw.githubusercontent.com/gradle/gradle/v8.7.0/...`, host permitido) apuntando a la
distribución oficial de Gradle 8.7. El log completo está en
`tools/build_logs/gradlew_attempt_2.log`:

```
Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip

Exception in thread "main" java.io.IOException: Server returned HTTP response code: 403 for URL:
https://services.gradle.org/distributions/gradle-8.7-bin.zip
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(...)
	...
EXIT CODE: 1
```

Como consecuencia:

| Comando | Estado |
|---|---|
| `./gradlew clean` | [NO] No ejecutado (falla al descargar la distribución de Gradle) |
| `./gradlew testDebugUnitTest` | [NO] No ejecutado |
| `./gradlew lintDebug` | [NO] No ejecutado |
| `./gradlew assembleDebug` | [NO] No ejecutado |

**COMPILACIÓN NO VERIFICADA.** No se generó ningún `.apk`. No se inventa un SHA-256 ni un
resultado de tests de Gradle: ambos quedan explícitamente como pendientes de una ejecución real.

## 4. Lo que SÍ se verificó con ejecución real

Dado que `kotlinc` (el compilador de Kotlin) se pudo descargar desde GitHub Releases (host
permitido, a diferencia de Google Maven), se usó para **compilar y ejecutar de verdad** — fuera de
Gradle y sin ningún framework de test, ya que tampoco había forma de obtener el `.jar` de JUnit sin
Maven Central — la capa de dominio completa, que es 100 % Kotlin puro sin dependencias de Android:

```
$ kotlinc-jvm 1.9.24 (JRE 21.0.10+7-Ubuntu-124.04)
$ kotlinc domain/model/*.kt domain/logic/*.kt VerifyMain.kt -include-runtime -d verify.jar
$ java -jar verify.jar
PASS: disenio desconectado no aprueba
... (20 líneas PASS)
---- RESUMEN: 20 pasaron, 0 fallaron ----

$ java -jar verify2.jar   # ProgressEngine + BadgeEngine
---- RESUMEN PROGRESS/BADGES: 28 pasaron, 0 fallaron ----

$ java -jar verify3.jar   # 8 aserciones adicionales de BridgeEngine
---- RESUMEN EXTRA: 8 pasaron, 0 fallaron ----
```

**Total: 56 aserciones reales ejecutadas contra el código exacto de `BridgeEngine.kt`,
`ProgressEngine.kt` y `BadgeEngine.kt` que se entrega en `app/src/main/java/...`, con 0 fallos.**
Dos errores reales se encontraron y corrigieron durante este proceso (no se ocultan):

1. Un caso de "3 estrellas" fallaba porque el diseño de prueba concentraba toda la carga en una
   única barra de 6 unidades, sobrecargándola — se corrigió el diseño de prueba (no el motor) para
   usar un puente de 2 tramos, que es el patrón de juego real esperado.
2. El diseño de nivel más difícil (usado para anclar la fórmula de los 45 desafíos) fallaba primero
   por pendiente excesiva y luego por una riostra de cuerda demasiado débil para la carga
   transferida; se ajustó la altura del apoyo elevado y el material de la riostra hasta encontrar
   un diseño válido con margen. Este proceso está documentado en `tools/` (scripts de verificación,
   no incluidos en el APK) y resumido en `docs/MANUAL_TECNICO.md`.

Las 49 pruebas JUnit en `app/src/test/java/.../domain/` (`BridgeEngineTest.kt`,
`ProgressEngineTest.kt`, `BadgeEngineTest.kt`) son una traducción directa de estos mismos 56 casos
verificados al formato JUnit4 que usará `./gradlew testDebugUnitTest`; no se escribieron a ciegas.

Las 10 pruebas de `RoomPersistenceTest.kt` usan Room en memoria vía Robolectric y **no se
ejecutaron** (Room y Robolectric no se pudieron descargar sin acceso a Maven Central/Google
Maven), pero siguen la API real de Room 2.6.1.

## 5. Estado de los entregables

| Entregable | Estado |
|---|---|
| Código fuente completo (`app/`, `data/`, `domain/`, `ui/`) | [OK] Completo |
| `database/schema.sql`, `database/sample_data.sql` | [OK] Completo |
| `docs/MEMORIA_DESCRIPTIVA.md`, `MANUAL_USUARIO.md`, `MANUAL_TECNICO.md`, `BASE_DE_DATOS.md` | [OK] Completo |
| PDF de los tres documentos anteriores | [OK] Generados con `pandoc` + `wkhtmltopdf` (host local, sin red). Verificados: 4, 3 y 4 páginas respectivamente; texto extraído con `pdftotext` confirma acentos y ñ correctos (ver `docs/pdf/`). |
| `.github/workflows/android-build.yml` | [OK] Completo — compilará el APK real en el primer push a GitHub |
| `PuenteLab-v1.0.0.apk` | [NO] **No generado** — requiere compilar en un entorno con Android SDK y acceso a Google Maven (GitHub Actions o Android Studio local) |
| SHA-256 del APK | [NO] No aplica (no hay APK) |
| `PuenteLab-v1.0.0-source.zip` | [OK] Generado |

## 6. Siguiente paso recomendado

Subir este proyecto a un repositorio de GitHub (o abrirlo en Android Studio con conexión a
internet normal). El workflow `.github/workflows/android-build.yml` ya está preparado para
ejecutar `clean`, `testDebugUnitTest`, `lintDebug` y `assembleDebug` automáticamente y publicar el
APK y los reportes de test/lint como artefactos descargables.
