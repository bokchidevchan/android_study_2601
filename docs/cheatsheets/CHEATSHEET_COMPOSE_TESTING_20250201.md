# Compose UI Testing 치트시트

> 최근 대화 기반 핵심 정리 | 2025-02-01

---

## 1. 핵심 개념

| 개념 | 설명 | 면접 포인트 |
|------|------|------------|
| **Semantics Tree** | UI의 의미론적 구조 (View Hierarchy 대체) | 접근성 서비스와 테스트가 공유하는 추상화 |
| **Merged Tree** | 사용자가 인지하는 구조 (기본) | Button + Text → "확인 버튼" |
| **Unmerged Tree** | 구현 세부사항 | 레이아웃 측정, 내부 요소 접근 시 사용 |
| **ComposeTestRule** | 테스트 진입점, 동기화 관리 | `createComposeRule()` vs `createAndroidComposeRule()` |
| **State Hoisting** | ViewModel 분리, 상태/콜백 전달 | 테스트 용이성의 핵심 |

---

## 2. Finder 우선순위 (중요!)

```
1순위: onNodeWithText()              ← 텍스트 있으면 이것
2순위: onNodeWithContentDescription() ← 아이콘/이미지
3순위: onNodeWithTag()               ← 위 둘로 안 될 때만!
```

### 왜 이 순서인가?

| Finder | 접근성 | 사용자 관점 | 추천 |
|--------|--------|------------|------|
| `onNodeWithText` | ✅ | ✅ | 🟢 권장 |
| `onNodeWithContentDescription` | ✅ | ✅ | 🟢 권장 |
| `onNodeWithTag` | ❌ | ❌ | 🟡 최후수단 |

**면접 답변**: "testTag는 코드 오염입니다. Semantic 속성으로 테스트하면 접근성도 좋아지고 리팩토링에도 강해집니다."

---

## 3. 코드 스니펫

### 기본 패턴: Finder → Action → Assertion

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun login_button_works() {
    composeTestRule.setContent {
        LoginScreen(state = testState, onEvent = {})
    }
    
    // Finder → Action
    composeTestRule.onNodeWithText("로그인").performClick()
    
    // Assertion
    composeTestRule.onNodeWithText("환영합니다").assertIsDisplayed()
}
```

### 상태 변경 테스트

```kotlin
@Test
fun counter_increments() {
    composeTestRule.setContent { CounterScreen() }
    
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
    composeTestRule.onNodeWithText("+").performClick()
    composeTestRule.waitForIdle()  // Recomposition 대기
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
}
```

### 폼 유효성 검사 테스트

```kotlin
@Test
fun form_validation() {
    composeTestRule.setContent { LoginForm() }
    
    // 초기: 버튼 비활성화
    composeTestRule.onNodeWithText("제출").assertIsNotEnabled()
    
    // 입력 후 활성화
    composeTestRule.onNodeWithText("이메일").performTextInput("test@test.com")
    composeTestRule.onNodeWithText("비밀번호").performTextInput("password123")
    composeTestRule.waitForIdle()
    
    composeTestRule.onNodeWithText("제출").assertIsEnabled()
}
```

### 디버깅: printToLog

```kotlin
@Test
fun debug_semantics_tree() {
    composeTestRule.setContent { MyScreen() }
    
    // Merged Tree (사용자 관점)
    composeTestRule.onRoot().printToLog("MERGED")
    
    // Unmerged Tree (구현 세부사항)
    composeTestRule.onRoot(useUnmergedTree = true).printToLog("UNMERGED")
}
```

---

## 4. ❌ 안티패턴 vs ✅ 베스트 프랙티스

### Thread.sleep() 금지

```kotlin
// ❌ BAD
composeTestRule.onNodeWithText("+").performClick()
Thread.sleep(1000)  // 절대 금지!

// ✅ GOOD
composeTestRule.onNodeWithText("+").performClick()
composeTestRule.waitForIdle()  // Compose 동기화 사용
```

### ViewModel 직접 전달 금지

```kotlin
// ❌ BAD: 테스트하기 어려움
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) { ... }

// ✅ GOOD: State Hoisting
@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit
) { ... }

// 테스트가 간단해짐
composeTestRule.setContent {
    ProfileScreen(
        state = ProfileState(name = "홍길동"),
        onEvent = {}
    )
}
```

### testTag 남용 금지

```kotlin
// ❌ BAD: 모든 곳에 testTag
Button(modifier = Modifier.testTag("submit_btn")) {
    Text("제출", modifier = Modifier.testTag("submit_text"))
}

// ✅ GOOD: Semantic 속성 활용
Button(onClick = {}) {
    Text("제출")  // onNodeWithText("제출")로 찾기
}
```

---

## 5. 비교표: Finder 선택 가이드

| 상황 | 사용할 Finder | 예시 |
|------|--------------|------|
| 텍스트가 보이는 버튼 | `onNodeWithText` | `onNodeWithText("로그인")` |
| 아이콘 버튼 | `onNodeWithContentDescription` | `onNodeWithContentDescription("검색")` |
| 동적 콘텐츠 (숫자 등) | `onNodeWithTag` | `onNodeWithTag("count_text")` |
| 여러 같은 텍스트 | `onAllNodesWithText` | `onAllNodesWithText("아이템")[0]` |
| 복합 조건 | `onNode` + matcher | `onNode(hasText("A") and isEnabled())` |

---

## 6. 동기화 패턴

| 상황 | 방법 |
|------|------|
| Recomposition 대기 | `waitForIdle()` |
| 특정 조건 대기 | `waitUntil { condition }` |
| 노드 존재 대기 | `waitUntilAtLeastOneExists(hasText("완료"))` |
| 애니메이션 제어 | `mainClock.autoAdvance = false` |

```kotlin
// 조건부 대기
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onAllNodesWithText("로딩 완료")
        .fetchSemanticsNodes().isNotEmpty()
}
```

---

## 7. 면접 Q&A

| 질문 | 핵심 답변 |
|------|----------|
| **Semantics Tree란?** | View Hierarchy 대신 사용하는 의미론적 구조. 접근성 서비스와 테스트가 공유 |
| **testTag vs semantic 속성?** | testTag는 코드 오염. semantic 속성은 접근성도 개선하고 리팩토링에 강함 |
| **Merged vs Unmerged Tree?** | Merged는 사용자 관점(기본), Unmerged는 구현 세부사항. 레이아웃 측정 시만 Unmerged |
| **Thread.sleep() 왜 안 쓰나?** | Flaky 테스트 원인. waitForIdle()로 Compose 동기화 사용 |
| **테스트 가능한 Composable 설계?** | State Hoisting - ViewModel 직접 전달 대신 state/callback 분리 |
| **애니메이션 테스트 방법?** | mainClock.autoAdvance = false → advanceTimeBy()로 시간 제어 |

---

## 8. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| Thread.sleep() 사용 | `waitForIdle()` 또는 `waitUntil()` 사용 |
| ViewModel 직접 전달 | State Hoisting으로 분리 |
| testTag 남용 | semantic 속성 우선, testTag는 최후수단 |
| Merged Tree 혼동 | 기본은 Merged, 레이아웃 측정만 Unmerged |
| 동기화 누락 | `performClick()` 후 `waitForIdle()` |
| 디버깅 못함 | `onRoot().printToLog("TAG")` 활용 |

---

## 9. 빠른 참조

### Finder

```kotlin
onNodeWithText("텍스트", substring = true, ignoreCase = true)
onNodeWithContentDescription("설명")
onNodeWithTag("태그")
onNode(hasText("A") and hasClickAction())
onAllNodesWithText("아이템")
onRoot(useUnmergedTree = true)
```

### Assertion

```kotlin
assertExists() / assertDoesNotExist()
assertIsDisplayed() / assertIsNotDisplayed()
assertIsEnabled() / assertIsNotEnabled()
assertIsSelected() / assertIsNotSelected()
assertTextEquals("예상 텍스트")
assertHasClickAction()
```

### Action

```kotlin
performClick()
performTextInput("텍스트")
performTextClearance()
performScrollToIndex(10)
performTouchInput { swipeUp() }
```

### 동기화

```kotlin
waitForIdle()
waitUntil(5000) { condition }
waitUntilAtLeastOneExists(hasText("완료"))
mainClock.autoAdvance = false
mainClock.advanceTimeBy(1000)
```

---

## 10. 시니어 개발자 차별화 포인트

```
┌────────────────────────────────────────────────────────────┐
│  🎯 면접에서 차별화되는 답변                                │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  "Compose 테스트가 Semantics Tree를 쓰는 이유는            │
│   접근성 서비스와 동일한 추상화를 사용하기 때문입니다.       │
│   그래서 접근성이 좋은 UI = 테스트하기 좋은 UI입니다."      │
│                                                             │
│  "testTag보다 semantic 속성을 우선하는 이유는               │
│   1) 접근성 개선 2) 리팩토링 내성 3) 코드 오염 방지입니다." │
│                                                             │
│  "테스트 가능한 Composable 설계의 핵심은 State Hoisting.   │
│   ViewModel을 직접 전달하지 않고 state/callback을 분리하면  │
│   테스트, Preview, 재사용성 모두 좋아집니다."               │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

---

## 관련 파일

- `app/src/androidTest/.../compose/ComposeTestingGuide.kt` - 심화 예제
- `app/src/androidTest/.../compose/ComposeTestAntiPatterns.kt` - 안티패턴 예제
- `app/src/androidTest/.../compose/AccessibilityTest.kt` - 접근성 기반 테스트

---

*Generated from conversation on 2025-02-01*
