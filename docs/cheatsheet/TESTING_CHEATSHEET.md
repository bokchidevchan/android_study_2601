# Android Testing 치트시트

> 빠른 참조용 한 장 요약

---

## 1. AAA 패턴 (테스트 기본 구조)

```kotlin
@Test
fun `테스트_이름`() {
    // Arrange (준비)
    val input = "test@example.com"
    
    // Act (실행)
    val result = validator.validate(input)
    
    // Assert (검증)
    assertTrue(result.isValid)
}
```

---

## 2. JUnit 생명주기

```
@BeforeClass ──┐
               │ (클래스당 1번)
               ▼
     ┌─► @Before
     │       │
     │       ▼
     │    @Test
     │       │
     │       ▼
     │   @After
     │       │
     └───────┘ (각 테스트마다 반복)
               │
               ▼
@AfterClass ──┘ (클래스당 1번)
```

---

## 3. MockK 빠른 참조

### Mock 생성

```kotlin
val mock = mockk<Repository>()           // 일반 Mock
val relaxed = mockk<Logger>(relaxed = true)  // 기본값 자동
val spy = spyk(realObject)               // 부분 모킹
```

### Stubbing

```kotlin
every { mock.get(any()) } returns "value"
every { mock.get("key") } returns "specific"
every { mock.get(match { it.startsWith("a") }) } returns "starts with a"
coEvery { mock.fetchAsync() } returns data    // suspend 함수
```

### 검증

```kotlin
verify { mock.get("key") }
verify(exactly = 2) { mock.get(any()) }
verify(atLeast = 1, atMost = 3) { mock.get(any()) }
coVerify { mock.fetchAsync() }            // suspend 함수
verifyOrder { mock.first(); mock.second() }
```

### 인자 캡처

```kotlin
val slot = slot<User>()
every { mock.save(capture(slot)) } returns Unit

// 테스트 실행 후
assertEquals("John", slot.captured.name)
```

---

## 4. Coroutine 테스트

### 기본 구조

```kotlin
@Test
fun `코루틴 테스트`() = runTest {
    // delay가 실제로 기다리지 않음!
    delay(10_000)
    
    // 시간 제어
    advanceTimeBy(1000)
    advanceUntilIdle()
}
```

### Main Dispatcher 교체

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

### Turbine (Flow 테스트)

```kotlin
flow.test {
    assertEquals(expected, awaitItem())  // 다음 값
    awaitComplete()                       // 완료
    awaitError()                          // 에러
    cancelAndIgnoreRemainingEvents()      // 종료
}
```

---

## 5. Compose UI 테스트

### 기본 구조

```kotlin
@get:Rule
val rule = createComposeRule()

@Test
fun test() {
    rule.setContent { MyComposable() }
    
    rule.onNodeWithText("Hello").assertIsDisplayed()
}
```

### Finder (노드 찾기)

| 함수 | 설명 |
|-----|------|
| `onNodeWithText("text")` | 텍스트로 찾기 |
| `onNodeWithTag("tag")` | testTag로 찾기 |
| `onNodeWithContentDescription("desc")` | 접근성 설명으로 |
| `onAllNodes(matcher)` | 여러 노드 찾기 |

### Action (동작)

| 함수 | 설명 |
|-----|------|
| `performClick()` | 클릭 |
| `performTextInput("text")` | 텍스트 입력 |
| `performTextClearance()` | 텍스트 지우기 |
| `performScrollTo()` | 스크롤 |

### Assertion (검증)

| 함수 | 설명 |
|-----|------|
| `assertIsDisplayed()` | 표시됨 |
| `assertDoesNotExist()` | 존재하지 않음 |
| `assertIsEnabled()` | 활성화됨 |
| `assertTextEquals("text")` | 텍스트 일치 |

---

## 6. 테스트 더블 선택 가이드

```
┌─────────────────────────────────────────────┐
│            어떤 테스트 더블을 사용할까?        │
├─────────────────────────────────────────────┤
│                                             │
│  호출 여부를 검증하고 싶다 ───────► Mock     │
│                                             │
│  특정 값을 반환하고 싶다 ────────► Stub      │
│                                             │
│  일부만 가짜로 하고 싶다 ────────► Spy       │
│                                             │
│  상태 기반 테스트가 필요하다 ────► Fake      │
│                                             │
└─────────────────────────────────────────────┘
```

---

## 7. 명명 규칙

### Unit Test (JVM)

```kotlin
// 백틱 + 한글 OK
fun `빈_이메일은_Invalid`() { }
fun `덧셈 - 양수 + 양수`() { }
```

### Instrumented Test (Android)

```kotlin
// 언더스코어만 (DEX 제한)
fun finder_onNodeWithText_findsNode() { }
fun scenario_loginFlow_success() { }
```

---

## 8. 실행 명령어

```bash
# Unit Test
./gradlew test

# 특정 테스트
./gradlew test --tests "*.CalculatorTest"

# Instrumented Test (기기 필요)
./gradlew connectedAndroidTest
```

---

## 9. 자주 하는 실수

| 실수 | 해결 |
|-----|------|
| Main Dispatcher 미교체 | `Dispatchers.setMain()` 추가 |
| suspend 함수에 `every` 사용 | `coEvery` 사용 |
| `advanceUntilIdle()` 누락 | 코루틴 완료 대기 추가 |
| Compose에서 상태 변경 후 검증 | `waitForIdle()` 호출 |
| Mock 정리 안 함 | `@After`에 `unmockkAll()` |

---

## 10. 핵심 원칙

```
┌────────────────────────────────────────────┐
│  F.I.R.S.T 원칙                            │
├────────────────────────────────────────────┤
│  Fast    - 빠르게 실행                      │
│  Independent - 테스트 간 독립적             │
│  Repeatable - 같은 결과 반복                │
│  Self-validating - 자동 검증               │
│  Timely  - 적시에 작성                      │
└────────────────────────────────────────────┘
```

---

*이 치트시트를 프린트하거나 IDE 옆에 두고 참고하세요!*
