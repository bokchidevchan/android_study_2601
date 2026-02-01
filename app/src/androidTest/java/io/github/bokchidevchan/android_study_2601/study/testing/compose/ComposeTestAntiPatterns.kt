package io.github.bokchidevchan.android_study_2601.study.testing.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * ========================================================================
 * ⚠️ Compose 테스트 주의사항 및 안티패턴
 * ========================================================================
 *
 * 면접에서 "Compose 테스트 시 주의할 점이 뭔가요?" 질문에 대비!
 *
 * 다루는 내용:
 * 1. Thread.sleep() 사용 금지
 * 2. ViewModel 직접 전달 금지
 * 3. testTag 남용 금지
 * 4. Merged vs Unmerged Tree 혼동
 * 5. 동기화 문제 해결법
 *
 * ========================================================================
 */
class ComposeTestAntiPatterns {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========================================================================
    // ❌ Anti-Pattern 1: Thread.sleep() 사용
    // ========================================================================

    /**
     * ❌ BAD: Thread.sleep() 사용
     *
     * 문제점:
     * - 불필요한 대기 시간
     * - Flaky 테스트 원인
     * - CI에서 더 오래 걸림
     */
    // @Test  // 주석 처리 - 안티패턴 예시
    fun bad_using_thread_sleep() {
        composeTestRule.setContent {
            var count by remember { mutableStateOf(0) }
            Button(onClick = { count++ }) {
                Text("Count: $count")
            }
        }

        composeTestRule.onNodeWithText("Count: 0").performClick()

        // ❌ 절대 하지 마세요!
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()
    }

    /**
     * ✅ GOOD: waitForIdle() 사용
     *
     * Compose가 Recomposition을 완료할 때까지 자동 대기
     */
    @Test
    fun good_using_waitForIdle() {
        composeTestRule.setContent {
            var count by remember { mutableStateOf(0) }
            Button(onClick = { count++ }) {
                Text("Count: $count")
            }
        }

        composeTestRule.onNodeWithText("Count: 0").performClick()

        // ✅ Compose 동기화 사용
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()
    }

    // ========================================================================
    // ❌ Anti-Pattern 2: ViewModel 직접 전달
    // ========================================================================

    /**
     * ❌ BAD: ViewModel을 Composable에 직접 전달
     *
     * 문제점:
     * - 테스트 시 ViewModel mock 필요
     * - Preview 불가능
     * - 재사용성 낮음
     */
    @Composable
    fun BadScreen(viewModel: BadViewModel) {
        val state by viewModel.state.collectAsState()
        Column {
            Text("Count: ${state.count}")
            Button(onClick = { viewModel.increment() }) {
                Text("증가")
            }
        }
    }

    /**
     * ✅ GOOD: State와 Callback 분리 (State Hoisting)
     *
     * 장점:
     * - ViewModel 없이 테스트 가능
     * - Preview 가능
     * - 재사용성 높음
     */
    @Composable
    fun GoodScreen(
        count: Int,
        onIncrement: () -> Unit
    ) {
        Column {
            Text("Count: $count")
            Button(onClick = onIncrement) {
                Text("증가")
            }
        }
    }

    @Test
    fun good_screen_is_easily_testable() {
        var count = 0

        composeTestRule.setContent {
            MaterialTheme {
                GoodScreen(
                    count = count,
                    onIncrement = { count++ }
                )
            }
        }

        // ViewModel 없이 바로 테스트 가능!
        composeTestRule.onNodeWithText("Count: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("증가").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, count)
    }

    // ========================================================================
    // ❌ Anti-Pattern 3: testTag 남용
    // ========================================================================

    /**
     * ❌ BAD: 모든 곳에 testTag 사용
     *
     * 문제점:
     * - 프로덕션 코드 오염
     * - 접근성 무시
     * - 유지보수 어려움
     */
    @Composable
    fun BadOverusingTestTags() {
        Column(modifier = Modifier.testTag("main_column")) {
            Text(
                text = "제목",
                modifier = Modifier.testTag("title_text")
            )
            Button(
                onClick = {},
                modifier = Modifier.testTag("submit_button")
            ) {
                Text(
                    text = "제출",
                    modifier = Modifier.testTag("submit_text")
                )
            }
        }
    }

    /**
     * ✅ GOOD: Semantic 속성 활용, testTag는 최소화
     *
     * 규칙:
     * - 텍스트가 있으면 → onNodeWithText
     * - 아이콘/이미지면 → contentDescription + onNodeWithContentDescription
     * - 동적 콘텐츠면 → testTag (최후의 수단)
     */
    @Composable
    fun GoodMinimalTestTags() {
        Column {
            Text(text = "제목")  // onNodeWithText("제목")로 찾기
            Button(onClick = {}) {
                Text("제출")  // onNodeWithText("제출")로 찾기
            }
        }
    }

    @Test
    fun good_using_semantic_finders() {
        composeTestRule.setContent {
            MaterialTheme {
                GoodMinimalTestTags()
            }
        }

        // ✅ 사용자 관점의 테스트
        composeTestRule.onNodeWithText("제목").assertIsDisplayed()
        composeTestRule.onNodeWithText("제출").assertIsDisplayed()
    }

    // ========================================================================
    // ❌ Anti-Pattern 4: Merged/Unmerged Tree 혼동
    // ========================================================================

    @Test
    fun understanding_merged_vs_unmerged_tree() {
        composeTestRule.setContent {
            MaterialTheme {
                Button(onClick = {}) {
                    Text("확인")
                }
            }
        }

        // Merged Tree (기본) - 사용자 관점
        // Button과 Text가 하나로 합쳐짐 → "확인" 버튼
        composeTestRule.onNodeWithText("확인").assertIsDisplayed()

        // Unmerged Tree - 구현 세부사항
        // Button과 Text가 별개의 노드
        composeTestRule
            .onNodeWithText("확인", useUnmergedTree = true)
            .assertIsDisplayed()

        // 디버깅: 두 트리 비교
        composeTestRule.onRoot().printToLog("MERGED")
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("UNMERGED")
    }

    // ========================================================================
    // ✅ Best Practice: 비동기 대기 패턴
    // ========================================================================

    @Test
    fun async_wait_patterns() {
        composeTestRule.setContent {
            MaterialTheme {
                var showContent by remember { mutableStateOf(false) }
                Column {
                    Button(onClick = { showContent = true }) {
                        Text("로드")
                    }
                    if (showContent) {
                        Text("콘텐츠 로딩됨", modifier = Modifier.testTag("content"))
                    }
                }
            }
        }

        // 방법 1: waitForIdle (기본)
        composeTestRule.onNodeWithText("로드").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("content").assertIsDisplayed()
    }

    @Test
    fun async_wait_until_pattern() {
        composeTestRule.setContent {
            MaterialTheme {
                var items by remember { mutableStateOf(emptyList<String>()) }
                Column {
                    Button(onClick = { items = listOf("A", "B", "C") }) {
                        Text("로드")
                    }
                    items.forEach { Text(it) }
                }
            }
        }

        composeTestRule.onNodeWithText("로드").performClick()

        // 방법 2: waitUntil (조건부 대기)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("A")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertIsDisplayed()
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
    }
}

// 안티패턴 예시용 ViewModel
class BadViewModel : ViewModel() {
    private val _state = MutableStateFlow(BadState())
    val state: StateFlow<BadState> = _state.asStateFlow()

    fun increment() {
        _state.value = _state.value.copy(count = _state.value.count + 1)
    }
}

data class BadState(val count: Int = 0)
