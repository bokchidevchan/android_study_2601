package io.github.bokchidevchan.android_study_2601.study.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 Navigation 2 vs Navigation 3 비교
 * ========================================================================
 *
 * 핵심 차이점:
 * - Navigation 2: NavController 중심, 내부 상태 관리
 * - Navigation 3: NavBackStack 중심, 직접 백스택 조작
 *
 * 선택 기준:
 * - 기존 프로젝트, 안정성 중시 → Navigation 2
 * - 새 프로젝트, Two-pane 필요 → Navigation 3
 */
@Composable
fun NavigationComparisonScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        ApiComparisonTable()
        CodeComparisonSection()
        ProsConsSection()
        WhenToUseSection()
        MigrationChecklistSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "⚖️ Navigation 2 vs 3 비교",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "핵심 API 차이점, 장단점, 선택 가이드",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ApiComparisonTable() {
    SectionCard(title = "1️⃣ API 비교 테이블") {
        val comparisons = listOf(
            Triple("상태 관리", "NavController", "NavBackStack"),
            Triple("UI 컨테이너", "NavHost", "NavDisplay"),
            Triple("목적지 정의", "composable<Route> { }", "entry<Route> { }"),
            Triple("라우트 타입", "data class (Serializable)", "NavKey (Serializable)"),
            Triple("화면 이동", "navController.navigate()", "backStack.add()"),
            Triple("뒤로 가기", "navController.popBackStack()", "backStack.removeLastOrNull()"),
            Triple("인자 접근", "backStackEntry.toRoute()", "entry 람다의 key 파라미터"),
            Triple("레이아웃 전략", "제한적", "Scene (Dialog, TwoPane)"),
            Triple("백스택 조작", "제한적 (popUpTo 등)", "리스트처럼 직접 조작"),
            Triple("안정성", "✅ Stable", "⚠️ Alpha/Experimental")
        )

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text("구분", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f), fontSize = 13.sp)
                Text("Nav 2", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f), fontSize = 13.sp, textAlign = TextAlign.Center)
                Text("Nav 3", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f), fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            comparisons.forEachIndexed { index, (category, nav2, nav3) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.Transparent
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .padding(8.dp)
                ) {
                    Text(category, modifier = Modifier.weight(0.3f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(nav2, modifier = Modifier.weight(0.35f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(nav3, modifier = Modifier.weight(0.35f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun CodeComparisonSection() {
    SectionCard(title = "2️⃣ 코드 비교") {
        Text(
            text = "Navigation 2",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1976D2)
        )

        CodeBlock(
            code = """
// Route 정의
@Serializable
data class ProductDetail(val id: String)

// NavHost 설정
val navController = rememberNavController()
NavHost(navController, startDestination = Home) {
    composable<Home> { 
        HomeScreen(
            onClick = { navController.navigate(ProductDetail("123")) }
        )
    }
    composable<ProductDetail> { entry ->
        val product: ProductDetail = entry.toRoute()
        DetailScreen(product.id)
    }
}

// 뒤로가기
navController.popBackStack()
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Navigation 3",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF388E3C)
        )

        CodeBlock(
            code = """
// Route 정의 (NavKey 구현)
@Serializable
data class ProductDetail(val id: String) : NavKey

// NavDisplay 설정
val backStack = rememberNavBackStack(Home)
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
        entry<Home> { 
            HomeScreen(
                onClick = { backStack.add(ProductDetail("123")) }
            )
        }
        entry<ProductDetail> { key ->
            // key가 직접 ProductDetail 타입
            DetailScreen(key.id)
        }
    }
)

// 뒤로가기
backStack.removeLastOrNull()
            """.trimIndent()
        )
    }
}

@Composable
private fun ProsConsSection() {
    SectionCard(title = "3️⃣ 장단점 비교") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📍 Navigation 2",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("✅ 장점", fontWeight = FontWeight.SemiBold, color = Color(0xFF388E3C), fontSize = 13.sp)
                listOf(
                    "안정적 (Stable)",
                    "풍부한 문서/예제",
                    "커뮤니티 지원",
                    "Hilt 통합 성숙"
                ).forEach {
                    Text("• $it", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("❌ 단점", fontWeight = FontWeight.SemiBold, color = Color(0xFFD32F2F), fontSize = 13.sp)
                listOf(
                    "백스택 조작 제한적",
                    "Two-pane 지원 약함",
                    "내부 상태 접근 어려움"
                ).forEach {
                    Text("• $it", fontSize = 12.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🚀 Navigation 3",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF388E3C)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("✅ 장점", fontWeight = FontWeight.SemiBold, color = Color(0xFF388E3C), fontSize = 13.sp)
                listOf(
                    "직접 백스택 제어",
                    "Scene 지원 (TwoPane)",
                    "더 선언적 API",
                    "key 직접 접근"
                ).forEach {
                    Text("• $it", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("❌ 단점", fontWeight = FontWeight.SemiBold, color = Color(0xFFD32F2F), fontSize = 13.sp)
                listOf(
                    "아직 Alpha 단계",
                    "문서/예제 부족",
                    "Breaking changes 가능",
                    "마이그레이션 비용"
                ).forEach {
                    Text("• $it", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun WhenToUseSection() {
    SectionCard(title = "4️⃣ 선택 가이드") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "📍 Navigation 2 선택",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    "기존 프로젝트 유지보수",
                    "프로덕션 안정성 중시",
                    "팀원 학습 곡선 고려",
                    "풍부한 레퍼런스 필요",
                    "단순한 네비게이션 구조"
                ).forEach {
                    Text("• $it", fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "🚀 Navigation 3 선택",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    "새 프로젝트 시작",
                    "태블릿/폴더블 지원 필수",
                    "복잡한 백스택 조작 필요",
                    "최신 API 실험 가능",
                    "Two-pane 레이아웃 필요"
                ).forEach {
                    Text("• $it", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MigrationChecklistSection() {
    SectionCard(title = "5️⃣ 마이그레이션 체크리스트") {
        val checklist = listOf(
            "Route 클래스에 : NavKey 추가" to "data class Product(val id: String) : NavKey",
            "rememberNavController() 제거" to "rememberNavBackStack(StartRoute) 사용",
            "NavHost → NavDisplay" to "sceneStrategy, entryProvider 파라미터",
            "composable<> → entry<>" to "entry 람다에서 key 직접 접근",
            "navigate() → add()" to "backStack.add(route)",
            "popBackStack() → removeLastOrNull()" to "backStack.removeLastOrNull()",
            "toRoute() 제거" to "entry 람다 파라미터로 직접 전달",
            "savedStateHandle 패턴 변경" to "ViewModel에서 SavedStateHandle 사용 방식 확인"
        )

        checklist.forEachIndexed { index, (task, detail) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "☐",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(text = task, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        text = detail,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
            if (index < checklist.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: Nav 2와 Nav 3의 핵심 차이?" to
                    "A: Nav2는 NavController가 상태 관리, Nav3는 NavBackStack 직접 조작. 'You own the back stack'",
            "Q: 언제 Nav 3를 선택?" to
                    "A: Two-pane 필요, 복잡한 백스택, 새 프로젝트. 기존 앱은 안정성 위해 Nav 2 유지",
            "Q: Scene이란?" to
                    "A: Nav 3의 레이아웃 전략. Single(기본), Dialog(팝업), TwoPane(태블릿) 등 지원",
            "Q: 마이그레이션 핵심?" to
                    "A: Route에 NavKey 구현, NavHost→NavDisplay, composable→entry, navigate→add",
            "Q: Nav 3 단점?" to
                    "A: 아직 Alpha, Breaking changes 가능, 문서/커뮤니티 부족, Hilt 통합 미성숙"
        )

        qnas.forEachIndexed { index, (q, a) ->
            Column {
                Text(
                    text = q,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = a,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (index < qnas.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFFE0E0E0)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NavigationComparisonScreenPreview() {
    NavigationComparisonScreen()
}
