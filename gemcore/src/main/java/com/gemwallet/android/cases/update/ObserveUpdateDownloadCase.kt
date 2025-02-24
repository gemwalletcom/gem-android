package com.gemwallet.android.cases.update

import com.gemwallet.android.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface ObserveUpdateDownloadCase {
    fun observeUpdateDownload(): Flow<DownloadStatus?>
}