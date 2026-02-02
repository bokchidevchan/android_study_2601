# Coroutine 치트시트

> 최근 대화 기반 핵심 정리 | 2026-02-02

---

## 1. 핵심 개념

| 개념 | 설명 | 면접 포인트 |
|------|------|------------|
| suspend | "비동기"가 아닌 "일시 중단 가능" 표시 | 컴파일러가 상태 머신으로 변환, 스레드 지정은 Dispatcher 담당 |
| CoroutineScope | 코루틴 생명주기 관리 울타리 | viewModelScope, lifecycleScope로 자동 취소 |
| CoroutineContext | 실행 환경 설정 (Job + Dispatcher + Name + Handler) | `+` 연산자로 조합 |
| Job | 코루틴 생명주기 핸들 | cancel(), join(), isActive |
| Deferred | 결과를 반환하는 Job | async의 반환 타입, await()로 결과 받기 |
| Structured Concurrency | 부모-자식 관계 | 부모 취소→자식 취소, 자식 완료 대기 |

---

## 2. launch vs async

| 구분 | launch | async |
|------|--------|-------|
| 반환 타입 | `Job` | `Deferred<T>` |
| 결과 | 없음 (fire-and-forget) | `await()`로 결과 받기 |
| 사용 시점 | 저장, 로깅, 이벤트 전송 | 결과가 필요한 비동기 작업 |
| 예외 처리 | Handler 또는 내부 try-catch | await() 시점에서 try-catch |
| 병렬 실행 | 각각 독립 실행 | `async + await` 패턴 |

```kotlin
// launch: 결과 필요 없을 때
viewModelScope.launch {
    repository.saveData(data)
}

// async: 결과 필요할 때 (병렬 실행)
viewModelScope.launch {
    val user = async { fetchUser() }
    val posts = async { fetchPosts() }
    
    // 병렬 실행 후 결과 조합
    showDashboard(user.await(), posts.await())
}
```

---

## 3. Dispatchers 비교

| Dispatcher | 스레드 | 용도 | 예시 |
|------------|--------|------|------|
| **Main** | UI 스레드 | UI 업데이트 | TextView.text, Toast |
| **IO** | 64개 스레드풀 | I/O 작업 | 네트워크, DB, 파일 |
| **Default** | 코어 수 스레드 | CPU 연산 | 정렬, JSON 파싱 |
| **Unconfined** | 호출 스레드 | 테스트용만 | 프로덕션 X |

```kotlin
// Repository: IO 보장 (Main-safe)
suspend fun getUser(id: Int): User = 
    withContext(Dispatchers.IO) {
        api.getUser(id)
    }

// ViewModel: 그냥 호출 (Dispatcher 걱정 X)
viewModelScope.launch {
    val user = repository.getUser(id)  // 내부에서 IO
    _state.value = user  // Main에서 UI 업데이트
}
```

---

## 4. Flow 비교표

| 구분 | Flow | StateFlow | SharedFlow |
|------|------|-----------|------------|
| Cold/Hot | Cold | Hot | Hot |
| 초기값 | 없음 | 필수 | 선택 |
| Replay | 없음 | 최신 1개 | 설정 가능 |
| 같은 값 | 모두 emit | 무시 (distinctUntilChanged) | 모두 emit |
| 용도 | 일회성 데이터 | UI 상태 | 이벤트 |
| 예시 | API 응답 | Loading/Success/Error | Toast, Navigation |

```kotlin
// StateFlow: UI 상태
private val _uiState = MutableStateFlow(UiState.Loading)
val uiState = _uiState.asStateFlow()

// SharedFlow: 일회성 이벤트
private val _events = MutableSharedFlow<UiEvent>()
val events = _events.asSharedFlow()

// Compose에서 수집
val state by viewModel.uiState.collectAsStateWithLifecycle()
LaunchedEffect(Unit) {
    viewModel.events.collect { event -> handleEvent(event) }
}
```

---

## 5. 예외 처리 비교

| 방법 | 적용 대상 | 위치 | 동작 |
|------|----------|------|------|
| 내부 try-catch | launch/async | suspend 함수 호출부 | 직접 처리 |
| await() try-catch | async | await() 호출부 | Deferred에서 꺼낼 때 |
| CoroutineExceptionHandler | launch | 루트 코루틴 | 마지막 방어선 |
| supervisorScope | 독립 자식 | 스코프 블록 | 형제 영향 X |

```kotlin
// ✅ Repository: Result로 감싸기
suspend fun getUser(id: Int): Result<User> = 
    runCatching {
        withContext(Dispatchers.IO) { api.getUser(id) }
    }

// ✅ ViewModel: Result 처리
viewModelScope.launch {
    repository.getUser(id)
        .onSuccess { _state.value = UiState.Success(it) }
        .onFailure { _state.value = UiState.Error(it.message) }
}

// supervisorScope: 독립 실행
supervisorScope {
    launch { loadUser() }   // 실패해도
    launch { loadPosts() }  // 계속 실행 ✅
}
```

---

## 6. 면접 Q&A

| 질문 | 핵심 답변 |
|------|----------|
| suspend의 의미? | "비동기"가 아닌 "일시 중단 가능". 컴파일러가 상태 머신 변환. 스레드 지정은 Dispatcher |
| launch vs async? | launch는 Job(fire-and-forget), async는 Deferred(결과 필요). 병렬 실행은 async+await |
| GlobalScope 왜 안 돼? | 생명주기 바인딩 없음 → 메모리 누수. viewModelScope/lifecycleScope 사용 |
| IO vs Default? | IO는 I/O 대기(64스레드), Default는 CPU 연산(코어수). 네트워크=IO, 정렬=Default |
| StateFlow vs SharedFlow? | StateFlow는 상태(현재값, 중복무시), SharedFlow는 이벤트(일회성, 모두전달) |
| Cold vs Hot Flow? | Cold는 수집 시 시작(독립), Hot은 항상 활성(공유). Flow=Cold, StateFlow=Hot |
| supervisorScope 언제? | 자식 실패가 형제에게 영향 주면 안 될 때. Dashboard에서 독립적 로딩 |
| CancellationException? | 절대 삼키지 말 것! catch하면 다시 throw. runCatching은 자동 전파 |

---

## 7. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| `Thread.sleep()` 사용 | `delay()` 사용 - 스레드 블로킹 X |
| `GlobalScope` 사용 | `viewModelScope`, `lifecycleScope` 사용 |
| launch 바깥에서 try-catch | launch 내부에서 try-catch 또는 Handler |
| async 예외 무시 | `await()` 호출부에서 try-catch |
| CancellationException 삼키기 | catch 후 다시 throw 또는 runCatching |
| Main에서 긴 작업 | `withContext(Dispatchers.IO)` 또는 Default |
| ViewModel에서 Dispatcher 지정 | Repository에서 보장 (Main-safe) |

---

## 8. 코드 패턴 모음

### 병렬 요청 패턴
```kotlin
suspend fun loadDashboard(): Dashboard = coroutineScope {
    val user = async { fetchUser() }
    val posts = async { fetchPosts() }
    Dashboard(user.await(), posts.await())
}
```

### 검색 debounce 패턴
```kotlin
val searchResults = searchQuery
    .debounce(300)
    .filter { it.length >= 2 }
    .distinctUntilChanged()
    .flatMapLatest { repository.search(it) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### Result 패턴 (권장)
```kotlin
// Repository
suspend fun getData(): Result<Data> = runCatching { api.fetch() }

// ViewModel
viewModelScope.launch {
    repository.getData()
        .onSuccess { _state.value = Success(it) }
        .onFailure { _state.value = Error(it) }
}
```

---

## 9. 핵심 한 줄 요약

```
suspend = 일시 중단 가능 (스레드 지정 X)
launch = fire-and-forget (Job)
async = 결과 필요 (Deferred + await)
IO = I/O 대기, Default = CPU 연산
StateFlow = 상태, SharedFlow = 이벤트
supervisorScope = 형제 독립 실행
```

---

*Generated from conversation on 2026-02-02*
