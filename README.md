# 🦣 맘모스 (Mom's Memos) - 어머니를 위한 스마트 음성 메모장

> **"말 한마디로 손쉽게 정리되는 엄마의 스마트 메모장"**  
> **맘모스(Mom's Memos)**는 스마트폰 사용이 서툰 어르신과 어머니를 위해 **시인성 높은 큰 글씨, 큼직한 터치 영역, 음성 인식(STT), AI 자연어 자동 정돈, 그리고 스마트 사전 알림**을 제공하는 Android 애플리케이션입니다.

---

## 📌 주요 특징 (Key Features)

### 1. 🎤 스마트 음성 인식(STT) & AI 자연어 정돈 (`MemoAiParser`)
- **원터치 음성 입력**: 바텀 시트의 큼직한 마이크 버튼 터치 한 번으로 음성을 실시간 텍스트로 전환합니다.
- **자연어 맥락 분석 엔진**:
  - *"3시 36분에 약을 먹어야 돼"* ➔ **제목**: `약 먹기`, **주요 할 일**: `오후 3시 36분에 약 먹기`
  - 불필요한 조사("을/를/에/도") 및 종결 어미("~해야 돼", "~할 거야")를 정제하고 **명사형 개조식(~하기, ~먹기)**으로 깔끔하게 정돈합니다.
- **컨텍스트 기반 AM/PM 시간대 추론**:
  - 오전/오후 명시가 없는 경우, 현재 시각을 기준으로 아직 지나지 않은 미래 시점(오후 18:36)으로 자동 추론합니다.
- **카테고리 자동 추천**:
  - 음성 맥락을 분석하여 `건강`, `장보기`, `모임`, `금융`, `일반` 카테고리를 자동으로 분류합니다.

---

### 2. ⏰ 스마트 단계별 푸시 알림 (`AlarmManager` & `Notification`)
- **잔여 시간 기반 사전 알림 (`ReminderScheduler`)**:
  - 감지된 알림 예정 시간과 현재 시간의 차이를 계산하여 조건별 사전 알림을 등록합니다.
  - **60분 초과 남음**: `1시간 전`, `10분 전`, `정시` 알림 등록
  - **30분 ~ 60분 남음**: `30분 전`, `10분 전`, `정시` 알림 등록
  - **10분 ~ 30분 남음**: `10분 전`, `정시` 알림 등록
  - **1분 ~ 10분 남음**: `5분 전` / `1분 전`, `정시` 알림 등록
- **고중요도 알림 채널**:
  - `NotificationManager.IMPORTANCE_HIGH` 채널을 통해 화면 상단에 명확하게 알림을 노출합니다.
- **과거 시점 방어 로직**:
  - 이미 지난 과거 시점의 알림은 예약되지 않도록 안전하게 처리되었습니다.

---

### 3. 🎨 시니어 친화적 모던 UI/UX (Material 3)
- **동적 텍스트 자동 스케길링 (`AutoSizeText`)**:
  - 어머니께서 시스템 글꼴 크기를 크게 설정하시더라도 텍스트가 잘리지 않도록 컨테이너 크기에 맞추어 비율이 자동 조정됩니다.
- **넓은 터치 영역 확보**:
  - 주요 아이콘(핀 고정, 중요 표시, 삭제 등)의 클릭 영역을 **최소 48dp 이상**으로 설정하여 오치치를 방지합니다.
- **실시간 필터 칩 (`FilterChip`)**:
  - `전체`, `상단 고정`, `중요 메모` 필터 칩을 통해 원하는 메모만 빠르게 모아볼 수 있습니다.
- **모던 바텀 시트 (`ModalBottomSheet`)**:
  - 화면 하단에서 부드럽게 올라오는 바텀 시트로 메모 작성 및 수정 경험을 단일화하였습니다.

---

### 4. 💾 오프라인 지원 로컬 데이터베이스 (Room DB)
- **Room Database 기반 Offline-First**:
  - 인터넷 연결이 없어도 언제든지 메모를 작성하고 조회할 수 있습니다.
- **소프트 삭제(휴지통) 지원**:
  - 실수로 삭제하더라도 복구가 가능한 소프트 삭제 구조를 갖추고 있습니다.

---

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 스택 |
| :--- | :--- |
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM Pattern, Repository Pattern |
| **Asynchronous / Reactive** | Coroutines, Flow, StateFlow |
| **Local Database** | Room Database 2.6.1 + KSP |
| **System Services** | SpeechRecognizer (STT), AlarmManager, BroadcastReceiver, NotificationManager |
| **Build Tool** | Android Gradle Plugin 8.7.3, Gradle 9.6, Gradle Version Catalog |

---

## 📂 프로젝트 구조 (Project Architecture)

```text
app/src/main/java/com/example/memp/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── MemoDao.kt
│   │   ├── entity/
│   │   │   ├── MemoEntity.kt
│   │   │   ├── ReminderEntity.kt
│   │   │   └── ChecklistItemEntity.kt
│   │   └── AppDatabase.kt
│   └── repository/
│       ├── MemoRepository.kt
│       └── MemoRepositoryImpl.kt
├── receiver/
│   └── ReminderReceiver.kt         # AlarmManager 브로드캐스트 푸시 알림 리시버
├── ui/
│   ├── screen/
│   │   └── MemoListScreen.kt       # Material 3 UI & BottomSheet
│   └── viewmodel/
│       └── MemoViewModel.kt        # StateFlow 기반 메모 UI 상태 관리
├── util/
│   ├── MemoAiParser.kt             # 자연어 파싱 및 명사형(~하기) 정규화 엔진
│   ├── ReminderScheduler.kt        # 잔여 시간별 단계별 알림 스케줄러
│   └── SpeechToTextManager.kt      # Android SpeechRecognizer STT 래퍼
└── MainActivity.kt                 # 앱 진입점 및 의존성 주입
```

---

## 🗺 개발 완료 로드맵 (Development Roadmap)

- [x] **Phase 1: 기본 메모 CRUD & Room DB & 모던 UI**
  - Room DB 엔티티 및 DAO 설계 (`Memo`, `Reminder`, `ChecklistItem`)
  - StateFlow 기반 MVVM 아키텍처 구축
  - Material 3 기반 반응형 큰 글씨 UI 구현
- [x] **Phase 2: 음성 입력(STT) & AI 메모 정돈**
  - Android `SpeechRecognizer` 실시간 음성 변환 래퍼 구현
  - `MemoAiParser` 자연어 분석: 제목 요약, 명사형(~하기) 정규화, 카테고리 추천
- [x] **Phase 3: 스마트 단계별 알림 시스템 (AlarmManager)**
  - 잔여 시간에 따른 단계별 사전 알림(1시간 전, 30분 전, 10분 전, 5분 전, 정시) 자동 예약
  - 고중요도 알림 채널 및 푸시 알림 리시버 구현
- [x] **Phase 4: 브랜딩 및 사용성 완성**
  - 앱 명칭 **'맘모스'** 적용 및 시니어 접근성 검증 완료
