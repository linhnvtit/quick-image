package com.n0te15m3.quick.disk_manager

import android.content.Context
import com.n0te15m3.quick.cache.pool.LFUPool
import com.n0te15m3.quick.cache.pool.QuickCachePool
import com.n0te15m3.quick.utils.EMPTY
import com.n0te15m3.quick.utils.QuickLog
import com.n0te15m3.quick.utils.truncateHead
import java.io.File

class DiskManager(
    private val storageHelper: IStorageHelper,
    context: Context,
) {
    companion object {
        const val CACHE_FOLDER = "quick_cache"
        const val CLEAN_DURATION = 86400 * 1000
        const val MAX_HISTORY_RECORD = 1000
        const val MAX_POOL_CAPACITY = 1000
    }

    private val pool: QuickCachePool<Any> = LFUPool(MAX_POOL_CAPACITY)
    private var lastUpdatedFile: String? = null

    init {
        loadCacheHistory(context)
    }

    /**
     * Make new folder if it not created
     * Then write data to file and
     * Record file's profile
     */
    fun createCacheFile(context: Context, fileName: String, data: ByteArray) {
        createCacheFolder(context)
        writeFileContent(context.cacheDir, "$CACHE_FOLDER/$fileName", data)
        writeFileProfile(context.cacheDir, "$CACHE_FOLDER/$fileName.prof")
        updateCacheHistory(context, fileName)
    }

    fun deleteCacheFile(context: Context, fileName: String) {
        storageHelper.deleteFile(context.cacheDir, "$CACHE_FOLDER/${fileName}")
        storageHelper.deleteFile(context.cacheDir, "$CACHE_FOLDER/${fileName}.prof")
    }

    private fun loadCacheHistory(context: Context) {
        val content =
            storageHelper.readFileDataByText(context.cacheDir, "$CACHE_FOLDER/history").trim()
        val toBeRemovedItems = hashSetOf<String>()

        val listFiles = File(context.cacheDir, CACHE_FOLDER).listFiles()?.filter { it.nameWithoutExtension != "history" }?.map { it.nameWithoutExtension }?.toHashSet()
        val contents = content.split("\n")
        val records = contents.truncateHead(MAX_HISTORY_RECORD)
        val truncatedRecords = if (contents.size > MAX_HISTORY_RECORD) contents.take(contents.size - MAX_HISTORY_RECORD) else listOf()

        // Add truncated files
        truncatedRecords.forEach {
            toBeRemovedItems.add(it)
        }

        // Add files that removed in cache pool
        records.forEach {
            listFiles?.remove(it)
            pool.put(it, Unit)?.let { data ->
                toBeRemovedItems.add(data.first)
            }
        }

        // Add unrelated files in Quick's cache folder
        listFiles?.forEach {
            toBeRemovedItems.add(it)
        }

        val recordsStr = records.joinToString(separator = "\n")

        val removeFilesRegex = buildString {
            toBeRemovedItems.forEach {
                storageHelper.deleteFile(context.cacheDir, "$CACHE_FOLDER/$it")
                storageHelper.deleteFile(context.cacheDir, "$CACHE_FOLDER/$it.prof")
                append("$it\\n|")
            }
        }.dropLast(1)

        writeFileContent(
            context.cacheDir,
            "$CACHE_FOLDER/history",
            recordsStr.replace(removeFilesRegex.toRegex(), EMPTY).toByteArray()
        )
    }

    private fun updateCacheHistory(context: Context, fileName: String) {
        val content = storageHelper.readFileDataByText(context.cacheDir, "$CACHE_FOLDER/history")
        storageHelper.writeDataToFile(
            File(context.cacheDir, "$CACHE_FOLDER/history"), "$content$fileName".toByteArray()
        )
        pool.put(fileName, Unit)?.let { data ->
            storageHelper.deleteFile(context.cacheDir, "$CACHE_FOLDER/${data.first}")
            storageHelper.deleteFile(context.cacheDir, "$CACHE_FOLDER/${data.first}.prof")
        }

        lastUpdatedFile = fileName
    }

    /**
     * Load cache file
     */
    fun loadCacheFile(context: Context, fileName: String): Pair<ByteArray, Boolean>? {
        val fileProfile =
            storageHelper.readFileDataByText(context.cacheDir, "$CACHE_FOLDER/$fileName.prof")
        val fileData =
            storageHelper.readFileDataByBytes(context.cacheDir, "$CACHE_FOLDER/$fileName")

        return try {
            if (fileName != lastUpdatedFile) {
                updateCacheHistory(context, fileName)
            }

            fileData!! to (fileProfile.trim().split("-")[1].toLong() <= System.currentTimeMillis())
        } catch (e: Exception) {
            QuickLog.e("${e.message}")
            null
        }
    }

    /**
     * Write file content.
     */
    private fun writeFileContent(parentFile: File, fileName: String, data: ByteArray) {
        storageHelper.writeDataToFile(
            parentFile = File(parentFile, fileName), data = data
        )
    }

    /**
     * Write profile for file.
     * For now, the format is: {CreateDate}-{ExpireDate}.
     */
    private fun writeFileProfile(parentFile: File, fileName: String) {
        storageHelper.writeDataToFile(
            parentFile = File(parentFile, fileName),
            data = "${System.currentTimeMillis()}-${System.currentTimeMillis() + CLEAN_DURATION}".toByteArray()
        )
    }

    /**
     * Create Quick's cache folder if it not existed
     */
    private fun createCacheFolder(context: Context) {
        storageHelper.createFolder(context.cacheDir, CACHE_FOLDER)
    }
}