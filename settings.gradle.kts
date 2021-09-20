import org.gradle.internal.os.OperatingSystem

rootProject.name = "openrndr-template"

enableFeaturePreview("VERSION_CATALOGS")

/**
 * Here I tried making use of version catalogs,
 * but I don't see the advantage for our use case.
 *
 * We don't need to share dependency versions between projects,
 * which seems to be the main point in
 * https://docs.gradle.org/current/userguide/platforms.html
 *
 * Also, the examples seem to use a list of hardcoded dependencies,
 * but not generating those dependencies based on a function call
 * as we do when calling OS.getOsString(). OS seems to be undefined
 * in this file, even if the IDE shows no error and it allows to
 * ctrl+click to jump to the definition.
 *
 * Finally, the code actually becomes more verbose, because we
 * define versions, then aliases, and then we uses those aliases.
 *
 * This is nice if you want to have a simplified alias for a dependency
 * and use that alias in build.gradle.kts, but it's not what I'm trying
 * to do here. Instead, I want to separate the core code required
 * for building (which the end user will likely not change) from the
 * code that will probably be changed (adding orx and orml extensions,
 * adding custom dependencies, etc).
 *
 * So for now I will not continue working on this branch.
 */
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            println("BEFORE")
            println(OperatingSystem.current())

            gradle.projectsLoaded {
                println(OS.getOsString(rootProject))
                if(rootProject.hasProperty("targetPlatform")) {
                    println("yes")
                } else {
                    println("no")
                }
            }

            println("AFTER")

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
