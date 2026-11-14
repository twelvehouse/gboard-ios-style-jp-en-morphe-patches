package app.gboardios.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"

    /**
     * Supported Gboard builds, newest first, arm64-v8a only: phones need it, and listing the
     * x86_64 build let Morphe Manager accept it for a phone, where it fails with
     * INSTALL_FAILED_NO_MATCHING_ABIS. Every ABI build shares the same resources.arsc, so the
     * x86_64 build still patches fine for an emulator with `morphe-desktop patch -f`. The version
     * name carries the ABI suffix.
     *
     * Each version maps to its resource id table under `/gboardios/ids/`, see [ResourceIds]. The
     * ids in the patches are those of 18.5.4, so the builds where the resources the patches use
     * keep their ids, such as the other 18.5.x builds, need no table (null).
     */
    val GBOARD_VERSIONS: Map<String, String?> = linkedMapOf(
        "18.6.3.1002251336-release" to "18.6.3.1002251336-release",
        "18.5.11.993608717-release" to null,
        "18.5.10.993608717-release" to null,
        "18.5.9.993608717-release" to null,
        "18.5.6.993608717-release" to null,
        "18.5.4.993608717-release" to null,
    )

    val COMPATIBILITY_GBOARD = Compatibility(
        name = "Gboard",
        packageName = GBOARD_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8,
        targets = GBOARD_VERSIONS.keys.map { AppTarget(version = "$it-arm64-v8a") },
    )
}
