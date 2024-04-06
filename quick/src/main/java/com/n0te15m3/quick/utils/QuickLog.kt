package com.n0te15m3.quick.utils

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

internal object QuickLog {
    var enabled: AtomicBoolean = AtomicBoolean(false)

    fun d(message: String) {
        if (enabled.get()) {
            Thread.currentThread().stackTrace[3].let {
                Log.d(QUICK, "[${it.fileName}:${it.lineNumber}] [Debug]: $message")
            }
        }
    }

    fun e(message: String) {
        if (enabled.get()) {
            Thread.currentThread().stackTrace[3].let {
                Log.e(QUICK, "[${it.fileName}:${it.lineNumber}] [Error]: $message")
            }
        }
    }

    fun network(message: String) {
        if (enabled.get()) {
            Thread.currentThread().stackTrace[3].let {
                Log.i(QUICK, "[${it.fileName}:${it.lineNumber}] [Network]: $message")
            }
        }
    }
}