package app.gboardios.patches.symbols

import app.gboardios.patches.shared.Constants.COMPATIBILITY_GBOARD
import app.gboardios.patches.shared.ResourceIds
import app.gboardios.patches.shared.writeBundled
import app.gboardios.patches.signature.signatureCheckBypassPatch
import app.morphe.patcher.patch.resourcePatch

/*
 * The symbol keyboard (0x7f170878) is shared by all languages. Its body layout has fixed key
 * positions (D01-D10, C01-C10, B01-B07, A02, A03, A08, ...) and the softkeys are assigned to them
 * by <key_mapping>, the second page ("=\<") being the SUB_CATEGORY_1 state.
 *
 * For English only, the patch selects a body layout with five wide keys in the third row and
 * [ABC] [emoji/globe] [space] [enter] at the bottom, and maps the keys in the order of the iOS
 * "123" and "#+=" pages. The page switch keys show "#+=" and "123" instead of icons, and the
 * symbol key of the letter keyboards shows "123" instead of "?123".
 *
 * Gboard caches parsed keyboard definitions in its data, so an update of an already installed
 * patched app only shows the change after its data is cleared.
 */

/** Includes choosing the body layout of the symbol keyboard. */
private const val XML_SYMBOL_BODY_SELECTION = 0x7f17087eL

/** The symbol keyboard definition. */
private const val XML_SYMBOL_KEYBOARD = 0x7f170878L

/** Default phone body layout of the symbol keyboard. */
private const val LAYOUT_SYMBOL_BODY = 0x7f0e0516L

/** Softkeys of the symbol page switch keys (0x7f0b24c4 "=\<" and 0x7f0b2053 "?123"). */
private const val XML_PAGE_SWITCH_SOFTKEYS = 0x7f170f16L
private const val SOFTKEY_MORE_SYMBOLS = 0x7f0b24c4L
private const val SOFTKEY_SYMBOLS = 0x7f0b2053L

/** Softkey files defining the "?123" key of the letter keyboards. */
private val XML_SYMBOL_SWITCH_SOFTKEYS = listOf(0x7f170f05L, 0x7f170f0eL, 0x7f170f0fL, 0x7f170f17L, 0x7f171222L)

private const val ENGLISH = "en|en-US|en-GB|en-CA|en-AU|en-IN|en-PH|en-ZA|en-NG|en-KE|en-IE|en-NZ|en-SG"

private const val BODY_LAYOUT = "ios_symbols_body"
private const val KEY_MAPPING_XML = "ios_symbols_key_mapping"

@Suppress("unused")
val iosSymbolKeyboardPatch = resourcePatch(
    name = "iOS symbol keyboard",
    description = "Lays out the English symbol keyboard like the iOS \"123\" and \"#+=\" pages, " +
        "and labels the switch keys \"123\" and \"#+=\". Gboard's number row setting keeps working.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(signatureCheckBypassPatch)

    execute {
        val ids = ResourceIds.read(this)

        writeBundled(ids, "symbols", "$BODY_LAYOUT.xml", "res/layout/$BODY_LAYOUT.xml")
        writeBundled(ids, "symbols", "$KEY_MAPPING_XML.xml", "res/xml/$KEY_MAPPING_XML.xml")

        // Use the new body layout for English on phones in normal mode: the stock layout is the
        // last <else> of the selection, so an <else_if> goes right before it.
        resourceXml(ids.path(XML_SYMBOL_BODY_SELECTION)).use { xml ->
            val stockLayout = ids.ref(LAYOUT_SYMBOL_BODY)
            val stockElse = xml.elements("view")
                .single {
                    it.attribute("layout") == stockLayout &&
                        it.parent?.tag == "else" &&
                        it.parent?.parent?.tag == xml.root.tag
                }
                .parent!!
            xml.root.insertChild(stockElse.index, "else_if") {
                setAttribute("language", ENGLISH)
                appendChild("view") {
                    setAttribute("layout", "@layout/$BODY_LAYOUT")
                    setAttribute("type", "body")
                }
            }
        }

        // The mappings go last in the body view so that they override the stock ones.
        resourceXml(ids.path(XML_SYMBOL_KEYBOARD)).use { xml ->
            val body = xml.elements("view").single { it.attribute("type") == "body" && it.children("include").isNotEmpty() }
            body.appendChild("if") {
                setAttribute("language", ENGLISH)
                appendChild("include") { setAttribute("href", "@xml/$KEY_MAPPING_XML") }
            }
        }

        // Text labels for the page switch keys. "#" must be escaped: Gboard reads values
        // starting with "#" as resource ids.
        resourceXml(ids.path(XML_PAGE_SWITCH_SOFTKEYS)).use { xml ->
            mapOf(SOFTKEY_MORE_SYMBOLS to "\\#+=", SOFTKEY_SYMBOLS to "123").forEach { (softkey, label) ->
                val key = xml.elements("softkey").single { it.attribute("id") == ids.ref(softkey) }
                key.children("icon").forEach { it.setAttribute("value", "") }
                key.children("label").single().setAttribute("value", label)
            }
        }

        // "?123" -> "123" on the letter keyboards.
        XML_SYMBOL_SWITCH_SOFTKEYS.forEach { file ->
            resourceXml(ids.path(file)).use { xml ->
                xml.elements().forEach { element ->
                    listOf("value", "label_value").forEach { name ->
                        if (element.attribute(name)?.removePrefix("\\") == "?123") element.setAttribute(name, "123")
                    }
                }
            }
        }
    }
}
