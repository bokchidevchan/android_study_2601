# Android Testing 면접 치트시트

> 면접에서 자주 나오는 개념들 한 장 정리

---

## 1. Main Dispatcher 교체

```kotlin
@Before
fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
}

@After
fun tearDown() {
    Dispatchers.resetMain()
}
```

| 질문 | 답변 |
|------|------|
| **왜 교체하나요?** | `viewModelScope`가 `Dispatchers.Main` 사용하는데, JVM에는 Android Main Looper가 없음 |
| **안 하면?** | `IllegalStateException: Module with the Main dispatcher had failed to initialize` |

### StandardTestDispatcher vs UnconfinedTestDispatcher

| 종류 | 특징 | 언제 사용? |
|------|------|-----------|
| **Standard** | `advanceUntilIdle()` 호출해야 실행 | 시간 제어 필요할 때 (권장) |
| **Unconfined** | 즉시 실행 | 간단한 테스트, 순서 상관없을 때 |

---

## 2. Virtual Time (가상 시간)

```kotlin
@Test
fun test() = runTest {
    delay(10_000)  // 실제로 10초 안 기다림!
    
    assertEquals(10_000, currentTime)  // 가상 시간은 10초 경과
}
```

| 함수 | 역할 |
|------|------|
| `advanceTimeBy(ms)` | 지정 시간만큼 전진 |
| `advanceUntilIdle()` | 모든 코루틴 완료까지 전진 |
| `currentTime` | 현재 가상 시간 확인 |

> **면접 포인트**: "테스트가 빠른 이유?" → Virtual Time Clock 사용

---

## 3. 테스트 더블 (Test Double)

```
┌─────────────────────────────────────────────────────────┐
│                    테스트 더블 선택                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  "이 메서드가 호출되었나?" ──────────────► Mock          │
│                                                          │
│  "호출하면 이 값을 반환해라" ─────────────► Stub         │
│                                                          │
│  "대부분 실제, 일부만 가짜" ──────────────► Spy          │
│                                                          │
│  "상태를 가진 간이 버전" ─────────────────► Fake         │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Mock vs Stub 차이 (면접 단골!)

| 구분 | Mock | Stub |
|------|------|------|
| **목적** | 행위 검증 | 상태 검증 |
| **질문** | "호출되었나?" | "결과가 맞나?" |
| **MockK** | `verify { }` | `every { } returns` |

```kotlin
// Stub: 반환값 설정
every { repo.getUser("1") } returns User("홍길동")

// Mock: 호출 검증
verify(exactly = 1) { repo.getUser("1") }
```

---

## 4. MockK 핵심

### 일반 함수 vs suspend 함수

```kotlin
// 일반 함수
every { mock.normalFun() } returns value
verify { mock.normalFun() }

// suspend 함수 (co 붙임!)
coEvery { mock.suspendFun() } returns value
coVerify { mock.suspendFun() }
```

> **실수 주의**: suspend 함수에 `every` 쓰면 stub 안 됨!

### relaxedMockK

```kotlin
val logger = mockk<Logger>(relaxed = true)
// stub 없이도 기본값 반환 (0, null, false, 빈 리스트)
```

### 인자 캡처

```kotlin
val slot = slot<User>()
every { repo.save(capture(slot)) } returns Unit

// 테스트 실행 후
assertEquals("홍길동", slot.captured.name)
```

---

## 5. Turbine (Flow 테스트)

```kotlin
viewModel.uiState.test {
    // 1. 초기 상태
    assertEquals(UiState.Idle, awaitItem())
    
    // 2. 액션 실행
    viewModel.loadArticles()
    
    // 3. 로딩 상태
    assertEquals(UiState.Loading, awaitItem())
    
    // 4. 코루틴 완료 대기
    advanceUntilIdle()
    
    // 5. 최종 상태
    assertTrue(awaitItem() is UiState.Success)
    
    // 6. 종료
    cancelAndIgnoreRemainingEvents()
}
```

| 함수 | 역할 |
|------|------|
| `awaitItem()` | 다음 emit 값 대기 |
| `awaitComplete()` | Flow 완료 대기 |
| `awaitError()` | 에러 대기 |
| `cancelAndIgnoreRemainingEvents()` | 조기 종료 |

> **면접 포인트**: "왜 Turbine?" → Flow의 **상태 변화 순서**를 검증하기 위해

---

## 6. sealed class UI 상태

```kotlin
sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data object Empty : UiState()
    data class Success(val data: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}
```

| 장점 | 설명 |
|------|------|
| **컴파일 타임 안전** | when에서 else 불필요 (모든 케이스 강제) |
| **상태 전이 명확** | Idle → Loading → Success/Error |
| **불변성** | data class로 immutable 보장 |

### data object vs data class (Kotlin 1.9+)

```kotlin
data object Loading : UiState()     // 싱글톤, 파라미터 없음
data class Error(val msg: String)   // 인스턴스, 파라미터 있음
```

---

## 7. Compose UI 테스트

### Semantics Tree란?

```
View Hierarchy (전통적)     vs     Semantics Tree (Compose)
     ViewGroup                        의미론적 구조
        │                             접근성 + 테스트용
     TextView
```

### 기본 패턴: Finder → Action → Assertion

```kotlin
composeTestRule
    .onNodeWithTag("button")      // Finder
    .performClick()               // Action
    .assertIsDisplayed()          // Assertion
```

### Finder 종류

| 함수 | 용도 |
|------|------|
| `onNodeWithText("텍스트")` | 텍스트로 찾기 (권장) |
| `onNodeWithTag("태그")` | testTag로 찾기 |
| `onNodeWithContentDescription("설명")` | 접근성 설명으로 |
| `onAllNodes(matcher)` | 여러 노드 |

### Action 종류

| 함수 | 용도 |
|------|------|
| `performClick()` | 클릭 |
| `performTextInput("text")` | 텍스트 입력 |
| `performTextClearance()` | 텍스트 삭제 |
| `performScrollTo()` | 스크롤 |

### 동기화

```kotlin
composeTestRule.waitForIdle()  // Recomposition 완료 대기
```

> **면접 포인트**: "왜 `onNodeWithText`가 좋은가?" → 접근성 고려 = 좋은 테스트

---

## 8. Stateless Composable (Humble Object)

```kotlin
// ❌ 테스트 어려움 - ViewModel 직접 사용
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    val state by viewModel.uiState.collectAsState()
    Text(text = state.count.toString())
}

// ✅ 테스트 쉬움 - 데이터와 콜백만 받음
@Composable
fun CounterContent(
    count: Int,
    onIncrement: () -> Unit
) {
    Text(text = count.toString())
    Button(onClick = onIncrement) { Text("+1") }
}
```

| 분리 | 테스트 방법 |
|------|------------|
| **ViewModel** | Unit Test (JVM) |
| **Stateless Composable** | UI Test (Compose Rule) |

> State Hoisting과 같은 개념

---

## 9. DEX 제한 - 메서드명 규칙

```kotlin
// Unit Test (src/test/) - JVM 실행
fun `빈_이메일은_Invalid`() { }     // ✅ 백틱 + 한글 OK

// Instrumented Test (src/androidTest/) - 기기 실행
fun finder_onNodeWithText_test() { }  // ✅ 언더스코어만
fun `한글 메서드명`() { }              // ❌ DEX 에러!
```

> **면접 포인트**: "왜 테스트 메서드명 스타일이 다른가?" → DEX 버전 제한

---

## 10. F.I.R.S.T 원칙

```
┌────────────────────────────────────────────┐
│  F - Fast          빠르게 실행              │
│  I - Independent   테스트 간 독립적          │
│  R - Repeatable    같은 결과 반복            │
│  S - Self-validating  자동 검증 (pass/fail) │
│  T - Timely        적시에 작성              │
└────────────────────────────────────────────┘
```

---

## 11. AAA 패턴

```kotlin
@Test
fun `이메일_검증_테스트`() {
    // Arrange (준비)
    val email = "test@example.com"
    
    // Act (실행)
    val result = validator.validate(email)
    
    // Assert (검증)
    assertTrue(result.isValid)
}
```

---

## 12. 면접 예상 Q&A

| 질문 | 핵심 답변 |
|------|----------|
| ViewModel 테스트 시 Main Dispatcher를 왜 교체? | JVM에 Android Main Looper가 없어서 |
| Mock과 Stub의 차이는? | Mock=행위검증, Stub=상태검증 |
| runTest에서 delay가 빠른 이유? | Virtual Time 사용 |
| Compose 테스트의 Semantics Tree란? | UI의 의미론적 구조, 접근성+테스트용 |
| Turbine을 왜 사용? | Flow의 상태 변화 순서를 검증하기 위해 |
| sealed class로 상태 정의하는 이유? | 컴파일 타임 안전, when에서 모든 케이스 강제 |
| coEvery vs every 차이? | suspend 함수는 coEvery 사용 |
| Stateless Composable의 장점? | 테스트 용이, State Hoisting |

---

## 13. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| Main Dispatcher 미교체 | `Dispatchers.setMain()` 추가 |
| suspend 함수에 `every` 사용 | `coEvery` 사용 |
| `advanceUntilIdle()` 누락 | 코루틴 완료 대기 추가 |
| Compose 상태 변경 후 바로 검증 | `waitForIdle()` 호출 |
| Mock 정리 안 함 | `@After`에 `unmockkAll()` |
| Instrumented Test에 백틱 메서드명 | 언더스코어 형식으로 변경 |

---

*면접 전에 한 번 쭉 읽어보세요!*
