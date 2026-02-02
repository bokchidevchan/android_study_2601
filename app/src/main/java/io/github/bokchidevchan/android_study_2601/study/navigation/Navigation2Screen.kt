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
 * 📚 Navigation 2 (현재 안정 버전)
 * ========================================================================
 *
 * 핵심 구성요소:
 * 1. NavController - 네비게이션 상태 관리, 화면 전환 명령
 * 2. NavHost - 네비게이션 그래프 정의, 목적지 컨테이너
 * 3. composable() - 개별 목적지 정의
 * 4. Route - 목적지 식별자 (String 또는 data class)
 */
@Composable
fun Navigation2Screen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        SetupSection()
        BasicUsageSection()
        TypeSafeArgsSection()
        NestedNavigationSection()
        DeepLinkSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "📍 Navigation 2 (현재)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NavHost, NavController, composable { } 기반 네비게이션",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SetupSection() {
    SectionCard(title = "1️⃣ 설정 (build.gradle.kts)") {
        CodeBlock(
            code = """
// libs.versions.toml
[versions]
navigationCompose = "2.8.5"

[libraries]
navigation-compose = { 
    group = "androidx.navigation", 
    name = "navigation-compose", 
    version.ref = "navigationCompose" 
}

// app/build.gradle.kts
dependencies {
    implementation(libs.navigation.compose)
}
            """.trimIndent()
        )
    }
}

@Composable
private fun BasicUsageSection() {
    SectionCard(title = "2️⃣ 기본 사용법") {
        Text(
            text = "NavController + NavHost 패턴",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Route 정의 (Serializable data class 권장)
@Serializable
data object Home

@Serializable
data class ProductDetail(val productId: String)

@Serializable
data object Profile

@Composable
fun AppNavigation() {
    // 1. NavController 생성
    val navController = rememberNavController()
    
    // 2. NavHost로 네비게이션 그래프 정의
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        // 3. 각 목적지를 composable로 정의
        composable<Home> {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate(ProductDetail(productId))
                },
                onProfileClick = {
                    navController.navigate(Profile)
                }
            )
        }
        
        composable<ProductDetail> { backStackEntry ->
            // 인자 추출
            val product: ProductDetail = backStackEntry.toRoute()
            ProductDetailScreen(
                productId = product.productId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Profile> {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "핵심 API",
            items = listOf(
                "rememberNavController() - NavController 생성 및 remember",
                "NavHost() - 네비게이션 그래프 컨테이너",
                "composable<Route> { } - 목적지 정의",
                "navController.navigate(route) - 화면 전환",
                "navController.popBackStack() - 뒤로 가기"
            )
        )
    }
}

@Composable
private fun TypeSafeArgsSection() {
    SectionCard(title = "3️⃣ Type-Safe Arguments") {
        Text(
            text = "Kotlin Serialization으로 타입 안전 인자 전달",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 복잡한 인자를 가진 Route
@Serializable
data class OrderDetail(
    val orderId: String,
    val userId: String,
    val amount: Int,
    val status: OrderStatus = OrderStatus.PENDING
)

@Serializable
enum class OrderStatus { PENDING, COMPLETED, CANCELLED }

// 네비게이션
navController.navigate(
    OrderDetail(
        orderId = "ORD-001",
        userId = "USR-123",
        amount = 50000,
        status = OrderStatus.PENDING
    )
)

// 목적지에서 인자 추출
composable<OrderDetail> { backStackEntry ->
    val order: OrderDetail = backStackEntry.toRoute()
    
    OrderDetailScreen(
        orderId = order.orderId,
        userId = order.userId,
        amount = order.amount,
        status = order.status
    )
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ComparisonBox(
                title = "❌ String Route (구식)",
                items = listOf(
                    "\"product/{id}\"",
                    "런타임 에러 가능",
                    "오타 발생 위험"
                ),
                color = Color(0xFFFFCDD2)
            )
            ComparisonBox(
                title = "✅ Type-Safe Route",
                items = listOf(
                    "data class Product(id)",
                    "컴파일 타임 검증",
                    "IDE 자동완성"
                ),
                color = Color(0xFFC8E6C9)
            )
        }
    }
}

@Composable
private fun NestedNavigationSection() {
    SectionCard(title = "4️⃣ Nested Navigation (중첩 그래프)") {
        Text(
            text = "Bottom Navigation + 중첩 그래프 패턴",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 탭별 중첩 그래프 정의
@Serializable data object HomeGraph
@Serializable data object SearchGraph  
@Serializable data object ProfileGraph

NavHost(
    navController = navController,
    startDestination = HomeGraph
) {
    // Home 탭 그래프
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> { HomeScreen() }
        composable<ProductDetail> { ProductDetailScreen() }
    }
    
    // Search 탭 그래프
    navigation<SearchGraph>(startDestination = Search) {
        composable<Search> { SearchScreen() }
        composable<SearchResult> { SearchResultScreen() }
    }
    
    // Profile 탭 그래프
    navigation<ProfileGraph>(startDestination = Profile) {
        composable<Profile> { ProfileScreen() }
        composable<Settings> { SettingsScreen() }
    }
}

// Bottom Navigation에서 그래프 간 이동
BottomNavigation {
    items.forEach { screen ->
        BottomNavigationItem(
            selected = currentRoute == screen.route,
            onClick = {
                navController.navigate(screen.route) {
                    // 백스택 정리
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
            """.trimIndent()
        )
    }
}

@Composable
private fun DeepLinkSection() {
    SectionCard(title = "5️⃣ Deep Link") {
        Text(
            text = "URL로 앱 내 특정 화면 직접 접근",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// AndroidManifest.xml에 intent-filter 추가
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data 
            android:scheme="myapp"
            android:host="product" />
    </intent-filter>
</activity>

// 코드에서 deepLinks 정의
composable<ProductDetail>(
    deepLinks = listOf(
        navDeepLink<ProductDetail>(
            basePath = "myapp://product"
        )
    )
) { backStackEntry ->
    val product: ProductDetail = backStackEntry.toRoute()
    ProductDetailScreen(productId = product.productId)
}

// 딥링크 테스트 (adb)
// adb shell am start -d "myapp://product/SKU-123" 
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: NavController vs NavHost?" to
                    "A: NavController는 상태 관리/명령, NavHost는 UI 컨테이너. Controller가 Host 내 화면 전환",
            "Q: popBackStack vs navigate?" to
                    "A: popBackStack은 백스택에서 제거(뒤로가기), navigate는 새 화면 추가",
            "Q: saveState/restoreState?" to
                    "A: Bottom Nav에서 탭 전환 시 이전 상태 유지. launchSingleTop과 함께 사용",
            "Q: 왜 Type-Safe Route?" to
                    "A: String route는 오타/런타임 에러 위험. data class로 컴파일 타임 검증",
            "Q: ViewModel 스코핑?" to
                    "A: composable 내 hiltViewModel()은 해당 목적지에 스코핑. 백스택 제거 시 자동 정리"
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
private fun ComparisonBox(
    title: String,
    items: List<String>,
    color: Color
) {
    Column(
        modifier = Modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            Text(text = "• $item", fontSize = 11.sp)
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
private fun Navigation2ScreenPreview() {
    Navigation2Screen()
}
