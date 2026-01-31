# Android Testing 완벽 가이드

> 이 문서는 프로젝트의 테스트 코드를 학습하기 위한 종합 가이드입니다.

---

## 목차

1. [테스트 피라미드](#1-테스트-피라미드)
2. [프로젝트 구조](#2-프로젝트-구조)
3. [학습 순서](#3-학습-순서)
4. [핵심 패턴 정리](#4-핵심-패턴-정리)
5. [실행 방법](#5-실행-방법)
6. [치트시트](#6-치트시트)

---

## 1. 테스트 피라미드

```
         /\
        /  \         10% - E2E/UI Test (느림, 비용 높음)
       /----\
      /      \       20% - Integration Test
     /--------\
    /          \     70% - Unit Test (빠름, 안정적)
   /--------------\
```

| 레벨 | 특징 | 이 프로젝트에서 |
|------|------|----------------|
| **Unit Test** | 빠름, 격리됨, JVM에서 실행 | `src/test/` |
| **Integration** | 모듈 간 통합, 실제 의존성 | `src/test/` (Hilt) |
| **UI Test** | 에뮬레이터 필요, 느림 | `src/androidTest/` |

---

## 2. 프로젝트 구조

```
app/src/
├── main/java/.../study/testing/      # 테스트 대상 코드
│   ├── basics/
│   │   └── Calculator.kt             # 순수 함수 예제
│   ├── mockk/
│   │   └── UserService.kt            # 의존성 있는 클래스
│   ├── coroutine/
│   │   └── AsyncRepository.kt        # 비동기 코드
│   ├── compose/
│   │   └── CounterScreen.kt          # Compose UI
│   └── tdd/
│       └── EmailValidator.kt         # TDD로 개발된 코드
│
├── test/java/...                     # Unit Tests (JVM)
│   └── testing/
│       ├── basics/CalculatorTest.kt
│       ├── mockk/MockKAdvancedTest.kt
│       ├── coroutine/CoroutineTestExamples.kt
│       └── tdd/EmailValidatorTest.kt
│
└── androidTest/java/...              # Instrumented Tests
    └── testing/
        └── compose/ComposeUITest.kt
```

---

## 3. 학습 순서

### Step 1: JUnit 기초 (`basics/`)

**목표**: 테스트의 기본 구조와 생명주기 이해

```kotlin
// 파일: CalculatorTest.kt

class CalculatorTest {
    
    private lateinit var calculator: Calculator
    
    @Before  // 각 테스트 전에 실행
    fun setUp() {
        calculator = Calculator()
    }
    
    @Test
    fun `덧셈 - 양수 + 양수`() {
        // Arrange (준비)
        val a = 10
        val b = 5
        
        // Act (실행)
        val result = calculator.add(a, b)
        
        // Assert (검증)
        assertEquals(15, result)
    }
}
```

**핵심 개념**:
- `@Before` / `@After`: 각 테스트 전후 실행
- `@BeforeClass` / `@AfterClass`: 클래스당 1번 실행
- AAA 패턴: Arrange → Act → Assert

---

### Step 2: MockK 심화 (`mockk/`)

**목표**: 의존성 Mocking과 행위 검증

```kotlin
// 파일: MockKAdvancedTest.kt

class MockKAdvancedTest {
    
    @MockK
    lateinit var userRepository: UserRepository
    
    @RelaxedMockK  // stub 없이도 기본값 반환
    lateinit var logger: Logger
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }
    
    @Test
    fun `사용자 조회 테스트`() {
        // Stub 설정
        every { userRepository.findById("1") } returns User("1", "홍길동", "", 25)
        
        // 테스트 실행
        val result = userService.getUser("1")
        
        // 검증
        assertEquals("홍길동", result?.name)
        verify { userRepository.findById("1") }
    }
}
```

**핵심 개념**:
- `mockk<T>()`: Mock 객체 생성
- `every { } returns`: Stub 설정
- `coEvery { }`: suspend 함수용
- `verify { }`: 호출 검증
- `slot<T>()` + `capture()`: 인자 캡처

---

### Step 3: Coroutine 테스트 (`coroutine/`)

**목표**: 비동기 코드와 Flow 테스트

```kotlin
// 파일: CoroutineTestExamples.kt

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestExamples {
    
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `runTest - delay가 실제로 기다리지 않음`() = runTest {
        val startTime = currentTime
        
        delay(10_000)  // 10초 - 실제론 즉시!
        
        assertEquals(10_000, currentTime - startTime)
    }
    
    @Test
    fun `Turbine - Flow 테스트`() = runTest {
        val flow = flowOf(1, 2, 3)
        
        flow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }
}
```

**핵심 개념**:
- `runTest { }`: 코루틴 테스트 스코프
- Virtual Time: delay가 실제로 기다리지 않음
- `advanceTimeBy()` / `advanceUntilIdle()`: 시간 제어
- Turbine: `flow.test { awaitItem() }`

---

### Step 4: Compose UI 테스트 (`compose/`)

**목표**: Semantics Tree 기반 UI 테스트

```kotlin
// 파일: ComposeUITest.kt

class ComposeUITest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun finder_onNodeWithTag_findsNodeByTestTag() {
        composeTestRule.setContent {
            CounterContent(count = 5, ...)
        }
        
        // Finder → Assertion
        composeTestRule
            .onNodeWithTag("counter_card")
            .assertIsDisplayed()
    }
    
    @Test
    fun action_performClick_triggersCallback() {
        var count = 0
        
        composeTestRule.setContent {
            CounterContent(
                count = count,
                onIncrement = { count++ },
                ...
            )
        }
        
        // Finder → Action
        composeTestRule
            .onNodeWithTag("increment_button")
            .performClick()
        
        // Assertion
        assert(count == 1)
    }
}
```

**핵심 개념**:
- `createComposeRule()`: Compose 테스트 규칙
- Finder: `onNodeWithText()`, `onNodeWithTag()`
- Action: `performClick()`, `performTextInput()`
- Assertion: `assertIsDisplayed()`, `assertIsEnabled()`

---

### Step 5: TDD 실습 (`tdd/`)

**목표**: Red-Green-Refactor 사이클 체험

```
┌──────────────────────────────────────────────────┐
│                    TDD 사이클                     │
│                                                   │
│    ┌─────┐      ┌─────┐      ┌──────────┐        │
│    │ RED │ ───▶ │GREEN│ ───▶ │ REFACTOR │        │
│    └─────┘      └─────┘      └──────────┘        │
│       │                            │              │
│       └────────────────────────────┘              │
│                                                   │
│  1. 실패하는 테스트 작성                          │
│  2. 테스트 통과하는 최소 코드                     │
│  3. 코드 정리 (테스트는 계속 통과)                │
└──────────────────────────────────────────────────┘
```

```kotlin
// EmailValidatorTest.kt

@Test
fun `빈_이메일은_Invalid`() {
    val result = validator.validate("")
    
    assertFalse(result.isValid)
    assertEquals("이메일을 입력해주세요", 
                 (result as EmailValidationResult.Invalid).reason)
}

@Test
fun `올바른_이메일은_Valid`() {
    val result = validator.validate("test@example.com")
    
    assertTrue(result.isValid)
}
```

---

## 4. 핵심 패턴 정리

### Mock vs Stub vs Spy vs Fake

| 종류 | 설명 | 언제 사용? |
|------|------|-----------|
| **Mock** | 호출 여부/횟수 검증 | "이 메서드가 호출되었는가?" |
| **Stub** | 미리 정의된 응답 반환 | "호출하면 이 값을 반환해라" |
| **Spy** | 실제 객체 + 부분 모킹 | "대부분 실제 동작, 일부만 가짜" |
| **Fake** | 실제 동작하는 간단한 구현 | "상태를 가지고 실제처럼 동작" |

```kotlin
// Mock + Stub
val mockRepo = mockk<UserRepository>()
coEvery { mockRepo.getBalance() } returns 5000  // Stub
coVerify { mockRepo.getBalance() }               // Mock 검증

// Spy
val realRepo = FakeUserRepository()
val spyRepo = spyk(realRepo)
coEvery { spyRepo.getBalance() } returns 99999  // 부분 오버라이드

// Fake
val fakeRepo = FakeUserRepository()
fakeRepo.fakeBalance = 10000  // 상태 직접 설정
```

### Stateless Composable 분리 (Humble Object Pattern)

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

---

## 5. 실행 방법

### Unit Test 실행

```bash
# 전체 Unit Test
./gradlew test

# 특정 테스트만
./gradlew test --tests "*.CalculatorTest"

# 특정 메서드만
./gradlew test --tests "*CalculatorTest.덧셈*"
```

### Instrumented Test 실행

```bash
# 에뮬레이터/기기 필요
./gradlew connectedAndroidTest
```

### Android Studio에서 실행

1. 테스트 파일 열기
2. 클래스명 또는 메서드명 옆 ▶️ 클릭
3. 또는 `Ctrl + Shift + F10` (선택된 테스트 실행)

---

## 6. 치트시트

### JUnit 어노테이션

| 어노테이션 | 설명 |
|-----------|------|
| `@Test` | 테스트 메서드 |
| `@Before` | 각 테스트 전 실행 |
| `@After` | 각 테스트 후 실행 |
| `@BeforeClass` | 클래스당 1번 (static) |
| `@AfterClass` | 클래스당 1번 (static) |
| `@Ignore("이유")` | 테스트 스킵 |
| `@Test(expected = Exception::class)` | 예외 기대 |
| `@Test(timeout = 1000)` | 시간 제한 |

### Assertion 메서드

```kotlin
assertEquals(expected, actual)
assertNotEquals(a, b)
assertTrue(condition)
assertFalse(condition)
assertNull(value)
assertNotNull(value)
assertSame(obj1, obj2)      // 참조 비교
assertThrows(Exception::class.java) { ... }
```

### MockK 함수

```kotlin
// Mock 생성
mockk<T>()
mockk<T>(relaxed = true)
spyk(realObject)

// Stubbing
every { mock.method() } returns value
every { mock.method() } throws Exception()
every { mock.method() } answers { ... }
coEvery { mock.suspendMethod() } returns value

// 검증
verify { mock.method() }
verify(exactly = 1) { mock.method() }
verify(atLeast = 1) { mock.method() }
coVerify { mock.suspendMethod() }
verifyOrder { ... }
verifySequence { ... }

// 캡처
val slot = slot<T>()
every { mock.method(capture(slot)) } returns value
slot.captured  // 캡처된 값
```

### Coroutine Test

```kotlin
@Test
fun test() = runTest {
    // Virtual Time 사용
    delay(1000)  // 실제로 안 기다림
    
    advanceTimeBy(500)      // 시간 전진
    advanceUntilIdle()      // 모든 작업 완료
    
    currentTime             // 경과 시간 확인
}

// Main Dispatcher 교체
Dispatchers.setMain(StandardTestDispatcher())
Dispatchers.resetMain()
```

### Turbine (Flow 테스트)

```kotlin
flow.test {
    awaitItem()           // 다음 아이템 대기
    awaitComplete()       // 완료 대기
    awaitError()          // 에러 대기
    expectNoEvents()      // 이벤트 없음 확인
    cancelAndIgnoreRemainingEvents()  // 조기 종료
}
```

### Compose Test

```kotlin
// Finder
onNodeWithText("텍스트")
onNodeWithTag("태그")
onNodeWithContentDescription("설명")
onAllNodes(hasClickAction())

// Action
performClick()
performTextInput("텍스트")
performTextClearance()
performScrollTo()

// Assertion
assertIsDisplayed()
assertDoesNotExist()
assertIsEnabled()
assertIsNotEnabled()
assertTextEquals("텍스트")

// 동기화
waitForIdle()
```

---

## 언제 테스트를 작성해야 할까?

### ✅ 테스트 필수

- 결제, 인증 등 **핵심 비즈니스 로직**
- 조건 분기가 많은 **복잡한 코드**
- **리팩토링 예정**인 코드
- **버그 수정** 시 (회귀 방지)

### ⚠️ ROI 고려

- 단순 getter/setter
- 프레임워크가 보장하는 기능
- 자주 변경되는 UI 세부사항

> "모든 코드에 테스트를 작성하는 것은 비효율적입니다.
> ROI를 고려하여 핵심 로직에 집중하세요."

---

## 추가 학습 자료

- [Android Testing 공식 문서](https://developer.android.com/training/testing)
- [MockK 공식 문서](https://mockk.io/)
- [Turbine GitHub](https://github.com/cashapp/turbine)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)

---

*이 가이드와 함께 각 테스트 파일의 주석을 읽으며 학습하세요!*
