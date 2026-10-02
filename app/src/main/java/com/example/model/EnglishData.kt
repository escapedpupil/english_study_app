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
        // Animals (20 words)
        EnglishWord("cat", "Cat", "猫咪", WordCategory.ANIMALS, "🐱", "喵喵喵~ Meow!", 0xFFFFB74D),
        EnglishWord("dog", "Dog", "小狗", WordCategory.ANIMALS, "🐶", "汪汪汪~ Woof!", 0xFFFF8A65),
        EnglishWord("duck", "Duck", "小鸭", WordCategory.ANIMALS, "🦆", "嘎嘎嘎~ Quack!", 0xFFFFD54F),
        EnglishWord("rabbit", "Rabbit", "兔子", WordCategory.ANIMALS, "🐰", "蹦蹦跳~ Hop!", 0xFFF06292),
        EnglishWord("bear", "Bear", "小熊", WordCategory.ANIMALS, "🐻", "大黑熊~ Growl!", 0xFF8D6E63),
        EnglishWord("sheep", "Sheep", "绵羊", WordCategory.ANIMALS, "🐑", "咩咩咩~ Baa!", 0xFFE0E0E0),
        EnglishWord("horse", "Horse", "小马", WordCategory.ANIMALS, "🐴", "得儿驾~ Neigh!", 0xFFA1887F),
        EnglishWord("cow", "Cow", "奶牛", WordCategory.ANIMALS, "🐮", "哞哞哞~ Moo!", 0xFF90A4AE),
        EnglishWord("pig", "Pig", "小猪", WordCategory.ANIMALS, "🐷", "哼哼哼~ Oink!", 0xFFFF80AB),
        EnglishWord("bird", "Bird", "小鸟", WordCategory.ANIMALS, "🐦", "叽叽喳喳~ Chirp!", 0xFF4FC3F7),
        EnglishWord("frog", "Frog", "青蛙", WordCategory.ANIMALS, "🐸", "呱呱呱~ Ribbit!", 0xFF81C784),
        EnglishWord("fish", "Fish", "小鱼", WordCategory.ANIMALS, "🐟", "游啊游~ Swish!", 0xFF29B6F6),
        EnglishWord("panda", "Panda", "熊猫", WordCategory.ANIMALS, "🐼", "大熊猫~ Chomp!", 0xFF81C784),
        EnglishWord("tiger", "Tiger", "老虎", WordCategory.ANIMALS, "🐯", "大老虎~ Roar!", 0xFFFF9800),
        EnglishWord("lion", "Lion", "狮子", WordCategory.ANIMALS, "🦁", "百兽之王~ Roar!", 0xFFFFB300),
        EnglishWord("elephant", "Elephant", "大象", WordCategory.ANIMALS, "🐘", "长鼻子~ Pawoo!", 0xFF64B5F6),
        EnglishWord("monkey", "Monkey", "猴子", WordCategory.ANIMALS, "🐵", "抓抓头~ Ooh aah!", 0xFFA1887F),
        EnglishWord("penguin", "Penguin", "企鹅", WordCategory.ANIMALS, "🐧", "摇摇摆摆~ Waddle!", 0xFF455A64),
        EnglishWord("giraffe", "Giraffe", "长颈鹿", WordCategory.ANIMALS, "🦒", "长脖子~ Munch!", 0xFFFFB300),
        EnglishWord("bee", "Bee", "小蜜蜂", WordCategory.ANIMALS, "🐝", "嗡嗡嗡~ Buzz!", 0xFFFFD54F),

        // Fruits & Food (18 words)
        EnglishWord("apple", "Apple", "苹果", WordCategory.FRUITS, "🍎", "红红的甜苹果~ Crunch!", 0xFFE57373),
        EnglishWord("banana", "Banana", "香蕉", WordCategory.FRUITS, "🍌", "甜甜弯香蕉~ Sweet!", 0xFFFFF176),
        EnglishWord("orange", "Orange", "橙子", WordCategory.FRUITS, "🍊", "多汁的大橙子~ Juicy!", 0xFFFFB74D),
        EnglishWord("strawberry", "Strawberry", "草莓", WordCategory.FRUITS, "🍓", "红红小草莓~ Yum!", 0xFFFF4081),
        EnglishWord("watermelon", "Watermelon", "西瓜", WordCategory.FRUITS, "🍉", "清凉甜西瓜~ Cool!", 0xFF4CAF50),
        EnglishWord("grape", "Grape", "葡萄", WordCategory.FRUITS, "🍇", "一串串紫葡萄~ Pop!", 0xFFBA68C8),
        EnglishWord("pear", "Pear", "雪梨", WordCategory.FRUITS, "🍐", "清甜大雪梨~ Crisp!", 0xFFDCE775),
        EnglishWord("peach", "Peach", "桃子", WordCategory.FRUITS, "🍑", "粉嫩蜜桃~ Soft!", 0xFFFF8A80),
        EnglishWord("mango", "Mango", "芒果", WordCategory.FRUITS, "🥭", "香香芒果~ Sweet!", 0xFFFFB300),
        EnglishWord("pineapple", "Pineapple", "菠萝", WordCategory.FRUITS, "🍍", "酸甜大菠萝~ Tangy!", 0xFFFFD54F),
        EnglishWord("cherry", "Cherry", "樱桃", WordCategory.FRUITS, "🍒", "红红小樱桃~ Fresh!", 0xFFD32F2F),
        EnglishWord("lemon", "Lemon", "柠檬", WordCategory.FRUITS, "🍋", "酸酸柠檬~ Sour!", 0xFFFFEE58),
        EnglishWord("kiwi", "Kiwi", "奇异果", WordCategory.FRUITS, "🥝", "绿色奇异果~ Healthy!", 0xFF8BC34A),
        EnglishWord("carrot", "Carrot", "胡萝卜", WordCategory.FRUITS, "🥕", "小兔爱吃的胡萝卜~ Snap!", 0xFFFF7043),
        EnglishWord("corn", "Corn", "玉米", WordCategory.FRUITS, "🌽", "金黄香甜玉米~ Pop!", 0xFFFFCA28),
        EnglishWord("milk", "Milk", "牛奶", WordCategory.FRUITS, "🥛", "香浓纯牛奶~ Glug!", 0xFFE0E0E0),
        EnglishWord("cake", "Cake", "蛋糕", WordCategory.FRUITS, "🍰", "生日甜蛋糕~ Delicious!", 0xFFFF80AB),
        EnglishWord("icecream", "Ice Cream", "冰淇淋", WordCategory.FRUITS, "🍦", "甜甜冰淇淋~ Yummy!", 0xFF80D8FF),

        // Vehicles (14 words)
        EnglishWord("car", "Car", "小汽车", WordCategory.VEHICLES, "🚗", "嘀嘀嘀~ Beep Beep!", 0xFF29B6F6),
        EnglishWord("bus", "Bus", "大巴车", WordCategory.VEHICLES, "🚌", "滴滴公共汽车~ Honk!", 0xFFFFCA28),
        EnglishWord("train", "Train", "小火车", WordCategory.VEHICLES, "🚂", "呜呜喀嚓~ Choo Choo!", 0xFF8D6E63),
        EnglishWord("plane", "Airplane", "飞机", WordCategory.VEHICLES, "✈️", "飞向蓝天~ Whoosh!", 0xFF4DD0E1),
        EnglishWord("boat", "Boat", "小轮船", WordCategory.VEHICLES, "⛵", "水上漂呀漂~ Splash!", 0xFF26A69A),
        EnglishWord("rocket", "Rocket", "火箭", WordCategory.VEHICLES, "🚀", "嗖！飞向太空~ Blast off!", 0xFFEF5350),
        EnglishWord("bike", "Bicycle", "自行车", WordCategory.VEHICLES, "🚲", "叮铃铃~ Ring Ring!", 0xFF66BB6A),
        EnglishWord("truck", "Truck", "大卡车", WordCategory.VEHICLES, "🚚", "运货大卡车~ Rumble!", 0xFFFF9800),
        EnglishWord("helicopter", "Helicopter", "直升机", WordCategory.VEHICLES, "🚁", "哒哒哒螺旋桨~ Whir!", 0xFF00ACC1),
        EnglishWord("taxi", "Taxi", "出租车", WordCategory.VEHICLES, "🚕", "招手就来的的士~ Beep!", 0xFFFFD600),
        EnglishWord("ambulance", "Ambulance", "救护车", WordCategory.VEHICLES, "🚑", "嘟呜嘟呜救护车~ Wee Woo!", 0xFFE53935),
        EnglishWord("police", "Police Car", "警车", WordCategory.VEHICLES, "🚓", "威武小警车~ Siren!", 0xFF1E88E5),

        // Colors & Nature (12 words)
        EnglishWord("red", "Red", "红色", WordCategory.COLORS, "🔴", "热情美丽红色~ Red!", 0xFFEF5350),
        EnglishWord("blue", "Blue", "蓝色", WordCategory.COLORS, "🔵", "大海天空蓝色~ Blue!", 0xFF42A5F5),
        EnglishWord("yellow", "Yellow", "黄色", WordCategory.COLORS, "🟡", "明亮阳光黄色~ Yellow!", 0xFFFFEE58),
        EnglishWord("green", "Green", "绿色", WordCategory.COLORS, "🟢", "大自然小草绿~ Green!", 0xFF66BB6A),
        EnglishWord("pink", "Pink", "粉色", WordCategory.COLORS, "🌸", "浪漫可爱粉色~ Pink!", 0xFFF48FB1),
        EnglishWord("purple", "Purple", "紫色", WordCategory.COLORS, "🟣", "梦幻神奇紫色~ Purple!", 0xFFAB47BC),
        EnglishWord("orange_color", "Orange", "橙色", WordCategory.COLORS, "🟧", "温暖活力橙色~ Orange!", 0xFFFF9800),
        EnglishWord("brown", "Brown", "棕色", WordCategory.COLORS, "🟫", "大地泥土棕色~ Brown!", 0xFF795548),
        EnglishWord("star", "Star", "星星", WordCategory.COLORS, "⭐", "一闪一闪亮晶晶~ Twinkle!", 0xFFFFD700),
        EnglishWord("sun", "Sun", "太阳", WordCategory.COLORS, "☀️", "温暖大太阳~ Sunshine!", 0xFFFF9800),
        EnglishWord("moon", "Moon", "月亮", WordCategory.COLORS, "🌙", "弯弯小月亮~ Goodnight!", 0xFFFFF59D),
        EnglishWord("rainbow", "Rainbow", "彩虹", WordCategory.COLORS, "🌈", "七彩大彩虹~ Colorful!", 0xFFFF4081),

        // Body (8 words)
        EnglishWord("eye", "Eye", "眼睛", WordCategory.BODY, "👀", "明亮大眼睛~ Blink!", 0xFF4FC3F7),
        EnglishWord("ear", "Ear", "耳朵", WordCategory.BODY, "👂", "听声音小耳朵~ Listen!", 0xFFFFB74D),
        EnglishWord("nose", "Nose", "鼻子", WordCategory.BODY, "👃", "闻花香小鼻子~ Sniff!", 0xFF81C784),
        EnglishWord("mouth", "Mouth", "嘴巴", WordCategory.BODY, "👄", "吃东西小嘴巴~ Yum!", 0xFFFF8A80),
        EnglishWord("hand", "Hand", "小手", WordCategory.BODY, "✋", "拍拍拍小手~ Clap Clap!", 0xFFFFD54F),
        EnglishWord("foot", "Foot", "小脚", WordCategory.BODY, "🦶", "跑跑跳跳小脚丫~ Step!", 0xFFBA68C8),
        EnglishWord("arm", "Arm", "手臂", WordCategory.BODY, "💪", "有力的小手臂~ Strong!", 0xFFFFB300),
        EnglishWord("head", "Head", "脑袋", WordCategory.BODY, "🧒", "聪明的小脑袋~ Think!", 0xFF4DD0E1)
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
            wordIds = listOf("cat", "dog", "duck", "rabbit", "bear", "sheep", "horse", "cow", "pig", "bird", "frog", "fish"),
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
            wordIds = listOf("apple", "banana", "orange", "strawberry", "watermelon", "grape", "pear", "peach", "mango", "pineapple", "cherry", "lemon"),
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
            wordIds = listOf("panda", "tiger", "lion", "elephant", "monkey", "penguin", "giraffe", "bee", "frog", "bear", "rabbit", "dog"),
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
            wordIds = listOf("car", "bus", "train", "plane", "boat", "rocket", "bike", "truck", "helicopter", "taxi", "ambulance", "police"),
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
            wordIds = listOf("red", "blue", "yellow", "green", "pink", "purple", "orange_color", "brown", "star", "sun", "moon", "rainbow"),
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
            wordIds = listOf("watermelon", "grape", "pear", "peach", "kiwi", "carrot", "corn", "milk", "cake", "icecream", "apple", "banana"),
            defaultMode = GameModeType.CARD_FLIP,
            stickerName = "超级记忆大师",
            stickerEmoji = "🧠🍉",
            requiredStarsToUnlock = 12
        ),
        LevelConfig(
            levelNumber = 7,
            title = "身体与自然",
            subtitle = "Body & Nature",
            iconEmoji = "🖐️",
            themeColor = 0xFFFFA726,
            wordIds = listOf("eye", "ear", "nose", "mouth", "hand", "foot", "arm", "head", "sun", "moon", "star", "rainbow"),
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
            wordIds = listOf("cat", "apple", "rocket", "panda", "plane", "strawberry", "tiger", "lion", "bus", "train", "rainbow", "star"),
            defaultMode = GameModeType.LISTEN_AND_PICK,
            stickerName = "英语冒险大王",
            stickerEmoji = "🏆🌟",
            requiredStarsToUnlock = 18
        )
    )
}
