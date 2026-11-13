package app.gboardios.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"

    /**
     * Gboard 18.5.4. Every ABI build shares the same resources.arsc, so the resource ids that the
     * patches rely on are the same for each of them. The version name carries the ABI suffix.
     */
    private const val GBOARD_VERSION = "18.5.4.993608717-release"

    val COMPATIBILITY_GBOARD = Compatibility(
        name = "Gboard",
        packageName = GBOARD_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(version = "$GBOARD_VERSION-arm64-v8a"),
            AppTarget(version = "$GBOARD_VERSION-x86_64"),
        ),
    )
}
