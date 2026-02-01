# Android Testing & Hilt 노트

> 손으로 필기하기 좋은 핵심 정리

---

## Part 1: 테스트 기초

### 테스트 피라미드

```
      E2E (10%) ← 느림, 비쌈
     Integration (20%)
    Unit Test (70%) ← 빠름, 안정
```

### AAA 패턴

```
Arrange → 준비 (테스트 데이터 세팅)
Act     → 실행 (테스트 대상 호출)
Assert  → 검증 (결과 확인)
```

### JUnit 생명주기

```
@BeforeClass  (클래스당 1번)
    ↓
@Before       ─┐
@Test          │ 각 테스트마다 반복
@After        ─┘
    ↓
@AfterClass   (클래스당 1번)
```

---

## Part 2: MockK

### Mock 생성

```kotlin
mockk<Type>()              // 일반 Mock
mockk<Type>(relaxed=true)  // 기본값 자동 반환
spyk(realObject)           // 부분 모킹
```

### Stubbing

```kotlin
every { mock.fun() } returns value     // 일반
coEvery { mock.suspend() } returns val // suspend용
```

### 검증

```kotlin
verify { mock.fun() }                  // 호출됨?
verify(exactly=2) { mock.fun() }       // 정확히 2번?
coVerify { mock.suspend() }            // suspend용
```

### 핵심: Mock vs Stub

| Mock | Stub |
|------|------|
| 행위 검증 | 상태 검증 |
| "호출됐나?" | "결과가 맞나?" |
| verify { } | every { } returns |

---

## Part 3: Coroutine 테스트

### Main Dispatcher 교체 (필수!)

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

**왜?** → JVM에 Android Main Looper 없음

### runTest + Virtual Time

```kotlin
@Test
fun test() = runTest {
    delay(10_000)  // 실제로 안 기다림!
    // Virtual Time: 시계만 전진
}
```

### 시간 제어

```kotlin
advanceTimeBy(1000)   // 1초 전진
advanceUntilIdle()    // 모든 코루틴 완료
currentTime           // 현재 가상 시간
```

### StandardTest vs Unconfined

| Standard (권장) | Unconfined |
|----------------|------------|
| advanceUntilIdle() 필요 | 즉시 실행 |
| 시간 제어 가능 | 시간 제어 불가 |

---

## Part 4: Turbine (Flow 테스트)

```kotlin
flow.test {
    awaitItem()      // 다음 값 대기
    awaitComplete()  // 완료 대기
    awaitError()     // 에러 대기
    cancelAndIgnoreRemainingEvents()
}
```

### 실전 패턴

```kotlin
viewModel.uiState.test {
    assertEquals(Idle, awaitItem())     // 초기
    viewModel.load()
    assertEquals(Loading, awaitItem())  // 로딩
    advanceUntilIdle()
    assertTrue(awaitItem() is Success)  // 성공
    cancelAndIgnoreRemainingEvents()
}
```

**왜 Turbine?** → Flow의 상태 변화 **순서** 검증

---

## Part 5: Compose UI 테스트

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

### Finder → Action → Assertion

```kotlin
rule.onNodeWithText("버튼")   // Finder
    .performClick()           // Action
    .assertIsDisplayed()      // Assertion
```

### Finder 우선순위

```
1순위: onNodeWithText("텍스트")
2순위: onNodeWithContentDescription("설명")
3순위: onNodeWithTag("태그")  ← 마지막 수단
```

### Semantics Tree

- Compose는 별도의 의미론적 트리 관리
- 테스트 = 접근성 (같은 트리 사용)
- 접근성 좋은 UI = 테스트 쉬운 UI

---

## Part 6: Stateless Composable

### Humble Object Pattern

```kotlin
// ❌ 테스트 어려움
@Composable
fun Screen(viewModel: VM) {
    val state by viewModel.state.collectAsState()
    Text(state.value)
}

// ✅ 테스트 쉬움
@Composable
fun Content(value: Int, onClick: () -> Unit) {
    Text(value.toString())
    Button(onClick = onClick) { Text("+") }
}
```

| Stateful | Stateless |
|----------|-----------|
| ViewModel 연결 | 데이터+콜백만 |
| 테스트 어려움 | 테스트 쉬움 |

---

## Part 7: Dagger Hilt

### 핵심 질문

> "그냥 생성자에 Fake 넣으면 되잖아?"

**Unit Test**: ✅ 맞음. Hilt 불필요

```kotlin
val viewModel = MyViewModel(FakeRepo())
```

**Integration Test**: ❌ Hilt 필요

```kotlin
launchActivity<MainActivity>()
// → 시스템이 Activity 생성
// → ViewModel, Repository 자동 생성
// → 우리가 끼어들 수 없음!
```

### Hilt의 해결책

```kotlin
@HiltAndroidTest
class IntegrationTest {

    @BindValue
    val fakeRepo: UserRepository = FakeUserRepository()
    
    @Test
    fun test() {
        // fakeRepo가 앱 전체에 자동 주입!
        launchActivity<MainActivity>()
    }
}
```

### Hilt 테스트 어노테이션

| 어노테이션 | 용도 |
|-----------|------|
| @HiltAndroidTest | 통합 테스트 클래스 표시 |
| @TestInstallIn | 프로덕션 모듈 → 테스트 모듈 교체 (전역) |
| @UninstallModules | 특정 모듈 비활성화 |
| @BindValue | 특정 의존성만 교체 (개별 테스트) |

### Unit vs Integration

| 구분 | Unit Test | Integration Test |
|------|-----------|------------------|
| 위치 | test/ | androidTest/ |
| 대상 | 클래스 하나 | Activity→VM→Repo 전체 |
| 객체 생성 | 개발자가 직접 | 시스템이 생성 |
| Hilt 필요 | ❌ | ✅ |

---

## Part 8: 자주 하는 실수

| 실수 | 해결 |
|------|------|
| Main Dispatcher 미교체 | Dispatchers.setMain() |
| suspend에 every 사용 | coEvery 사용 |
| advanceUntilIdle() 누락 | 코루틴 완료 대기 |
| Mock 정리 안 함 | @After에 unmockkAll() |
| androidTest에 백틱 메서드 | 언더스코어로 변경 (DEX 제한) |

---

## Part 9: 면접 원라이너

| 질문 | 한 줄 답변 |
|------|-----------|
| Main Dispatcher 교체 이유? | JVM에 Main Looper 없음 |
| runTest의 delay가 빠른 이유? | Virtual Time 사용 |
| Mock vs Stub? | Mock=행위검증, Stub=상태검증 |
| Turbine 왜 씀? | Flow emit 순서 검증 |
| sealed class 장점? | 불가능한 상태 조합 방지 |
| coEvery vs every? | suspend는 co 접두사 |
| Hilt가 테스트에 필요한 이유? | Integration Test에서 Fake 주입 |

---

## F.I.R.S.T 원칙

```
F - Fast        빠르게
I - Independent 독립적
R - Repeatable  반복 가능
S - Self-valid  자동 검증
T - Timely      적시에 작성
```

---

*이 노트를 손으로 옮겨 적으면서 외우세요!*
