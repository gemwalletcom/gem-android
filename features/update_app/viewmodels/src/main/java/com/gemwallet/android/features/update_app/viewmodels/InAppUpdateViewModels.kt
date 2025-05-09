package com.gemwallet.android.features.update_app.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.config.GetInAppUpdateTask
import com.gemwallet.android.cases.config.GetLatestVersion
import com.gemwallet.android.cases.config.SetInAppUpdateTask
import com.gemwallet.android.model.BuildInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class InAppUpdateViewModels @Inject constructor(
    @ApplicationContext private val context: Context,
    private val buildInfo: BuildInfo,
    private val getLatestVersion: GetLatestVersion,
    private val getInAppUpdateTask: GetInAppUpdateTask,
    private val setInAppUpdateTask: SetInAppUpdateTask,
) : ViewModel() {

    val updateAvailable = getLatestVersion.getLatestVersion().map {
        if (buildInfo.versionName != it) {
            it
        } else {
            null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val updateTask = getInAppUpdateTask.getInAppUpdateTask()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val progress = getInAppUpdateTask.getInAppUpdateTask()
        .flatMapLatest { id ->
            flow {
                if (id == null) {
                    emit(null)
                    return@flow
                }

                while (true) {
                    val status = observeDownload(id, context)
                    emit(status)

                    if (status?.state != DownloadState.PROGRESS) {
                        break
                    }
                    delay(60 * 1000)
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun startDownload() = viewModelScope.launch(Dispatchers.IO) {
        val version = updateAvailable.value ?: return@launch
        val url = "https://apk.gemwallet.com/gem_wallet_universal_v${buildInfo.versionName}.apk"
        val fileName = version
        val file: File? = null//createDocumentFile(fileName, context)

        val request = DownloadManager.Request(url.toUri())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setTitle(fileName) // Title of the Download Notification
            .setDescription("Gem Wallet update") // Description of the Download Notification
            .setRequiresCharging(false) // Set if charging is required to begin the download
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        setInAppUpdateTask.setInAppUpdateTask(downloadID)
    }

    fun observeDownload(downloadID: Long, context: Context): AppVersionDownloadStatus? {
        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        return if (cursor.moveToFirst()) {
            val colIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = if (colIndex < 0) null else cursor.getInt(colIndex)
            when (status) {
                DownloadManager.STATUS_FAILED -> AppVersionDownloadStatus(DownloadState.FAIL)
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING -> {
                    val total = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES).let {
                        if (it < 0) -1 else cursor.getLong(it)
                    }
                    val progress = if (total >= 0) {
                        val downloaded = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR).let {
                            if (it < 0) 0 else cursor.getLong(it)
                        }
                        ((downloaded * 100L) / total).toInt()
                        // if you use downloadmanger in async task, here you can use like this to display progress.
                        // Don't forget to do the division in long to get more digits rather than double.
                        //  publishProgress((int) ((downloaded * 100L) / total));
                    } else {
                        0
                    }
                    AppVersionDownloadStatus(DownloadState.PROGRESS, progress)
                }

                DownloadManager.STATUS_SUCCESSFUL -> AppVersionDownloadStatus(DownloadState.COMPLETE, 100)
                else -> {
                    AppVersionDownloadStatus(DownloadState.PROGRESS, 0)
                }
            }
        } else {
            null
        }
    }
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