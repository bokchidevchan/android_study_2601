package io.github.bokchidevchan.android_study_2601

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bokchidevchan.android_study_2601.study.compose.derived.DerivedStateOfScreen
import io.github.bokchidevchan.android_study_2601.study.compose.effect.SideEffectScreen
import io.github.bokchidevchan.android_study_2601.study.compose.immutable.StrongSkippingModeScreen
import io.github.bokchidevchan.android_study_2601.study.compose.recomposition.StabilityRecompositionScreen
import io.github.bokchidevchan.android_study_2601.study.compose.state.RememberVsSaveableScreen
import dagger.hilt.android.AndroidEntryPoint
import io.github.bokchidevchan.android_study_2601.study.hilt.HiltStudyScreen
import io.github.bokchidevchan.android_study_2601.study.hilt.comparison.HiltComparisonScreen
import io.github.bokchidevchan.android_study_2601.study.networking.HttpVsRetrofitScreen
import io.github.bokchidevchan.android_study_2601.study.testing.TestingStudyScreen
import io.github.bokchidevchan.android_study_2601.study.memory.MemoryLeakScreen
import io.github.bokchidevchan.android_study_2601.study.kotlin.KotlinStudyScreen
import io.github.bokchidevchan.android_study_2601.study.kotlin.functional.FunctionalProgrammingScreen
import io.github.bokchidevchan.android_study_2601.study.kotlin.oop.ObjectOrientedScreen
import io.github.bokchidevchan.android_study_2601.study.kotlin.generics.GenericsScreen
import io.github.bokchidevchan.android_study_2601.ui.theme.Android_study_2601Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_study_2601Theme {
                StudyNavigator()
            }
        }
    }
}

// ========================================================================
// 네비게이션 구조: Root -> Category -> Detail
// ========================================================================

sealed class Category(val title: String, val subtitle: String, val emoji: String, val color: Color) {
    data object Compose : Category("Compose 학습", "State, Recomposition, Side Effects", "🎨", Color(0xFFE3F2FD))
    data object Networking : Category("Networking", "HttpURLConnection, Retrofit, OkHttp", "🌐", Color(0xFFFFF3E0))
    data object Hilt : Category("Hilt DI", "의존성 주입, 테스트, Mock", "💉", Color(0xFFE8EAF6))
    data object Testing : Category("Testing", "Unit, MockK, Coroutine, Compose UI, TDD", "🧪", Color(0xFFE8F5E9))
    data object Memory : Category("Memory", "메모리 누수 패턴, 디버깅 도구", "🧠", Color(0xFFFCE4EC))
    data object Kotlin : Category("Kotlin 심화", "함수형, 객체지향, 제네릭", "🎯", Color(0xFFF3E5F5))
}

sealed class ComposeScreen(val title: String, val subtitle: String, val color: Color) {
    data object StateSaving : ComposeScreen("State 저장", "remember vs rememberSaveable", Color(0xFFE3F2FD))
    data object Stability : ComposeScreen("Stability", "Recomposition 최적화", Color(0xFFFFF3E0))
    data object SideEffects : ComposeScreen("Side Effects", "LaunchedEffect, DisposableEffect", Color(0xFFFCE4EC))
    data object StrongSkipping : ComposeScreen("Strong Skipping", "ImmutableList vs List", Color(0xFFE8F5E9))
    data object DerivedState : ComposeScreen("DerivedStateOf", "derivedStateOf vs remember(key)", Color(0xFFF3E5F5))
}

sealed class NetworkingScreen(val title: String, val subtitle: String, val color: Color) {
    data object HttpVsRetrofit : NetworkingScreen("HttpURLConnection vs Retrofit", "저수준 vs 고수준 API 비교", Color(0xFFE8F5E9))
}

sealed class HiltScreen(val title: String, val subtitle: String, val color: Color) {
    data object HiltBasics : HiltScreen("Hilt 기초", "@HiltAndroidApp, @Inject, @Module", Color(0xFFE8EAF6))
    data object HiltComparison : HiltScreen("Hilt 사용 전/후 비교", "수동 DI vs Hilt 코드 비교", Color(0xFFFFF3E0))
}

sealed class TestingScreen(val title: String, val subtitle: String, val color: Color) {
    data object TestingOverview : TestingScreen("Testing 개요", "테스트 피라미드, 철학, 가이드", Color(0xFFE8F5E9))
}

sealed class MemoryScreen(val title: String, val subtitle: String, val color: Color) {
    data object MemoryLeak : MemoryScreen("메모리 누수 패턴", "7가지 누수 패턴과 해결책", Color(0xFFFCE4EC))
}

sealed class KotlinScreen(val title: String, val subtitle: String, val color: Color) {
    data object Functional : KotlinScreen("함수형 프로그래밍", "순수 함수, 고차 함수, Scope Functions", Color(0xFFE3F2FD))
    data object ObjectOriented : KotlinScreen("객체지향 프로그래밍", "캡슐화, 다형성, SOLID", Color(0xFFFFF3E0))
    data object Generics : KotlinScreen("제네릭", "Variance, Constraints, reified", Color(0xFFE8F5E9))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyNavigator() {
    // 네비게이션 상태: "Root", "Compose", "Networking", 또는 세부 화면 이름
    var currentScreen by rememberSaveable { mutableStateOf("Root") }

    // 뒤로가기 처리
    BackHandler(enabled = currentScreen != "Root") {
        currentScreen = when (currentScreen) {
            "StateSaving", "Stability", "SideEffects", "StrongSkipping", "DerivedState" -> "Compose"
            "HttpVsRetrofit" -> "Networking"
            "HiltBasics", "HiltComparison" -> "Hilt"
            "TestingOverview" -> "Testing"
            "MemoryLeak" -> "Memory"
            "FunctionalProgramming", "ObjectOriented", "Generics" -> "Kotlin"
            else -> "Root"
        }
    }

    val topBarTitle = when (currentScreen) {
        "Root" -> ""
        "Compose" -> "Compose 학습"
        "Networking" -> "Networking"
        "Hilt" -> "Hilt DI"
        "Testing" -> "Testing"
        "Memory" -> "Memory"
        "Kotlin" -> "Kotlin 심화"
        "StateSaving" -> ComposeScreen.StateSaving.title
        "Stability" -> ComposeScreen.Stability.title
        "SideEffects" -> ComposeScreen.SideEffects.title
        "StrongSkipping" -> ComposeScreen.StrongSkipping.title
        "DerivedState" -> ComposeScreen.DerivedState.title
        "HttpVsRetrofit" -> NetworkingScreen.HttpVsRetrofit.title
        "HiltBasics" -> HiltScreen.HiltBasics.title
        "HiltComparison" -> HiltScreen.HiltComparison.title
        "TestingOverview" -> TestingScreen.TestingOverview.title
        "MemoryLeak" -> MemoryScreen.MemoryLeak.title
        "FunctionalProgramming" -> KotlinScreen.Functional.title
        "ObjectOriented" -> KotlinScreen.ObjectOriented.title
        "Generics" -> KotlinScreen.Generics.title
        else -> ""
    }

    val backDestination = when (currentScreen) {
        "StateSaving", "Stability", "SideEffects", "StrongSkipping", "DerivedState" -> "Compose"
        "HttpVsRetrofit" -> "Networking"
        "HiltBasics", "HiltComparison" -> "Hilt"
        "TestingOverview" -> "Testing"
        "MemoryLeak" -> "Memory"
        "FunctionalProgramming", "ObjectOriented", "Generics" -> "Kotlin"
        else -> "Root"
    }

    Scaffold(
        topBar = {
            if (currentScreen != "Root") {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { currentScreen = backDestination }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            "Root" -> RootScreen(
                modifier = Modifier.padding(innerPadding),
                onCategorySelect = { currentScreen = it }
            )
            "Compose" -> ComposeHomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "Networking" -> NetworkingHomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "Hilt" -> HiltHomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "Testing" -> TestingHomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "Memory" -> MemoryHomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "Kotlin" -> KotlinStudyScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "StateSaving" -> RememberVsSaveableScreen(Modifier.padding(innerPadding))
            "Stability" -> StabilityRecompositionScreen(Modifier.padding(innerPadding))
            "SideEffects" -> SideEffectScreen(Modifier.padding(innerPadding))
            "StrongSkipping" -> StrongSkippingModeScreen(Modifier.padding(innerPadding))
            "DerivedState" -> DerivedStateOfScreen(Modifier.padding(innerPadding))
            // Networking 세부 화면
            "HttpVsRetrofit" -> HttpVsRetrofitScreen(Modifier.padding(innerPadding))
            // Hilt 세부 화면
            "HiltBasics" -> HiltStudyScreen(Modifier.padding(innerPadding))
            "HiltComparison" -> HiltComparisonScreen(Modifier.padding(innerPadding))
            // Testing 세부 화면
            "TestingOverview" -> TestingStudyScreen(Modifier.padding(innerPadding))
            // Memory 세부 화면
            "MemoryLeak" -> MemoryLeakScreen(Modifier.padding(innerPadding))
            // Kotlin 세부 화면
            "FunctionalProgramming" -> FunctionalProgrammingScreen(Modifier.padding(innerPadding))
            "ObjectOriented" -> ObjectOrientedScreen(Modifier.padding(innerPadding))
            "Generics" -> GenericsScreen(Modifier.padding(innerPadding))
        }
    }
}

// ========================================================================
// Root Screen - 카테고리 선택
// ========================================================================

@Composable
fun RootScreen(
    modifier: Modifier = Modifier,
    onCategorySelect: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Android 학습",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "학습하고 싶은 카테고리를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Compose 학습
        CategoryCard(
            emoji = Category.Compose.emoji,
            title = Category.Compose.title,
            subtitle = Category.Compose.subtitle,
            description = "remember, Recomposition, Side Effects, Strong Skipping Mode 등",
            color = Category.Compose.color,
            onClick = { onCategorySelect("Compose") }
        )

        // Networking
        CategoryCard(
            emoji = Category.Networking.emoji,
            title = Category.Networking.title,
            subtitle = Category.Networking.subtitle,
            description = "HttpURLConnection vs Retrofit, OkHttp, JSON 직렬화 등",
            color = Category.Networking.color,
            onClick = { onCategorySelect("Networking") }
        )

        // Hilt DI
        CategoryCard(
            emoji = Category.Hilt.emoji,
            title = Category.Hilt.title,
            subtitle = Category.Hilt.subtitle,
            description = "Dagger Hilt 의존성 주입, Mock/Fake 테스트, Clean Architecture",
            color = Category.Hilt.color,
            onClick = { onCategorySelect("Hilt") }
        )

        // Testing
        CategoryCard(
            emoji = Category.Testing.emoji,
            title = Category.Testing.title,
            subtitle = Category.Testing.subtitle,
            description = "JUnit, MockK, Coroutine Test, Compose UI Test, TDD 실습",
            color = Category.Testing.color,
            onClick = { onCategorySelect("Testing") }
        )

        // Memory
        CategoryCard(
            emoji = Category.Memory.emoji,
            title = Category.Memory.title,
            subtitle = Category.Memory.subtitle,
            description = "메모리 누수 7가지 패턴, LeakCanary, Memory Profiler 사용법",
            color = Category.Memory.color,
            onClick = { onCategorySelect("Memory") }
        )

        // Kotlin 심화
        CategoryCard(
            emoji = Category.Kotlin.emoji,
            title = Category.Kotlin.title,
            subtitle = Category.Kotlin.subtitle,
            description = "순수 함수, 고차 함수, 캡슐화, 다형성, Variance, reified 등",
            color = Category.Kotlin.color,
            onClick = { onCategorySelect("Kotlin") }
        )
    }
}

@Composable
fun CategoryCard(
    emoji: String,
    title: String,
    subtitle: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// ========================================================================
// Compose Home Screen - Compose 학습 주제 선택
// ========================================================================

@Composable
fun ComposeHomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "학습하고 싶은 주제를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. State 저장
        StudyCard(
            title = ComposeScreen.StateSaving.title,
            subtitle = ComposeScreen.StateSaving.subtitle,
            description = "Configuration Change와 Process Death에서 상태 유지 방법",
            color = ComposeScreen.StateSaving.color,
            onClick = { onNavigate("StateSaving") }
        )

        // 2. Stability
        StudyCard(
            title = ComposeScreen.Stability.title,
            subtitle = ComposeScreen.Stability.subtitle,
            description = "Immutable, Stable, Unstable과 Recomposition 스킵 조건",
            color = ComposeScreen.Stability.color,
            onClick = { onNavigate("Stability") }
        )

        // 3. Side Effects
        StudyCard(
            title = ComposeScreen.SideEffects.title,
            subtitle = ComposeScreen.SideEffects.subtitle,
            description = "LaunchedEffect, DisposableEffect, rememberUpdatedState",
            color = ComposeScreen.SideEffects.color,
            onClick = { onNavigate("SideEffects") }
        )

        // 4. Strong Skipping
        StudyCard(
            title = ComposeScreen.StrongSkipping.title,
            subtitle = ComposeScreen.StrongSkipping.subtitle,
            description = "Strong Skipping Mode와 ImmutableList 사용 가이드",
            color = ComposeScreen.StrongSkipping.color,
            onClick = { onNavigate("StrongSkipping") }
        )

        // 5. DerivedStateOf
        StudyCard(
            title = ComposeScreen.DerivedState.title,
            subtitle = ComposeScreen.DerivedState.subtitle,
            description = "상태 변화 빈도를 줄이는 derivedStateOf와 remember(key) 비교",
            color = ComposeScreen.DerivedState.color,
            onClick = { onNavigate("DerivedState") }
        )
    }
}

// ========================================================================
// Networking Home Screen - Networking 학습 주제 선택
// ========================================================================

@Composable
fun NetworkingHomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "학습하고 싶은 주제를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. HttpURLConnection vs Retrofit
        StudyCard(
            title = NetworkingScreen.HttpVsRetrofit.title,
            subtitle = NetworkingScreen.HttpVsRetrofit.subtitle,
            description = "저수준 API와 고수준 추상화의 차이점, Dynamic Proxy 이해하기",
            color = NetworkingScreen.HttpVsRetrofit.color,
            onClick = { onNavigate("HttpVsRetrofit") }
        )

        // TODO: 추가 예정
        // - OkHttp 내부 동작
        // - JSON 직렬화 (Gson vs Kotlinx.Serialization)
        // - Interceptor 활용
    }
}

// ========================================================================
// Hilt Home Screen - Hilt DI 학습 주제 선택
// ========================================================================

@Composable
fun HiltHomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "학습하고 싶은 주제를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Hilt 기초
        StudyCard(
            title = HiltScreen.HiltBasics.title,
            subtitle = HiltScreen.HiltBasics.subtitle,
            description = "Hilt를 사용하는 이유, 기본 어노테이션, Mock/Fake 테스트 가이드",
            color = HiltScreen.HiltBasics.color,
            onClick = { onNavigate("HiltBasics") }
        )

        // 2. Hilt 사용 전/후 비교
        StudyCard(
            title = HiltScreen.HiltComparison.title,
            subtitle = HiltScreen.HiltComparison.subtitle,
            description = "같은 기능을 수동 DI와 Hilt로 구현한 코드 비교, 왜 Hilt를 쓰는지 이해",
            color = HiltScreen.HiltComparison.color,
            onClick = { onNavigate("HiltComparison") }
        )

        // TODO: 추가 예정
        // - Hilt with Compose (hiltViewModel)
        // - Custom Scope & Qualifier
        // - Multi-module Hilt
    }
}

// ========================================================================
// Testing Home Screen - Testing 학습 주제 선택
// ========================================================================

@Composable
fun TestingHomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "학습하고 싶은 주제를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Testing 개요
        StudyCard(
            title = TestingScreen.TestingOverview.title,
            subtitle = TestingScreen.TestingOverview.subtitle,
            description = "테스트 피라미드, 언제 테스트해야 하는지, Unit/MockK/Coroutine/Compose/TDD",
            color = TestingScreen.TestingOverview.color,
            onClick = { onNavigate("TestingOverview") }
        )

        // TODO: 추가 예정
        // - Screenshot Testing
        // - Performance Testing
        // - Integration Testing
    }
}

// ========================================================================
// Memory Home Screen - Memory 학습 주제 선택
// ========================================================================

@Composable
fun MemoryHomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "학습하고 싶은 주제를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        StudyCard(
            title = MemoryScreen.MemoryLeak.title,
            subtitle = MemoryScreen.MemoryLeak.subtitle,
            description = "Static Reference, Inner Class, Handler, Singleton 등 7가지 누수 패턴과 해결책",
            color = MemoryScreen.MemoryLeak.color,
            onClick = { onNavigate("MemoryLeak") }
        )
    }
}

// ========================================================================
// Networking Placeholder Screen - 아직 구현 전
// ========================================================================

@Composable
fun NetworkingPlaceholderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🚧",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "준비 중입니다",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Networking 학습 콘텐츠를 준비 중입니다.\n곧 추가될 예정입니다!",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StudyCard(
    title: String,
    subtitle: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
