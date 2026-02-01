package io.github.bokchidevchan.android_study_2601.study.testing.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 접근성(Accessibility) 기반 테스트 예제
 * ========================================================================
 *
 * 핵심 개념:
 * - Compose는 View hierarchy 대신 Semantics Tree 사용
 * - Semantics Tree = 접근성 도구(스크린 리더)가 읽는 구조
 * - 테스트도 같은 Semantics Tree를 사용!
 *
 * 결론: 접근성 좋은 UI = 테스트하기 좋은 UI
 *
 * ========================================================================
 * 비교: testTag vs Semantics 기반 Finder
 * ========================================================================
 *
 * | Finder                          | 접근성 | 테스트 |
 * |---------------------------------|--------|--------|
 * | onNodeWithText("텍스트")         | ✅     | ✅ 권장 |
 * | onNodeWithContentDescription()   | ✅     | ✅ 권장 |
 * | onNodeWithTag("태그")            | ❌     | 🔶 최후수단 |
 *
 * testTag는:
 * - 스크린 리더가 인식 못함
 * - 사용자에게 보이지 않음
 * - 개발자만 아는 정보
 */

// ========================================================================
// ❌ 나쁜 예제: 접근성 없는 UI (testTag만 사용)
// ========================================================================

/**
 * 접근성 나쁜 예제 - 아이콘에 설명 없음
 *
 * 문제점:
 * 1. 스크린 리더 사용자가 버튼 용도를 알 수 없음
 * 2. 테스트에서 onNodeWithContentDescription 사용 불가
 * 3. testTag로만 찾을 수 있음 (접근성 문제 감춤)
 */
@Composable
fun BadAccessibilityToolbar(
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        // ❌ contentDescription = null → 스크린 리더가 "버튼"만 읽음
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.testTag("search_icon")  // testTag만 있음
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null  // ❌ 설명 없음!
            )
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier.testTag("add_icon")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null  // ❌ 설명 없음!
            )
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.testTag("delete_icon")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null  // ❌ 설명 없음!
            )
        }
    }
}

// ========================================================================
// ✅ 좋은 예제: 접근성 있는 UI (Semantics 활용)
// ========================================================================

/**
 * 접근성 좋은 예제 - 모든 요소에 의미 있는 설명
 *
 * 장점:
 * 1. 스크린 리더가 "검색 버튼", "추가 버튼" 등으로 읽어줌
 * 2. 테스트에서 onNodeWithContentDescription("검색") 사용 가능
 * 3. 사용자 관점에서 테스트 작성 가능
 */
@Composable
fun GoodAccessibilityToolbar(
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        // ✅ contentDescription으로 의미 전달
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색"  // ✅ 스크린 리더: "검색 버튼"
            )
        }

        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "새 항목 추가"  // ✅ 스크린 리더: "새 항목 추가 버튼"
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제"  // ✅ 스크린 리더: "삭제 버튼"
            )
        }
    }
}

// ========================================================================
// 상품 카드 비교 예제
// ========================================================================

/**
 * ❌ 나쁜 예제: 클릭 가능한 카드인데 역할(Role) 없음
 */
@Composable
fun BadProductCard(
    name: String,
    price: Int,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card")  // ❌ testTag만
            .clickable(onClick = onClick)  // 클릭 가능하지만 역할 없음
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    modifier = Modifier.testTag("product_name")  // ❌ testTag만
                )
                Text(
                    text = "${price}원",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.testTag("product_price")  // ❌ testTag만
                )
            }

            // ❌ 좋아요 상태를 스크린 리더가 알 수 없음
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,  // ❌ 설명 없음
                tint = if (isFavorite) Color.Red else Color.Gray,
                modifier = Modifier
                    .testTag("favorite_icon")
                    .clickable(onClick = onFavoriteClick)
            )
        }
    }
}

/**
 * ✅ 좋은 예제: 완전한 접근성 지원
 */
@Composable
fun GoodProductCard(
    name: String,
    price: Int,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            // ✅ semantics로 카드의 의미 전달
            .semantics {
                contentDescription = "$name, ${price}원"
                role = Role.Button  // 클릭 가능함을 알림
            }
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // ✅ Text는 자동으로 Semantics에 포함됨
                Text(
                    text = name,
                    fontSize = 18.sp
                )
                Text(
                    text = "${price}원",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // ✅ 좋아요 상태를 명확하게 전달
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.semantics {
                    contentDescription = if (isFavorite) {
                        "$name 좋아요 취소"
                    } else {
                        "$name 좋아요"
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,  // IconButton에서 이미 설정
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

// ========================================================================
// 액션 버튼 비교 예제
// ========================================================================

/**
 * ❌ 나쁜 예제: 아이콘만 있는 버튼
 */
@Composable
fun BadActionButtons(
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ❌ 버튼 안에 아이콘만, 텍스트 없음
        Button(
            onClick = onShare,
            modifier = Modifier.testTag("share_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,  // ❌
                modifier = Modifier.size(18.dp)
            )
        }

        Button(
            onClick = onFavorite,
            modifier = Modifier.testTag("favorite_button")
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,  // ❌
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * ✅ 좋은 예제: 아이콘 + 텍스트 또는 contentDescription
 */
@Composable
fun GoodActionButtons(
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ✅ 방법 1: 아이콘 + 텍스트 (가장 좋음)
        Button(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,  // 텍스트가 있으므로 불필요
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("공유")  // ✅ 텍스트가 있어서 onNodeWithText("공유") 가능
        }

        // ✅ 방법 2: 아이콘만 + contentDescription
        Button(
            onClick = onFavorite,
            modifier = Modifier.semantics {
                contentDescription = "좋아요에 추가"
            }
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "좋아요에 추가",  // ✅
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ========================================================================
// Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
private fun BadAccessibilityToolbarPreview() {
    MaterialTheme {
        Column {
            Text("❌ 나쁜 예제 (testTag만)", modifier = Modifier.padding(8.dp))
            BadAccessibilityToolbar(
                onSearchClick = {},
                onAddClick = {},
                onDeleteClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoodAccessibilityToolbarPreview() {
    MaterialTheme {
        Column {
            Text("✅ 좋은 예제 (contentDescription)", modifier = Modifier.padding(8.dp))
            GoodAccessibilityToolbar(
                onSearchClick = {},
                onAddClick = {},
                onDeleteClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductCardComparisonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("❌ 나쁜 예제")
            BadProductCard(
                name = "맥북 프로",
                price = 2500000,
                isFavorite = true,
                onClick = {},
                onFavoriteClick = {}
            )

            Text("✅ 좋은 예제")
            GoodProductCard(
                name = "맥북 프로",
                price = 2500000,
                isFavorite = true,
                onClick = {},
                onFavoriteClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonsComparisonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("❌ 나쁜 예제 (아이콘만)")
            BadActionButtons(onShare = {}, onFavorite = {})

            Text("✅ 좋은 예제 (아이콘 + 텍스트/설명)")
            GoodActionButtons(onShare = {}, onFavorite = {})
        }
    }
}

/**
 * ========================================================================
 * 📚 테스트 방법 비교 요약
 * ========================================================================
 *
 * ❌ 나쁜 테스트 (testTag 의존):
 * ```
 * // 개발자만 아는 태그로 찾음
 * composeTestRule.onNodeWithTag("search_icon").performClick()
 * ```
 *
 * ✅ 좋은 테스트 (Semantics 기반):
 * ```
 * // 사용자가 인식하는 방식으로 찾음
 * composeTestRule.onNodeWithContentDescription("검색").performClick()
 * composeTestRule.onNodeWithText("공유").performClick()
 * ```
 *
 * 왜 좋은가?
 * 1. 스크린 리더 사용자와 같은 방식으로 UI 접근
 * 2. 접근성 문제가 있으면 테스트가 실패 → 자동으로 발견
 * 3. UI 텍스트 변경 시 테스트가 실패 → 의도적인 알림
 * 4. "사용자 관점" 테스트 = 더 의미 있는 테스트
 */
