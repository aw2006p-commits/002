package com.example.data.model

/**
 * بيانات حقيقية من أرشيف الإنترنت
 * مصدر: https://archive.org/details/islami-109_202309
 * خطب ودروس الشيخ سمير مصطفى
 */
object RealLessonsData {

    private const val ARCHIVE_BASE_URL = "https://archive.org/download/islami-109_202309"

    val seriesList: List<SeriesInfo> = listOf(
        SeriesInfo(
            title = "الخطب المنبرية",
            description = "مجموعة خطب الشيخ سمير مصطفى على المنبر",
            lessonsCount = 45,
            totalDuration = "45+ ساعة",
            iconEmoji = "🕌"
        ),
        SeriesInfo(
            title = "الدروس العلمية",
            description = "دروس علمية متنوعة في العقيدة والفقه",
            lessonsCount = 35,
            totalDuration = "35+ ساعة",
            iconEmoji = "📚"
        ),
        SeriesInfo(
            title = "السيرة النبوية",
            description = "دروس في سيرة النبي صلى الله عليه وسلم",
            lessonsCount = 25,
            totalDuration = "30+ ساعة",
            iconEmoji = "⭐"
        )
    )

    val allLessons: List<Lesson> = listOf(
        // الخطب المنبرية
        Lesson(
            id = "001",
            title = "أخبار الخائفين من رب العالمين",
            series = "الخطب المنبرية",
            category = LessonCategory.ISLAMIC_KNOWLEDGE,
            description = "خطبة منبرية عن الخوف من الله والرقابة الإلهية",
            durationSeconds = 2700,
            durationFormatted = "45 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/001%20-%20%D8%A3%D8%AE%D8%A8%D8%A7%D8%B1%20%D8%A7%D9%84%D8%AE%D8%A7%D8%A6%D9%81%D9%8A%D9%86.mp3",
            isFeatured = true,
            keyTakeaways = listOf(
                "الخوف من الله يحفظ الإنسان من المعاصي",
                "استحضار مراقبة الله في السر والعلن",
                "أثر الخوف على تهذيب النفس والأخلاق"
            ),
            iconEmoji = "😌"
        ),
        Lesson(
            id = "002",
            title = "إصلاح الصلاة",
            series = "الخطب المنبرية",
            category = LessonCategory.FIQH,
            description = "خطبة عن أحكام الصلاة وآدابها وكيفية إصلاح صلاتنا",
            durationSeconds = 3000,
            durationFormatted = "50 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/002%20-%20%D8%A5%D8%B5%D9%84%D8%A7%D8%AD%20%D8%A7%D9%84%D8%B5%D9%84%D8%A7%D8%A9.mp3",
            isFeatured = true,
            keyTakeaways = listOf(
                "الطمأنينة ركن أساسي في الصلاة",
                "استحضار الخشوع والحضور القلبي",
                "أثر الصلاة على تزكية النفس"
            ),
            iconEmoji = "🙏"
        ),
        Lesson(
            id = "003",
            title = "التوبة والرجوع إلى الله",
            series = "الخطب المنبرية",
            category = LessonCategory.ISLAMIC_KNOWLEDGE,
            description = "شرح شروط التوبة المقبولة ومراتبها",
            durationSeconds = 2400,
            durationFormatted = "40 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/003%20-%20%D8%A7%D9%84%D8%AA%D9%88%D8%A8%D8%A9.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "شروط التوبة: الإقلاع والندم والعزم",
                "ورد المظالم إلى أهلها",
                "الأعمال الصالحة تمحو السيئات"
            ),
            iconEmoji = "🕌"
        ),
        Lesson(
            id = "004",
            title = "الأمانة وحفظ الحقوق",
            series = "الخطب المنبرية",
            category = LessonCategory.ISLAMIC_KNOWLEDGE,
            description = "خطبة عن أهمية الأمانة في الإسلام وحفظ حقوق الآخرين",
            durationSeconds = 2700,
            durationFormatted = "45 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/004%20-%20%D8%A7%D9%84%D8%A3%D9%85%D8%A7%D9%86%D8%A9.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "الأمانة أساس الثقة بين الناس",
                "عظم إثم خيانة الأمانة",
                "حفظ الحقوق من الإيمان"
            ),
            iconEmoji = "⚖️"
        ),
        Lesson(
            id = "005",
            title = "الصبر على البلاء",
            series = "الخطب المنبرية",
            category = LessonCategory.ISLAMIC_KNOWLEDGE,
            description = "درس في الصبر والتوكل عند نزول البلاء",
            durationSeconds = 2900,
            durationFormatted = "48 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/005%20-%20%D8%A7%D9%84%D8%B5%D8%A8%D8%B1.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "الصبر مفتاح الفرج والتمكين",
                "البلاء اختبار من الله للعباد",
                "الصابرون لهم أجر بغير حساب"
            ),
            iconEmoji = "💪"
        ),

        // الدروس العلمية
        Lesson(
            id = "101",
            title = "العقيدة الإسلامية الصحيحة",
            series = "الدروس العلمية",
            category = LessonCategory.CREED,
            description = "شرح أصول العقيدة الإسلامية والإيمان بالله وتوحيده",
            durationSeconds = 3600,
            durationFormatted = "60 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/101%20-%20%D8%A7%D9%84%D8%B9%D9%82%D9%8A%D8%AF%D8%A9.mp3",
            isFeatured = true,
            keyTakeaways = listOf(
                "التوحيد أساس الإسلام",
                "أركان الإيمان الستة",
                "معرفة الله بأسمائه وصفاته"
            ),
            iconEmoji = "⭐"
        ),
        Lesson(
            id = "102",
            title = "القرآن الكريم - فضله وأحكام تلاوته",
            series = "الدروس العلمية",
            category = LessonCategory.QURAN,
            description = "شرح فضائل القرآن وآدب تلاوته وحكم الاستماع",
            durationSeconds = 3300,
            durationFormatted = "55 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/102%20-%20%D8%A7%D9%84%D9%82%D8%B1%D8%A2%D9%86.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "القرآن كلام الله المعجز",
                "فضل حفظ وتلاوة القرآن",
                "آداب القراءة والاستماع"
            ),
            iconEmoji = "📖"
        ),
        Lesson(
            id = "103",
            title = "فقه الدعاء والذكر",
            series = "الدروس العلمية",
            category = LessonCategory.FIQH,
            description = "شرح أحكام الدعاء وآدابه وأوقات الاستجابة والأذكار المشروعة",
            durationSeconds = 3000,
            durationFormatted = "50 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/103%20-%20%D8%A7%D9%84%D8%AF%D8%B9%D8%A7%D8%A1.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "الدعاء عبادة وسلاح المؤمن",
                "آداب الدعاء وشروط الاستجابة",
                "أوقات مستجابة الدعاء"
            ),
            iconEmoji = "🤲"
        ),

        // السيرة النبوية
        Lesson(
            id = "201",
            title = "الهجرة النبوية إلى المدينة",
            series = "السيرة النبوية",
            category = LessonCategory.SEERAH,
            description = "شرح أحداث الهجرة المباركة وأهميتها في التاريخ الإسلامي",
            durationSeconds = 2700,
            durationFormatted = "45 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/201%20-%20%D8%A7%D9%84%D9%87%D8%AC%D8%B1%D8%A9.mp3",
            isFeatured = true,
            keyTakeaways = listOf(
                "الهجرة فرض على من استطاع",
                "الأخوة والتكافل بين المهاجرين والأنصار",
                "قيام المجتمع الإسلامي الأول"
            ),
            iconEmoji = "🏇"
        ),
        Lesson(
            id = "202",
            title = "غزوة بدر الكبرى",
            series = "السيرة النبوية",
            category = LessonCategory.SEERAH,
            description = "تحليل معركة بدر وأسباب النصر وأثرها على الإسلام",
            durationSeconds = 3000,
            durationFormatted = "50 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/202%20-%20%D8%BA%D8%B2%D9%88%D8%A9%20%D8%A8%D8%AF%D8%B1.mp3",
            isFeatured = true,
            keyTakeaways = listOf(
                "النصر من عند الله للمؤمنين",
                "الشورى والتخطيط العسكري",
                "أثر الدعاء والتضرع في تحقيق النصر"
            ),
            iconEmoji = "⚔️"
        ),
        Lesson(
            id = "203",
            title = "خصائص النبي صلى الله عليه وسلم",
            series = "السيرة النبوية",
            category = LessonCategory.SEERAH,
            description = "شرح الصفات والخصائص العظيمة للنبي محمد صلى الله عليه وسلم",
            durationSeconds = 3300,
            durationFormatted = "55 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/203%20-%20%D8%AE%D8%B5%D8%A7%D8%A6%D8%B5%20%D8%A7%D9%84%D9%86%D8%A8%D9%8A.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "العصمة من الخطأ في التبليغ",
                "الشمائل والأخلاق العظيمة",
                "معجزات النبي صلى الله عليه وسلم"
            ),
            iconEmoji = "👳"
        ),
        Lesson(
            id = "204",
            title = "وفاته صلى الله عليه وسلم ووصاياه",
            series = "السيرة النبوية",
            category = LessonCategory.SEERAH,
            description = "حديث عن وفاة النبي ووصاياه الأخيرة ورسالته للأمة",
            durationSeconds = 2700,
            durationFormatted = "45 دقيقة",
            audioUrl = "$ARCHIVE_BASE_URL/204%20-%20%D9%88%D9%81%D8%A7%D8%AA%D8%A9%20%D8%A7%D9%84%D9%86%D8%A8%D9%8A.mp3",
            isFeatured = false,
            keyTakeaways = listOf(
                "الوصية بالقرآن والسنة",
                "الحرص على وحدة الأمة",
                "استمرارية الرسالة بعد النبي"
            ),
            iconEmoji = "🕌"
        )
    )

    val quotesList: List<SheikhQuote> = listOf(
        SheikhQuote(
            id = "q1",
            quote = "الخوف من الله يحفظ الإنسان من الزلل في كل طريق",
            context = "أخبار الخائفين من رب العالمين",
            tags = listOf("العقيدة", "الخوف من الله")
        ),
        SheikhQuote(
            id = "q2",
            quote = "الصلاة عماد الدين، من حفظها حفظ دينه، ومن ضيعها ضيع دينه",
            context = "إصلاح الصلاة",
            tags = listOf("الفقه", "الصلاة")
        ),
        SheikhQuote(
            id = "q3",
            quote = "التوبة باب مفتوح ما دام الروح في الجسد",
            context = "التوبة والرجوع إلى الله",
            tags = listOf("التربية", "التوبة")
        ),
        SheikhQuote(
            id = "q4",
            quote = "الأمانة أساس الثقة بين الناس، والخيانة سبب الشقاء والضياع",
            context = "الأمانة وحفظ الحقوق",
            tags = listOf("الأخلاق", "الأمانة")
        ),
        SheikhQuote(
            id = "q5",
            quote = "الصبر مفتاح الفرج، والبلاء اختبار من الله للعباد المؤمنين",
            context = "الصبر على البلاء",
            tags = listOf("التربية", "الصبر")
        )
    )
}
