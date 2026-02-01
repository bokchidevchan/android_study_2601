# Android Testing 면접 심화 가이드

> 면접에서 "왜?"라고 물어볼 때 대답할 수 있는 깊이 있는 내용

---

## 1. Main Dispatcher 교체

### 문제 상황

```kotlin
class MyViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch {  // 내부적으로 Dispatchers.Main 사용
            // ...
        }
    }
}
```

### 왜 교체해야 하나?

```
┌─────────────────────────────────────────────────────────┐
│  Android 앱 실행 시                                      │
│  ┌─────────────┐                                        │
│  │ Main Looper │ ◄── Dispatchers.Main이 여기에 연결     │
│  └─────────────┘                                        │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Unit Test (JVM) 실행 시                                 │
│  ┌─────────────┐                                        │
│  │    없음!    │ ◄── Main Looper가 존재하지 않음        │
│  └─────────────┘                                        │
│                                                          │
│  → IllegalStateException 발생!                          │
└─────────────────────────────────────────────────────────┘
```

### 해결 방법

```kotlin
@Before
fun setUp() {
    // 테스트용 Dispatcher로 Main을 대체
    Dispatchers.setMain(StandardTestDispatcher())
}

@After
fun tearDown() {
    // 원래대로 복원 (다른 테스트에 영향 주지 않도록)
    Dispatchers.resetMain()
}
```

### StandardTestDispatcher vs UnconfinedTestDispatcher

| | StandardTestDispatcher | UnconfinedTestDispatcher |
|---|---|---|
| **실행 시점** | `advanceUntilIdle()` 호출 시 | 즉시 실행 |
| **시간 제어** | 가능 (`advanceTimeBy`) | 불가능 |
| **사용 시점** | 대부분의 경우 (권장) | 순서 상관없는 간단한 테스트 |

```kotlin
// StandardTestDispatcher - 명시적 제어
@Test
fun standard() = runTest {
    viewModel.loadData()
    // 아직 실행 안 됨!
    advanceUntilIdle()  // 이제 실행됨
    assertEquals(expected, viewModel.state.value)
}

// UnconfinedTestDispatcher - 즉시 실행
@Test
fun unconfined() = runTest(UnconfinedTestDispatcher()) {
    viewModel.loadData()
    // 이미 실행됨
    assertEquals(expected, viewModel.state.value)
}
```

### 면접 답변

> "viewModelScope는 Dispatchers.Main을 사용하는데, Unit Test는 JVM에서 실행되어 Android의 Main Looper가 없습니다. 그래서 Dispatchers.setMain()으로 테스트용 Dispatcher를 주입해야 합니다."

---

## 2. Virtual Time (가상 시간)

### 문제 상황

```kotlin
// 실제 코드
suspend fun fetchWithRetry(): Data {
    repeat(3) {
        try {
            return api.fetch()
        } catch (e: Exception) {
            delay(5000)  // 5초 대기 후 재시도
        }
    }
    throw RetryExhaustedException()
}
```

이 코드를 테스트하려면 **15초**를 기다려야 하나?

### Virtual Time의 원리

```
┌─────────────────────────────────────────────────────────┐
│  실제 시간 (Real Time)                                   │
│  delay(5000) → 실제로 5초 대기                           │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  가상 시간 (Virtual Time)                                │
│  delay(5000) → 시계만 5초 전진, 실제 대기 없음           │
│                                                          │
│  ┌──────┐  delay(5000)  ┌──────┐                        │
│  │ t=0  │ ───────────► │ t=5s │  (즉시!)                │
│  └──────┘               └──────┘                        │
└─────────────────────────────────────────────────────────┘
```

### runTest의 마법

```kotlin
@Test
fun `15초짜리 테스트가 밀리초 안에 완료`() = runTest {
    val startTime = currentTime
    
    delay(5000)   // 실제로 안 기다림
    delay(5000)   // 실제로 안 기다림
    delay(5000)   // 실제로 안 기다림
    
    // 가상 시간은 15초 경과
    assertEquals(15000, currentTime - startTime)
    
    // 실제 테스트 실행 시간: 몇 밀리초
}
```

### 시간 제어 함수

```kotlin
@Test
fun `시간 제어 예제`() = runTest {
    var result = ""
    
    launch {
        delay(1000)
        result = "완료"
    }
    
    // 아직 실행 안 됨
    assertEquals("", result)
    
    // 500ms만 전진 - 아직 부족
    advanceTimeBy(500)
    assertEquals("", result)
    
    // 500ms 더 전진 - 이제 1초 됨
    advanceTimeBy(500)
    assertEquals("완료", result)
    
    // 또는 한 번에 모든 코루틴 완료
    // advanceUntilIdle()
}
```

### 면접 답변

> "runTest는 Virtual Time을 사용해서 delay()가 실제로 기다리지 않고 가상 시계만 전진시킵니다. 그래서 10초짜리 delay도 밀리초 안에 테스트할 수 있습니다."

---

## 3. 테스트 더블 (Test Double) 심화

### 전체 그림

```
┌─────────────────────────────────────────────────────────┐
│                    테스트 더블 계보                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Dummy ──► 아무것도 안 함 (파라미터 채우기용)            │
│     │                                                    │
│     ▼                                                    │
│  Stub ───► 미리 정한 값 반환                             │
│     │                                                    │
│     ▼                                                    │
│  Spy ────► 실제 동작 + 호출 기록                         │
│     │                                                    │
│     ▼                                                    │
│  Mock ───► 호출 여부/횟수/순서 검증                      │
│     │                                                    │
│     ▼                                                    │
│  Fake ───► 실제 동작하는 간이 버전                       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 각각의 목적

#### Stub - "상태 검증"

```kotlin
// 질문: "잔액이 5000원일 때, 3000원 출금하면 결과가 뭐야?"
every { accountRepo.getBalance() } returns 5000

val result = withdrawService.withdraw(3000)

assertEquals(WithdrawResult.Success(2000), result)  // 상태(결과) 검증
```

#### Mock - "행위 검증"

```kotlin
// 질문: "출금할 때 로그가 기록되나? 알림이 발송되나?"
withdrawService.withdraw(3000)

verify { logger.log(any()) }           // 로그 기록됨?
verify { notificationService.send() }  // 알림 발송됨?
verify(exactly = 0) { errorHandler.handle(any()) }  // 에러 없음?
```

#### Spy - "일부만 가짜"

```kotlin
// 질문: "실제 계산 로직은 그대로, 외부 API 호출만 가짜로"
val realService = PaymentService()
val spyService = spyk(realService)

// 실제 로직 유지
spyService.calculateFee(10000)  // 진짜 계산

// 외부 호출만 가짜
every { spyService.callExternalApi() } returns "mocked"
```

#### Fake - "상태 기반 테스트"

```kotlin
// 질문: "사용자 3명 추가하고 1명 삭제하면 2명 남나?"
val fakeRepo = FakeUserRepository()

fakeRepo.save(user1)
fakeRepo.save(user2)
fakeRepo.save(user3)
fakeRepo.delete(user2.id)

assertEquals(2, fakeRepo.findAll().size)  // 상태 검증
```

### 언제 무엇을 쓸까?

| 상황 | 선택 | 이유 |
|------|------|------|
| 단순히 값 반환 필요 | Stub | 간단 |
| 호출 여부 확인 필요 | Mock | verify 사용 |
| 실제 로직 일부 유지 | Spy | 복잡한 객체 |
| 통합 테스트 느낌 | Fake | 상태 기반 |
| 외부 API (HTTP 등) | Fake | 상태 + 시나리오 |

### 면접 답변

> "Mock은 '이 메서드가 호출되었나?' 같은 행위 검증, Stub은 '이 입력에 이 출력' 같은 상태 검증에 씁니다. Spy는 실제 객체의 일부만 오버라이드할 때, Fake는 메모리 DB 같은 실제 동작하는 가짜가 필요할 때 사용합니다."

---

## 4. Turbine - Flow 테스트

### 왜 필요한가?

```kotlin
// 문제: Flow는 비동기라서 값이 "언제" emit되는지 알 수 없음
val flow = viewModel.uiState

// ❌ 이렇게 하면 안 됨
assertEquals(Loading, flow.first())  // 어떤 상태를 받을지 모름
```

### Turbine의 해결책

```kotlin
viewModel.uiState.test {
    // 상태 변화를 "순서대로" 검증
    assertEquals(Idle, awaitItem())      // 1번째 emit
    
    viewModel.load()
    
    assertEquals(Loading, awaitItem())   // 2번째 emit
    
    advanceUntilIdle()
    
    assertEquals(Success, awaitItem())   // 3번째 emit
    
    cancelAndIgnoreRemainingEvents()
}
```

### Turbine 핵심 함수

```kotlin
flow.test {
    // 값 가져오기
    awaitItem()          // 다음 값 대기 (없으면 에러)
    expectMostRecentItem()  // 가장 최근 값
    
    // 완료/에러
    awaitComplete()      // 완료 대기
    awaitError()         // 에러 대기
    
    // 타임아웃
    expectNoEvents()     // 이벤트 없음 확인
    
    // 종료
    cancelAndIgnoreRemainingEvents()  // 나머지 무시하고 종료
}
```

### 실전 패턴

```kotlin
@Test
fun `로딩 → 성공 상태 변화`() = runTest {
    viewModel.uiState.test {
        // Given: 초기 상태
        assertEquals(UiState.Idle, awaitItem())
        
        // When: 로딩 시작
        viewModel.loadArticles()
        
        // Then: 상태 변화 순서 검증
        assertEquals(UiState.Loading, awaitItem())
        
        advanceUntilIdle()
        
        val success = awaitItem()
        assertTrue(success is UiState.Success)
        assertEquals(3, (success as UiState.Success).data.size)
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 면접 답변

> "Turbine은 Flow의 emit 순서를 검증하는 라이브러리입니다. awaitItem()으로 다음 값을 기다리고, 상태 변화 순서를 순차적으로 검증할 수 있습니다."

---

## 5. sealed class UI 상태

### 왜 sealed class인가?

```kotlin
// ❌ 나쁜 예: 여러 변수로 상태 관리
data class UiState(
    val isLoading: Boolean = false,
    val data: List<Item>? = null,
    val error: String? = null
)

// 문제: 불가능한 상태 조합이 가능
UiState(isLoading = true, data = listOf(...), error = "에러")
// 로딩 중인데 데이터도 있고 에러도 있다?!
```

```kotlin
// ✅ 좋은 예: sealed class로 상태 정의
sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val data: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}

// 한 번에 하나의 상태만 가능!
```

### sealed class의 장점

```kotlin
// 1. 컴파일 타임 안전성 - when에서 else 불필요
fun render(state: UiState) {
    when (state) {
        is UiState.Idle -> showIdle()
        is UiState.Loading -> showLoading()
        is UiState.Success -> showData(state.data)
        is UiState.Error -> showError(state.message)
        // else 필요 없음! 새 상태 추가하면 컴파일 에러
    }
}

// 2. 상태 전이가 명확
Idle → Loading → Success
              ↘ Error

// 3. 불변성 보장
val newState = (currentState as UiState.Success).copy(
    data = newData
)
```

### data object vs data class

```kotlin
// Kotlin 1.9+
data object Loading : UiState()  // 싱글톤, equals/hashCode/toString 자동

data class Success(val data: List<Item>) : UiState()  // 인스턴스
```

### 면접 답변

> "sealed class를 사용하면 불가능한 상태 조합을 컴파일 타임에 방지할 수 있습니다. 또한 when 식에서 모든 케이스를 강제하므로 새 상태 추가 시 누락을 방지합니다."

---

## 6. Compose Semantics Tree

### View System vs Compose

```
┌─────────────────────────────────────────────────────────┐
│  View System                                             │
│  ┌─────────────────┐                                    │
│  │  View Hierarchy │ ◄── 실제 뷰 트리                   │
│  │  LinearLayout   │                                    │
│  │    ├ TextView   │                                    │
│  │    └ Button     │                                    │
│  └─────────────────┘                                    │
│                                                          │
│  테스트: withId(R.id.button)                            │
│  접근성: View의 contentDescription                       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Compose                                                 │
│  ┌─────────────────┐                                    │
│  │ Semantics Tree  │ ◄── 의미론적 트리 (별도!)          │
│  │  Role=Button    │                                    │
│  │  Text="클릭"    │                                    │
│  │  ContentDesc=..│                                    │
│  └─────────────────┘                                    │
│                                                          │
│  테스트: onNodeWithText("클릭")                          │
│  접근성: 같은 Semantics Tree 사용!                       │
└─────────────────────────────────────────────────────────┘
```

### 핵심: 테스트 = 접근성

```kotlin
// Semantics Tree에 정보가 있으면
// → 테스트에서 찾을 수 있음
// → 스크린 리더도 찾을 수 있음

// Semantics Tree에 정보가 없으면
// → 테스트에서 못 찾음 (testTag로만 가능)
// → 스크린 리더도 못 찾음 (접근성 문제!)
```

### Finder 우선순위

```kotlin
// 1순위: 텍스트 (사용자가 보는 것)
onNodeWithText("로그인")

// 2순위: contentDescription (아이콘, 이미지)
onNodeWithContentDescription("검색")

// 3순위: testTag (위 둘로 못 찾을 때만)
onNodeWithTag("complex_custom_view")
```

### 면접 답변

> "Compose는 View hierarchy와 별도로 Semantics Tree를 관리합니다. 테스트와 접근성 도구가 같은 Semantics Tree를 사용하므로, 접근성 좋은 UI가 테스트하기도 좋습니다."

---

## 7. Stateless Composable (Humble Object Pattern)

### 문제: ViewModel을 직접 사용하면

```kotlin
// ❌ 테스트하기 어려움
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    val state by viewModel.uiState.collectAsState()
    
    Text(text = state.count.toString())
    Button(onClick = { viewModel.increment() }) {
        Text("+1")
    }
}

// 테스트하려면 ViewModel을 Mock해야 함
// collectAsState()도 Mock해야 함
// 복잡!
```

### 해결: Stateless로 분리

```kotlin
// ✅ Stateful: ViewModel 연결 담당
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    val state by viewModel.uiState.collectAsState()
    
    CounterContent(
        count = state.count,
        onIncrement = viewModel::increment
    )
}

// ✅ Stateless: 순수 UI (테스트 쉬움)
@Composable
fun CounterContent(
    count: Int,
    onIncrement: () -> Unit
) {
    Text(text = count.toString())
    Button(onClick = onIncrement) {
        Text("+1")
    }
}
```

### 테스트 분리

```kotlin
// ViewModel 테스트 (Unit Test)
@Test
fun `increment increases count`() = runTest {
    val viewModel = CounterViewModel()
    viewModel.increment()
    assertEquals(1, viewModel.uiState.value.count)
}

// UI 테스트 (Compose Test)
@Test
fun `clicking button calls onIncrement`() {
    var clicked = false
    
    composeTestRule.setContent {
        CounterContent(
            count = 0,
            onIncrement = { clicked = true }
        )
    }
    
    composeTestRule.onNodeWithText("+1").performClick()
    assertTrue(clicked)
}
```

### 면접 답변

> "Humble Object Pattern으로 Stateful과 Stateless Composable을 분리합니다. Stateless는 데이터와 콜백만 받아서 테스트가 쉽고, 비즈니스 로직은 ViewModel에서 Unit Test합니다."

---

## 8. DEX 제한 - 메서드명 규칙

### 왜 다른가?

```
┌─────────────────────────────────────────────────────────┐
│  Unit Test (src/test/)                                   │
│  ┌─────────────────┐                                    │
│  │      JVM        │ ◄── Java/Kotlin 직접 실행          │
│  │  백틱 메서드 OK  │     어떤 메서드명도 가능            │
│  └─────────────────┘                                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Instrumented Test (src/androidTest/)                    │
│  ┌─────────────────┐                                    │
│  │   DEX 파일로    │ ◄── Android 기기에서 실행          │
│  │    컴파일됨     │     DEX 포맷 제한 있음             │
│  └─────────────────┘                                    │
│                                                          │
│  DEX 1.0: 메서드명에 공백, 한글 불가                     │
│  백틱 메서드 → 컴파일 에러!                              │
└─────────────────────────────────────────────────────────┘
```

### 규칙

```kotlin
// src/test/ (JVM)
@Test
fun `사용자 생성 시 이메일이 발송된다`() { }  // ✅ OK

// src/androidTest/ (Android)
@Test
fun `사용자 생성 시 이메일이 발송된다`() { }  // ❌ 컴파일 에러

@Test
fun user_creation_sends_email() { }  // ✅ OK

@Test
fun userCreation_sendsEmail() { }  // ✅ OK
```

### 면접 답변

> "Instrumented Test는 DEX 파일로 컴파일되는데, DEX 포맷이 메서드명에 공백이나 특수문자를 지원하지 않습니다. 그래서 백틱 대신 언더스코어를 사용합니다."

---

## 9. coEvery vs every

### 규칙: suspend 함수는 co- 접두사

```kotlin
interface Repository {
    fun getSync(): Data           // 일반 함수
    suspend fun getAsync(): Data  // suspend 함수
}
```

```kotlin
// 일반 함수
every { repo.getSync() } returns data
verify { repo.getSync() }

// suspend 함수
coEvery { repo.getAsync() } returns data
coVerify { repo.getAsync() }
```

### 왜?

```kotlin
// suspend 함수는 Continuation을 받음
// MockK가 이를 처리하려면 다른 방식 필요

// ❌ every로 suspend 함수 stub하면
every { repo.getAsync() } returns data
// → 동작 안 함 또는 에러

// ✅ coEvery 사용
coEvery { repo.getAsync() } returns data
// → 정상 동작
```

### 전체 목록

| 일반 함수 | suspend 함수 |
|-----------|-------------|
| `every` | `coEvery` |
| `verify` | `coVerify` |
| `answers` | `coAnswers` |
| `just Runs` | `just Runs` (동일) |

### 면접 답변

> "suspend 함수는 내부적으로 Continuation을 사용하는데, MockK가 이를 처리하려면 coEvery, coVerify 같은 코루틴 전용 함수를 써야 합니다."

---

## 10. F.I.R.S.T 원칙

### Fast (빠르게)

```kotlin
// ❌ 느린 테스트
@Test
fun slow() {
    Thread.sleep(5000)  // 실제 대기
    // ...
}

// ✅ 빠른 테스트
@Test
fun fast() = runTest {
    delay(5000)  // Virtual Time - 즉시 완료
    // ...
}
```

### Independent (독립적)

```kotlin
// ❌ 의존적 - 순서에 따라 결과 다름
class BadTest {
    companion object {
        var sharedState = 0
    }
    
    @Test fun test1() { sharedState = 1 }
    @Test fun test2() { assertEquals(0, sharedState) }  // 실패!
}

// ✅ 독립적 - 각 테스트가 상태 초기화
class GoodTest {
    private var state = 0
    
    @Before fun setUp() { state = 0 }
    
    @Test fun test1() { state = 1; assertEquals(1, state) }
    @Test fun test2() { assertEquals(0, state) }  // 성공!
}
```

### Repeatable (반복 가능)

```kotlin
// ❌ 환경에 따라 결과 다름
@Test
fun unrepeatable() {
    val result = api.fetchRealData()  // 네트워크 상태에 따라 다름
}

// ✅ 항상 같은 결과
@Test
fun repeatable() {
    coEvery { api.fetchData() } returns testData
    val result = service.getData()
    assertEquals(expected, result)
}
```

### Self-validating (자동 검증)

```kotlin
// ❌ 수동 확인 필요
@Test
fun manual() {
    val result = calculate()
    println(result)  // 눈으로 확인해야 함
}

// ✅ 자동 검증
@Test
fun automatic() {
    val result = calculate()
    assertEquals(42, result)  // Pass/Fail 명확
}
```

### Timely (적시에)

```
TDD: 테스트 먼저 → 코드 작성
일반: 코드와 함께 테스트 작성
나쁨: 나중에 테스트 작성 (안 하게 됨)
```

### 면접 답변

> "F.I.R.S.T는 좋은 테스트의 5가지 원칙입니다. Fast는 빠른 실행, Independent는 테스트 간 독립성, Repeatable은 환경과 무관한 일관된 결과, Self-validating은 자동화된 검증, Timely는 적절한 시점의 작성을 의미합니다."

---

## 면접 예상 질문 총정리

| 질문 | 핵심 키워드 |
|------|------------|
| Main Dispatcher를 왜 교체하나요? | JVM에 Main Looper 없음 |
| runTest에서 delay가 빠른 이유? | Virtual Time |
| Mock과 Stub의 차이? | 행위 검증 vs 상태 검증 |
| Turbine은 왜 쓰나요? | Flow emit 순서 검증 |
| sealed class로 상태 정의하는 이유? | 불가능한 상태 조합 방지 |
| Semantics Tree란? | Compose의 의미론적 구조, 접근성=테스트 |
| Stateless Composable의 장점? | 테스트 용이, State Hoisting |
| Instrumented Test에서 백틱이 안 되는 이유? | DEX 포맷 제한 |
| coEvery vs every? | suspend 함수는 co- 접두사 |
| F.I.R.S.T 원칙? | Fast, Independent, Repeatable, Self-validating, Timely |
