package com.n0te15m3.quick.disk_manager

import com.n0te15m3.quick.utils.EMPTY
import com.n0te15m3.quick.utils.QuickLog
import java.io.File
import java.io.FileOutputStream


interface IStorageHelper {
    fun createFolder(parentFile: File, folderName: String)
    fun readFileDataByBytes(parentFile: File, fileName: String): ByteArray?
    fun readFileDataByText(parentFile: File, fileName: String): String
    fun writeDataToFile(parentFile: File, data: ByteArray)
    fun deleteFile(parentFile: File, fileName: String): Boolean
}

class StorageHelper : IStorageHelper {
    override fun deleteFile(parentFile: File, fileName: String): Boolean {
        return try {
            File(parentFile, fileName).delete()
        } catch (e: Exception) {
            QuickLog.e("Failed to delete file: ${e.message}")
            false
        }
    }

    override fun createFolder(parentFile: File, folderName: String) {
        try {
            val directory = File(parentFile, folderName)
            if (!directory.exists()) {
                val success = directory.mkdirs()
                if (!success) {
                    QuickLog.e("Failed to create cache folder")
                }
            }
        } catch (e: Exception) {
            QuickLog.e("Failed to create cache folder: ${e.message}")
        }
    }

    override fun readFileDataByBytes(
        parentFile: File, fileName: String
    ): ByteArray? {
        val file = File(parentFile, fileName)

        return try {
            file.readBytes()
        } catch (e: Exception) {
            QuickLog.e("${e.message}")
            null
        }
    }

    override fun readFileDataByText(parentFile: File, fileName: String): String {
        val file = File(parentFile, fileName)

        return try {
            val content = StringBuilder()
            file.bufferedReader().useLines { lines ->
                lines.forEach {
                    content.appendln(it)
                }
            }
            content.toString()
        } catch (e: Exception) {
            QuickLog.e("${e.message}")
            EMPTY
        }
    }

    /**
     * Write data to cache file
     */
    override fun writeDataToFile(parentFile: File, data: ByteArray) {
        try {
            val fos = FileOutputStream(parentFile)
            fos.write(data)
            fos.close()
            QuickLog.d("Success")
        } catch (e: Exception) {
            QuickLog.e("${e.message}")
        }
    }
}