package com.fliprss.app.browser

import com.google.gson.JsonParser
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import org.junit.Assert.*
import org.junit.Test

class BundledAddonTest {
    private val root = File(".").canonicalFile.let { if (File(it, "src/main/assets").exists()) it else File(it, "app") }
    @Test fun pinnedArchiveHashIdentityLicenceAndAssetsMatchUpstream() {
        val archive = File(root.parentFile, "third_party/bpc/bypass_paywalls_clean-4.4.9.6.xpi")
        val sha = MessageDigest.getInstance("SHA-256").digest(archive.readBytes()).joinToString("") { "%02x".format(it) }
        assertEquals("0d0b9c627eb5085c16a263f61415c5706428ce7ba259566d0d50c4ac7e096dac", sha)
        ZipFile(archive).use { zip ->
            zip.entries().asSequence().filterNot { it.isDirectory }.forEach { entry ->
                assertArrayEquals(entry.name, zip.getInputStream(entry).use { it.readBytes() }, File(root, "src/main/assets/bpc/${entry.name}").readBytes())
            }
        }
        val manifest = JsonParser.parseString(File(root, "src/main/assets/bpc/manifest.json").readText()).asJsonObject
        assertEquals("4.4.9.6", manifest["version"].asString)
        assertEquals("magnolia@12.34", manifest["browser_specific_settings"].asJsonObject["gecko"].asJsonObject["id"].asString)
        assertTrue(File(root, "src/main/assets/bpc/LICENSE").readText().startsWith("MIT License"))
    }
}
