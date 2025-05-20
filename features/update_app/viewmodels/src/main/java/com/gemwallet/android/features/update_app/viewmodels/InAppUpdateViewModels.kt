package com.gemwallet.android.features.update_app.viewmodels

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.config.GetLatestVersion
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.model.BuildInfo
import com.wallet.core.primitives.PlatformStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.IOException
import okio.buffer
import okio.sink
import java.io.File
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InAppUpdateViewModels @Inject constructor(
    @ApplicationContext private val context: Context,
    private val buildInfo: BuildInfo,
    private val getLatestVersion: GetLatestVersion,
    private val userConfig: UserConfig,
) : ViewModel() {

    val appFileProvider = "${context.packageName}.provider"
    val intentDataType = "application/vnd.android.package-archive"

    val updateAvailable = getLatestVersion.getLatestVersion().map {
        if (buildInfo.versionName != it && buildInfo.platformStore == PlatformStore.ApkUniversal && userConfig.developEnabled()) {
            it
        } else {
            null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getApkFile().delete() }
        }
    }

    fun update(): Boolean {
        if (!requestInstallFromUnknownSources(context)) {
            return false
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                download()
                installApk()
            } catch (err: Throwable) {
                Log.d("DOWNLOAD", "Error", err)
                downloadState.update { DownloadState.Error }
            }
        }
        return true
    }

    private fun download() {
        val version = updateAvailable.value ?: throw IllegalArgumentException()
        val url = "https://apk.gemwallet.com/gem_wallet_universal_v${version}.apk"
        val destinationFile = getApkFile()
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body ?: throw IOException()
        val contentLength = body.contentLength()
        val source: BufferedSource = body.source()

        val sink: BufferedSink = destinationFile.sink().buffer()
        val sinkBuffer: Buffer = sink.buffer

        var totalBytesRead: Long = 0
        val bufferSize = 8 * 1024
        var bytesRead: Long = 0

        while ((source.read(sinkBuffer, bufferSize.toLong()).also { bytesRead = it }) != -1L) {
            sink.emit()
            totalBytesRead += bytesRead
            downloadState.update { DownloadState.Progress(totalBytesRead.toFloat() / contentLength.toFloat()) }
        }

        sink.flush()
        sink.close()
        source.close()
        downloadState.update { DownloadState.Success }
    }

    private fun requestInstallFromUnknownSources(context: Context): Boolean = context.packageManager.canRequestPackageInstalls()

    private fun installApk() {
        val fileName = getApkFile()
        val apkUri = FileProvider.getUriForFile(context, appFileProvider, fileName)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(apkUri, intentDataType)
        }
        context.startActivity(intent)
    }

    private fun getApkFile(): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "gem.apk")
    }
}

sealed interface DownloadState {
    object Idle : DownloadState
    object Preparing : DownloadState
    class Progress(val value: Float) : DownloadState
    object Success : DownloadState
    object Error : DownloadState
}