package io.github.bokchidevchan.android_study_2601.study.testing.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * ========================================================================
 * 📚 Compose UI Testing 심화 가이드
 * ========================================================================
 *
 * 🎯 학습 목표:
 * 1. Semantics Tree 이해 - View Hierarchy와 다른 점
 * 2. Finder 우선순위 - testTag vs Semantic 속성
 * 3. 테스트 가능한 Composable 설계
 * 4. 주의사항 및 안티패턴
 * 5. 면접 어필 포인트
 *
 * ========================================================================
 * 핵심 개념: Semantics Tree
 * ========================================================================
 *
 * Q: 왜 Compose는 View Hierarchy 대신 Semantics Tree를 사용하나?
 *
 * A: Compose는 선언적이라 모든 Composable이 실제 UI를 생성하지 않음.
 *    Semantics Tree는:
 *    - 접근성 서비스가 사용하는 구조
 *    - 테스트가 사용하는 구조
 *    → "접근성이 좋은 UI = 테스트하기 좋은 UI"
 *
 * ┌─────────────────────────────────────────────────────┐
 * │  Merged Tree (기본)     vs    Unmerged Tree         │
 * ├─────────────────────────────────────────────────────┤
 * │  사용자가 인지하는 구조       구현 세부사항           │
 * │  Button                      Button                 │
 * │  └─ "Like"                   ├─ Icon                │
 * │                              └─ Text("Like")        │
 * └─────────────────────────────────────────────────────┘
 *
 * ========================================================================
 * Finder 우선순위 (면접 포인트!)
 * ========================================================================
 *
 * 1순위: onNodeWithText()           - 사용자가 보는 텍스트
 * 2순위: onNodeWithContentDescription() - 아이콘/이미지
 * 3순위: onNodeWithTag()            - 위 둘로 못 찾을 때만!
 *
 * 왜? → testTag는 "코드 오염", Semantic 속성은 접근성도 개선
 *
 * ========================================================================
 */
class ComposeTestingGuide {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========================================================================
    // Section 1: 기본 테스트 패턴 - Finder → Action → Assertion
    // ========================================================================

    @Test
    fun basic_pattern_finder_action_assertion() {
        var clicked = false

        composeTestRule.setContent {
            MaterialTheme {
                Button(onClick = { clicked = true }) {
                    Text("제출")
                }
            }
        }

        // 패턴: Finder → Action → Assertion
        composeTestRule
            .onNodeWithText("제출")      // 1. Finder
            .performClick()              // 2. Action

        assertTrue(clicked)              // 3. Assertion
    }

    // ========================================================================
    // Section 2: Finder 종류 비교
    // ========================================================================

    @Test
    fun finder_comparison_semantic_vs_testTag() {
        composeTestRule.setContent {
            MaterialTheme {
                Column {
                    // ✅ Good: Semantic 속성 있음 (접근성 + 테스트)
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "검색"  // ← 접근성!
                        )
                    }

                    // ❌ Bad: testTag만 있음 (접근성 없음)
                    IconButton(
                        onClick = {},
                        modifier = Modifier.testTag("delete_btn")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null  // ← 접근성 없음!
                        )
                    }
                }
            }
        }

        // ✅ 권장: Semantic 기반 테스트
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertIsDisplayed()
            .assertHasClickAction()

        // 🔶 차선: testTag 기반 (위 방법이 안 될 때만)
        composeTestRule
            .onNodeWithTag("delete_btn")
            .assertIsDisplayed()
    }

    @Test
    fun finder_text_options() {
        composeTestRule.setContent {
            MaterialTheme {
                Column {
                    Text("Hello World")
                    Text("hello android")
                    Text("Say Hello to Compose")
                }
            }
        }

        // 정확히 일치
        composeTestRule.onNodeWithText("Hello World").assertIsDisplayed()

        // 부분 일치
        composeTestRule.onNodeWithText("Hello", substring = true).assertIsDisplayed()

        // 대소문자 무시
        composeTestRule.onNodeWithText("HELLO WORLD", ignoreCase = true).assertIsDisplayed()

        // 복합 조건
        composeTestRule.onNode(
            hasText("hello", ignoreCase = true) and hasText("android", substring = true)
        ).assertIsDisplayed()
    }

    // ========================================================================
    // Section 3: 상태 변경 테스트
    // ========================================================================

    @Test
    fun state_change_counter_increment() {
        composeTestRule.setContent {
            MaterialTheme {
                TestableCounter()
            }
        }

        // 초기 상태
        composeTestRule.onNodeWithTag("count_text").assertTextEquals("0")

        // 증가 버튼 클릭
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.waitForIdle()  // Recomposition 대기

        // 상태 변경 확인
        composeTestRule.onNodeWithTag("count_text").assertTextEquals("1")

        // 여러 번 클릭
        repeat(5) {
            composeTestRule.onNodeWithText("+").performClick()
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("count_text").assertTextEquals("6")
    }

    @Test
    fun state_change_form_validation() {
        composeTestRule.setContent {
            MaterialTheme {
                TestableLoginForm()
            }
        }

        // 초기: 버튼 비활성화
        composeTestRule.onNodeWithText("로그인").assertIsNotEnabled()

        // 이메일만 입력
        composeTestRule.onNodeWithText("이메일").performTextInput("test@test.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("로그인").assertIsNotEnabled()

        // 비밀번호도 입력
        composeTestRule.onNodeWithText("비밀번호").performTextInput("password123")
        composeTestRule.waitForIdle()

        // 이제 버튼 활성화
        composeTestRule.onNodeWithText("로그인").assertIsEnabled()
    }

    // ========================================================================
    // Section 4: 리스트 및 스크롤 테스트
    // ========================================================================

    @Test
    fun list_scroll_and_item_verification() {
        val items = (1..50).map { "아이템 $it" }

        composeTestRule.setContent {
            MaterialTheme {
                TestableItemList(items = items)
            }
        }

        // 첫 번째 아이템 확인
        composeTestRule.onNodeWithText("아이템 1").assertIsDisplayed()

        // 스크롤하여 특정 아이템으로 이동
        composeTestRule
            .onNodeWithTag("item_list")
            .performScrollToIndex(30)

        composeTestRule.waitForIdle()

        // 스크롤된 위치의 아이템 확인
        composeTestRule.onNodeWithText("아이템 31").assertIsDisplayed()
    }

    // ========================================================================
    // Section 5: 비동기 작업 테스트
    // ========================================================================

    @Test
    fun async_loading_state_test() {
        composeTestRule.setContent {
            MaterialTheme {
                TestableAsyncScreen(isLoading = true, data = null)
            }
        }

        // 로딩 상태 확인
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("content").assertDoesNotExist()
    }

    @Test
    fun async_loaded_state_test() {
        composeTestRule.setContent {
            MaterialTheme {
                TestableAsyncScreen(isLoading = false, data = "로딩 완료!")
            }
        }

        // 로딩 완료 상태 확인
        composeTestRule.onNodeWithTag("loading_indicator").assertDoesNotExist()
        composeTestRule.onNodeWithText("로딩 완료!").assertIsDisplayed()
    }

    // ========================================================================
    // Section 6: 디버깅 - printToLog 사용
    // ========================================================================

    @Test
    fun debugging_printToLog_example() {
        composeTestRule.setContent {
            MaterialTheme {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("제목", style = MaterialTheme.typography.titleLarge)
                        Text("설명입니다")
                        Button(onClick = {}) { Text("확인") }
                    }
                }
            }
        }

        // Semantics Tree 출력 (Logcat에서 확인)
        composeTestRule.onRoot().printToLog("COMPOSE_DEBUG")

        // Unmerged Tree도 출력 (구현 세부사항)
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("UNMERGED_DEBUG")
    }

    // ========================================================================
    // Section 7: 애니메이션 테스트 (Clock 제어)
    // ========================================================================

    @Test
    fun animation_test_with_clock_control() {
        composeTestRule.mainClock.autoAdvance = false  // 자동 진행 중지

        composeTestRule.setContent {
            MaterialTheme {
                TestableAnimatedContent()
            }
        }

        // 초기 상태
        composeTestRule.onNodeWithText("시작").assertIsDisplayed()

        // 애니메이션 시작
        composeTestRule.onNodeWithText("애니메이션 시작").performClick()

        // 시간을 수동으로 진행
        composeTestRule.mainClock.advanceTimeBy(500)

        // 중간 상태 확인 가능 (애니메이션 도중)
        // ...

        // 완료까지 진행
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }
}

// ============================================================================
// 테스트 대상 Composable들 (테스트 가능한 설계 예시)
// ============================================================================

/**
 * ✅ 테스트 가능한 설계: State Hoisting
 *
 * ViewModel을 직접 받지 않고, state와 callback을 분리
 * → Preview 가능, 테스트 용이, 재사용성 높음
 */
@Composable
fun TestableCounter(
    initialCount: Int = 0
) {
    var count by remember { mutableStateOf(initialCount) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Button(onClick = { count-- }) {
            Text("-")
        }

        Text(
            text = count.toString(),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .testTag("count_text"),  // 동적 텍스트는 testTag 사용
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = { count++ }) {
            Text("+")
        }
    }
}

@Composable
fun TestableLoginForm(
    onSubmit: (email: String, password: String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isValid = email.contains("@") && password.length >= 6

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSubmit(email, password) },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }
    }
}

@Composable
fun TestableItemList(
    items: List<String>,
    onItemClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.testTag("item_list")
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = { onItemClick(item) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, modifier = Modifier.weight(1f))
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "$item 좋아요"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestableAsyncScreen(
    isLoading: Boolean,
    data: String?,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.testTag("loading_indicator")
            )
        } else if (data != null) {
            Text(
                text = data,
                modifier = Modifier.testTag("content")
            )
        } else {
            Text("데이터 없음")
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
fun TestableAnimatedContent() {
    var started by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(if (started) "진행 중..." else "시작")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { started = true }) {
            Text("애니메이션 시작")
        }
    }
}
