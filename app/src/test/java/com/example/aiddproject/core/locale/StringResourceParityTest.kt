package com.example.aiddproject.core.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * String-resource parity check (T074).
 *
 * Loads `values/strings.xml` (VN, the default), filters out keys marked
 * `translatable="false"` (brand-fixed strings — `brand_root_further`, `brand_company`,
 * `login_cta_label`), and asserts every remaining key is also present in
 * `values-en/strings.xml` and `values-ja/strings.xml`.
 *
 * Catches the common regression where a Vietnamese copy update lands without the EN/JA
 * translation, leaving fallback-locale users staring at an MR_ key.
 */
class StringResourceParityTest {
    private val resRoot: File = File("src/main/res").canonicalFile

    private data class StringDef(
        val name: String,
        val translatable: Boolean,
    )

    private fun load(localeFolder: String): List<StringDef> {
        val file = File(resRoot, "$localeFolder/strings.xml")
        assertTrue("Missing strings file: ${file.absolutePath}", file.exists())
        val doc =
            DocumentBuilderFactory
                .newInstance()
                .apply {
                    isNamespaceAware = false
                }.newDocumentBuilder()
                .parse(file)
        val items = mutableListOf<StringDef>()
        val nodes = doc.getElementsByTagName("string")
        for (i in 0 until nodes.length) {
            val el = nodes.item(i) as Element
            items +=
                StringDef(
                    name = el.getAttribute("name"),
                    translatable = el.getAttribute("translatable") != "false",
                )
        }
        return items
    }

    @Test
    fun every_translatable_key_in_default_locale_exists_in_EN_and_JA() {
        val default = load("values").filter { it.translatable }.map { it.name }.toSet()
        val en = load("values-en").map { it.name }.toSet()
        val ja = load("values-ja").map { it.name }.toSet()

        val missingEn = default - en
        val missingJa = default - ja

        assertEquals(
            "EN translation missing for: $missingEn",
            emptySet<String>(),
            missingEn,
        )
        assertEquals(
            "JA translation missing for: $missingJa",
            emptySet<String>(),
            missingJa,
        )
    }

    @Test
    fun brand_fixed_keys_are_NOT_duplicated_into_locale_folders() {
        val brandFixed = load("values").filterNot { it.translatable }.map { it.name }.toSet()
        val en = load("values-en").map { it.name }.toSet()
        val ja = load("values-ja").map { it.name }.toSet()

        assertEquals(
            "EN should NOT contain brand-fixed keys (translatable=false): ${brandFixed intersect en}",
            emptySet<String>(),
            brandFixed intersect en,
        )
        assertEquals(
            "JA should NOT contain brand-fixed keys (translatable=false): ${brandFixed intersect ja}",
            emptySet<String>(),
            brandFixed intersect ja,
        )
    }
}
