package io.github.bokchidevchan.android_study_2601.study.coroutine

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
 * 📚 Flow (플로우)
 * ========================================================================
 *
 * Flow = 비동기 데이터 스트림
 *
 * 종류:
 * 1. Flow - Cold Stream (수집할 때 시작)
 * 2. StateFlow - Hot Stream + 현재 값 유지 (LiveData 대체)
 * 3. SharedFlow - Hot Stream + 이벤트 브로드캐스트
 * 4. Channel - Hot Stream + 점대점 통신
 *
 * 핵심: Cold vs Hot, collect vs collectLatest
 */
@Composable
fun CoroutineFlowScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        FlowOverviewSection()
        ColdVsHotSection()
        StateFlowSection()
        SharedFlowSection()
        FlowOperatorsSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "🌊 Flow",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Flow, StateFlow, SharedFlow - 리액티브 스트림 완벽 이해",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FlowOverviewSection() {
    SectionCard(title = "1️⃣ Flow 종류 비교") {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text("Type", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f), fontSize = 11.sp)
                Text("Hot/Cold", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f), fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("Replay", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f), fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("Use Case", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), fontSize = 11.sp)
            }

            val flows = listOf(
                listOf("Flow", "Cold", "No", "일회성 데이터, API 응답"),
                listOf("StateFlow", "Hot", "Latest", "UI 상태 (LiveData 대체)"),
                listOf("SharedFlow", "Hot", "설정가능", "이벤트 (Toast, Nav)"),
                listOf("Channel", "Hot", "No", "Producer-Consumer")
            )

            flows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.Transparent
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .padding(8.dp)
                ) {
                    Text(row[0], fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.2f), fontSize = 11.sp)
                    Text(row[1], modifier = Modifier.weight(0.2f), fontSize = 10.sp, textAlign = TextAlign.Center)
                    Text(row[2], modifier = Modifier.weight(0.2f), fontSize = 10.sp, textAlign = TextAlign.Center)
                    Text(row[3], modifier = Modifier.weight(0.4f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ColdVsHotSection() {
    SectionCard(title = "2️⃣ Cold Stream vs Hot Stream") {
        ComparisonRow(
            leftTitle = "🧊 Cold (Flow)",
            leftItems = listOf(
                "수집할 때 시작",
                "각 수집자 독립 스트림",
                "완료되면 끝",
                "예: API 호출 결과"
            ),
            leftColor = Color(0xFFE3F2FD),
            rightTitle = "🔥 Hot (StateFlow)",
            rightItems = listOf(
                "항상 활성 상태",
                "모든 수집자 공유",
                "항상 현재 값 있음",
                "예: UI 상태"
            ),
            rightColor = Color(0xFFFFF3E0)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 🧊 Cold Flow - 수집할 때마다 새로 시작
fun coldFlow(): Flow<Int> = flow {
    println("Flow started")  // 수집자마다 출력됨
    emit(1)
    emit(2)
}

val flow = coldFlow()
flow.collect { }  // "Flow started" 출력
flow.collect { }  // "Flow started" 또 출력 (독립 실행)

// 🔥 Hot Flow - 이미 실행 중, 수집자는 참여만
private val _state = MutableStateFlow(0)
val state: StateFlow<Int> = _state.asStateFlow()

// 수집자 A
state.collect { }  // 현재 값부터 받음
// 수집자 B  
state.collect { }  // 같은 값 공유
            """.trimIndent()
        )
    }
}

@Composable
private fun StateFlowSection() {
    SectionCard(title = "3️⃣ StateFlow (LiveData 대체)") {
        Text(
            text = "항상 현재 값을 가지는 Hot Stream",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
class UserViewModel : ViewModel() {
    // private MutableStateFlow
    private val _uiState = MutableStateFlow(UiState())
    // public 읽기 전용 StateFlow
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun loadUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            
            try {
                val user = repository.getUser(id)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    user = user
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }
}

// Compose에서 수집
@Composable
fun UserScreen(viewModel: UserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.loading -> LoadingSpinner()
        uiState.error != null -> ErrorMessage(uiState.error)
        else -> UserContent(uiState.user)
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "StateFlow 특징",
            items = listOf(
                "항상 초기값 필요 - MutableStateFlow(initialValue)",
                "새 수집자는 현재 값을 즉시 받음 (replay = 1)",
                "같은 값 연속 emit 시 무시 (distinctUntilChanged)",
                "Compose에서 collectAsStateWithLifecycle() 사용"
            )
        )
    }
}

@Composable
private fun SharedFlowSection() {
    SectionCard(title = "4️⃣ SharedFlow (이벤트용)") {
        Text(
            text = "일회성 이벤트 브로드캐스트 (Toast, Navigation)",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
class UserViewModel : ViewModel() {
    // SharedFlow for one-time events
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    fun onSaveClick() {
        viewModelScope.launch {
            repository.save()
            // 이벤트 발생 - 모든 수집자에게 전달
            _events.emit(UiEvent.ShowToast("저장 완료!"))
            _events.emit(UiEvent.NavigateBack)
        }
    }
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}

// Compose에서 이벤트 수집
@Composable
fun UserScreen(viewModel: UserViewModel) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> 
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                UiEvent.NavigateBack -> 
                    navController.popBackStack()
            }
        }
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow(
            leftTitle = "StateFlow",
            leftItems = listOf("현재 상태", "항상 값 있음", "UI 상태 표현", "같은 값 무시"),
            leftColor = Color(0xFFE8F5E9),
            rightTitle = "SharedFlow",
            rightItems = listOf("일회성 이벤트", "초기값 없음", "Toast, Nav", "모든 값 전달"),
            rightColor = Color(0xFFFCE4EC)
        )
    }
}

@Composable
private fun FlowOperatorsSection() {
    SectionCard(title = "5️⃣ Flow 연산자") {
        Text(
            text = "collect vs collectLatest, map, filter 등",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// collect vs collectLatest
flow.collect { value ->
    // 느린 처리 시 모든 값 순차 처리
    delay(1000)
    println(value)  // 1, 2, 3 모두 출력
}

flow.collectLatest { value ->
    // 새 값 오면 이전 처리 취소
    delay(1000)
    println(value)  // 마지막 값만 출력 (빠른 검색어 입력에 유용)
}

// 변환 연산자
flow
    .map { it * 2 }           // 값 변환
    .filter { it > 10 }       // 필터링
    .take(5)                  // 처음 5개만
    .distinctUntilChanged()   // 중복 제거
    .debounce(300)            // 300ms 대기 후 마지막 값만
    .catch { e -> emit(-1) }  // 에러 처리
    .flowOn(Dispatchers.IO)   // 업스트림 Dispatcher 지정
    .collect { }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "🎯 자주 사용하는 연산자",
            items = listOf(
                "map - 값 변환 (A → B)",
                "filter - 조건에 맞는 값만",
                "collectLatest - 새 값 오면 이전 취소 (검색어 입력)",
                "debounce - 일정 시간 대기 후 마지막 값",
                "combine - 여러 Flow 조합",
                "flatMapLatest - Flow<Flow<T>> → Flow<T> (최신만)"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 검색 기능 구현 예시
private val searchQuery = MutableStateFlow("")

val searchResults = searchQuery
    .debounce(300)         // 타이핑 멈추고 300ms 대기
    .filter { it.length >= 2 }  // 2글자 이상만
    .distinctUntilChanged() // 같은 검색어 무시
    .flatMapLatest { query ->
        repository.search(query)  // 새 검색어 오면 이전 취소
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: Cold vs Hot Flow?" to
                    "A: Cold는 수집 시 시작(각자 독립), Hot은 항상 활성(공유). Flow=Cold, StateFlow/SharedFlow=Hot",
            "Q: StateFlow vs LiveData?" to
                    "A: StateFlow는 코루틴 기반, 초기값 필수, Flow 연산자 사용 가능. LiveData는 Android 전용",
            "Q: StateFlow vs SharedFlow?" to
                    "A: StateFlow는 상태(현재값), SharedFlow는 이벤트(일회성). StateFlow는 같은 값 무시",
            "Q: collectLatest 언제?" to
                    "A: 새 값이 오면 이전 처리 취소해야 할 때. 검색어 입력, debounce와 함께 사용",
            "Q: flowOn vs launchIn?" to
                    "A: flowOn은 업스트림 Dispatcher 지정, launchIn은 다운스트림(collect) Scope 지정"
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

@Composable
private fun ComparisonRow(
    leftTitle: String,
    leftItems: List<String>,
    leftColor: Color,
    rightTitle: String,
    rightItems: List<String>,
    rightColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .background(leftColor, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(text = leftTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            leftItems.forEach { Text(text = "• $it", fontSize = 11.sp) }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .background(rightColor, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(text = rightTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            rightItems.forEach { Text(text = "• $it", fontSize = 11.sp) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoroutineFlowScreenPreview() {
    CoroutineFlowScreen()
}
