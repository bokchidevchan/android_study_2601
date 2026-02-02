package io.github.bokchidevchan.android_study_2601.study.coroutine

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
 * 📚 Coroutine 학습 - 메인 화면
 * ========================================================================
 *
 * 학습 주제:
 * 1. 코루틴 기초 (suspend, Scope, Job, launch vs async)
 * 2. Dispatchers (Main, IO, Default, Unconfined)
 * 3. Flow (Cold/Hot Stream, StateFlow, SharedFlow)
 * 4. 예외 처리 (CoroutineExceptionHandler, supervisorScope)
 *
 * 코루틴은 비동기 프로그래밍을 단순화하는 Kotlin의 핵심 기능입니다.
 * "suspend"는 "비동기"가 아닌 "일시 중단 가능"을 의미합니다.
 */
@Composable
fun CoroutineStudyScreen(
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
            text = "⚡ Coroutine 학습",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "비동기 프로그래밍의 핵심, 코루틴을 제대로 이해하기",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        StudyCard(
            title = "코루틴 기초",
            subtitle = "suspend, CoroutineScope, Job, Deferred",
            description = "suspend의 진짜 의미, launch vs async, 구조화된 동시성",
            color = Color(0xFFE3F2FD),
            onClick = { onNavigate("CoroutineBasics") }
        )

        StudyCard(
            title = "Dispatchers",
            subtitle = "Main, IO, Default, Unconfined",
            description = "각 Dispatcher의 특성과 언제 어떤 것을 사용해야 하는지",
            color = Color(0xFFFFF3E0),
            onClick = { onNavigate("CoroutineDispatchers") }
        )

        StudyCard(
            title = "Flow",
            subtitle = "Flow, StateFlow, SharedFlow, Channel",
            description = "Cold vs Hot Stream, 리액티브 스트림 완벽 이해",
            color = Color(0xFFE8F5E9),
            onClick = { onNavigate("CoroutineFlow") }
        )

        StudyCard(
            title = "예외 처리",
            subtitle = "CoroutineExceptionHandler, supervisorScope",
            description = "코루틴에서 예외를 올바르게 처리하는 방법",
            color = Color(0xFFFCE4EC),
            onClick = { onNavigate("CoroutineException") }
        )
    }
}

@Composable
private fun StudyCard(
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

@Preview(showBackground = true)
@Composable
private fun CoroutineStudyScreenPreview() {
    CoroutineStudyScreen(onNavigate = {})
}
