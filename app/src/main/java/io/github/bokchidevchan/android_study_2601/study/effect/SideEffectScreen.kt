package io.github.bokchidevchan.android_study_2601.study.effect

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ========================================================================
 * 📚 학습 목표: Side Effects 완벽 이해하기
 * ========================================================================
 *
 * Side Effect = 컴포저블의 실행(Composition) 이외에 발생하는 모든 부수 효과
 *
 * 컴포저블은 언제든 다시 그려질 수(Recomposition) 있고,
 * 심지어 중간에 취소될 수도 있습니다.
 * 따라서 네트워크 요청이나 타이머 같은 작업은
 * Compose의 생명주기에 동기화되어야 합니다.
 *
 * ========================================================================
 */

@Composable
fun SideEffectScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📚 Side Effects",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Compose 생명주기와 부수 효과 관리",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 핵심 개념
        EffectOverviewCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 2. LaunchedEffect 실습
        LaunchedEffectCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 3. DisposableEffect 실습
        DisposableEffectCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 4. rememberUpdatedState (Stale State 해결)
        RememberUpdatedStateCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. rememberCoroutineScope vs LaunchedEffect
        CoroutineScopeComparisonCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 6. SideEffect
        SideEffectExampleCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 7. ViewModel vs Side Effects (수명 주기 차이)
        ViewModelVsSideEffectCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 8. ViewModel과 Side Effects 협업 패턴
        ViewModelCollaborationCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 9. Senior's Warning
        SeniorWarningCard()
    }
}

@Composable
fun EffectOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔑 Side Effects API 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // LaunchedEffect
            EffectItem(
                emoji = "🚀",
                title = "LaunchedEffect",
                description = "코루틴 실행, Key 변경 시 취소 후 재시작",
                useCase = "네트워크 요청, 스낵바, 타이머",
                color = Color(0xFFE3F2FD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DisposableEffect
            EffectItem(
                emoji = "🧹",
                title = "DisposableEffect",
                description = "정리(Clean-up)가 필요한 On/Off 메커니즘",
                useCase = "리스너 등록/해제, 센서 구독",
                color = Color(0xFFFCE4EC)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SideEffect
            EffectItem(
                emoji = "🔄",
                title = "SideEffect",
                description = "Recomposition 성공 직후 매번 실행",
                useCase = "외부 라이브러리(Analytics)와 동기화",
                color = Color(0xFFF3E5F5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // rememberUpdatedState
            EffectItem(
                emoji = "📌",
                title = "rememberUpdatedState",
                description = "Effect 재시작 없이 최신 값 참조",
                useCase = "Stale State 문제 해결",
                color = Color(0xFFE8F5E9)
            )
        }
    }
}

@Composable
fun EffectItem(
    emoji: String,
    title: String,
    description: String,
    useCase: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "$emoji $title",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(text = description, fontSize = 12.sp)
            Text(
                text = "용도: $useCase",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun LaunchedEffectCard() {
    var count by remember { mutableIntStateOf(0) }
    var effectLog by remember { mutableStateOf("대기 중...") }

    // Key가 Unit이면 최초 1회만 실행
    LaunchedEffect(Unit) {
        Log.d("SideEffectStudy", "LaunchedEffect 시작 (최초 1회)")
        effectLog = "LaunchedEffect 시작됨!"
    }

    // Key가 count면 count가 바뀔 때마다 재실행
    LaunchedEffect(count) {
        if (count > 0) {
            Log.d("SideEffectStudy", "count 변경 감지: $count")
            effectLog = "count=$count 감지! 3초 후 작업 완료..."
            delay(3000)
            effectLog = "작업 완료! (count=$count)"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚀 LaunchedEffect 실습",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "현재 count: $count", fontSize = 14.sp)
            Text(
                text = "상태: $effectLog",
                fontSize = 14.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { count++ }) {
                Text("count 증가 (Effect 재실행)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// Key가 변경되면 기존 코루틴 취소 후 재시작
LaunchedEffect(count) {
    delay(3000)  // 3초 대기 중 count 바뀌면?
    // → 기존 작업 취소, 새로 시작!
    doSomething()
}

// ⚠️ LaunchedEffect(Unit) 주의!
// 최초 1회만 실행됨
// 상태 변화 감지 필요하면 Key로 전달
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun DisposableEffectCard() {
    var isTimerActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // 타이머 On/Off
    if (isTimerActive) {
        DisposableEffect(Unit) {
            Log.d("SideEffectStudy", "DisposableEffect: 타이머 시작")
            val startTime = System.currentTimeMillis()

            // 간단한 타이머 (실제로는 Timer 대신 코루틴 권장)
            val timer = java.util.Timer()
            timer.scheduleAtFixedRate(object : java.util.TimerTask() {
                override fun run() {
                    elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                }
            }, 0, 1000)

            onDispose {
                // 컴포저블이 사라지거나 isTimerActive가 false가 되면 호출
                Log.d("SideEffectStudy", "DisposableEffect: 타이머 정리!")
                timer.cancel()
                elapsedSeconds = 0
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧹 DisposableEffect 실습",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isTimerActive) "타이머: ${elapsedSeconds}초" else "타이머 비활성",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTimerActive) Color.Red else Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { isTimerActive = !isTimerActive },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTimerActive) Color.Red else Color.Green
                )
            ) {
                Text(if (isTimerActive) "타이머 중지" else "타이머 시작")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
DisposableEffect(key) {
    // 리소스 획득 (리스너 등록, 타이머 시작)
    val listener = registerListener()

    onDispose {
        // 반드시 정리! (메모리 누수 방지)
        unregisterListener(listener)
    }
}

// ⚠️ onDispose 블록 필수!
// 없으면 컴파일 에러
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RememberUpdatedStateCard() {
    var message by remember { mutableStateOf("초기 메시지") }
    var countdown by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // 💡 핵심: rememberUpdatedState로 최신 값 유지
    val currentMessage by rememberUpdatedState(message)

    // 5초 후 메시지 출력 (Stale State 테스트)
    LaunchedEffect(isRunning) {
        if (isRunning) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            // rememberUpdatedState 덕분에 항상 최신 message 참조
            Log.d("SideEffectStudy", "5초 후 메시지: $currentMessage")
            isRunning = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📌 rememberUpdatedState (Stale State 해결)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "현재 메시지: $message",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (isRunning) {
                Text(
                    text = "⏰ ${countdown}초 후 Logcat에 메시지 출력...",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { message = "변경된 메시지 ${System.currentTimeMillis() % 1000}" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("메시지 변경", fontSize = 11.sp)
                }

                Button(
                    onClick = { isRunning = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isRunning
                ) {
                    Text("5초 타이머", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// ❌ 문제: Stale State (오래된 값 참조)
LaunchedEffect(Unit) {
    delay(5000)
    Log.d("TAG", message)  // 5초 전 값!
}

// ✅ 해결: rememberUpdatedState
val currentMessage by rememberUpdatedState(message)

LaunchedEffect(Unit) {
    delay(5000)
    Log.d("TAG", currentMessage)  // 최신 값!
}
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 5초 타이머 실행 중 메시지를 바꿔보세요!\n" +
                       "Logcat에서 최신 메시지가 출력되는지 확인",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun CoroutineScopeComparisonCard() {
    var clickCount by remember { mutableIntStateOf(0) }
    var scopeLog by remember { mutableStateOf("버튼을 클릭하세요") }

    // 이벤트 핸들러용 코루틴 스코프
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔀 rememberCoroutineScope vs LaunchedEffect",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "클릭 횟수: $clickCount", fontSize = 14.sp)
            Text(
                text = scopeLog,
                fontSize = 14.sp,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    clickCount++
                    // ✅ 이벤트 핸들러에서는 rememberCoroutineScope 사용
                    coroutineScope.launch {
                        scopeLog = "작업 시작..."
                        delay(2000)
                        scopeLog = "작업 완료! (클릭 $clickCount)"
                    }
                }
            ) {
                Text("클릭 (coroutineScope.launch)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// ✅ LaunchedEffect: 선언적 (자동 취소)
// → 컴포지션 진입 시 실행, 이탈 시 취소
LaunchedEffect(key) {
    fetchData()
}

// ✅ rememberCoroutineScope: 명령적 (이벤트용)
// → 클릭 등 사용자 액션에 반응
val scope = rememberCoroutineScope()
Button(onClick = {
    scope.launch { fetchData() }
})

// ❌ 안티 패턴: LaunchedEffect 내에서 launch
LaunchedEffect(Unit) {
    launch { ... }  // 불필요한 중첩!
}
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SideEffectExampleCard() {
    var recomposeCount by remember { mutableIntStateOf(0) }
    var lastSyncedValue by remember { mutableIntStateOf(0) }

    // Recomposition 성공 시마다 실행 (코루틴 X)
    SideEffect {
        Log.d("SideEffectStudy", "SideEffect: recomposeCount = $recomposeCount")
        lastSyncedValue = recomposeCount
    }

    recomposeCount++

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 SideEffect 실습",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Recomposition 횟수: $recomposeCount", fontSize = 14.sp)
            Text(
                text = "마지막 동기화 값: $lastSyncedValue",
                fontSize = 14.sp,
                color = Color.Magenta
            )

            Spacer(modifier = Modifier.height(8.dp))

            var dummy by remember { mutableIntStateOf(0) }
            Button(onClick = { dummy++ }) {
                Text("Recomposition 유발")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// SideEffect: Recomposition 성공 시마다 실행
// 코루틴 X, suspend 함수 호출 불가

SideEffect {
    // Compose 상태 → 외부 시스템 동기화
    analytics.logScreenView(screenName)
    externalLibrary.updateValue(composeState)
}

// ⚠️ 주의
// - 무거운 작업 금지 (UI 스레드에서 실행)
// - suspend 필요하면 LaunchedEffect 사용
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * ========================================================================
 * 🏗️ 본질과 원리: ViewModel vs Side Effects
 * ========================================================================
 *
 * "비즈니스 로직과 데이터 흐름은 ViewModel이 담당하고,
 *  UI와 밀접한 효과는 Side-Effect API가 담당한다"
 *
 * 핵심은 **수명 주기(Lifecycle)**의 차이입니다.
 * ========================================================================
 */
@Composable
fun ViewModelVsSideEffectCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏗️ ViewModel vs Side Effects",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "수명 주기(Lifecycle)의 차이가 핵심!",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ViewModel Scope
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "📦 ViewModel Scope",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• 화면(Activity/Fragment)이 살아있는 동안 유지",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 화면 회전에도 작업 유지됨",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 용도: 데이터 로딩, DB 쓰기, 비즈니스 연산",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compose Side-Effect Scope
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "🎨 Compose Side-Effect Scope",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• 컴포저블이 화면에 있는 동안만 유지",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 컴포저블 제거 시 즉시 정리(onDispose)",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 용도: Snackbar, 스크롤, 포커스, 센서 구독",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 언제 어디서 써야 할까?
            Text(
                text = "⚖️ 트레이드오프: 언제 어디서?",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
| 상황                  | 추천 도구          |
|-----------------------|-------------------|
| API 호출, 데이터 가공   | ViewModel         |
| Snackbar, Toast 표시  | LaunchedEffect    |
| 자동 스크롤, 포커스     | LaunchedEffect    |
| 센서/리스너 등록       | DisposableEffect  |
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun ViewModelCollaborationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🤝 ViewModel + Side Effects 협업 패턴",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "네트워크는 ViewModel, UI 효과는 Compose에서!",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
@Composable
fun UserProfileScreen(
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    // ✅ UI 효과: LaunchedEffect로 Snackbar 표시
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Column {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Text("사용자: ${'$'}{uiState.userName}")
            Button(onClick = {
                // ✅ 비즈니스 로직은 ViewModel에서!
                viewModel.loadUserData()
            }) {
                Text("데이터 불러오기")
            }
        }
    }
}
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 핵심 포인트",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "• ViewModel: 데이터 로딩, 상태(State) 관리\n" +
                       "• LaunchedEffect: ViewModel 이벤트 관찰 → UI 반응\n" +
                       "• 버튼 클릭 → ViewModel 함수 호출",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SeniorWarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ Senior's Warning: ViewModel에서 하면 안 되는 일",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "많은 주니어들이 \"UI 조작 로직\"까지 ViewModel에 넣으려다 실패합니다.",
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 잘못된 예시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// ❌ 안티 패턴: ViewModel에서 UI 조작
class BadViewModel : ViewModel() {
    fun scrollToTop(scrollState: ScrollState) {
        // ScrollState는 UI 요소!
        // ViewModel은 이에 대해 몰라야 함 (관심사 분리)
        scrollState.animateScrollTo(0) // 컴파일 에러!
    }
}

// ✅ 올바른 패턴: 상태만 내려주기
class GoodViewModel : ViewModel() {
    private val _shouldScrollToTop = MutableStateFlow(false)
    val shouldScrollToTop = _shouldScrollToTop.asStateFlow()

    fun requestScrollToTop() {
        _shouldScrollToTop.value = true
    }

    fun onScrolled() {
        _shouldScrollToTop.value = false
    }
}

// ✅ Compose에서 UI 조작 실행
@Composable
fun Screen(viewModel: GoodViewModel) {
    val scrollState = rememberScrollState()
    val shouldScroll by viewModel.shouldScrollToTop
        .collectAsState()

    LaunchedEffect(shouldScroll) {
        if (shouldScroll) {
            scrollState.animateScrollTo(0)
            viewModel.onScrolled()
        }
    }
}
                    """.trimIndent(),
                    color = Color(0xFFFFCC80),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 면접 질문
            Text(
                text = "🧐 면접 질문",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Q1. LaunchedEffect(Unit)과 onClick에서 viewModelScope.launch의 차이?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "A: LaunchedEffect(Unit)은 화면 진입 시 자동 실행(초기화용), " +
                               "onClick의 launch는 사용자 액션에 의한 명시적 실행",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Q2. ViewModel이 있는데 DisposableEffect가 필요한 경우?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "A: 화면이 보일 때만 위치 정보 수집하는 경우. " +
                               "ViewModel은 백그라운드에서도 살아있지만, " +
                               "DisposableEffect는 컴포저블 제거 시 즉시 정리됨",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Q3. rememberCoroutineScope는 언제?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "A: onClick 핸들러에서 코루틴이 필요하고, " +
                               "그 작업이 컴포저블 수명과 일치해야 할 때. " +
                               "LaunchedEffect는 선언적, rememberCoroutineScope는 명령적",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SideEffectScreenPreview() {
    MaterialTheme {
        SideEffectScreen()
    }
}
