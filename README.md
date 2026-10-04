# 맘모스 (MAMMOTH - 엄마의 메모들)

어르신과 디지털 취약계층이 쉽게 사용할 수 있도록 가독성과 접근성에 집중한 음성·텍스트 기반 스마트 메모장 애플리케이션입니다.  
**'엄마의 메모들'**이라는 따뜻한 의미를 담아, 복잡한 입력 절차를 최소화하고 말 한마디로 일정을 정리하고 알림을 설정할 수 있도록 설계되었습니다.

---

## 📌 주요 특징 (Key Features)

- **시니어 친화적 UI/UX**:
  - 기본적으로 크고 선명한 폰트와 큼직한 터치 영역 적용
  - 시스템 글꼴 크기 변경 및 화면 해상도에 맞춰 텍스트가 잘리지 않도록 자동 비율 조정(Auto-scaling)
  - 복잡한 뎁스를 배제한 직관적인 화면 구조
- **음성 및 텍스트 메모 (STT)**:
  - 마이크 버튼 터치 한 번으로 음성을 텍스트로 즉시 변환
- **스마트 메모 구조화**:
  - 자연어 입력 내용에서 핵심 일정, 시간, 할 일(체크리스트)을 자동 분류
  - 일정 및 시간 인식 시 사용자 확인을 거쳐 정시 알림(AlarmManager) 연동
- **안정적인 로컬 데이터 관리**:
  - Room DB 기반의 오프라인 우선(Offline-first) 구조
  - 메모 고정, 즐겨찾기, 카테고리 분류 및 휴지통(소프트 삭제/복구) 지원

---

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 스택 |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM, Repository Pattern |
| **Local Database** | Room Database |
| **Asynchronous** | Coroutines, Flow (StateFlow) |
| **System API** | SpeechRecognizer, AlarmManager, Android Notification |

---

## 🗺 개발 로드맵 (Roadmap)

- [x] **Phase 1: 기본 메모 CRUD & Room 연동**
  - Room DB 엔티티 및 DAO 설계 (`Memo`, `Reminder`, `ChecklistItem`)
  - Repository 및 StateFlow 기반 상태 관리 구현
  - 큰 글씨 및 반응형 레이아웃 기반 메모 목록/작성 UI 구현
- [x] **Phase 2: 음성 입력(STT) 연동**
  - Android `SpeechRecognizer` 기반 실시간 음성-텍스트 변환 및 런타임 권한 처리
- [ ] **Phase 3: 알림 시스템 구축**
  - `AlarmManager` 및 `NotificationChannel` 기반 정시 알림 스케줄링
어플리케이션 이름 **'맘모스 (Mom's Memos)'**를 반영하여 프로젝트 문서 상단의 명칭, 소개 문구, 그리고 패키지 경로 예시를 정돈한 내용입니다.

---

# 맘모스 (Mom's Memos) - 스마트 음성 메모장

**'엄마의 메모들(Mom's Memos)'**이라는 뜻을 담아, 어르신과 디지털 취약계층이 쉽게 사용할 수 있도록 가독성과 접근성에 집중한 음성·텍스트 기반 스마트 메모장 애플리케이션입니다.  
복잡한 입력 절차를 최소화하고, 음성 및 텍스트 입력을 바탕으로 일정을 정리하고 알림을 설정할 수 있도록 설계되었습니다.

---

## 📌 주요 특징 (Key Features)

- **시니어 친화적 UI/UX**:
  - 기본적으로 크고 선명한 폰트와 큼직한 터치 영역 적용
  - 시스템 글꼴 크기 변경 및 화면 해상도에 맞춰 텍스트가 잘리지 않도록 자동 비율 조정(Auto-scaling)
  - 복잡한 뎁스를 배제한 직관적인 화면 구조
- **음성 및 텍스트 메모 (STT)**:
  - 마이크 버튼 터치 한 번으로 음성을 텍스트로 즉시 변환
- **스마트 메모 구조화**:
  - 자연어 입력 내용에서 핵심 일정, 시간, 할 일(체크리스트)을 자동 분류
  - 일정 및 시간 인식 시 사용자 확인을 거쳐 정시 알림(`AlarmManager`) 연동
- **안정적인 로컬 데이터 관리**:
  - Room DB 기반의 오프라인 우선(Offline-first) 구조
  - 메모 고정, 즐겨찾기, 카테고리 분류 및 휴지통(소프트 삭제/복구) 지원

---

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 스택 |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM, Repository Pattern |
| **Local Database** | Room Database |
| **Asynchronous** | Coroutines, Flow (StateFlow) |
| **System API** | SpeechRecognizer, AlarmManager, Android Notification |

---

## 🗺 개발 로드맵 (Roadmap)

- [x] **Phase 1: 기본 메모 CRUD & Room 연동**
  - Room DB 엔티티 및 DAO 설계 (`Memo`, `Reminder`, `ChecklistItem`)
  - Repository 및 StateFlow 기반 상태 관리 구현
  - 큰 글씨 및 반응형 레이아웃 기반 메모 목록/작성 UI 구현
- [ ] **Phase 2: 음성 입력(STT) 연동**
  - Android `SpeechRecognizer` 기반 실시간 음성-텍스트 변환 및 런타임 권한 처리
- [ ] **Phase 3: 알림 시스템 구축**
  - `AlarmManager` 및 `NotificationChannel` 기반 정시 알림 스케줄링
- [ ] **Phase 4: 메모 구조화 로직 연동**
  - 자연어 텍스트 분석을 통한 날짜, 시간, 세부 할 일 추출
- [ ] **Phase 5: 파이프라인 통합**
  - 음성 입력 → 텍스트 변환 → 구조화 → 저장 및 사용자 확인 후 알림 등록
- [ ] **Phase 6: 온디바이스 AI 확장성 검토**
  - 오프라인 환경을 위한 온디바이스 음성인식 및 온디바이스 언어 모델 적용 검토

---

## 📂 프로젝트 구조 (Architecture)

```text
app/src/main/java/com/example/momsmemo/  (또는 com.example.mammoth)
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
├── ui/
│   ├── screen/
│   │   └── MemoListScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       └── MemoViewModel.kt
└── MainActivity.kt
