package com.gemwallet.android.features.update_app.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.config.GetInAppUpdateTask
import com.gemwallet.android.cases.config.GetLatestVersion
import com.gemwallet.android.cases.config.SetInAppUpdateTask
import com.gemwallet.android.model.BuildInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.util.moveToByteArray
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.utils.io.read
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.writeByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InAppUpdateViewModels @Inject constructor(
    @ApplicationContext private val context: Context,
    private val buildInfo: BuildInfo,
    private val getLatestVersion: GetLatestVersion,
    private val getInAppUpdateTask: GetInAppUpdateTask,
    private val setInAppUpdateTask: SetInAppUpdateTask,
) : ViewModel() {

    val httpClient = HttpClient()

    val updateAvailable = getLatestVersion.getLatestVersion().map {
        if (buildInfo.versionName != it) {
            it
        } else {
            null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

//    val updateTask = getInAppUpdateTask.getInAppUpdateTask()
//        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val progress = MutableStateFlow<Int?>(null)

    fun startDownload() = viewModelScope.launch(Dispatchers.IO) {
        val version = updateAvailable.value ?: return@launch
//        val url = "https://apk.gemwallet.com/gem_wallet_universal_v${version}.apk"
        val url = "https://apk.gemwallet.com/gem_wallet_universal_v1.2.361.apk"
        val destinationFile = File(context.externalCacheDir, "${version}.apk")

        try {
            httpClient.downloadFile(destinationFile, url, progress) {  }
        } catch (err: Throwable) {
            Log.d("DOWNLOAD", "Error", err)
            // TODO: Show errors
        }
    }
}

suspend fun HttpClient.downloadFile(file: File, url: String, progress: MutableStateFlow<Int?>, callback: suspend (boolean: Boolean) -> Unit) {
    val response = request(url) {
        HttpMethod.Get
    }
    val size = response.contentLength()!!.toFloat()
    val writeChannel = file.writeChannel()
    var offset = 0
    val readChannel = response.bodyAsChannel()

    do {
        val buffer = ByteArray(10 * 1024)
        val currentRead = readChannel.readAvailable(buffer, 0, buffer.size)
        offset += currentRead
        progress.update { (offset.toFloat() * 100f / size).roundToInt() }
        writeChannel.writeByteArray(buffer)
    } while (currentRead > 0)
    if (!response.status.isSuccess()) {
        callback(false)
    }
    progress.update { null }
    callback(true)
}

data class AppVersionDownloadStatus(
    val state: DownloadState,
    val progress: Int = 0,
)

enum class DownloadState {
    COMPLETE,
    PROGRESS,
    FAIL,
}