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
 * 📚 Dispatchers (디스패처)
 * ========================================================================
 *
 * Dispatcher = 코루틴이 실행될 스레드를 결정
 *
 * 종류:
 * 1. Main - UI 스레드 (Android)
 * 2. IO - I/O 작업 최적화 (64 스레드)
 * 3. Default - CPU 연산 최적화 (코어 수)
 * 4. Unconfined - 호출한 스레드에서 시작 (위험)
 *
 * 핵심: withContext로 스레드 전환, Main에서 UI 업데이트
 */
@Composable
fun CoroutineDispatchersScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        DispatcherOverviewSection()
        MainDispatcherSection()
        IoDispatcherSection()
        DefaultDispatcherSection()
        WithContextSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "🎯 Dispatchers",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Main, IO, Default - 언제 무엇을 사용하는가?",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DispatcherOverviewSection() {
    SectionCard(title = "1️⃣ Dispatcher 비교 테이블") {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text("Dispatcher", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f), fontSize = 12.sp)
                Text("스레드", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f), fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("용도", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), fontSize = 12.sp)
            }

            val dispatchers = listOf(
                Triple("Main", "UI 스레드", "UI 업데이트, View 조작"),
                Triple("IO", "64개 스레드풀", "네트워크, DB, 파일 I/O"),
                Triple("Default", "코어 수 스레드", "CPU 연산, 정렬, JSON 파싱"),
                Triple("Unconfined", "호출 스레드", "테스트용 (프로덕션 X)")
            )

            dispatchers.forEachIndexed { index, (name, thread, usage) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.Transparent
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .padding(8.dp)
                ) {
                    Text(name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.25f), fontSize = 12.sp)
                    Text(thread, modifier = Modifier.weight(0.25f), fontSize = 11.sp, textAlign = TextAlign.Center)
                    Text(usage, modifier = Modifier.weight(0.5f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MainDispatcherSection() {
    SectionCard(title = "2️⃣ Dispatchers.Main") {
        Text(
            text = "Android UI 스레드에서 실행",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Main Dispatcher 사용 예시
viewModelScope.launch(Dispatchers.Main) {
    // UI 업데이트는 반드시 Main에서!
    textView.text = "Updated"
    progressBar.visibility = View.GONE
    showToast("완료!")
}

// Compose에서는 LaunchedEffect가 기본적으로 Main
LaunchedEffect(key1) {
    // 이미 Main Dispatcher
    delay(1000)
    showSnackbar()
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "📱 Main Dispatcher 사용",
            items = listOf(
                "UI 컴포넌트 업데이트 (TextView, RecyclerView 등)",
                "Toast, Snackbar 표시",
                "Navigation 실행",
                "LiveData/StateFlow 값 설정"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        WarningBox(
            text = "⚠️ Main에서 오래 걸리는 작업 = ANR (Application Not Responding)"
        )
    }
}

@Composable
private fun IoDispatcherSection() {
    SectionCard(title = "3️⃣ Dispatchers.IO") {
        Text(
            text = "I/O 작업에 최적화된 스레드풀 (최대 64개)",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// IO Dispatcher 사용 예시
suspend fun fetchUserFromNetwork(): User = 
    withContext(Dispatchers.IO) {
        // 네트워크 요청 (블로킹 I/O)
        api.getUser()
    }

suspend fun saveToDatabase(user: User) =
    withContext(Dispatchers.IO) {
        // 데이터베이스 쓰기
        database.userDao().insert(user)
    }

suspend fun readFile(path: String): String =
    withContext(Dispatchers.IO) {
        // 파일 읽기
        File(path).readText()
    }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "💾 IO Dispatcher 사용",
            items = listOf(
                "네트워크 요청 (Retrofit, OkHttp)",
                "데이터베이스 작업 (Room, SQLite)",
                "파일 읽기/쓰기",
                "SharedPreferences 접근"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// IO vs Default: 왜 다른가?
// IO: 대기 시간이 긴 작업 → 스레드 많이 필요 (64개)
//     스레드가 대기하는 동안 다른 스레드가 일함

// Default: CPU 사용 작업 → 코어 수만큼만 필요
//          더 많아도 컨텍스트 스위칭만 증가
            """.trimIndent()
        )
    }
}

@Composable
private fun DefaultDispatcherSection() {
    SectionCard(title = "4️⃣ Dispatchers.Default") {
        Text(
            text = "CPU 집약적 작업에 최적화 (코어 수만큼 스레드)",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Default Dispatcher 사용 예시
suspend fun sortLargeList(list: List<Int>): List<Int> =
    withContext(Dispatchers.Default) {
        // CPU 집약적 정렬 작업
        list.sorted()
    }

suspend fun parseJson(json: String): Data =
    withContext(Dispatchers.Default) {
        // JSON 파싱 (CPU 사용)
        gson.fromJson(json, Data::class.java)
    }

suspend fun processImage(bitmap: Bitmap): Bitmap =
    withContext(Dispatchers.Default) {
        // 이미지 처리 (CPU 집약적)
        applyFilter(bitmap)
    }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "🔥 Default Dispatcher 사용",
            items = listOf(
                "대용량 리스트 정렬/필터링",
                "JSON/XML 파싱",
                "이미지 처리 (비트맵 변환)",
                "암호화/해시 계산",
                "복잡한 수학 연산"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow(
            leftTitle = "IO (I/O 작업)",
            leftItems = listOf("대기 시간 긺", "스레드 64개", "네트워크, DB, 파일"),
            leftColor = Color(0xFFE3F2FD),
            rightTitle = "Default (CPU 작업)",
            rightItems = listOf("CPU 사용 높음", "코어 수 스레드", "정렬, 파싱, 연산"),
            rightColor = Color(0xFFFFF3E0)
        )
    }
}

@Composable
private fun WithContextSection() {
    SectionCard(title = "5️⃣ withContext로 스레드 전환") {
        Text(
            text = "withContext = suspend 함수 내에서 Dispatcher 변경",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 일반적인 패턴: Repository에서 IO, ViewModel에서 Main
class UserRepository @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) {
    // Repository는 IO에서 실행되도록 보장
    suspend fun getUser(id: Int): User = 
        withContext(Dispatchers.IO) {
            api.getUser(id)
        }
}

class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    fun loadUser(id: Int) {
        // viewModelScope는 기본적으로 Main
        viewModelScope.launch {
            _loading.value = true        // Main에서 UI 상태 변경
            
            val user = repository.getUser(id)  // IO에서 실행됨
            
            _user.value = user           // 다시 Main에서 UI 상태 변경
            _loading.value = false
        }
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "🎯 Best Practice",
            items = listOf(
                "Repository/DataSource에서 withContext(IO) 사용",
                "ViewModel은 viewModelScope.launch만 사용",
                "suspend 함수는 Main-safe하게 만들기",
                "호출자가 Dispatcher 걱정 안 하도록"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "❌ 안티패턴",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )

        CodeBlock(
            code = """
// ❌ ViewModel에서 Dispatcher 지정
viewModelScope.launch(Dispatchers.IO) {
    val user = repository.getUser(id)
    _user.value = user  // ❌ IO에서 UI 업데이트!
}

// ✅ Repository가 IO 보장, ViewModel은 그냥 호출
viewModelScope.launch {
    val user = repository.getUser(id)  // 내부에서 IO
    _user.value = user  // Main에서 UI 업데이트
}
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: Dispatchers.IO vs Default?" to
                    "A: IO는 I/O 작업(네트워크,DB,파일)용 64스레드풀. Default는 CPU 연산(정렬,파싱)용 코어수 스레드풀",
            "Q: Main Dispatcher 역할?" to
                    "A: Android UI 스레드. UI 업데이트, Toast, Navigation. 오래 걸리는 작업 금지(ANR)",
            "Q: withContext 언제 사용?" to
                    "A: suspend 함수 내에서 Dispatcher 전환. Repository에서 IO 보장 시 사용",
            "Q: Main-safe란?" to
                    "A: Main에서 호출해도 안전한 suspend 함수. 내부에서 withContext로 적절한 Dispatcher 사용",
            "Q: Unconfined는?" to
                    "A: 호출 스레드에서 시작, 재개 시 다른 스레드일 수 있음. 테스트 외 사용 금지"
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
private fun CoroutineDispatchersScreenPreview() {
    CoroutineDispatchersScreen()
}
