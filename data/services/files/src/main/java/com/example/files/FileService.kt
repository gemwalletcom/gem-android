package com.example.files

import com.gemwallet.android.model.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Named

class FileService(
    @Named("DownloadFileHttpClient") private val okHttpClient: OkHttpClient,
) {
    fun downloadFile(url: String, saveDirectory: String, fileName: String): Flow<DownloadStatus> =
        flow {
            emit(DownloadStatus.Started)

            val outputFile = File(saveDirectory, fileName)

            val request = Request.Builder().url(url).build()
            val response = try {
                okHttpClient.newCall(request).execute()
            } catch (e: Exception) {
                emit(DownloadStatus.Error(e))
                return@flow
            }

            if (!response.isSuccessful) {
                val errorMessage = "${response.code()}: ${response.message()}"
                emit(DownloadStatus.Error(Exception(errorMessage)))
                return@flow
            }

            val body = response.body() ?: run {
                emit(DownloadStatus.Error(Exception("Response body is null")))
                return@flow
            }

            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(outputFile)

            val totalSize = body.contentLength()
            val buffer = ByteArray(8 * 1024)
            var downloadedSize = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedSize += bytesRead
                val progress = (downloadedSize * 100 / totalSize).toInt()
                emit(DownloadStatus.Downloading(progress))
            }

            outputStream.flush()
            inputStream.close()
            outputStream.close()

            emit(DownloadStatus.Completed)
        }
            .catch {
                emit(DownloadStatus.Error(it))
                it.printStackTrace()
            }
            .flowOn(Dispatchers.IO)

    suspend fun deleteFile(directory: String, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(directory, fileName)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        }
}

