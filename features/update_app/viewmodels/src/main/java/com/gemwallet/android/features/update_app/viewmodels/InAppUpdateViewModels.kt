package com.gemwallet.android.features.update_app.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.config.GetInAppUpdateTask
import com.gemwallet.android.cases.config.GetLatestVersion
import com.gemwallet.android.cases.config.SetInAppUpdateTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InAppUpdateViewModels @Inject constructor(
    private val currentVersion: String,
    private val getLatestVersion: GetLatestVersion,
    private val getInAppUpdateTask: GetInAppUpdateTask,
    private val setInAppUpdateTask: SetInAppUpdateTask,
) : ViewModel() {

    val updateAvailable = getLatestVersion.getLatestVersion().map {
        if (currentVersion != it) {
            it
        } else {
            null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val updateTask = getInAppUpdateTask.getInAppUpdateTask()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

//    val progress = getInAppUpdateTask.getInAppUpdateTask()
//        .flatMapLatest { id ->
//            flow {
//                if (id == null) {
//                    emit(null)
//                    return@flow
//                }
//
//                while (true) {
//                    val status = observeDownload(id, context)
//                    emit(status)
//
//                    if (status?.state != DownloadState.PROGRESS) {
//                        break
//                    }
//                    delay(60 * 1000)
//                }
//            }
//        }
//        .flowOn(Dispatchers.IO)

    fun startDownload(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val version = updateAvailable.value ?: return@launch
        val url = "versionUrl"
        val fileName = version
        val file: File? = null//createDocumentFile(fileName, context)

        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) // Visibility of the download Notification
//            .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
            .setTitle(fileName) // Title of the Download Notification
            .setDescription("Gem Wallet update") // Description of the Download Notification
            .setRequiresCharging(false) // Set if charging is required to begin the download
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        setInAppUpdateTask.setInAppUpdateTask(downloadID)
    }

    suspend fun observeDownload(downloadID: Long, context: Context): AppVersionDownloadStatus? {
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