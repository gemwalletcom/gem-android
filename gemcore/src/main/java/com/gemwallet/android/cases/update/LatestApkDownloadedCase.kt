package com.gemwallet.android.cases.update

interface LatestApkDownloadedCase {
    suspend fun isLatestApkDownloaded(): Boolean
}