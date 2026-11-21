package com.fliprss.app.browser

import com.google.gson.JsonParser
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import org.junit.Assert.*
import org.junit.Test

class UBlockBundledAddonTest {
    private val root = File(".").canonicalFile.let { if (File(it, "src/main/assets").exists()) it else File(it, "app") }
    @Test fun pinnedArchiveHashIdentityLicenceAndAssetsMatchBundle() {
        val archive = File(root.parentFile, "third_party/ublock/uBlock0_1.76.7.0.firefox.xpi")
        val sha = MessageDigest.getInstance("SHA-256").digest(archive.readBytes()).joinToString("") { "%02x".format(it) }
        assertEquals("302d0966c04c905425306e53bc87e9660f91e0210b4639cb70234f7c3e72cd21", sha)
        ZipFile(archive).use { zip ->
            zip.entries().asSequence().filterNot { it.isDirectory }.forEach { entry ->
                assertArrayEquals(entry.name, zip.getInputStream(entry).use { it.readBytes() }, File(root, "src/main/assets/ublock/${entry.name}").readBytes())
            }
        }
        val manifest = JsonParser.parseString(File(root, "src/main/assets/ublock/manifest.json").readText()).asJsonObject
        assertEquals("1.76.7.0", manifest["version"].asString)
        assertEquals("uBlock0@raymondhill.net", manifest["browser_specific_settings"].asJsonObject["gecko"].asJsonObject["id"].asString)
        assertTrue(File(root, "src/main/assets/ublock/LICENSE.txt").readText().startsWith("GNU GENERAL PUBLIC LICENSE"))
    }
}
