package com.gemwallet.android.model

sealed class DownloadStatus {
    object Started : DownloadStatus()
    class Downloading(val progress: Int) : DownloadStatus()
    object Completed : DownloadStatus()
    class Error(val error: Throwable) : DownloadStatus()
}