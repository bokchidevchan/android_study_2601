package io.github.bokchidevchan.android_study_2601

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.bokchidevchan.android_study_2601.study.effect.SideEffectScreen
import io.github.bokchidevchan.android_study_2601.study.recomposition.StabilityRecompositionScreen
import io.github.bokchidevchan.android_study_2601.study.state.RememberVsSaveableScreen
import io.github.bokchidevchan.android_study_2601.ui.theme.Android_study_2601Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_study_2601Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StudyNavigator(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StudyNavigator(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        "State 저장" to "remember vs\nrememberSaveable",
        "Stability" to "Recomposition\n최적화",
        "Side Effects" to "LaunchedEffect\nDisposableEffect"
    )

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (title, subtitle) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Column {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = subtitle,
                                fontSize = 10.sp
                            )
                        }
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> RememberVsSaveableScreen()
            1 -> StabilityRecompositionScreen()
            2 -> SideEffectScreen()
        }
    }
}
