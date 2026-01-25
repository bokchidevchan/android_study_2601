package io.github.bokchidevchan.android_study_2601

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import io.github.bokchidevchan.android_study_2601.study.state.RememberVsSaveableScreen
import io.github.bokchidevchan.android_study_2601.ui.theme.Android_study_2601Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_study_2601Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RememberVsSaveableScreen()
                }
            }
        }
    }
}
