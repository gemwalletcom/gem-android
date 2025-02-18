package com.gemwallet.android.data.repositoreis.update

import com.gemwallet.android.cases.update.CheckForUpdateCase
import com.gemwallet.android.cases.update.DeleteLatestApkCase
import com.gemwallet.android.cases.update.DownloadLatestApkCase
import com.gemwallet.android.cases.update.LatestApkDownloadedCase
import com.gemwallet.android.cases.update.ObserveUpdateDownloadCase
import com.gemwallet.android.cases.update.SkipVersionCase
import com.gemwallet.android.data.repositoreis.BuildConfig
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.services.files.FileService
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.model.AppUpdateConfig
import com.gemwallet.android.model.BuildInfo
import com.gemwallet.android.model.DownloadStatus
import com.wallet.core.primitives.PlatformStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.net.URL

class AppUpdateRepository(
    private val apkDirectory: String,
    private val buildInfo: BuildInfo,
    private val appUpdateConfig: AppUpdateConfig,
    private val userConfig: UserConfig,
    private val gemApiClient: GemApiClient,
    private val fileService: FileService,
) : CheckForUpdateCase, DeleteLatestApkCase, DownloadLatestApkCase, LatestApkDownloadedCase,
    ObserveUpdateDownloadCase, SkipVersionCase {

    private val apkDownloadStatus: MutableStateFlow<DownloadStatus?> = MutableStateFlow(null)

    override suspend fun checkForUpdate(): String? {
        return withContext(Dispatchers.Default) {
            if (BuildConfig.DEBUG) {
                return@withContext null
            }

            val response = gemApiClient.getConfig().getOrNull()
            val current = response?.releases?.firstOrNull {
                val versionFlavor = when (it.store) {
                    PlatformStore.GooglePlay -> "google"
                    PlatformStore.Fdroid -> "fdroid"
                    PlatformStore.Huawei -> "huawei"
                    PlatformStore.SolanaStore -> "solana"
                    PlatformStore.SamsungStore -> "sumsung"
                    PlatformStore.ApkUniversal -> "universal"
                    PlatformStore.AppStore -> it.store.string
                    PlatformStore.Local -> "local"
                }

                buildInfo.platformStore.string == versionFlavor
            }?.version ?: return@withContext null

            val skipVersion = userConfig.getAppVersionSkip()
            if (current.compareTo(buildInfo.versionName) > 0 && skipVersion != current) {
                current
            } else {
                null
            }
        }
    }

    override suspend fun deleteLatestApk() {
        fileService.deleteFile(apkDirectory, getApkName(appUpdateConfig.updateUrl))
    }

    override suspend fun downloadLatestApk() {
        require(buildInfo.platformStore == PlatformStore.ApkUniversal) { "APK download is only supported for the 'universal' platform." }

        fileService.downloadFile(
            appUpdateConfig.updateUrl,
            apkDirectory,
            getApkName(appUpdateConfig.updateUrl)
        ).collect { status ->
            apkDownloadStatus.update { status }
        }
    }

    override suspend fun isLatestApkDownloaded(): Boolean {
        return fileService.isFileExists(apkDirectory, getApkName(appUpdateConfig.updateUrl))
    }

    private fun getApkName(updateUrl: String): String {
        return try {
            URL(updateUrl).path.substringAfterLast("/").takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Invalid APK URL: $updateUrl")
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL format: $updateUrl", e)
        }
    }

    override fun observeUpdateDownload(): Flow<DownloadStatus?> {
        return apkDownloadStatus
    }

    override suspend fun skipVersion(version: String) {
        userConfig.setAppVersionSkip(version)
        deleteLatestApk()
    }
}