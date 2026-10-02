package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.KidVoicePlayer
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.LevelRecord
import com.example.data.StickerRecord
import com.example.model.EnglishWord
import com.example.model.GameModeType
import com.example.model.LevelConfig
import com.example.model.LevelDefinitions
import com.example.model.WordCategory
import com.example.model.WordRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    MAP,
    GAMEPLAY,
    STICKER_ALBUM
}

data class BubbleItem(
    val id: String,
    val word: EnglishWord,
    val initialXRatio: Float,
    val initialYRatio: Float,
    val isPopped: Boolean = false,
    val isTarget: Boolean = false
)

data class FlipCardItem(
    val cardId: Int,
    val word: EnglishWord,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

data class GamePlayState(
    val levelConfig: LevelConfig,
    val questionIndex: Int = 0,
    val totalQuestions: Int = 8,
    val targetWord: EnglishWord,
    val questionWordIds: List<String> = emptyList(),
    val options: List<EnglishWord> = emptyList(),
    val bubbles: List<BubbleItem> = emptyList(),
    val flipCards: List<FlipCardItem> = emptyList(),
    val firstFlippedCardId: Int? = null,
    val isAnsweringCorrect: Boolean = false,
    val wrongAttemptsInQuestion: Int = 0,
    val totalMistakesInLevel: Int = 0,
    val isLevelFinished: Boolean = false,
    val earnedStars: Int = 0,
    val newlyUnlockedSticker: StickerRecord? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val voicePlayer = KidVoicePlayer(application.applicationContext)

    val levelRecords: StateFlow<List<LevelRecord>>
    val stickerRecords: StateFlow<List<StickerRecord>>

    private val _currentScreen = MutableStateFlow(ScreenDestination.MAP)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _gamePlayState = MutableStateFlow<GamePlayState?>(null)
    val gamePlayState: StateFlow<GamePlayState?> = _gamePlayState.asStateFlow()

    init {
        val db = GameDatabase.getInstance(application)
        repository = GameRepository(db.gameDao())

        levelRecords = repository.allLevels.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        stickerRecords = repository.allStickers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultLevelsIfNeeded()
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        voicePlayer.playClickTone()
        _currentScreen.value = screen
    }

    fun startLevel(levelConfig: LevelConfig) {
        voicePlayer.playClickTone()
        setupNewLevel(levelConfig)
        _currentScreen.value = ScreenDestination.GAMEPLAY
    }

    private fun setupNewLevel(levelConfig: LevelConfig) {
        val levelWords = levelConfig.wordIds.mapNotNull { WordRepository.getWordById(it) }
        val categoryWords = WordRepository.allWords.filter { it.category == (levelWords.firstOrNull()?.category ?: WordCategory.ANIMALS) }
        val combinedPool = (levelWords + categoryWords + WordRepository.allWords).distinctBy { it.id }

        // Pick 8 targets without duplicates for rich randomness
        val questionTargetWords = (levelWords.shuffled() + combinedPool.shuffled()).distinctBy { it.id }.take(8)
        val firstTarget = questionTargetWords.first()
        val optionCount = if (levelConfig.defaultMode == GameModeType.SHADOW_GUESS) 3 else 4

        val state = GamePlayState(
            levelConfig = levelConfig,
            questionIndex = 0,
            totalQuestions = questionTargetWords.size,
            targetWord = firstTarget,
            questionWordIds = questionTargetWords.map { it.id },
            options = generateOptions(firstTarget, combinedPool, optionCount),
            bubbles = generateBubbles(firstTarget, combinedPool),
            flipCards = generateFlipCards(questionTargetWords.take(4))
        )
        _gamePlayState.value = state

        // Play introduction audio
        viewModelScope.launch {
            delay(400)
            speakCurrentQuestion(firstTarget, levelConfig.defaultMode)
        }
    }

    private fun generateOptions(target: EnglishWord, pool: List<EnglishWord>, count: Int = 4): List<EnglishWord> {
        val poolOthers = pool.filter { it.id != target.id }.shuffled()
        val categoryOthers = WordRepository.allWords.filter { it.id != target.id && it.category == target.category }.shuffled()
        val allOthers = WordRepository.allWords.filter { it.id != target.id }.shuffled()

        val candidates = (poolOthers + categoryOthers + allOthers).distinctBy { it.id }.take(count - 1)
        val finalOptions = (candidates + target).shuffled()

        // Strict guarantee: target is always present in final list
        return if (finalOptions.any { it.id == target.id }) {
            finalOptions
        } else {
            (finalOptions.take(count - 1) + target).shuffled()
        }
    }

    private fun generateBubbles(target: EnglishWord, pool: List<EnglishWord>): List<BubbleItem> {
        val options = generateOptions(target, pool, count = 4)
        val positions = listOf(
            0.18f to 0.20f,
            0.66f to 0.22f,
            0.22f to 0.56f,
            0.64f to 0.58f
        ).shuffled()

        return options.mapIndexed { index, word ->
            val pos = positions.getOrElse(index) { 0.5f to 0.5f }
            BubbleItem(
                id = "bubble_${word.id}_$index",
                word = word,
                initialXRatio = pos.first,
                initialYRatio = pos.second,
                isPopped = false,
                isTarget = (word.id == target.id)
            )
        }
    }

    private fun generateFlipCards(words: List<EnglishWord>): List<FlipCardItem> {
        val chosenWords = words.take(3) // 3 pairs = 6 cards
        val paired = (chosenWords + chosenWords).shuffled()
        return paired.mapIndexed { index, word ->
            FlipCardItem(cardId = index, word = word, isFaceUp = false, isMatched = false)
        }
    }

    fun speakCurrentQuestion(target: EnglishWord? = null, mode: GameModeType? = null) {
        val state = _gamePlayState.value ?: return
        val currentTarget = target ?: state.targetWord
        val currentMode = mode ?: state.levelConfig.defaultMode

        when (currentMode) {
            GameModeType.POP_BUBBLE -> {
                // Bubble pop mode: strictly no "找一找"
                voicePlayer.speakQuestionPrompt(
                    chineseInstruction = "快戳破 ${currentTarget.chinese} 泡泡！",
                    englishPrompt = "Pop the ${currentTarget.english}! ... ${currentTarget.english}!"
                )
            }
            GameModeType.SHADOW_GUESS -> {
                // Shadow guess mode: strictly no "找一找"
                voicePlayer.speakQuestionPrompt(
                    chineseInstruction = "这是谁的影子呢？快认出 ${currentTarget.chinese}！",
                    englishPrompt = "Look at the shadow! ... It's ${currentTarget.english}! ... ${currentTarget.english}!"
                )
            }
            GameModeType.CARD_FLIP -> {
                // Card flip mode
                voicePlayer.speakQuestionPrompt(
                    chineseInstruction = "翻一翻卡片，找出 ${currentTarget.chinese} 的好朋友！",
                    englishPrompt = "Find the ${currentTarget.english}! ... ${currentTarget.english}!"
                )
            }
            GameModeType.LISTEN_AND_PICK -> {
                // Listen & Pick mode
                voicePlayer.speakQuestionPrompt(
                    chineseInstruction = "找一找，${currentTarget.chinese} 在哪里呢？",
                    englishPrompt = "Find the ${currentTarget.english}! ... ${currentTarget.english}!"
                )
            }
        }
    }

    /**
     * Speaks ONLY the English word and Chinese name (e.g. "Cat. 猫咪。"),
     * without re-reading the question instruction.
     */
    fun repeatTargetWord() {
        val state = _gamePlayState.value ?: return
        voicePlayer.speakEnglishWord(state.targetWord.english, state.targetWord.chinese)
    }

    /**
     * Kid taps an answer option (Listen & Pick or Shadow Guess)
     */
    fun selectOption(selectedWord: EnglishWord) {
        val state = _gamePlayState.value ?: return
        if (state.isAnsweringCorrect || state.isLevelFinished) return

        if (selectedWord.id == state.targetWord.id) {
            // Correct!
            _gamePlayState.value = state.copy(isAnsweringCorrect = true)
            voicePlayer.handleCorrectChoice(state.targetWord.english, state.targetWord.chinese) {
                advanceToNextQuestionOrFinish()
            }
        } else {
            // Wrong option: sequence: error tone -> gentle hint -> pronounce tapped option cleanly
            _gamePlayState.value = state.copy(
                wrongAttemptsInQuestion = state.wrongAttemptsInQuestion + 1,
                totalMistakesInLevel = state.totalMistakesInLevel + 1
            )
            voicePlayer.handleWrongChoice(selectedWord.english, selectedWord.chinese)
        }
    }

    /**
     * Kid taps a bubble in Bubble Pop mode
     */
    fun popBubble(bubble: BubbleItem) {
        val state = _gamePlayState.value ?: return
        if (bubble.isPopped || state.isAnsweringCorrect || state.isLevelFinished) return

        voicePlayer.playBubblePopTone()

        if (bubble.isTarget) {
            // Correct bubble!
            val updatedBubbles = state.bubbles.map {
                if (it.id == bubble.id) it.copy(isPopped = true) else it
            }
            _gamePlayState.value = state.copy(
                bubbles = updatedBubbles,
                isAnsweringCorrect = true
            )
            voicePlayer.handleCorrectChoice(state.targetWord.english, state.targetWord.chinese) {
                advanceToNextQuestionOrFinish()
            }
        } else {
            // Non-target bubble popped
            val updatedBubbles = state.bubbles.map {
                if (it.id == bubble.id) it.copy(isPopped = true) else it
            }
            _gamePlayState.value = state.copy(
                bubbles = updatedBubbles,
                wrongAttemptsInQuestion = state.wrongAttemptsInQuestion + 1,
                totalMistakesInLevel = state.totalMistakesInLevel + 1
            )
            voicePlayer.handleWrongChoice(bubble.word.english, bubble.word.chinese)
        }
    }

    /**
     * Card Flip mode tap
     */
    fun flipCard(cardId: Int) {
        val state = _gamePlayState.value ?: return
        val card = state.flipCards.find { it.cardId == cardId } ?: return
        if (card.isFaceUp || card.isMatched) return

        voicePlayer.playClickTone()
        voicePlayer.speakEnglishWord(card.word.english)

        val firstId = state.firstFlippedCardId
        if (firstId == null) {
            // First card flipped
            val updated = state.flipCards.map {
                if (it.cardId == cardId) it.copy(isFaceUp = true) else it
            }
            _gamePlayState.value = state.copy(flipCards = updated, firstFlippedCardId = cardId)
        } else {
            // Second card flipped
            val firstCard = state.flipCards.find { it.cardId == firstId } ?: return
            val updated = state.flipCards.map {
                if (it.cardId == cardId) it.copy(isFaceUp = true) else it
            }
            _gamePlayState.value = state.copy(flipCards = updated, firstFlippedCardId = null)

            viewModelScope.launch {
                delay(600)
                if (firstCard.word.id == card.word.id) {
                    // Match!
                    val matchedList = updated.map {
                        if (it.cardId == firstId || it.cardId == cardId) it.copy(isMatched = true) else it
                    }
                    val allMatched = matchedList.all { it.isMatched }
                    _gamePlayState.value = _gamePlayState.value?.copy(flipCards = matchedList)

                    voicePlayer.handleCorrectChoice(card.word.english, card.word.chinese) {
                        if (allMatched) {
                            finishCurrentLevel()
                        }
                    }
                } else {
                    // Mismatch, turn back
                    val turnedBack = updated.map {
                        if ((it.cardId == firstId || it.cardId == cardId) && !it.isMatched) {
                            it.copy(isFaceUp = false)
                        } else it
                    }
                    _gamePlayState.value = _gamePlayState.value?.copy(
                        flipCards = turnedBack,
                        totalMistakesInLevel = (_gamePlayState.value?.totalMistakesInLevel ?: 0) + 1
                    )
                    voicePlayer.handleWrongChoice(card.word.english, card.word.chinese)
                }
            }
        }
    }

    private fun advanceToNextQuestionOrFinish() {
        val state = _gamePlayState.value ?: return
        val nextIndex = state.questionIndex + 1

        if (nextIndex >= state.totalQuestions || nextIndex >= state.questionWordIds.size) {
            finishCurrentLevel()
        } else {
            val nextTargetId = state.questionWordIds[nextIndex]
            val nextTarget = WordRepository.getWordById(nextTargetId) ?: WordRepository.allWords.random()

            val levelWords = state.levelConfig.wordIds.mapNotNull { WordRepository.getWordById(it) }
            val categoryWords = WordRepository.allWords.filter { it.category == nextTarget.category }
            val combinedPool = (levelWords + categoryWords + WordRepository.allWords).distinctBy { it.id }
            val optionCount = if (state.levelConfig.defaultMode == GameModeType.SHADOW_GUESS) 3 else 4

            val nextState = state.copy(
                questionIndex = nextIndex,
                targetWord = nextTarget,
                options = generateOptions(nextTarget, combinedPool, optionCount),
                bubbles = generateBubbles(nextTarget, combinedPool),
                isAnsweringCorrect = false,
                wrongAttemptsInQuestion = 0
            )
            _gamePlayState.value = nextState

            viewModelScope.launch {
                delay(300)
                speakCurrentQuestion(nextTarget, state.levelConfig.defaultMode)
            }
        }
    }

    private fun finishCurrentLevel() {
        val state = _gamePlayState.value ?: return
        val mistakes = state.totalMistakesInLevel
        val stars = when {
            mistakes <= 1 -> 3
            mistakes <= 3 -> 2
            else -> 1
        }
        val score = (stars * 100) - (mistakes * 10)

        val stickerRecord = StickerRecord(
            stickerId = "sticker_${state.levelConfig.levelNumber}",
            title = state.levelConfig.stickerName,
            emoji = state.levelConfig.stickerEmoji,
            unlockedAt = System.currentTimeMillis()
        )

        _gamePlayState.value = state.copy(
            isLevelFinished = true,
            earnedStars = stars,
            newlyUnlockedSticker = stickerRecord
        )

        viewModelScope.launch {
            repository.completeLevel(state.levelConfig.levelNumber, stars, score)
            delay(400)
            voicePlayer.speakEnglishWord("Victory! Level Cleared!", "闯关成功！获得 ${stars} 颗金星和新贴纸！")
        }
    }

    fun playNextLevel() {
        val state = _gamePlayState.value ?: return
        val nextLevelNumber = state.levelConfig.levelNumber + 1
        val nextConfig = LevelDefinitions.levels.find { it.levelNumber == nextLevelNumber }
        if (nextConfig != null) {
            setupNewLevel(nextConfig)
        } else {
            _currentScreen.value = ScreenDestination.MAP
        }
    }

    fun restartCurrentLevel() {
        val state = _gamePlayState.value ?: return
        setupNewLevel(state.levelConfig)
    }

    override fun onCleared() {
        super.onCleared()
        voicePlayer.release()
    }
}
