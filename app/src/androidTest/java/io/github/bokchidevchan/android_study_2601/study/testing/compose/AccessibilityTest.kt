package io.github.bokchidevchan.android_study_2601.study.testing.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * 접근성 기반 테스트 vs testTag 기반 테스트 비교
 *
 * 핵심: onNodeWithText/onNodeWithContentDescription = 사용자 관점
 *       onNodeWithTag = 개발자 관점 (최후의 수단)
 */
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========================================================================
    // 1. testTag 기반 테스트 (나쁜 예)
    // ========================================================================

    @Test
    fun badExample_findByTestTag_worksButMissesAccessibilityIssues() {
        var searchClicked = false
        var addClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                BadAccessibilityToolbar(
                    onSearchClick = { searchClicked = true },
                    onAddClick = { addClicked = true },
                    onDeleteClick = {}
                )
            }
        }

        // testTag로 찾기 - 동작은 하지만...
        composeTestRule
            .onNodeWithTag("search_icon")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag("add_icon")
            .performClick()

        assertEquals(true, searchClicked)
        assertEquals(true, addClicked)

        // 문제: 접근성 검증이 안 됨!
        // contentDescription이 없어도 테스트는 통과
        // 스크린 리더 사용자는 이 버튼들을 구분할 수 없음
    }

    // ========================================================================
    // 2. Semantics 기반 테스트 (좋은 예)
    // ========================================================================

    @Test
    fun goodExample_findByContentDescription_verifiesAccessibility() {
        var searchClicked = false
        var addClicked = false
        var deleteClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                GoodAccessibilityToolbar(
                    onSearchClick = { searchClicked = true },
                    onAddClick = { addClicked = true },
                    onDeleteClick = { deleteClicked = true }
                )
            }
        }

        // contentDescription으로 찾기 - 접근성 검증됨!
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("새 항목 추가")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("삭제")
            .performClick()

        assertEquals(true, searchClicked)
        assertEquals(true, addClicked)
        assertEquals(true, deleteClicked)

        // 장점: contentDescription이 없으면 테스트 실패
        // = 접근성 문제 자동 발견!
    }

    // ========================================================================
    // 3. 접근성 없는 UI는 테스트 불가능 증명
    // ========================================================================

    @Test
    fun badExample_cannotFindByContentDescription() {
        composeTestRule.setContent {
            MaterialTheme {
                BadAccessibilityToolbar(
                    onSearchClick = {},
                    onAddClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // BadAccessibilityToolbar는 contentDescription이 없어서
        // onNodeWithContentDescription으로 찾을 수 없음!

        // 이 코드는 실패함:
        // composeTestRule.onNodeWithContentDescription("검색").assertIsDisplayed()

        // testTag로만 찾을 수 있음 = 접근성 문제 있다는 신호!
        composeTestRule.onNodeWithTag("search_icon").assertIsDisplayed()
    }

    // ========================================================================
    // 4. 상품 카드 테스트 비교
    // ========================================================================

    @Test
    fun badProductCard_findByTestTagOnly() {
        composeTestRule.setContent {
            MaterialTheme {
                BadProductCard(
                    name = "아이폰 15",
                    price = 1200000,
                    isFavorite = false,
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }

        // testTag로 찾기 - 접근성 무관
        composeTestRule.onNodeWithTag("product_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("product_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("favorite_icon").assertIsDisplayed()

        // 문제: 스크린 리더가 이 카드를 어떻게 읽는지 모름
    }

    @Test
    fun goodProductCard_findBySemantics() {
        var cardClicked = false
        var favoriteClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                GoodProductCard(
                    name = "아이폰 15",
                    price = 1200000,
                    isFavorite = false,
                    onClick = { cardClicked = true },
                    onFavoriteClick = { favoriteClicked = true }
                )
            }
        }

        // 사용자가 보는 텍스트로 찾기
        composeTestRule.onNodeWithText("아이폰 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("1200000원").assertIsDisplayed()

        // 좋아요 버튼 - 상태를 포함한 설명으로 찾기
        composeTestRule
            .onNodeWithContentDescription("아이폰 15 좋아요")
            .performClick()

        assertEquals(true, favoriteClicked)
    }

    @Test
    fun goodProductCard_favoriteStateChangesDescription() {
        composeTestRule.setContent {
            MaterialTheme {
                GoodProductCard(
                    name = "갤럭시 S24",
                    price = 1000000,
                    isFavorite = true,  // 이미 좋아요 상태
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }

        // 좋아요 상태일 때는 "좋아요 취소"로 표시
        composeTestRule
            .onNodeWithContentDescription("갤럭시 S24 좋아요 취소")
            .assertIsDisplayed()
    }

    // ========================================================================
    // 5. 버튼 텍스트 기반 테스트
    // ========================================================================

    @Test
    fun goodActionButtons_findByText() {
        var shareClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                GoodActionButtons(
                    onShare = { shareClicked = true },
                    onFavorite = {}
                )
            }
        }

        // 버튼 안의 텍스트로 찾기 (가장 자연스러움)
        composeTestRule
            .onNodeWithText("공유")
            .performClick()

        assertEquals(true, shareClicked)

        // 또는 contentDescription으로 찾기
        composeTestRule
            .onNodeWithContentDescription("좋아요에 추가")
            .assertIsDisplayed()
    }

    // ========================================================================
    // 6. Matcher 조합 예제
    // ========================================================================

    @Test
    fun advancedMatchers_combineConditions() {
        composeTestRule.setContent {
            MaterialTheme {
                GoodAccessibilityToolbar(
                    onSearchClick = {},
                    onAddClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // 클릭 가능하고 "검색" 설명이 있는 노드 찾기
        composeTestRule
            .onNode(
                hasContentDescription("검색") and hasClickAction()
            )
            .assertIsDisplayed()
    }
}

/**
 * ========================================================================
 * 테스트 방법 비교 정리
 * ========================================================================
 *
 * | 방법 | 코드 | 접근성 검증 |
 * |------|------|------------|
 * | testTag | onNodeWithTag("id") | ❌ |
 * | 텍스트 | onNodeWithText("버튼") | ✅ |
 * | 설명 | onNodeWithContentDescription("검색") | ✅ |
 *
 * 우선순위:
 * 1. onNodeWithText - 사용자가 보는 텍스트
 * 2. onNodeWithContentDescription - 아이콘, 이미지
 * 3. onNodeWithTag - 위 둘로 못 찾을 때만
 *
 * 기억하세요: 테스트에서 못 찾으면 스크린 리더도 못 찾습니다!
 */
