package com.remote.tvremote

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.appcompat.app.AppCompatActivity

/**
 * Wraps Android's built-in SpeechRecognizer. Call [startListening]; the
 * recognized (lowercased) text is delivered through [onCommand]. Also reports
 * listening state changes through [onStateChange] so the UI can show
 * "Listening…".
 */
class VoiceCommandManager(
    private val activity: AppCompatActivity,
    private val onCommand: (String) -> Unit,
    private val onStateChange: ((listening: Boolean) -> Unit)? = null
) {
    private var recognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            onStateChange?.invoke(false)
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onStateChange?.invoke(true)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                onStateChange?.invoke(false)
            }

            override fun onError(error: Int) {
                onStateChange?.invoke(false)
                recognizer?.destroy()
                recognizer = null
            }

            override fun onResults(results: Bundle?) {
                onStateChange?.invoke(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase()?.trim()
                if (!text.isNullOrEmpty()) onCommand(text)
                recognizer?.destroy()
                recognizer = null
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer?.startListening(intent)
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
