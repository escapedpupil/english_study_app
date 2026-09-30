package com.example.model

enum class WordCategory(val title: String, val emoji: String) {
    ANIMALS("动物萌宠", "🐾"),
    FRUITS("美味水果", "🍎"),
    VEHICLES("酷炫交通", "🚗"),
    COLORS("七彩世界", "🎨"),
    BODY("奇妙身体", "🖐️")
}

data class EnglishWord(
    val id: String,
    val english: String,
    val chinese: String,
    val category: WordCategory,
    val emoji: String,
    val soundHint: String, // Sound or onomatopoeia kid recognizes
    val accentColor: Long, // Color for cards and badges
    val funFact: String = ""
)

object WordRepository {
    val allWords = listOf(
        // Animals 1
        EnglishWord("cat", "Cat", "猫咪", WordCategory.ANIMALS, "🐱", "喵喵喵~ Meow!", 0xFFFFB74D),
        EnglishWord("dog", "Dog", "小狗", WordCategory.ANIMALS, "🐶", "汪汪汪~ Woof!", 0xFFFF8A65),
        EnglishWord("duck", "Duck", "小鸭", WordCategory.ANIMALS, "🦆", "嘎嘎嘎~ Quack!", 0xFFFFD54F),
        EnglishWord("rabbit", "Rabbit", "兔子", WordCategory.ANIMALS, "🐰", "蹦蹦跳~ Hop!", 0xFFF06292),

        // Animals 2
        EnglishWord("panda", "Panda", "熊猫", WordCategory.ANIMALS, "🐼", "大熊猫~ Chomp!", 0xFF81C784),
        EnglishWord("tiger", "Tiger", "老虎", WordCategory.ANIMALS, "🐯", "大老虎~ Roar!", 0xFFFF9800),
        EnglishWord("lion", "Lion", "狮子", WordCategory.ANIMALS, "🦁", "森林之王~ Roar!", 0xFFFFB300),
        EnglishWord("elephant", "Elephant", "大象", WordCategory.ANIMALS, "🐘", "长鼻子~ Pawoo!", 0xFF64B5F6),
        EnglishWord("pig", "Pig", "小猪", WordCategory.ANIMALS, "🐷", "哼哼哼~ Oink!", 0xFFFF80AB),
        EnglishWord("monkey", "Monkey", "猴子", WordCategory.ANIMALS, "🐵", "抓抓头~ Ooh aah!", 0xFFA1887F),

        // Fruits 1
        EnglishWord("apple", "Apple", "苹果", WordCategory.FRUITS, "🍎", "红彤彤的大苹果~ Crunch!", 0xFFE57373),
        EnglishWord("banana", "Banana", "香蕉", WordCategory.FRUITS, "🍌", "甜甜弯弯的香蕉~ Sweet!", 0xFFFFF176),
        EnglishWord("orange", "Orange", "橙子", WordCategory.FRUITS, "🍊", "酸酸甜甜的橙子~ Juicy!", 0xFFFFB74D),
        EnglishWord("strawberry", "Strawberry", "草莓", WordCategory.FRUITS, "🍓", "红红的小草莓~ Yum!", 0xFFFF4081),

        // Fruits 2
        EnglishWord("watermelon", "Watermelon", "西瓜", WordCategory.FRUITS, "🍉", "大西瓜~ Cool!", 0xFF4CAF50),
        EnglishWord("grape", "Grape", "葡萄", WordCategory.FRUITS, "🍇", "一串串紫葡萄~ Pop!", 0xFFBA68C8),
        EnglishWord("pear", "Pear", "雪梨", WordCategory.FRUITS, "🍐", "清甜的大雪梨~ Crisp!", 0xFFDCE775),
        EnglishWord("peach", "Peach", "桃子", WordCategory.FRUITS, "🍑", "粉嫩的蜜桃~ Soft!", 0xFFFF8A80),

        // Vehicles
        EnglishWord("car", "Car", "小汽车", WordCategory.VEHICLES, "🚗", "嘀嘀嘀~ Beep Beep!", 0xFF29B6F6),
        EnglishWord("bus", "Bus", "大巴车", WordCategory.VEHICLES, "🚌", "滴滴大公共汽车~ Honk!", 0xFFFFCA28),
        EnglishWord("train", "Train", "小火车", WordCategory.VEHICLES, "🚂", "呜呜呜~ 喀嚓喀嚓~ Choo Choo!", 0xFF8D6E63),
        EnglishWord("plane", "Airplane", "飞机", WordCategory.VEHICLES, "✈️", "呼呼呼~ 飞上蓝天~ Whoosh!", 0xFF4DD0E1),
        EnglishWord("boat", "Boat", "小轮船", WordCategory.VEHICLES, "⛵", "在海里游呀游~ Splash!", 0xFF26A69A),
        EnglishWord("rocket", "Rocket", "火箭", WordCategory.VEHICLES, "🚀", "嗖！飞向太空~ 3-2-1 Blast off!", 0xFFEF5350),

        // Colors
        EnglishWord("red", "Red", "红色", WordCategory.COLORS, "🔴", "热情美丽的红色~ Red!", 0xFFEF5350),
        EnglishWord("blue", "Blue", "蓝色", WordCategory.COLORS, "🔵", "大海天空的蓝色~ Blue!", 0xFF42A5F5),
        EnglishWord("yellow", "Yellow", "黄色", WordCategory.COLORS, "🟡", "明亮阳光的黄色~ Yellow!", 0xFFFFEE58),
        EnglishWord("green", "Green", "绿色", WordCategory.COLORS, "🟢", "大自然的小草绿~ Green!", 0xFF66BB6A),
        EnglishWord("pink", "Pink", "粉色", WordCategory.COLORS, "🌸", "浪漫可爱的粉色~ Pink!", 0xFFF48FB1),
        EnglishWord("purple", "Purple", "紫色", WordCategory.COLORS, "🟣", "梦幻神奇的紫色~ Purple!", 0xFFAB47BC),

        // Body
        EnglishWord("eye", "Eye", "眼睛", WordCategory.BODY, "👀", "亮晶晶的眼睛~ Blink!", 0xFF4FC3F7),
        EnglishWord("ear", "Ear", "耳朵", WordCategory.BODY, "👂", "听声音的小耳朵~ Listen!", 0xFFFFB74D),
        EnglishWord("nose", "Nose", "鼻子", WordCategory.BODY, "👃", "闻花香的小鼻子~ Sniff!", 0xFF81C784),
        EnglishWord("mouth", "Mouth", "嘴巴", WordCategory.BODY, "👄", "吃东西的小嘴巴~ Nom Nom!", 0xFFFF8A80),
        EnglishWord("hand", "Hand", "小手", WordCategory.BODY, "✋", "拍拍拍的小手~ Clap Clap!", 0xFFFFD54F)
    )

    fun getWordById(id: String): EnglishWord? = allWords.find { it.id == id }
}

enum class GameModeType {
    LISTEN_AND_PICK, // 听音选图
    POP_BUBBLE,      // 戳泡泡
    SHADOW_GUESS,    // 影子猜猜乐
    CARD_FLIP        // 翻翻乐对对碰
}

data class LevelConfig(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val themeColor: Long,
    val wordIds: List<String>,
    val defaultMode: GameModeType,
    val stickerName: String,
    val stickerEmoji: String,
    val requiredStarsToUnlock: Int
)

object LevelDefinitions {
    val levels = listOf(
        LevelConfig(
            levelNumber = 1,
            title = "萌宠乐园",
            subtitle = "Cute Pets",
            iconEmoji = "🐱",
            themeColor = 0xFFFF7043,
            wordIds = listOf("cat", "dog", "duck", "rabbit"),
            defaultMode = GameModeType.LISTEN_AND_PICK,
            stickerName = "金牌小猫勋章",
            stickerEmoji = "🎖️🐱",
            requiredStarsToUnlock = 0
        ),
        LevelConfig(
            levelNumber = 2,
            title = "泡泡水果屋",
            subtitle = "Fruit Bubble",
            iconEmoji = "🍎",
            themeColor = 0xFFEF5350,
            wordIds = listOf("apple", "banana", "orange", "strawberry"),
            defaultMode = GameModeType.POP_BUBBLE,
            stickerName = "甜甜苹果之星",
            stickerEmoji = "⭐🍎",
            requiredStarsToUnlock = 1
        ),
        LevelConfig(
            levelNumber = 3,
            title = "森林影子谜",
            subtitle = "Forest Shadow",
            iconEmoji = "🐼",
            themeColor = 0xFF26A69A,
            wordIds = listOf("panda", "tiger", "lion", "elephant", "monkey"),
            defaultMode = GameModeType.SHADOW_GUESS,
            stickerName = "丛林探险王",
            stickerEmoji = "👑🐼",
            requiredStarsToUnlock = 3
        ),
        LevelConfig(
            levelNumber = 4,
            title = "炫酷汽车城",
            subtitle = "Vehicle Town",
            iconEmoji = "🚗",
            themeColor = 0xFF29B6F6,
            wordIds = listOf("car", "bus", "train", "plane", "rocket"),
            defaultMode = GameModeType.LISTEN_AND_PICK,
            stickerName = "极速赛车手",
            stickerEmoji = "🏎️💨",
            requiredStarsToUnlock = 6
        ),
        LevelConfig(
            levelNumber = 5,
            title = "七彩泡泡海",
            subtitle = "Color Pop",
            iconEmoji = "🎨",
            themeColor = 0xFFAB47BC,
            wordIds = listOf("red", "blue", "yellow", "green", "pink", "purple"),
            defaultMode = GameModeType.POP_BUBBLE,
            stickerName = "彩虹魔术师",
            stickerEmoji = "🌈✨",
            requiredStarsToUnlock = 9
        ),
        LevelConfig(
            levelNumber = 6,
            title = "果园翻翻乐",
            subtitle = "Fruit Flip Match",
            iconEmoji = "🍉",
            themeColor = 0xFF66BB6A,
            wordIds = listOf("watermelon", "grape", "pear", "peach"),
            defaultMode = GameModeType.CARD_FLIP,
            stickerName = "超级记忆大师",
            stickerEmoji = "🧠🍉",
            requiredStarsToUnlock = 12
        ),
        LevelConfig(
            levelNumber = 7,
            title = "身体大发现",
            subtitle = "My Body",
            iconEmoji = "🖐️",
            themeColor = 0xFFFFA726,
            wordIds = listOf("eye", "ear", "nose", "mouth", "hand"),
            defaultMode = GameModeType.SHADOW_GUESS,
            stickerName = "小小观察家",
            stickerEmoji = "🔍👀",
            requiredStarsToUnlock = 15
        ),
        LevelConfig(
            levelNumber = 8,
            title = "终极星星宝藏",
            subtitle = "Star Champion",
            iconEmoji = "🏆",
            themeColor = 0xFFFFD700,
            wordIds = listOf("cat", "apple", "rocket", "panda", "plane", "strawberry"),
            defaultMode = GameModeType.LISTEN_AND_PICK,
            stickerName = "英语冒险大王",
            stickerEmoji = "🏆🌟",
            requiredStarsToUnlock = 18
        )
    )
}
