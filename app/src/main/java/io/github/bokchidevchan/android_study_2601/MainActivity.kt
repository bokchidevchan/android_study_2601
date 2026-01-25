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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bokchidevchan.android_study_2601.study.derived.DerivedStateOfScreen
import io.github.bokchidevchan.android_study_2601.study.effect.SideEffectScreen
import io.github.bokchidevchan.android_study_2601.study.immutable.StrongSkippingModeScreen
import io.github.bokchidevchan.android_study_2601.study.recomposition.StabilityRecompositionScreen
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

sealed class Screen(val title: String, val subtitle: String, val color: Color) {
    data object Home : Screen("", "", Color.Transparent)
    data object StateSaving : Screen("State 저장", "remember vs rememberSaveable", Color(0xFFE3F2FD))
    data object Stability : Screen("Stability", "Recomposition 최적화", Color(0xFFFFF3E0))
    data object SideEffects : Screen("Side Effects", "LaunchedEffect, DisposableEffect", Color(0xFFFCE4EC))
    data object StrongSkipping : Screen("Strong Skipping", "ImmutableList vs List", Color(0xFFE8F5E9))
    data object DerivedState : Screen("DerivedStateOf", "derivedStateOf vs remember(key)", Color(0xFFF3E5F5))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyNavigator() {
    var currentScreen by rememberSaveable { mutableStateOf<String>("Home") }

    // Back handler
    BackHandler(enabled = currentScreen != "Home") {
        currentScreen = "Home"
    }

    Scaffold(
        topBar = {
            if (currentScreen != "Home") {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                "StateSaving" -> Screen.StateSaving.title
                                "Stability" -> Screen.Stability.title
                                "SideEffects" -> Screen.SideEffects.title
                                "StrongSkipping" -> Screen.StrongSkipping.title
                                "DerivedState" -> Screen.DerivedState.title
                                else -> ""
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { currentScreen = "Home" }) {
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
            "Home" -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigate = { currentScreen = it }
            )
            "StateSaving" -> RememberVsSaveableScreen(Modifier.padding(innerPadding))
            "Stability" -> StabilityRecompositionScreen(Modifier.padding(innerPadding))
            "SideEffects" -> SideEffectScreen(Modifier.padding(innerPadding))
            "StrongSkipping" -> StrongSkippingModeScreen(Modifier.padding(innerPadding))
            "DerivedState" -> DerivedStateOfScreen(Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun HomeScreen(
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
            text = "Compose 학습",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "학습하고 싶은 주제를 선택하세요",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. State 저장
        StudyCard(
            title = Screen.StateSaving.title,
            subtitle = Screen.StateSaving.subtitle,
            description = "Configuration Change와 Process Death에서 상태 유지 방법",
            color = Screen.StateSaving.color,
            onClick = { onNavigate("StateSaving") }
        )

        // 2. Stability
        StudyCard(
            title = Screen.Stability.title,
            subtitle = Screen.Stability.subtitle,
            description = "Immutable, Stable, Unstable과 Recomposition 스킵 조건",
            color = Screen.Stability.color,
            onClick = { onNavigate("Stability") }
        )

        // 3. Side Effects
        StudyCard(
            title = Screen.SideEffects.title,
            subtitle = Screen.SideEffects.subtitle,
            description = "LaunchedEffect, DisposableEffect, rememberUpdatedState",
            color = Screen.SideEffects.color,
            onClick = { onNavigate("SideEffects") }
        )

        // 4. Strong Skipping
        StudyCard(
            title = Screen.StrongSkipping.title,
            subtitle = Screen.StrongSkipping.subtitle,
            description = "Strong Skipping Mode와 ImmutableList 사용 가이드",
            color = Screen.StrongSkipping.color,
            onClick = { onNavigate("StrongSkipping") }
        )

        // 5. DerivedStateOf
        StudyCard(
            title = Screen.DerivedState.title,
            subtitle = Screen.DerivedState.subtitle,
            description = "상태 변화 빈도를 줄이는 derivedStateOf와 remember(key) 비교",
            color = Screen.DerivedState.color,
            onClick = { onNavigate("DerivedState") }
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
