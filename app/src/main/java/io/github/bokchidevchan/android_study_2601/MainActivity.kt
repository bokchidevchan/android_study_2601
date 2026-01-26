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
import io.github.bokchidevchan.android_study_2601.study.derived.DerivedStateOfScreen
import io.github.bokchidevchan.android_study_2601.study.effect.SideEffectScreen
import io.github.bokchidevchan.android_study_2601.study.immutable.StrongSkippingModeScreen
import io.github.bokchidevchan.android_study_2601.study.recomposition.StabilityRecompositionScreen
import io.github.bokchidevchan.android_study_2601.study.networking.HttpVsRetrofitScreen
import io.github.bokchidevchan.android_study_2601.study.state.RememberVsSaveableScreen
import io.github.bokchidevchan.android_study_2601.ui.theme.Android_study_2601Theme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyNavigator() {
    // 네비게이션 상태: "Root", "Compose", "Networking", 또는 세부 화면 이름
    var currentScreen by rememberSaveable { mutableStateOf("Root") }

    // 뒤로가기 처리
    BackHandler(enabled = currentScreen != "Root") {
        currentScreen = when (currentScreen) {
            // Compose 세부 화면에서 뒤로가기 -> Compose 카테고리로
            "StateSaving", "Stability", "SideEffects", "StrongSkipping", "DerivedState" -> "Compose"
            // Networking 세부 화면에서 뒤로가기 -> Networking 카테고리로
            "HttpVsRetrofit" -> "Networking"
            // 카테고리에서 뒤로가기 -> Root로
            else -> "Root"
        }
    }

    val topBarTitle = when (currentScreen) {
        "Root" -> ""
        "Compose" -> "Compose 학습"
        "Networking" -> "Networking"
        "StateSaving" -> ComposeScreen.StateSaving.title
        "Stability" -> ComposeScreen.Stability.title
        "SideEffects" -> ComposeScreen.SideEffects.title
        "StrongSkipping" -> ComposeScreen.StrongSkipping.title
        "DerivedState" -> ComposeScreen.DerivedState.title
        "HttpVsRetrofit" -> NetworkingScreen.HttpVsRetrofit.title
        else -> ""
    }

    val backDestination = when (currentScreen) {
        "StateSaving", "Stability", "SideEffects", "StrongSkipping", "DerivedState" -> "Compose"
        "HttpVsRetrofit" -> "Networking"
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
            // Compose 세부 화면
            "StateSaving" -> RememberVsSaveableScreen(Modifier.padding(innerPadding))
            "Stability" -> StabilityRecompositionScreen(Modifier.padding(innerPadding))
            "SideEffects" -> SideEffectScreen(Modifier.padding(innerPadding))
            "StrongSkipping" -> StrongSkippingModeScreen(Modifier.padding(innerPadding))
            "DerivedState" -> DerivedStateOfScreen(Modifier.padding(innerPadding))
            // Networking 세부 화면
            "HttpVsRetrofit" -> HttpVsRetrofitScreen(Modifier.padding(innerPadding))
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
