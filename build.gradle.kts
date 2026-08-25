// PuenteLab - Root build file
// Versiones fijadas explícitamente (sin '+' ni 'latest') para builds reproducibles.
plugins {
    id("com.android.application") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
