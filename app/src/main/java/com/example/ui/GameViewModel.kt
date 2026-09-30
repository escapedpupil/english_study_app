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
    val totalQuestions: Int = 5,
    val targetWord: EnglishWord,
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
        val pool = if (levelWords.isNotEmpty()) levelWords else WordRepository.allWords.take(4)
        val target = pool.random()

        val state = GamePlayState(
            levelConfig = levelConfig,
            questionIndex = 0,
            totalQuestions = minOf(5, maxOf(3, pool.size + 1)),
            targetWord = target,
            options = generateOptions(target, pool),
            bubbles = generateBubbles(target, pool),
            flipCards = generateFlipCards(pool.take(4))
        )
        _gamePlayState.value = state

        // Play introduction audio
        viewModelScope.launch {
            delay(400)
            speakCurrentQuestion(target, levelConfig.defaultMode)
        }
    }

    private fun generateOptions(target: EnglishWord, pool: List<EnglishWord>): List<EnglishWord> {
        val otherWords = WordRepository.allWords.filter { it.id != target.id }.shuffled()
        val poolOthers = pool.filter { it.id != target.id }
        val candidates = (poolOthers + otherWords).distinctBy { it.id }.take(3)
        return (candidates + target).shuffled()
    }

    private fun generateBubbles(target: EnglishWord, pool: List<EnglishWord>): List<BubbleItem> {
        val options = generateOptions(target, pool)
        val positions = listOf(
            0.2f to 0.25f,
            0.7f to 0.28f,
            0.35f to 0.55f,
            0.75f to 0.62f
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
                voicePlayer.speakQuestionPrompt(currentTarget.english, "快戳破 ${currentTarget.chinese} 泡泡")
            }
            GameModeType.SHADOW_GUESS -> {
                voicePlayer.speakQuestionPrompt(currentTarget.english, "这是谁的影子呢？快找到 ${currentTarget.chinese}")
            }
            GameModeType.CARD_FLIP -> {
                voicePlayer.speakEnglishWord(currentTarget.english, "翻一翻，帮 ${currentTarget.chinese} 找到好朋友")
            }
            GameModeType.LISTEN_AND_PICK -> {
                voicePlayer.speakQuestionPrompt(currentTarget.english, currentTarget.chinese, currentTarget.soundHint)
            }
        }
    }

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

        if (nextIndex >= state.totalQuestions) {
            finishCurrentLevel()
        } else {
            val levelWords = state.levelConfig.wordIds.mapNotNull { WordRepository.getWordById(it) }
            val pool = if (levelWords.isNotEmpty()) levelWords else WordRepository.allWords.take(4)
            val otherTargets = pool.filter { it.id != state.targetWord.id }
            val nextTarget = if (otherTargets.isNotEmpty()) otherTargets.random() else pool.random()

            val nextState = state.copy(
                questionIndex = nextIndex,
                targetWord = nextTarget,
                options = generateOptions(nextTarget, pool),
                bubbles = generateBubbles(nextTarget, pool),
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
            mistakes == 0 -> 3
            mistakes <= 2 -> 2
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
