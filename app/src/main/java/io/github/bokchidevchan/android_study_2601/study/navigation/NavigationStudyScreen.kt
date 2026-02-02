package io.github.bokchidevchan.android_study_2601.study.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 Navigation 학습 - 메인 화면
 * ========================================================================
 *
 * Jetpack Navigation의 진화:
 * - Navigation 2: 현재 안정 버전 (NavHost, NavController)
 * - Navigation 3: 2025년 발표된 새로운 API (NavDisplay, NavBackStack)
 *
 * Navigation 3의 핵심 변화:
 * 1. NavController → NavBackStack (직접 상태 관리)
 * 2. NavHost → NavDisplay
 * 3. composable { } → entry<Route> { }
 * 4. String route → NavKey 인터페이스
 */
@Composable
fun NavigationStudyScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🧭 Navigation 학습",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Jetpack Compose Navigation의 기본과 Navigation 3의 새로운 패러다임",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationTopicCard(
            emoji = "📍",
            title = "Navigation 2 (현재)",
            subtitle = "NavHost, NavController, composable { }",
            description = "현재 안정 버전. NavController로 화면 전환, NavHost로 목적지 정의",
            color = Color(0xFFE3F2FD),
            highlights = listOf(
                "NavController - 네비게이션 상태 관리",
                "NavHost - 네비게이션 그래프 정의",
                "composable() - 목적지 정의",
                "Type-safe args with Route data class"
            ),
            onClick = { onNavigate("Navigation2") }
        )

        NavigationTopicCard(
            emoji = "🚀",
            title = "Navigation 3 (신규)",
            subtitle = "NavDisplay, NavBackStack, entry { }",
            description = "2025년 발표. 더 선언적이고 유연한 API. 직접 백스택 관리",
            color = Color(0xFFE8F5E9),
            highlights = listOf(
                "NavBackStack - 직접 백스택 조작",
                "NavDisplay - 화면 렌더링",
                "entryProvider - 목적지 정의",
                "NavKey - 타입 안전 라우트"
            ),
            onClick = { onNavigate("Navigation3") }
        )

        NavigationTopicCard(
            emoji = "⚖️",
            title = "Navigation 2 vs 3 비교",
            subtitle = "핵심 차이점과 마이그레이션 가이드",
            description = "두 버전의 API 비교, 언제 어떤 것을 사용해야 하는지",
            color = Color(0xFFFFF3E0),
            highlights = listOf(
                "API 비교 테이블",
                "마이그레이션 단계",
                "장단점 분석",
                "선택 가이드"
            ),
            onClick = { onNavigate("NavigationComparison") }
        )

        WhyNavigationCard()
    }
}

@Composable
private fun NavigationTopicCard(
    emoji: String,
    title: String,
    subtitle: String,
    description: String,
    color: Color,
    highlights: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            highlights.forEach { highlight ->
                Text(
                    text = "• $highlight",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun WhyNavigationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 왜 Jetpack Navigation을 사용하는가?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val reasons = listOf(
                "백스택 관리" to "시스템 백 버튼 자동 처리, 상태 저장",
                "타입 안전성" to "Route 클래스로 컴파일 타임 검증",
                "Deep Link" to "URL로 앱 내 특정 화면 직접 접근",
                "ViewModel 스코핑" to "화면별 ViewModel 자동 관리",
                "애니메이션" to "화면 전환 애니메이션 내장 지원",
                "테스트" to "NavController Mock으로 테스트 용이"
            )

            reasons.forEach { (title, desc) ->
                Text(
                    text = "• $title",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "  $desc",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NavigationStudyScreenPreview() {
    NavigationStudyScreen(onNavigate = {})
}
