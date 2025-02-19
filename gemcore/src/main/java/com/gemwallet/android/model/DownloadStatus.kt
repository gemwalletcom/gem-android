package com.gemwallet.android.model

import java.io.File

sealed class DownloadStatus {
    object Started : DownloadStatus()
    class Downloading(val progress: Int) : DownloadStatus()
    class Completed(val file: File) : DownloadStatus()
    class Error(val error: Throwable) : DownloadStatus()
}