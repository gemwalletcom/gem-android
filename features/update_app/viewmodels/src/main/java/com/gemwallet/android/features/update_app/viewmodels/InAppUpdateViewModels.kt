package com.gemwallet.android.features.update_app.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InAppUpdateViewModels @Inject constructor(
    private val currentVersion: String,
    private val getLatestVersion: GetLatestVersion,
    private val getInAppUpdateTask: GetInAppUpdateTask,
    private val setInAppUpdateTask: SetInAppUpdateTask,
) : ViewModel() {

    val updateAvailable = MutableStateFlow<String?>(null)
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val latestVersion = getLatestVersion.getLatestVersion()
            if (currentVersion != latestVersion) {
                updateAvailable.update { latestVersion }
            }
        }
    }

    fun startDownload(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val version = updateAvailable.value ?: return@launch
        val url = "versionUrl"
        val fileName = version
        val file: File? = null//createDocumentFile(fileName, context)

        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) // Visibility of the download Notification
            .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
            .setTitle(fileName) // Title of the Download Notification
            .setDescription("Gem Wallet update") // Description of the Download Notification
            .setRequiresCharging(false) // Set if charging is required to begin the download
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        setInAppUpdateTask.setInAppUpdateTask(downloadID)
    }

    private suspend fun observeDownload(downloadID: Long, context: Context): AppVersionDownloadStatus? {
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

interface GetLatestVersion {
    suspend fun getLatestVersion(): String
}

interface GetInAppUpdateTask {
    fun getInAppUpdateTask(): Flow<Long?>
}

interface SetInAppUpdateTask {
    suspend fun setInAppUpdateTask(task: Long?)
}