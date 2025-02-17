package com.example.files
import app.cash.turbine.test
import com.gemwallet.android.model.DownloadStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class FileServiceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var fileService: FileService
    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var mockCall: Call
    private lateinit var mockResponse: Response
    private lateinit var mockResponseBody: ResponseBody

    @Before
    fun setup() {
        mockOkHttpClient = mockk(relaxed = true)
        mockCall = mockk()
        mockResponse = mockk()
        mockResponseBody = mockk()

        fileService = FileService(mockOkHttpClient)
    }

    @Test
    fun `downloadFile should emit Started, Downloading and Completed states`() = runTest {
        val testUrl = "https://example.com/file.apk"
        val testFileName = "file.apk"
        val saveDirectory = tempFolder.root.absolutePath
        val fileContent = ByteArray(1024 * 10) { it.toByte() }

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponseBody.byteStream() } returns ByteArrayInputStream(fileContent)
        every { mockResponseBody.contentLength() } returns fileContent.size.toLong()
        every { mockResponse.body() } returns mockResponseBody

        fileService.downloadFile(testUrl, saveDirectory, testFileName).test {
            assertEquals(DownloadStatus.Started, awaitItem())
            var progressState: DownloadStatus
            do {
                progressState = awaitItem()
            } while (progressState is DownloadStatus.Downloading)
            assertEquals(DownloadStatus.Completed, progressState)
            cancelAndConsumeRemainingEvents()
        }

        val downloadedFile = File(saveDirectory, testFileName)
        assertTrue(downloadedFile.exists())
        assertEquals(fileContent.size.toLong(), downloadedFile.length())
    }

    @Test
    fun `downloadFile should emit Error state when request fails`() = runTest {
        val testUrl = "https://example.com/file.apk"
        val saveDirectory = tempFolder.root.absolutePath
        val testFileName = "file.apk"

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 404
        every { mockResponse.message() } returns "Not Found"

        fileService.downloadFile(testUrl, saveDirectory, testFileName).test {
            assertEquals(DownloadStatus.Started, awaitItem())
            assertTrue(awaitItem() is DownloadStatus.Error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteFile should return true when file is deleted`() = runTest {
        val testFile = tempFolder.newFile("test.apk")
        assertTrue(testFile.exists())

        val result = fileService.deleteFile(tempFolder.root.absolutePath, "test.apk")

        assertTrue(result)
        assertFalse(testFile.exists())
    }

    @Test
    fun `deleteFile should return false when file does not exist`() = runTest {
        val result = fileService.deleteFile(tempFolder.root.absolutePath, "non_existing.apk")

        assertFalse(result)
    }
}
