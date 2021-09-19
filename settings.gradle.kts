rootProject.name = "openrndr-template"

@Suppress("UnstableApiUsage")
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            if (true) {
                version("openrndr", "0.5.1-SNAPSHOT")
            } else {
                version("openrndr", "0.3.58")
            }
            //version("kotlin", "1.5.30")

            val impl = mutableListOf(
                "openrndr-openal",
                "openrndr-application",
                "openrndr-svg",
                "openrndr-animatable",
                "openrndr-extensions",
                "openrndr-filter"
            )

            impl.forEach {
                alias(it)
                    .to("org.openrndr", it)
                    .versionRef("openrndr")
            }

            alias("coroutines")
                .to("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
                .version("1.5.0")

            alias("logging")
                .to("io.github.microutils", "kotlin-logging-jvm")
                .version("2.0.6")

            alias("openrndr-gl3")
                .to("org.openrndr", "openrndr-gl3")
                .versionRef("openrndr")

            val os = "linux-x64"

            alias("openrndr-gl3-native")
                .to("org.openrndr", "openrndr-gl3-natives-$os")
                .versionRef("openrndr")

            alias("openrndr-openal")
                .to("org.openrndr", "openrndr-openal-natives-$os")
                .versionRef("openrndr")

            bundle(
                "openrndrImpl", impl + listOf(
                    "coroutines",
                    "logging"
                )
            )

            bundle(
                "openrndrRuntime", listOf(
                    "openrndr-gl3",
                    "openrndr-gl3-native",
                    "openrndr-openal"
                )
            )
        }
    }
}
