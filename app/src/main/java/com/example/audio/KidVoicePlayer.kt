package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class KidVoicePlayer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e("KidVoicePlayer", "Init error", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setSpeechRate(0.85f) // Slower, clearer speech for kids
            tts?.setPitch(1.15f)     // Cheerful, friendly pitch
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    /**
     * Speaks an English word clearly with its sound hint, and optional Chinese guide.
     */
    fun speakEnglishWord(english: String, chinese: String = "", soundHint: String = "") {
        if (!isTtsReady || tts == null) return

        tts?.stop()
        tts?.language = Locale.US
        tts?.setSpeechRate(0.82f)
        tts?.setPitch(1.1f)

        // Read English word first with clean pronunciation
        val utteranceId = "word_${System.currentTimeMillis()}"
        tts?.speak(english, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        // Then speak Chinese translation so toddler connects the sound
        if (chinese.isNotEmpty()) {
            scope.launch {
                delay(800)
                if (tts?.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE ||
                    tts?.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_AVAILABLE
                ) {
                    tts?.language = Locale.CHINESE
                    tts?.setSpeechRate(0.95f)
                    tts?.speak(chinese, TextToSpeech.QUEUE_ADD, null, "zh_$utteranceId")
                }
            }
        }
    }

    /**
     * Spoken prompt for a question:
     * e.g. "找一找: Apple! 苹果在哪里呢？"
     */
    fun speakQuestionPrompt(english: String, chinese: String, soundHint: String = "") {
        if (!isTtsReady || tts == null) return

        tts?.stop()
        
        // Friendly Chinese invitation
        if (tts?.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE ||
            tts?.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_AVAILABLE
        ) {
            tts?.language = Locale.CHINESE
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.15f)
            val promptText = "找一找，$chinese。"
            tts?.speak(promptText, TextToSpeech.QUEUE_FLUSH, null, "prompt_zh")

            // Then clearly pronounce the English target word
            scope.launch {
                delay(1000)
                tts?.language = Locale.US
                tts?.setSpeechRate(0.82f)
                tts?.setPitch(1.1f)
                tts?.speak(english, TextToSpeech.QUEUE_ADD, null, "prompt_en")
            }
        } else {
            // Fallback English
            tts?.language = Locale.US
            tts?.setSpeechRate(0.85f)
            tts?.speak("Find $english!", TextToSpeech.QUEUE_FLUSH, null, "prompt_en_only")
        }
    }

    /**
     * Joyful cheering when child answers correctly!
     */
    fun speakSuccessCheer(english: String, chinese: String) {
        playSuccessTone()
        vibrateSuccess()

        val cheers = listOf("太棒啦！答对了！", "哇！真厉害！", "太聪明了！", "真棒！好厉害！")
        val cheer = cheers.random()

        if (!isTtsReady || tts == null) return
        tts?.stop()

        if (tts?.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE ||
            tts?.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_AVAILABLE
        ) {
            tts?.language = Locale.CHINESE
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.2f)
            tts?.speak(cheer, TextToSpeech.QUEUE_FLUSH, null, "cheer_zh")

            // Re-affirm the English word!
            scope.launch {
                delay(900)
                tts?.language = Locale.US
                tts?.setSpeechRate(0.85f)
                tts?.speak(english, TextToSpeech.QUEUE_ADD, null, "cheer_en")
            }
        } else {
            tts?.language = Locale.US
            tts?.speak("Awesome! $english!", TextToSpeech.QUEUE_FLUSH, null, "cheer_en")
        }
    }

    /**
     * Encouragement on wrong answer - gentle, loving, never harsh
     */
    fun speakEncourage() {
        playTryAgainTone()
        vibrateGentle()

        val encouragements = listOf("再试一次哦~", "加油，看看是哪个？", "别灰心，再找找看~")
        val enc = encouragements.random()

        if (!isTtsReady || tts == null) return
        tts?.stop()

        if (tts?.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE ||
            tts?.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_AVAILABLE
        ) {
            tts?.language = Locale.CHINESE
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.1f)
            tts?.speak(enc, TextToSpeech.QUEUE_FLUSH, null, "enc_zh")
        } else {
            tts?.language = Locale.US
            tts?.speak("Try again!", TextToSpeech.QUEUE_FLUSH, null, "enc_en")
        }
    }

    fun playClickTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        } catch (_: Exception) {}
    }

    fun playSuccessTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (_: Exception) {}
    }

    fun playBubblePopTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 90)
        } catch (_: Exception) {}
    }

    fun playTryAgainTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
        } catch (_: Exception) {}
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateGentle() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(40, 80))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}
