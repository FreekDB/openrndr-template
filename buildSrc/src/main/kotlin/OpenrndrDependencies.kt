import org.gradle.api.Project

class OpenrndrDependencies(project: Project) {
    lateinit var orxFeatures: List<String>
    lateinit var ormlFeatures: List<String>
    lateinit var openrndrFeatures: List<String>
    lateinit var orxTensorflowBackend: String

    init {
        // Test what runs first, this or settings.gradle.kts
        println("A")
    }

    private val openrndrOs = OS.getOsString(project)

    private fun openrndr(module: String) =
        "org.openrndr:openrndr-$module:${Versions.openrndr}"

    private fun openrndrNatives(module: String) =
        "org.openrndr:openrndr-$module-natives-$openrndrOs:${Versions.openrndr}"

    private fun orxNatives(module: String) =
        "org.openrndr.extra:$module-natives-$openrndrOs:${Versions.orx}"

    fun runtimeOnly() = listOfNotNull(
        if ("video" in openrndrFeatures)
            openrndrNatives("ffmpeg")
        else null,

        if ("orx-tensorflow" in orxFeatures)
            orxNatives(orxTensorflowBackend)
        else null,

        if ("orx-kinect-v1" in orxFeatures)
            orxNatives("orx-kinect-v1")
        else null,
    )

    fun implementation() = listOfNotNull(
        if ("orx-olive" in orxFeatures)
            "org.jetbrains.kotlin:kotlin-script-runtime:${Versions.kotlin}"
        else null,

        if ("video" in openrndrFeatures)
            openrndr("ffmpeg")
        else null,
    ) +
            orxFeatures.map { "org.openrndr.extra:$it:${Versions.orx}" } +
            ormlFeatures.map { "org.openrndr.orml:$it:${Versions.orml}" }
}
