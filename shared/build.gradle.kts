plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }
    
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:5.8.0")
                implementation("io.kotest:kotest-property:5.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
                implementation("app.cash.turbine:turbine:1.0.0")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
                implementation("com.microsoft.onnxruntime:onnxruntime:1.17.1")
                implementation("ai.djl.huggingface:tokenizers:0.28.0")
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:5.8.0")
            }
        }
    }
}

sqldelight {
    databases {
        create("SynapseDatabase") {
            packageName.set("dev.synapse.database")
        }
    }
}

