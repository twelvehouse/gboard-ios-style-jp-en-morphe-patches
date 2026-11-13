package app.gboardios.patches.flicklayout

import app.gboardios.patches.shared.Constants.COMPATIBILITY_GBOARD
import app.gboardios.patches.shared.ResourceIds
import app.gboardios.patches.signature.signatureCheckBypassPatch
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch

/*
 * How Gboard builds the Japanese 12-key keyboards (Gboard 18.5.4):
 *
 * - The kana (prime), 12-key alphabet and number (tri_state_digit) keyboards are declared by the
 *   keyboard definitions below and all use the same body layout. That layout is made of three
 *   columns: the left function keys, the 3x4 character grid and the right function keys.
 * - A layout only holds empty SoftKeyViews. `<key_mapping>` elements assign a softkey to each view
 *   id, optionally per state such as COMPOSING. A view without a softkey keeps the visibility of
 *   the layout, and a mapping to a view id that is not in the layout is ignored.
 *
 * The patch replaces the two side columns, adds the mode keys and their mappings, and includes
 * them in the three keyboard definitions.
 */

/** Left function column: undo, cursor left, symbol/emoji, kana/alphabet switch. */
private const val LAYOUT_LEFT_COLUMN = 0x7f0e0241L

/** Right function column: backspace, cursor right, space, enter. */
private const val LAYOUT_RIGHT_COLUMN = 0x7f0e0244L

/** Keyboard definitions of the kana, 12-key alphabet and number keyboards. */
private val KEYBOARD_DEFINITIONS = listOf(0x7f1706e7L, 0x7f1706e6L, 0x7f1706eeL)

/** Last include of each keyboard body. The new softkeys and mappings go after it, so they win. */
private const val XML_LAST_BODY_INCLUDE = 0x7f170c01L

/** Key mappings that put the globe key into the 小゛゜ slot when several languages are enabled. */
private val XML_LANGUAGE_SWITCH_MAPPINGS = listOf(0x7f170a57L, 0x7f170a5eL)

private const val SOFTKEYS_XML = "ios_flick_softkeys"
private const val KEY_MAPPING_XML = "ios_flick_key_mapping"

private val NEW_IDS = listOf(
    "ios_flick_key_pos_digit",
    "ios_flick_key_pos_alphabet",
    "ios_flick_key_pos_kana",
    "ios_flick_key_pos_globe",
    "ios_flick_key_digit",
    "ios_flick_key_alphabet",
    "ios_flick_key_kana",
)

@Suppress("unused")
val iosFlickLayoutPatch = resourcePatch(
    name = "iOS flick layout",
    description = "Changes the Japanese 12-key keyboard to the layout of iOS with \"flick only\" enabled: " +
        "☆123, ABC and あいう keys and the globe key on the left, and backspace, space and " +
        "a two-row enter key on the right.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(signatureCheckBypassPatch)

    execute {
        val ids = ResourceIds.read(this)

        addIds()
        writeBundled(ids, "left_column.xml", ids.path(LAYOUT_LEFT_COLUMN))
        writeBundled(ids, "right_column.xml", ids.path(LAYOUT_RIGHT_COLUMN))
        writeBundled(ids, "$SOFTKEYS_XML.xml", "res/xml/$SOFTKEYS_XML.xml")
        writeBundled(ids, "$KEY_MAPPING_XML.xml", "res/xml/$KEY_MAPPING_XML.xml")

        val lastInclude = ids.ref(XML_LAST_BODY_INCLUDE)
        KEYBOARD_DEFINITIONS.forEach { definition ->
            resourceXml(ids.path(definition)).use { xml ->
                val anchors = xml.elements("include").filter { it.attribute("href") == lastInclude }.toList()
                check(anchors.isNotEmpty()) { "No body include found in ${ids.path(definition)}" }
                anchors.forEach { anchor ->
                    val parent = anchor.parent!!
                    parent.insertChild(anchor.index + 1, "include") {
                        setAttribute("href", "@xml/$KEY_MAPPING_XML")
                    }
                    parent.insertChild(anchor.index + 1, "softkeys") {
                        setAttribute("href", "@xml/$SOFTKEYS_XML")
                    }
                }
            }
        }

        // The globe key now has its own slot in the left column.
        XML_LANGUAGE_SWITCH_MAPPINGS.forEach { mapping ->
            resourceXml(ids.path(mapping)).use { xml ->
                xml.elements("key_mapping")
                    .filter { it.attribute("state") == "SHOW_LANGUAGE_SWITCH_KEY" }
                    .toList()
                    .forEach { it.remove() }
            }
        }
    }
}

private fun ResourcePatchContext.addIds() {
    resourceXml("res/values/ids.xml", copy = false).use { xml ->
        NEW_IDS.forEach { name ->
            xml.root.appendChild("item") {
                setAttribute("type", "id")
                setAttribute("name", name)
            }
        }
    }
}

private fun ResourcePatchContext.writeBundled(ids: ResourceIds, bundled: String, target: String) {
    val text = object {}.javaClass.getResourceAsStream("/gboardios/flicklayout/$bundled")
        ?.use { it.readBytes().toString(Charsets.UTF_8) }
        ?: error("Missing bundled resource $bundled")
    resourceFile(target, copy = false).apply {
        parentFile.mkdirs()
        writeText(ids.expand(text), Charsets.UTF_8)
    }
}
