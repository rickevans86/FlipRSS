package com.fliprss.app

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fliprss.app.data.*
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class UpdateState(val busy: Boolean = false, val message: String? = null,
    val update: AppUpdate? = null, val file: File? = null)
internal class AppUpdater(application: Application) : AndroidViewModel(application) {
    private val app = application
    private val remote = GitHubUpdates()
    private val mutable = MutableStateFlow(UpdateState())
    val state = mutable.asStateFlow()
    @Suppress("DEPRECATION")
    val installedCode: Long get() = app.packageManager.getPackageInfo(app.packageName, 0).let {
        if (Build.VERSION.SDK_INT >= 28) it.longVersionCode else it.versionCode.toLong()
    }
    val installedVersion: String get() = app.packageManager.getPackageInfo(app.packageName, 0).versionName.orEmpty()
    fun check() {
        if (mutable.value.busy) return
        mutable.value = UpdateState(busy = true, message = "Checking GitHub Releases…")
        viewModelScope.launch {
            try {
                val update = remote.check(installedCode)
                mutable.value = UpdateState(update = update, message = update?.let { "FlipRSS ${it.version} is available." } ?: "You have the latest release.")
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { mutable.value = UpdateState(message = problem(error)) }
        }
    }
    fun download() {
        val old = mutable.value; val update = old.update ?: return
        if (old.busy) return
        mutable.value = old.copy(busy = true, file = null, message = "Downloading and verifying FlipRSS.apk…")
        viewModelScope.launch {
            var file: File? = null
            try {
                file = remote.download(update, File(app.cacheDir, "updates"))
                withContext(Dispatchers.IO) { verifyPackage(file, update) }
                mutable.value = old.copy(file = file, message = "Update verified. Tap Install update to continue.")
            } catch (cancelled: CancellationException) { file?.delete(); throw cancelled }
            catch (error: Exception) { file?.delete(); mutable.value = old.copy(file = null, message = problem(error)) }
        }
    }
    @Suppress("DEPRECATION")
    private fun verifyPackage(file: File, update: AppUpdate) {
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val candidate = app.packageManager.getPackageArchiveInfo(file.path, flags) ?: throw UpdateProblem("Downloaded file is not an Android app.")
        val installed = app.packageManager.getPackageInfo(app.packageName, flags)
        val version = if (Build.VERSION.SDK_INT >= 28) candidate.longVersionCode else candidate.versionCode.toLong()
        fun signers(info: android.content.pm.PackageInfo): Set<String> {
            val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
            return signatures.orEmpty().map { signature -> MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
                .joinToString("") { "%02x".format(it) } }.toSet()
        }
        val trusted = signers(installed)
        if (candidate.packageName != app.packageName || version != update.code || version <= installedCode ||
            trusted.isEmpty() || trusted != signers(candidate)) throw UpdateProblem("This APK does not match this app's signing identity or version. Install the matching release from GitHub.")
    }
    fun install() {
        val old = mutable.value; val file = old.file ?: return; val update = old.update ?: return
        try {
            if (!app.packageManager.canRequestPackageInstalls()) {
                app.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${app.packageName}".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                mutable.value = old.copy(message = "Allow updates from FlipRSS, return here, then tap Install update again.")
                return
            }
            // Revalidate immediately before granting the installer access.
            verifyPackage(file, update)
            val uri = FileProvider.getUriForFile(app, "${app.packageName}.updates", file)
            app.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION))
            mutable.value = old.copy(message = "Finish the update in Android's installer.")
        } catch (error: Exception) { mutable.value = old.copy(message = problem(error)) }
    }
    private fun problem(error: Exception): String = (error as? UpdateProblem)?.message
        ?: "Could not complete the update. Check your connection and free storage, or open GitHub Releases."
}
