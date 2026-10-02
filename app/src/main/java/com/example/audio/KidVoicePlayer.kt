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
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

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
    private var currentSpeechJob: Job? = null

    private val utteranceCallbacks = ConcurrentHashMap<String, () -> Unit>()
    private val utteranceCounter = AtomicLong(0)

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
            selectOptimalVoice()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (utteranceId != null) {
                        utteranceCallbacks.remove(utteranceId)?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (utteranceId != null) {
                        utteranceCallbacks.remove(utteranceId)?.invoke()
                    }
                }
            })
        }
    }

    private fun selectOptimalVoice() {
        try {
            val voices = tts?.voices ?: return
            val usVoices = voices.filter { it.locale.language == "en" && it.locale.country == "US" }
            if (usVoices.isEmpty()) return

            val preferredVoice = usVoices.firstOrNull { voice ->
                !voice.isNetworkConnectionRequired &&
                (voice.name.contains("natural", ignoreCase = true) ||
                 voice.name.contains("female", ignoreCase = true) ||
                 voice.name.contains("sfg", ignoreCase = true) ||
                 voice.quality >= Voice.QUALITY_HIGH)
            } ?: usVoices.firstOrNull { !it.isNetworkConnectionRequired }
              ?: usVoices.firstOrNull()

            if (preferredVoice != null) {
                tts?.voice = preferredVoice
            }
        } catch (e: Exception) {
            Log.w("KidVoicePlayer", "Voice setup fallback", e)
        }
    }

    /**
     * Speaks a single utterance and awaits its real completion from the TTS engine.
     */
    private suspend fun speakTextAndWait(
        text: String,
        locale: Locale,
        speechRate: Float = 0.75f,
        pitch: Float = 1.02f
    ): Boolean {
        if (!isTtsReady || tts == null) return false

        return suspendCancellableCoroutine { continuation ->
            val id = "utt_${utteranceCounter.incrementAndGet()}"
            utteranceCallbacks[id] = {
                if (continuation.isActive) {
                    continuation.resume(true)
                }
            }

            continuation.invokeOnCancellation {
                utteranceCallbacks.remove(id)
            }

            try {
                tts?.language = locale
                if (locale.language == "en") {
                    selectOptimalVoice()
                }
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(pitch)
                val res = tts?.speak(text, TextToSpeech.QUEUE_ADD, null, id)
                if (res != TextToSpeech.SUCCESS) {
                    utteranceCallbacks.remove(id)
                    if (continuation.isActive) continuation.resume(false)
                }
            } catch (e: Exception) {
                utteranceCallbacks.remove(id)
                if (continuation.isActive) continuation.resume(false)
            }
        }
    }

    /**
     * Appends a silent buffer to keep the audio channel open and prevent abrupt cutoff of consonant tails.
     */
    private suspend fun playSilentBuffer(durationMs: Long = 400L) {
        if (!isTtsReady || tts == null) {
            delay(durationMs)
            return
        }

        suspendCancellableCoroutine { continuation ->
            val id = "silent_${utteranceCounter.incrementAndGet()}"
            utteranceCallbacks[id] = {
                if (continuation.isActive) continuation.resume(true)
            }
            continuation.invokeOnCancellation {
                utteranceCallbacks.remove(id)
            }
            try {
                val res = tts?.playSilentUtterance(durationMs, TextToSpeech.QUEUE_ADD, id)
                if (res != TextToSpeech.SUCCESS) {
                    utteranceCallbacks.remove(id)
                    if (continuation.isActive) continuation.resume(false)
                }
            } catch (e: Exception) {
                utteranceCallbacks.remove(id)
                if (continuation.isActive) continuation.resume(false)
            }
        }
    }

    /**
     * Speaks English with natural, human-like cadence and appends tail silence to ensure full pronunciation.
     */
    private suspend fun speakEnglishPromptAndWait(englishText: String) {
        speakTextAndWait(englishText, Locale.US, speechRate = 0.74f, pitch = 1.03f)
        playSilentBuffer(400L)
    }

    /**
     * Speak an English word clearly with friendly tone, clean repetition, and optional Chinese meaning.
     */
    fun speakEnglishWord(english: String, chinese: String = "") {
        currentSpeechJob?.cancel()
        currentSpeechJob = scope.launch {
            try {
                tts?.stop()
                _isSpeaking.value = true

                // Conversational repetition: e.g. "Apple... Apple!"
                speakEnglishPromptAndWait("$english... $english!")
                delay(300)

                if (chinese.isNotEmpty() && hasChineseSupport()) {
                    speakTextAndWait(chinese, Locale.CHINESE, speechRate = 0.95f, pitch = 1.05f)
                    playSilentBuffer(250L)
                }
            } catch (_: CancellationException) {
            } finally {
                _isSpeaking.value = false
            }
        }
    }

    /**
     * Spoken prompt for a question with specific Chinese instruction and vivid human-like English prompt.
     * Guarantees NO accidental "找一找" prefix on non-matching levels!
     */
    fun speakQuestionPrompt(
        chineseInstruction: String,
        englishPrompt: String
    ) {
        currentSpeechJob?.cancel()
        currentSpeechJob = scope.launch {
            try {
                tts?.stop()
                _isSpeaking.value = true

                // 1. Chinese instruction
                if (hasChineseSupport() && chineseInstruction.isNotEmpty()) {
                    speakTextAndWait(chineseInstruction, Locale.CHINESE, speechRate = 0.95f, pitch = 1.05f)
                    delay(350)
                }

                // 2. Human-like English prompt with warm cadence and complete pronunciation
                speakEnglishPromptAndWait(englishPrompt)
                delay(250)
            } catch (_: CancellationException) {
            } finally {
                _isSpeaking.value = false
            }
        }
    }

    /**
     * When user selects the wrong option:
     * 1. Play error sound effect (嘟嘟提示音) + gentle vibration.
     * 2. Wait until tone finishes, then speak gentle reminder ("再试一次哦~").
     * 3. Speak the touched option's English and Chinese clearly ("This is: Dog... Dog! 小狗").
     */
    fun handleWrongChoice(selectedEnglish: String, selectedChinese: String) {
        currentSpeechJob?.cancel()
        currentSpeechJob = scope.launch {
            try {
                tts?.stop()
                _isSpeaking.value = true

                // 1. Play wrong hint tone and vibration first
                playTryAgainTone()
                vibrateGentle()
                delay(400)

                // 2. Play gentle reminder speech
                if (hasChineseSupport()) {
                    val prompt = listOf("再试一次哦~", "不对哦，再看看呢？", "别灰心，再找找看~").random()
                    speakTextAndWait(prompt, Locale.CHINESE, speechRate = 0.95f, pitch = 1.05f)
                    delay(300)
                }

                // 3. Pronounce the selected wrong item so toddler learns what they tapped
                if (hasChineseSupport()) {
                    speakTextAndWait("这个是", Locale.CHINESE, speechRate = 1.0f, pitch = 1.05f)
                    delay(200)
                }

                // Speak English clearly with full syllable pronunciation
                speakEnglishPromptAndWait("$selectedEnglish... $selectedEnglish!")

                if (selectedChinese.isNotEmpty() && hasChineseSupport()) {
                    delay(300)
                    speakTextAndWait(selectedChinese, Locale.CHINESE, speechRate = 0.95f, pitch = 1.05f)
                    playSilentBuffer(250L)
                }
            } catch (_: CancellationException) {
            } finally {
                _isSpeaking.value = false
            }
        }
    }

    /**
     * Joyful celebration when answering correctly.
     * Awaits completion before invoking onComplete callback to avoid cutoffs when transitioning questions.
     */
    fun handleCorrectChoice(
        targetEnglish: String,
        targetChinese: String,
        onComplete: () -> Unit
    ) {
        currentSpeechJob?.cancel()
        currentSpeechJob = scope.launch {
            try {
                tts?.stop()
                _isSpeaking.value = true

                playSuccessTone()
                vibrateSuccess()
                delay(350)

                val cheer = listOf("太棒啦！答对了！", "哇！真厉害！", "太聪明了！").random()
                if (hasChineseSupport()) {
                    speakTextAndWait(cheer, Locale.CHINESE, speechRate = 1.0f, pitch = 1.1f)
                    delay(300)
                }

                // Reinforce English target word with full pronunciation and room to echo
                speakEnglishPromptAndWait("Great job! ... $targetEnglish!")
                delay(400)

                onComplete()
            } catch (_: CancellationException) {
            } finally {
                _isSpeaking.value = false
            }
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
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
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
                vibrator?.vibrate(VibrationEffect.createOneShot(50, 90))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    private fun hasChineseSupport(): Boolean {
        return tts?.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE ||
                tts?.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_AVAILABLE
    }

    fun stop() {
        currentSpeechJob?.cancel()
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    fun release() {
        currentSpeechJob?.cancel()
        try {
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}
