package com.remote.tvremote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var client: AdbRemoteClient
    private lateinit var prefs: Prefs
    private lateinit var voice: VoiceCommandManager

    private lateinit var etIp: EditText
    private lateinit var tvStatus: TextView

    private val recordAudioRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = AdbRemoteClient(applicationContext)
        prefs = Prefs(applicationContext)

        etIp = findViewById(R.id.etIp)
        tvStatus = findViewById(R.id.tvStatus)
        etIp.setText(prefs.lastIp)

        voice = VoiceCommandManager(
            activity = this,
            onCommand = { text -> handleVoiceCommand(text) },
            onStateChange = { listening ->
                runOnUiThread {
                    if (listening) tvStatus.text = getString(R.string.listening)
                    else updateStatusText()
                }
            }
        )

        findViewById<Button>(R.id.btnConnect).setOnClickListener { doConnect() }

        findViewById<ImageButton>(R.id.btnPower).setOnClickListener { client.sendKeyEvent(KeyCodes.POWER) }
        findViewById<Button>(R.id.btnHome).setOnClickListener { client.sendKeyEvent(KeyCodes.HOME) }
        findViewById<Button>(R.id.btnBack).setOnClickListener { client.sendKeyEvent(KeyCodes.BACK) }
        findViewById<Button>(R.id.btnMenu).setOnClickListener { client.sendKeyEvent(KeyCodes.MENU) }

        findViewById<ImageButton>(R.id.btnUp).setOnClickListener { client.sendKeyEvent(KeyCodes.UP) }
        findViewById<ImageButton>(R.id.btnDown).setOnClickListener { client.sendKeyEvent(KeyCodes.DOWN) }
        findViewById<ImageButton>(R.id.btnLeft).setOnClickListener { client.sendKeyEvent(KeyCodes.LEFT) }
        findViewById<ImageButton>(R.id.btnRight).setOnClickListener { client.sendKeyEvent(KeyCodes.RIGHT) }
        findViewById<Button>(R.id.btnOk).setOnClickListener { client.sendKeyEvent(KeyCodes.CENTER) }

        findViewById<ImageButton>(R.id.btnVolUp).setOnClickListener { client.sendKeyEvent(KeyCodes.VOLUME_UP) }
        findViewById<ImageButton>(R.id.btnVolDown).setOnClickListener { client.sendKeyEvent(KeyCodes.VOLUME_DOWN) }
        findViewById<ImageButton>(R.id.btnMute).setOnClickListener { client.sendKeyEvent(KeyCodes.MUTE) }

        findViewById<ImageButton>(R.id.btnRewind).setOnClickListener { client.sendKeyEvent(KeyCodes.REWIND) }
        findViewById<ImageButton>(R.id.btnPlayPause).setOnClickListener { client.sendKeyEvent(KeyCodes.PLAY_PAUSE) }
        findViewById<ImageButton>(R.id.btnForward).setOnClickListener { client.sendKeyEvent(KeyCodes.FAST_FORWARD) }

        val touchpad = findViewById<TouchpadView>(R.id.touchpad)
        touchpad.onTap = { client.sendKeyEvent(KeyCodes.CENTER) }
        touchpad.onLongPress = { client.sendKeyEvent(KeyCodes.BACK) }
        touchpad.onSwipe = { direction ->
            when (direction) {
                "UP" -> client.sendKeyEvent(KeyCodes.UP)
                "DOWN" -> client.sendKeyEvent(KeyCodes.DOWN)
                "LEFT" -> client.sendKeyEvent(KeyCodes.LEFT)
                "RIGHT" -> client.sendKeyEvent(KeyCodes.RIGHT)
            }
        }

        findViewById<ImageButton>(R.id.btnMic).setOnClickListener { requestVoice() }
    }

    private fun doConnect() {
        val ip = etIp.text.toString().trim()
        if (ip.isEmpty()) {
            Toast.makeText(this, "TV ka IP address daalo", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.lastIp = ip
        tvStatus.text = getString(R.string.connecting)
        client.connect(ip) { success, error ->
            runOnUiThread {
                if (success) {
                    tvStatus.text = getString(R.string.connected)
                } else {
                    tvStatus.text = getString(R.string.not_connected)
                    Toast.makeText(this, "Connect fail: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateStatusText() {
        tvStatus.text = getString(if (client.isConnected) R.string.connected else R.string.not_connected)
    }

    private fun requestVoice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
            return
        }
        voice.startListening()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            voice.startListening()
        }
    }

    private fun handleVoiceCommand(command: String) {
        when {
            "home" in command -> client.sendKeyEvent(KeyCodes.HOME)
            "back" in command -> client.sendKeyEvent(KeyCodes.BACK)
            "menu" in command -> client.sendKeyEvent(KeyCodes.MENU)
            "power" in command || "turn off" in command || "band karo" in command -> client.sendKeyEvent(KeyCodes.POWER)
            "volume up" in command || "awaaz badhao" in command -> client.sendKeyEvent(KeyCodes.VOLUME_UP)
            "volume down" in command || "awaaz kam" in command -> client.sendKeyEvent(KeyCodes.VOLUME_DOWN)
            "mute" in command -> client.sendKeyEvent(KeyCodes.MUTE)
            "pause" in command -> client.sendKeyEvent(KeyCodes.PLAY_PAUSE)
            "play" in command -> client.sendKeyEvent(KeyCodes.PLAY_PAUSE)
            "up" in command || "upar" in command -> client.sendKeyEvent(KeyCodes.UP)
            "down" in command || "neeche" in command -> client.sendKeyEvent(KeyCodes.DOWN)
            "left" in command || "baye" in command -> client.sendKeyEvent(KeyCodes.LEFT)
            "right" in command || "daye" in command -> client.sendKeyEvent(KeyCodes.RIGHT)
            "select" in command || "ok" in command || "enter" in command -> client.sendKeyEvent(KeyCodes.CENTER)
            "netflix" in command -> client.launchApp("com.netflix.mediaclient")
            "youtube" in command -> client.launchApp("com.google.android.youtube.tv")
            "prime video" in command || "amazon prime" in command -> client.launchApp("com.amazon.avod.thirdpartyclient")
            "hotstar" in command -> client.launchApp("in.startv.hotstar")
            else -> client.sendText(command)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voice.destroy()
        client.disconnect()
    }
}
