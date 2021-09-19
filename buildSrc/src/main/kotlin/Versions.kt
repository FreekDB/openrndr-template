object Versions {
    const val kotlin = "1.5.30"

    private const val defaultVersion = "0.4.0"
    private const val snapshotVersion = "0.5.1-SNAPSHOT"

    // The following versions can be customized independently
    var openrndr = defaultVersion
    var orx = defaultVersion
    var orml = defaultVersion

    var openrndrUseSnapshot = false
        set(value) {
            field = value
            if (value) openrndr = snapshotVersion
        }

    var orxUseSnapshot = false
        set(value) {
            field = value
            if (value) orx = snapshotVersion
        }

    var ormlUseSnapshot = false
        set(value) {
            field = value
            if (value) orml = snapshotVersion
        }
}