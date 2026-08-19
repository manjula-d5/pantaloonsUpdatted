package com.rfid.rfidreader.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val TAG = "AppLogger"
    private const val LOG_FILE_NAME = "rfid_reader_logs.txt"
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun init(context: Context) {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        logFile = File(dir, LOG_FILE_NAME)
        log("Logger initialized. Path: ${logFile?.absolutePath}")
    }

    fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        val formattedMessage = "[$timestamp] $message"
        
        // Print to Logcat
        Log.d(TAG, message)

        // Write to File - synchronized to prevent concurrent write issues
        synchronized(this) {
            try {
                logFile?.let { file ->
                    val writer = FileWriter(file, true)
                    writer.append(formattedMessage).append("\n")
                    writer.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to log file: ${e.message}")
            }
        }
    }

    fun logError(message: String, throwable: Throwable? = null) {
        val errorMsg = if (throwable != null) {
            "$message | Error: ${throwable.message}\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        log("ERROR: $errorMsg")
    }

    fun getLogFilePath(): String = logFile?.absolutePath ?: "Not initialized"
}
