package com.remote.tvremote

import android.content.Context
import android.util.Base64
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import java.io.File
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Talks to an Android TV over WiFi using the ADB protocol (no root, no USB
 * cable needed) via the cgutman/AdbLib pure-Java implementation.
 *
 * The very first connection will show an "Allow USB debugging?" prompt on
 * the TV screen - the user has to accept it once, after which this app's
 * key is remembered by the TV (as long as the same adbkey files are reused,
 * which we persist in internal storage).
 *
 * NOTE: AdbLib's exact API can differ slightly between versions/forks. If
 * Gradle resolves a different version and this file fails to compile, check
 * https://github.com/cgutman/AdbLib for the exact method names and adjust
 * this file only - the rest of the app does not need to change.
 */
class AdbRemoteClient(private val context: Context) {

    private var socket: Socket? = null
    private var connection: AdbConnection? = null
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    var isConnected: Boolean = false
        private set

    private fun getCrypto(): AdbCrypto {
        val privateKeyFile = File(context.filesDir, "adbkey")
        val publicKeyFile = File(context.filesDir, "adbkey.pub")
        return if (privateKeyFile.exists() && publicKeyFile.exists()) {
            AdbCrypto.loadAdbKeyPair(base64Impl, privateKeyFile, publicKeyFile)
        } else {
            val crypto = AdbCrypto.generateAdbKeyPair(base64Impl)
            crypto.saveAdbKeyPair(privateKeyFile, publicKeyFile)
            crypto
        }
    }

    /** Connects on a background thread. [onResult] runs on that same thread. */
    fun connect(ip: String, port: Int = 5555, onResult: (success: Boolean, error: String?) -> Unit) {
        executor.execute {
            try {
                disconnectInternal()
                val s = Socket(ip, port)
                s.tcpNoDelay = true
                socket = s
                val crypto = getCrypto()
                val conn = AdbConnection.create(s, crypto)
                conn.connect()
                connection = conn
                isConnected = true
                onResult(true, null)
            } catch (e: Exception) {
                isConnected = false
                onResult(false, e.message ?: e.toString())
            }
        }
    }

    private fun runShell(command: String) {
        val conn = connection ?: return
        executor.execute {
            try {
                val stream = conn.open("shell:$command")
                stream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendKeyEvent(keyCode: Int) = runShell("input keyevent $keyCode")

    fun sendText(text: String) {
        val escaped = text.replace(" ", "%s").replace("\"", "")
        runShell("input text \"$escaped\"")
    }

    fun tap(x: Int, y: Int) = runShell("input tap $x $y")

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Int = 200) =
        runShell("input swipe $x1 $y1 $x2 $y2 $durationMs")

    fun launchApp(packageName: String) =
        runShell("monkey -p $packageName -c android.intent.category.LAUNCHER 1")

    private fun disconnectInternal() {
        try { connection?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        connection = null
        socket = null
        isConnected = false
    }

    fun disconnect() {
        executor.execute { disconnectInternal() }
    }

    private val base64Impl = object : AdbBase64 {
        override fun encodeToString(data: ByteArray): String =
            Base64.encodeToString(data, Base64.NO_WRAP)
    }
}
