package com.n0te15m3.quick.cache

import com.n0te15m3.quick.DataSource
import com.n0te15m3.quick.LoadingState
import com.n0te15m3.quick.utils.QuickLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.headers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.ceil
import kotlin.math.min

class ImageDownloader(
    private val outChannel: Channel<Pair<String, LoadingState>>
) {
    companion object {
        const val MAX_ATTEMPT_ALLOW = 5
        const val NUMBER_OF_SPLIT_BATCH = 5
    }

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = HttpClient {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
//                    QuickLog.network(message)
                }
            }
            level = LogLevel.HEADERS
        }
    }

    private val jobs: HashMap<String, Job> = hashMapOf()

    suspend fun fetchImage(url: String, inSilentMode: Boolean = false) = scope.launch {
        if (url !in jobs) {
            val response = client.head(urlString = url)

            val fileSize = response.headers["Content-Length"]?.toInt() ?: 0
            QuickLog.d("FileSize: $fileSize")

            batchDownload(url, fileSize, inSilentMode)
        }
    }

    private suspend fun batchDownload(url: String, fileSize: Int, inSilentMode: Boolean) {
        jobs[url] = scope.launch {
            if (!inSilentMode) {
                outChannel.send(url to LoadingState.Loading)
            }

            val batches = arrayOfNulls<ByteArray>(NUMBER_OF_SPLIT_BATCH)
            val eachBatchSize = ceil(fileSize / NUMBER_OF_SPLIT_BATCH.toDouble()).toInt()

            (0 until NUMBER_OF_SPLIT_BATCH).forEach { index ->
                launch {
                    var currAttempt = 1

                    while (currAttempt < MAX_ATTEMPT_ALLOW) {
                        try {
                            val batchResponse = client.get(url) {
                                headers {
                                    append(
                                        "Range", "bytes=${(index * eachBatchSize)}-${
                                            min(fileSize - 1, (index + 1) * eachBatchSize - 1)
                                        }"
                                    )
                                }
                            }

                            val binaryResponse: ByteArray = batchResponse.body()

                            if ((binaryResponse.size != eachBatchSize) && (index == NUMBER_OF_SPLIT_BATCH - 1 && binaryResponse.size != fileSize % eachBatchSize)) {
                                throw Exception("Size not matched")
                            }

                            batches[index] = binaryResponse
                            tryJoinBatches(url, batches, inSilentMode)

                            return@launch
                        } catch (e: Exception) {
                            currAttempt += 1
                        }
                    }

                    cancelBatchJob(url, "Ran out of attempt to fetch data", inSilentMode)
                }
            }
        }
    }

    private suspend fun tryJoinBatches(
        url: String, batches: Array<ByteArray?>, inSilentMode: Boolean
    ) {
        mutex.withLock {
            batches.filterNotNull().let {
                if (it.size == batches.size) {
                    var joinResult = byteArrayOf()
                    it.forEach { segment -> joinResult += segment }

                    try {
                        // Finish fetching data, but if somehow the data is invalid, not fetching again, just send failure signal
                        finishFetching(url, joinResult)
                    } catch (e: Exception) {
                        cancelBatchJob(url, e.message.orEmpty(), inSilentMode)
                    }
                }
            }
        }
    }

    private suspend fun finishFetching(url: String, byteArr: ByteArray) {
        outChannel.send(
            url to LoadingState.Success(
                source = DataSource.REMOTE,
                data = byteArr
            )
        )
        cancelAndRemoveJob(url)
    }

    private suspend fun cancelBatchJob(url: String, reason: String, inSilentMode: Boolean) {
        if (!inSilentMode)
            outChannel.send(url to LoadingState.Failure(reason))
        cancelAndRemoveJob(url)
    }

    private fun cancelAndRemoveJob(key: String) {
        jobs[key]?.cancel()
        jobs.remove(key)
    }
}