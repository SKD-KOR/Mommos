package com.example.memp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ParsedTimeInfo(
    val displayString: String,
    val shortTimeLabel: String,
    val targetTimeMillis: Long
)

data class AiAnalysisResult(
    val rawText: String,
    val title: String,
    val contentText: String,
    val todoList: List<String>,
    val category: String,
    val hasReminder: Boolean,
    val reminderDisplayString: String?,
    val targetTimeMillis: Long?
)

object MemoAiParser {

    fun parse(speechText: String): AiAnalysisResult {
        val trimmed = speechText.trim()
        if (trimmed.isBlank()) {
            return AiAnalysisResult(
                rawText = "",
                title = "",
                contentText = "",
                todoList = emptyList(),
                category = "일반",
                hasReminder = false,
                reminderDisplayString = null,
                targetTimeMillis = null
            )
        }

        val category = extractCategory(trimmed)
        val timeInfo = extractDateTime(trimmed)
        val rawTodoList = extractTodoList(trimmed)

        // 시간 정보가 추출된 경우 주요 할 일 문두에 시간 수식어 결합 ("오후 3시 36분에 약 먹기")
        val formattedTodoList = if (timeInfo != null) {
            rawTodoList.map { item ->
                if (!item.startsWith(timeInfo.shortTimeLabel)) {
                    "${timeInfo.shortTimeLabel}에 $item"
                } else {
                    item
                }
            }
        } else {
            rawTodoList
        }

        val title = generateSummaryTitle(trimmed, category, rawTodoList)
        val contentText = buildFormattedContent(trimmed, formattedTodoList)

        return AiAnalysisResult(
            rawText = trimmed,
            title = title,
            contentText = contentText,
            todoList = formattedTodoList,
            category = category,
            hasReminder = timeInfo != null,
            reminderDisplayString = timeInfo?.displayString,
            targetTimeMillis = timeInfo?.targetTimeMillis
        )
    }

    private fun extractCategory(text: String): String {
        return when {
            text.containsAny("병원", "약", "의사", "진료", "건강", "치과", "안과", "주사", "검진") -> "건강"
            text.containsAny("마트", "사야", "사고", "장보기", "우유", "계란", "고기", "구입", "구매", "시장", "과일") -> "장보기"
            text.containsAny("친구", "모임", "동창", "약속", "만나", "점심", "저녁", "식사", "생일", "밥") -> "모임"
            text.containsAny("은행", "송금", "이체", "세금", "공과금", "돈", "통장", "어플", "앱", "카카오페이") -> "금융"
            else -> "일반"
        }
    }

    private fun extractTodoList(text: String): List<String> {
        // 날짜/시간 수식어 및 시간 조사("쯤에", "경에", "에" 등) 완전 제거
        val timeCleaned = text.replace(
            Regex("(오늘|내일|모레|다음\\s*주|이번\\s*주|월요일|화요일|수요일|목요일|금요일|토요일|일요일|\\d{1,2}월\\s*\\d{1,2}일)?\\s*(오전|오후|아침|점심|저녁|밤|새벽|낮)?\\s*\\d{1,2}시(\\s*\\d{1,2}분)?\\s*(쯤에|경에|정도에|쯤|경|정도|에|날에)?"),
            ""
        ).replace(
            Regex("^(오늘|내일|모레|다음\\s*주|이번\\s*주)\\s*(에|날에)?"),
            ""
        ).trim()

        val clauses = timeCleaned.split(Regex("(하고|그리고|가서|해서|한 뒤|사고|만나서|~도|,|\\n|그리고는)"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val todos = mutableListOf<String>()
        for (clause in clauses) {
            val normalized = normalizeActionPhrase(clause)
            if (normalized.isNotBlank() && normalized.length >= 2) {
                todos.add(normalized)
            }
        }

        return if (todos.isNotEmpty()) todos else listOf(if (timeCleaned.isNotBlank()) timeCleaned else text)
    }

    private fun normalizeActionPhrase(phrase: String): String {
        var text = phrase.trim()

        // 1. 다양한 서술어 어미 및 의도/의무 표현 제거 (~어야 돼, ~해야 돼, ~어야 해, ~먹어, ~가라 등)
        text = text.replace(
            Regex("(어야\\s*돼|아야\\s*돼|여야\\s*돼|야\\s*돼|해야\\s*돼|어야\\s*해|아야\\s*해|여야\\s*해|야\\s*해|해야\\s*해|어야지|아야지|여야지|야지|해야지|기로\\s*했어|기로\\s*함|하기로\\s*함|예정이야|예정임|예정이다|예정|할\\s*거야|할\\s*거다|할거야|해야\\s*함|하려구|하려고|하겠다|하겠습니다|합시다|해보자|하려\\s*해|할\\s*것|갑시다|먹자|사자|먹어|가라|사라|봐라|해라)$"),
            ""
        ).trim()

        // 단순 종결 어미 어/아/여 보정
        if (text.endsWith("어") || text.endsWith("아") || text.endsWith("여")) {
            text = text.dropLast(1).trim()
        }

        // 2. 불필요한 목적격/장소 조사 정리 ("약을" -> "약", "어플을" -> "어플", "병원에" -> "병원")
        text = text.replace(Regex("^(.*?[가-힣]+)(을|를|에|도|에게|한테|으로|로)$")) { matchResult ->
            matchResult.groupValues[1]
        }.trim()

        // 3. 서술어 동사 및 명사형(~하기) 변환
        return when {
            text.endsWith("먹") -> text + "기"
            text.endsWith("가") -> text + "기"
            text.endsWith("사") -> text + "기"
            text.endsWith("만나") -> text + "기"
            text.endsWith("보") -> text + "기"
            text.endsWith("하") -> text + "기"
            text.endsWith("맞") -> text + "기"
            text.endsWith("걸") -> text + "기"
            text.endsWith("받") -> text + "기"
            text.endsWith("찾") -> text + "기"
            text.endsWith("들어가") -> text + "기"
            text.endsWith("접속하") -> text.dropLast(1) + "하기"
            text == "밥" -> "밥 먹기"
            text == "약" -> "약 먹기"
            text.endsWith("기") -> text
            text.endsWith("방문") || text.endsWith("구입") || text.endsWith("구매") || text.endsWith("예약") || text.endsWith("식사") || text.endsWith("복용") -> text
            else -> {
                if (text.endsWith("먹어") || text.endsWith("먹아")) text.dropLast(2) + "먹기"
                else if (text.endsWith("들어")) text + "가기"
                else text
            }
        }
    }

    private fun extractDateTime(text: String): ParsedTimeInfo? {
        val cal = Calendar.getInstance()
        var hasExplicitDate = false
        var hasTime = false

        // 날짜 파싱
        when {
            text.contains("오늘") -> {
                hasExplicitDate = true
            }
            text.contains("내일") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                hasExplicitDate = true
            }
            text.contains("모레") -> {
                cal.add(Calendar.DAY_OF_YEAR, 2)
                hasExplicitDate = true
            }
            text.contains("다음 주") -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                hasExplicitDate = true
                val dayOfWeek = parseDayOfWeek(text)
                if (dayOfWeek != null) {
                    cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                }
            }
            text.contains("이번 주") -> {
                hasExplicitDate = true
                val dayOfWeek = parseDayOfWeek(text)
                if (dayOfWeek != null) {
                    cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                }
            }
            else -> {
                val dayOfWeek = parseDayOfWeek(text)
                if (dayOfWeek != null) {
                    hasExplicitDate = true
                    while (cal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }
        }

        // 시간 파싱 (예: "3시 36분", "오전 10시", "오후 3시 30분")
        val timeRegex = Regex("(\\d{1,2})시(\\s*(\\d{1,2})분)?")
        val match = timeRegex.find(text)
        if (match != null) {
            hasTime = true
            val rawHour = match.groupValues[1].toIntOrNull() ?: 9
            val minute = match.groupValues[3].toIntOrNull() ?: 0

            val hour = determineHour(rawHour, text, cal, hasExplicitDate)

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        } else if (hasExplicitDate) {
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }

        if (!hasExplicitDate && !hasTime) return null

        val displayFormat = SimpleDateFormat("M월 d일(E) a h:mm", Locale.KOREAN)

        val hourVal = cal.get(Calendar.HOUR)
        val hStr = if (hourVal == 0) 12 else hourVal
        val mVal = cal.get(Calendar.MINUTE)
        val amPmStr = if (cal.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val shortLabel = if (mVal > 0) "$amPmStr ${hStr}시 ${mVal}분" else "$amPmStr ${hStr}시"

        return ParsedTimeInfo(
            displayString = displayFormat.format(cal.time),
            shortTimeLabel = shortLabel,
            targetTimeMillis = cal.timeInMillis
        )
    }

    private fun determineHour(rawHour: Int, text: String, cal: Calendar, hasExplicitDate: Boolean): Int {
        val isMorningExplicit = text.containsAny("오전", "아침", "새벽")
        val isAfternoonExplicit = text.containsAny("오후", "저녁", "밤", "점심", "낮")

        if (isMorningExplicit) {
            return if (rawHour == 12) 0 else rawHour
        }

        if (isAfternoonExplicit) {
            return if (rawHour < 12) rawHour + 12 else rawHour
        }

        if (rawHour >= 12) return rawHour

        val now = Calendar.getInstance()

        if (isSameDay(cal, now)) {
            val pmHour = rawHour + 12
            val amHour = rawHour

            val pmTime = cal.clone() as Calendar
            pmTime.set(Calendar.HOUR_OF_DAY, pmHour)

            if (pmTime.after(now)) {
                return pmHour
            }

            val amTime = cal.clone() as Calendar
            amTime.set(Calendar.HOUR_OF_DAY, amHour)

            if (amTime.after(now)) {
                return amHour
            }

            if (hasExplicitDate && text.contains("오늘")) {
                return pmHour
            } else {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                return pmHour
            }
        } else {
            return if (rawHour in 1..11) rawHour + 12 else rawHour
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun parseDayOfWeek(text: String): Int? {
        return when {
            text.contains("월요일") -> Calendar.MONDAY
            text.contains("화요일") -> Calendar.TUESDAY
            text.contains("수요일") -> Calendar.WEDNESDAY
            text.contains("목요일") -> Calendar.THURSDAY
            text.contains("금요일") -> Calendar.FRIDAY
            text.contains("토요일") -> Calendar.SATURDAY
            text.contains("일요일") -> Calendar.SUNDAY
            else -> null
        }
    }

    private fun generateSummaryTitle(raw: String, category: String, todos: List<String>): String {
        if (todos.isNotEmpty() && todos.size <= 2) {
            return todos.joinToString(" 및 ")
        } else if (todos.size > 2) {
            return "${todos[0]} 외 ${todos.size - 1}건"
        }

        return when (category) {
            "건강" -> "병원 방문 및 약 챙기기"
            "장보기" -> "장보기 목록"
            "모임" -> "모임 약속"
            "금융" -> "은행 및 금융 업무"
            else -> if (raw.length > 15) raw.take(15) + "..." else raw
        }
    }

    private fun buildFormattedContent(raw: String, todos: List<String>): String {
        val sb = StringBuilder()
        if (todos.isNotEmpty()) {
            sb.append("📌 주요 할 일:\n")
            todos.forEach { item ->
                sb.append("• ").append(item).append("\n")
            }
            sb.append("\n💬 음성 원문:\n\"").append(raw).append("\"")
        } else {
            sb.append(raw)
        }
        return sb.toString()
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}