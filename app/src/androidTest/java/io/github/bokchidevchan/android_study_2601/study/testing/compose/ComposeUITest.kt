package io.github.bokchidevchan.android_study_2601.study.testing.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * ========================================================================
 * 📚 Compose UI Test - Semantics Tree 기반 테스트
 * ========================================================================
 *
 * Compose 테스트의 핵심:
 * 1. Semantics Tree: UI의 의미론적 구조
 * 2. Finder: 노드 찾기 (onNode, onNodeWithText 등)
 * 3. Assertion: 상태 검증 (assertIsDisplayed 등)
 * 4. Action: 사용자 동작 시뮬레이션 (performClick 등)
 *
 * 테스트 패턴:
 * - finder → action → assertion
 * - "무엇을 찾고 → 무엇을 하고 → 무엇을 확인"
 *
 * 주의: Android Instrumented Test에서는 백틱(`) 메서드명에 공백/한글 사용 불가
 * DEX 버전 제한으로 인해 언더스코어(_) 형식 사용
 */
class ComposeUITest {

    /**
     * createComposeRule: Compose 콘텐츠를 설정하고 테스트
     * - Activity 없이 순수 Composable 테스트
     * - setContent로 테스트할 UI 설정
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    // ========================================================================
    // 1. Finder - 노드 찾기
    // ========================================================================

    /**
     * onNodeWithText - 텍스트로 노드 찾기
     */
    @Test
    fun finder_onNodeWithText_findsNodeByText() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 0,
                    message = "초기 상태",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // 텍스트로 노드 찾기
        composeTestRule.onNodeWithText("Counter").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithText("초기 상태").assertIsDisplayed()
    }

    /**
     * onNodeWithTag - testTag로 노드 찾기
     */
    @Test
    fun finder_onNodeWithTag_findsNodeByTestTag() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 5,
                    message = "",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // testTag로 노드 찾기 (코드에서 Modifier.testTag("xxx") 설정)
        composeTestRule.onNodeWithTag("counter_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("increment_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("decrement_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reset_button").assertIsDisplayed()
    }

    /**
     * onNodeWithContentDescription - 접근성 설명으로 찾기
     */
    @Test
    fun finder_onNodeWithContentDescription_findsNodeByAccessibility() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 42,
                    message = "",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // contentDescription으로 찾기 (접근성 + 테스트)
        composeTestRule
            .onNodeWithContentDescription("카운터 값: 42")
            .assertIsDisplayed()
    }

    /**
     * onAllNodes - 여러 노드 찾기
     */
    @Test
    fun finder_onAllNodes_findsMultipleNodes() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 0,
                    message = "",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // 모든 버튼 찾기 (Button 컴포넌트들)
        val buttons = composeTestRule.onAllNodes(hasClickAction())
        buttons.assertCountEquals(3)  // +1, -1, Reset
    }

    // ========================================================================
    // 2. Assertion - 상태 검증
    // ========================================================================

    /**
     * assertIsDisplayed - 노드가 표시되는지
     */
    @Test
    fun assertion_assertIsDisplayed_verifiesNodeIsVisible() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 10,
                    message = "테스트 메시지",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("테스트 메시지").assertIsDisplayed()
    }

    /**
     * assertDoesNotExist - 노드가 존재하지 않는지
     */
    @Test
    fun assertion_assertDoesNotExist_verifiesNodeNotPresent() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 0,
                    message = "",  // 빈 메시지
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // 빈 메시지일 때 message_text가 없음
        composeTestRule.onNodeWithTag("message_text").assertDoesNotExist()
    }

    /**
     * assertIsEnabled / assertIsNotEnabled - 활성화 상태
     */
    @Test
    fun assertion_assertIsNotEnabled_verifiesButtonDisabled() {
        composeTestRule.setContent {
            MaterialTheme {
                InputFormContent(
                    name = "",
                    email = "",
                    onNameChange = {},
                    onEmailChange = {},
                    onSubmit = {},
                    isSubmitEnabled = false,
                    errorMessage = null
                )
            }
        }

        // 제출 버튼이 비활성화 상태
        composeTestRule
            .onNodeWithTag("submit_button")
            .assertIsNotEnabled()
    }

    /**
     * assertTextEquals - 정확한 텍스트 확인
     */
    @Test
    fun assertion_assertTextEquals_verifiesExactText() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 100,
                    message = "",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("카운터 값: 100")
            .assertTextEquals("100")
    }

    // ========================================================================
    // 3. Action - 사용자 동작
    // ========================================================================

    /**
     * performClick - 클릭 동작
     */
    @Test
    fun action_performClick_triggersCallback() {
        var count = 0

        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = count,
                    message = "",
                    onIncrement = { count++ },
                    onDecrement = { count-- },
                    onReset = { count = 0 }
                )
            }
        }

        // +1 버튼 클릭
        composeTestRule.onNodeWithTag("increment_button").performClick()
        // Recomposition 대기
        composeTestRule.waitForIdle()

        // 콜백이 호출되었는지 확인
        assert(count == 1)
    }

    /**
     * performTextInput - 텍스트 입력
     */
    @Test
    fun action_performTextInput_entersText() {
        var name = ""
        var email = ""

        composeTestRule.setContent {
            MaterialTheme {
                InputFormContent(
                    name = name,
                    email = email,
                    onNameChange = { name = it },
                    onEmailChange = { email = it },
                    onSubmit = {},
                    isSubmitEnabled = true,
                    errorMessage = null
                )
            }
        }

        // 이름 입력
        composeTestRule
            .onNodeWithTag("name_input")
            .performTextInput("홍길동")

        composeTestRule.waitForIdle()
        assert(name == "홍길동")

        // 이메일 입력
        composeTestRule
            .onNodeWithTag("email_input")
            .performTextInput("hong@test.com")

        composeTestRule.waitForIdle()
        assert(email == "hong@test.com")
    }

    /**
     * performTextClearance - 텍스트 지우기
     */
    @Test
    fun action_performTextClearance_clearsText() {
        var text = "초기값"

        composeTestRule.setContent {
            MaterialTheme {
                InputFormContent(
                    name = text,
                    email = "",
                    onNameChange = { text = it },
                    onEmailChange = {},
                    onSubmit = {},
                    isSubmitEnabled = true,
                    errorMessage = null
                )
            }
        }

        // 텍스트 지우기
        composeTestRule
            .onNodeWithTag("name_input")
            .performTextClearance()

        composeTestRule.waitForIdle()
        assert(text == "")
    }

    // ========================================================================
    // 4. 통합 시나리오 테스트
    // ========================================================================

    /**
     * 카운터 증가/감소/리셋 시나리오
     */
    @Test
    fun scenario_counterIncrementDecrementReset() {
        var count = 0

        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = count,
                    message = "",
                    onIncrement = { count++ },
                    onDecrement = { count-- },
                    onReset = { count = 0 }
                )
            }
        }

        // 초기 상태 확인
        composeTestRule.onNodeWithText("0").assertIsDisplayed()

        // +1 클릭 3번
        repeat(3) {
            composeTestRule.onNodeWithTag("increment_button").performClick()
            composeTestRule.waitForIdle()
        }
        assert(count == 3)

        // -1 클릭 1번
        composeTestRule.onNodeWithTag("decrement_button").performClick()
        composeTestRule.waitForIdle()
        assert(count == 2)

        // Reset 클릭
        composeTestRule.onNodeWithTag("reset_button").performClick()
        composeTestRule.waitForIdle()
        assert(count == 0)
    }

    /**
     * 입력폼 유효성검사 시나리오
     */
    @Test
    fun scenario_inputFormValidation() {
        var name = ""
        var email = ""
        var errorMessage: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                InputFormContent(
                    name = name,
                    email = email,
                    onNameChange = { name = it },
                    onEmailChange = {
                        email = it
                        errorMessage = if (!it.contains("@") && it.isNotEmpty()) {
                            "올바른 이메일 형식이 아닙니다"
                        } else null
                    },
                    onSubmit = {},
                    isSubmitEnabled = name.isNotBlank() && email.contains("@"),
                    errorMessage = errorMessage
                )
            }
        }

        // 초기: 제출 버튼 비활성화
        composeTestRule.onNodeWithTag("submit_button").assertIsNotEnabled()

        // 이름 입력
        composeTestRule.onNodeWithTag("name_input").performTextInput("홍길동")
        composeTestRule.waitForIdle()

        // 아직 비활성화 (이메일 없음)
        composeTestRule.onNodeWithTag("submit_button").assertIsNotEnabled()

        // 잘못된 이메일 입력
        composeTestRule.onNodeWithTag("email_input").performTextInput("invalid")
        composeTestRule.waitForIdle()

        // 에러 메시지 표시
        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()

        // 올바른 이메일로 수정
        composeTestRule.onNodeWithTag("email_input").performTextClearance()
        composeTestRule.onNodeWithTag("email_input").performTextInput("hong@test.com")
        composeTestRule.waitForIdle()

        // 에러 메시지 사라짐, 제출 버튼 활성화
        composeTestRule.onNodeWithTag("error_message").assertDoesNotExist()
        composeTestRule.onNodeWithTag("submit_button").assertIsEnabled()
    }

    // ========================================================================
    // 5. 고급 기능
    // ========================================================================

    /**
     * printToLog - 디버깅용 Semantics Tree 출력
     */
    @Test
    fun advanced_printToLog_outputsSemanticsTree() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 0,
                    message = "디버깅",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // 전체 Semantics Tree 출력 (Logcat에서 확인)
        composeTestRule.onRoot().printToLog("COMPOSE_TEST")

        // 특정 노드의 Semantics 출력
        composeTestRule
            .onNodeWithTag("counter_card")
            .printToLog("COUNTER_CARD")
    }

    /**
     * onChildren - 계층 구조 확인
     */
    @Test
    fun advanced_onChildren_checksHierarchy() {
        composeTestRule.setContent {
            MaterialTheme {
                CounterContent(
                    count = 0,
                    message = "",
                    onIncrement = {},
                    onDecrement = {},
                    onReset = {}
                )
            }
        }

        // counter_card 안에 텍스트가 있는지
        composeTestRule
            .onNodeWithTag("counter_card")
            .onChildren()
            .assertAny(hasText("Counter"))
    }
}

/**
 * ========================================================================
 * 📚 Compose UI Test 요약
 * ========================================================================
 *
 * 1. Finder (노드 찾기)
 *    - onNodeWithText("텍스트"): 텍스트로 찾기
 *    - onNodeWithTag("태그"): testTag로 찾기
 *    - onNodeWithContentDescription("설명"): 접근성 설명으로 찾기
 *    - onAllNodes(matcher): 여러 노드 찾기
 *
 * 2. Assertion (검증)
 *    - assertIsDisplayed(): 표시됨
 *    - assertDoesNotExist(): 존재하지 않음
 *    - assertIsEnabled() / assertIsNotEnabled(): 활성화 상태
 *    - assertTextEquals("텍스트"): 텍스트 일치
 *
 * 3. Action (동작)
 *    - performClick(): 클릭
 *    - performTextInput("텍스트"): 텍스트 입력
 *    - performTextClearance(): 텍스트 지우기
 *    - performScrollTo(): 스크롤
 *
 * 4. 동기화
 *    - waitForIdle(): Recomposition 완료 대기
 *    - waitUntil { condition }: 조건 충족까지 대기
 *
 * 5. 디버깅
 *    - printToLog("TAG"): Semantics Tree 출력
 *
 * 핵심 원칙:
 * - Stateless Composable로 분리하여 테스트
 * - 의미론적 Matcher 우선 (onNodeWithText > onNodeWithTag)
 * - 접근성(Accessibility) 고려 = 좋은 테스트
 *
 * 주의: Android Instrumented Test는 DEX 제한으로
 * 백틱 메서드명에 공백/한글 사용 불가 → 언더스코어 사용
 */
