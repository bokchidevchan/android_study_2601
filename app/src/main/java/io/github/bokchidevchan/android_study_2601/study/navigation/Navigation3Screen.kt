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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 Navigation 3 (2025년 신규)
 * ========================================================================
 *
 * Navigation 3의 핵심 변화:
 * 1. NavController → NavBackStack (직접 백스택 조작)
 * 2. NavHost → NavDisplay (화면 렌더링)
 * 3. composable { } → entry<Route> { } (목적지 정의)
 * 4. String route → NavKey 인터페이스 (타입 안전 라우트)
 *
 * 핵심 철학: "You own the back stack"
 * - 백스택을 직접 관리하는 선언적 API
 * - Scene 개념으로 다양한 레이아웃 지원 (Two-pane, Dialog 등)
 */
@Composable
fun Navigation3Screen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        SetupSection()
        CoreConceptsSection()
        BasicUsageSection()
        SceneSection()
        MigrationSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "🚀 Navigation 3 (신규)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NavDisplay, NavBackStack, entryProvider 기반 선언적 네비게이션",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        WarningBox(
            text = "⚠️ Navigation 3는 2025년 발표된 신규 API입니다. 아직 알파/실험 단계일 수 있습니다."
        )
    }
}

@Composable
private fun WarningBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = text, fontSize = 13.sp, color = Color(0xFFE65100))
    }
}

@Composable
private fun SetupSection() {
    SectionCard(title = "1️⃣ 설정 (build.gradle.kts)") {
        CodeBlock(
            code = """
// libs.versions.toml
[versions]
navigation3 = "1.0.0-alpha01"  // 최신 버전 확인 필요

[libraries]
navigation3-runtime = { 
    group = "androidx.navigation3", 
    name = "navigation3-runtime", 
    version.ref = "navigation3" 
}
navigation3-ui = { 
    group = "androidx.navigation3", 
    name = "navigation3-ui", 
    version.ref = "navigation3" 
}

// app/build.gradle.kts
plugins {
    // Serialization 플러그인 필요
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

dependencies {
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
}
            """.trimIndent()
        )
    }
}

@Composable
private fun CoreConceptsSection() {
    SectionCard(title = "2️⃣ 핵심 개념") {
        val concepts = listOf(
            Triple("NavKey", "타입 안전 라우트 인터페이스", "@Serializable data class/object가 구현"),
            Triple("NavBackStack", "백스택 직접 관리", "add(), removeLastOrNull(), contains()"),
            Triple("NavDisplay", "화면 렌더링", "NavHost 대체, Scene 지원"),
            Triple("entryProvider", "목적지 정의", "entry<Route> { } DSL"),
            Triple("Scene", "레이아웃 전략", "Single, Dialog, TwoPane 등")
        )

        concepts.forEach { (name, desc, detail) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(0.3f),
                    color = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(0.7f)) {
                    Text(text = desc, fontSize = 13.sp)
                    Text(
                        text = detail,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicUsageSection() {
    SectionCard(title = "3️⃣ 기본 사용법") {
        Text(
            text = "NavBackStack + NavDisplay 패턴",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 1. NavKey 구현 (Serializable 필수)
@Serializable
data object Home : NavKey

@Serializable
data class ProductDetail(val productId: String) : NavKey

@Serializable
data object Profile : NavKey

@Composable
fun AppNavigation() {
    // 2. 백스택 생성 (시작 목적지 지정)
    val backStack = rememberNavBackStack(Home)
    
    // 3. NavDisplay로 화면 렌더링
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            // 4. entry<Route>로 목적지 정의
            entry<Home> {
                HomeScreen(
                    onProductClick = { productId ->
                        backStack.add(ProductDetail(productId))
                    },
                    onProfileClick = {
                        backStack.add(Profile)
                    }
                )
            }
            
            entry<ProductDetail> { key ->
                // key: ProductDetail 인스턴스 직접 접근
                ProductDetailScreen(
                    productId = key.productId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            
            entry<Profile> {
                ProfileScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "핵심 차이점: You Own The Back Stack",
            items = listOf(
                "backStack.add(route) - 화면 추가 (Nav2: navigate)",
                "backStack.removeLastOrNull() - 마지막 제거 (Nav2: popBackStack)",
                "backStack.contains(route) - 중복 체크 가능",
                "백스택을 리스트처럼 직접 조작 가능"
            )
        )
    }
}

@Composable
private fun SceneSection() {
    SectionCard(title = "4️⃣ Scene (레이아웃 전략)") {
        Text(
            text = "다양한 화면 레이아웃 지원",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Dialog Scene - 다이얼로그로 표시
entry<ConfirmDialog>(
    metadata = DialogSceneStrategy.dialog()
) { key ->
    AlertDialog(
        onDismissRequest = { backStack.removeLastOrNull() },
        title = { Text("확인") },
        text = { Text(key.message) },
        confirmButton = { /* ... */ }
    )
}

// Two-Pane Scene - 태블릿/폴더블 대응
class TwoPaneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContent {
            val backStack = rememberNavBackStack(Home)
            val twoPaneStrategy = rememberTwoPaneSceneStrategy<NavKey>()
            
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = twoPaneStrategy,  // Two-pane 전략 적용
                entryProvider = entryProvider {
                    entry<Home>(
                        metadata = TwoPaneScene.twoPane()
                    ) {
                        // 왼쪽 패널
                    }
                    entry<ProductDetail>(
                        metadata = TwoPaneScene.twoPane()
                    ) { key ->
                        // 오른쪽 패널에 표시
                    }
                }
            )
        }
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SceneTypeBox("Single", "기본 전체화면", "📱")
            SceneTypeBox("Dialog", "팝업 다이얼로그", "💬")
            SceneTypeBox("TwoPane", "2단 레이아웃", "📑")
        }
    }
}

@Composable
private fun SceneTypeBox(title: String, desc: String, emoji: String) {
    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = desc, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun MigrationSection() {
    SectionCard(title = "5️⃣ 마이그레이션 가이드") {
        Text(
            text = "Navigation 2 → Navigation 3 전환 단계",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        val steps = listOf(
            "1. Navigation 3 의존성 추가",
            "2. Route에 NavKey 인터페이스 구현",
            "3. NavController → NavBackStack 교체",
            "4. NavHost → NavDisplay 교체",
            "5. composable { } → entry<Route> { } 변환",
            "6. navigate() → backStack.add() 변환",
            "7. popBackStack() → backStack.removeLastOrNull() 변환",
            "8. Navigation 2 의존성 제거"
        )

        steps.forEach { step ->
            Text(
                text = step,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Before (Navigation 2)
@Serializable
data class ProductDetail(val id: String)

navController.navigate(ProductDetail("123"))
navController.popBackStack()

// After (Navigation 3)
@Serializable
data class ProductDetail(val id: String) : NavKey

backStack.add(ProductDetail("123"))
backStack.removeLastOrNull()
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: Navigation 3의 핵심 철학?" to
                    "A: 'You own the back stack' - 백스택을 직접 관리하는 선언적 API",
            "Q: NavController vs NavBackStack?" to
                    "A: NavController는 내부 상태 관리. NavBackStack은 리스트처럼 직접 조작 가능",
            "Q: Scene이란?" to
                    "A: 레이아웃 전략. Single(기본), Dialog(팝업), TwoPane(태블릿) 등 지원",
            "Q: NavKey 역할?" to
                    "A: 타입 안전 라우트 인터페이스. @Serializable과 함께 사용",
            "Q: 언제 Nav 3 사용?" to
                    "A: Two-pane 필요, 복잡한 백스택 조작, 새 프로젝트. 기존 앱은 신중히 마이그레이션"
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
            fontSize = 12.sp,
            color = Color(0xFFE0E0E0)
        )
    }
}

@Composable
private fun HighlightBox(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF43A047).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Text(
                text = "• $item",
                fontSize = 13.sp,
                color = Color(0xFF388E3C)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Navigation3ScreenPreview() {
    Navigation3Screen()
}
