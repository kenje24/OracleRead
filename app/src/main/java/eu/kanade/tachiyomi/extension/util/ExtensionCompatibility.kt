package eu.kanade.tachiyomi.extension.util

internal object ExtensionCompatibility {
    private val supportedLibVersions = setOf(1.4, 1.5, 1.6)

    fun isSupportedLibVersion(version: Double): Boolean {
        return version in supportedLibVersions
    }

    fun resolveLibVersion(metadataVersion: Float?, versionName: String): Double? {
        return metadataVersion
            ?.takeUnless { it == 0.0f }
            ?.toString()
            ?.toDouble()
            ?: versionName.substringBeforeLast('.').toDoubleOrNull()
    }

    fun supportedLibVersionsLabel(): String {
        return supportedLibVersions.sorted().joinToString()
    }
}
