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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 코루틴 예외 처리 (Coroutine Exception Handling)
 * ========================================================================
 *
 * 코루틴에서 예외 처리는 일반 코드와 다릅니다!
 *
 * 핵심 개념:
 * 1. 예외 전파 - 자식 실패 → 부모에게 전파 → 형제 취소
 * 2. CoroutineExceptionHandler - 최상위에서 예외 잡기
 * 3. supervisorScope - 자식 실패가 형제에게 영향 X
 * 4. try-catch 주의점 - launch와 async의 차이
 *
 * 핵심: launch는 Handler, async는 try-catch + await
 */
@Composable
fun CoroutineExceptionScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        ExceptionPropagationSection()
        TryCatchSection()
        ExceptionHandlerSection()
        SupervisorScopeSection()
        BestPracticesSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "⚠️ 코루틴 예외 처리",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CoroutineExceptionHandler, supervisorScope, try-catch 올바르게 사용하기",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExceptionPropagationSection() {
    SectionCard(title = "1️⃣ 예외 전파 규칙") {
        Text(
            text = "자식이 실패하면 → 부모에게 전파 → 모든 형제 취소",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 기본 동작: 하나가 실패하면 전부 취소
viewModelScope.launch {
    launch {
        delay(1000)
        throw Exception("Child 1 failed!")  // 💥 실패
    }
    launch {
        delay(2000)
        println("Child 2")  // ❌ 실행 안됨 (형제가 실패해서 취소됨)
    }
    launch {
        delay(3000)
        println("Child 3")  // ❌ 실행 안됨
    }
}
// 결과: 모든 코루틴이 취소됨!
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "예외 전파 순서",
            items = listOf(
                "1. 자식 코루틴에서 예외 발생",
                "2. 해당 자식 취소",
                "3. 부모에게 예외 전파",
                "4. 부모가 모든 다른 자식 취소",
                "5. 부모도 취소됨"
            )
        )
    }
}

@Composable
private fun TryCatchSection() {
    SectionCard(title = "2️⃣ try-catch 주의점") {
        Text(
            text = "launch와 async에서 try-catch 동작이 다릅니다!",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "❌ launch에서 잘못된 try-catch",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )

        CodeBlock(
            code = """
// ❌ 이 try-catch는 예외를 못 잡음!
viewModelScope.launch {
    try {
        launch {
            throw Exception("Error!")  // 💥
        }
    } catch (e: Exception) {
        // 여기 안 옴! launch는 예외를 부모에게 전파함
    }
}

// ❌ 바깥 try-catch도 소용없음
try {
    viewModelScope.launch {
        throw Exception("Error!")  // 💥
    }
} catch (e: Exception) {
    // 여기도 안 옴! launch는 즉시 반환됨
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "✅ launch에서 올바른 try-catch",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )

        CodeBlock(
            code = """
// ✅ launch 내부에서 try-catch
viewModelScope.launch {
    try {
        riskyOperation()  // suspend 함수 직접 호출
    } catch (e: Exception) {
        // 여기서 잡힘!
        handleError(e)
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "✅ async에서 try-catch",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )

        CodeBlock(
            code = """
// async는 await() 시점에서 예외 발생
viewModelScope.launch {
    val deferred = async {
        throw Exception("Async error!")
    }
    
    try {
        val result = deferred.await()  // 💥 여기서 예외 발생!
    } catch (e: Exception) {
        // ✅ 여기서 잡힘!
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow(
            leftTitle = "launch 예외",
            leftItems = listOf("즉시 부모에게 전파", "try-catch 밖에서 발생", "Handler로 처리"),
            leftColor = Color(0xFFFFCDD2),
            rightTitle = "async 예외",
            rightItems = listOf("await() 시점에 발생", "try-catch로 잡기 가능", "Deferred에 저장됨"),
            rightColor = Color(0xFFC8E6C9)
        )
    }
}

@Composable
private fun ExceptionHandlerSection() {
    SectionCard(title = "3️⃣ CoroutineExceptionHandler") {
        Text(
            text = "최상위에서 처리되지 않은 예외를 잡는 Handler",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Handler 정의
val handler = CoroutineExceptionHandler { context, exception ->
    Log.e("Coroutine", "Uncaught: ${'$'}{exception.message}")
    // 에러 리포팅, UI 업데이트 등
}

// 루트 코루틴에 Handler 설정
viewModelScope.launch(handler) {
    launch {
        throw Exception("Error!")  // handler에서 잡힘
    }
}

// ⚠️ 주의: Handler는 루트 코루틴에만 설정!
viewModelScope.launch {
    launch(handler) {  // ❌ 자식에 설정하면 안 잡힘!
        throw Exception("Error!")
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        WarningBox(
            text = "⚠️ CoroutineExceptionHandler는 launch에만 동작. async는 await()에서 예외 발생"
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "Handler 사용 규칙",
            items = listOf(
                "루트 코루틴에만 설정 (자식에 설정하면 무시됨)",
                "launch에만 동작 (async는 await에서 예외)",
                "예외 발생 후에도 다른 코루틴은 취소됨",
                "마지막 방어선으로만 사용 (try-catch 우선)"
            )
        )
    }
}

@Composable
private fun SupervisorScopeSection() {
    SectionCard(title = "4️⃣ supervisorScope") {
        Text(
            text = "자식 실패가 형제에게 영향을 주지 않음",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// coroutineScope: 하나 실패 → 전부 취소
coroutineScope {
    launch {
        delay(100)
        throw Exception("Failed!")  // 💥
    }
    launch {
        delay(1000)
        println("완료")  // ❌ 실행 안됨
    }
}

// supervisorScope: 실패해도 형제는 계속
supervisorScope {
    launch {
        delay(100)
        throw Exception("Failed!")  // 💥
    }
    launch {
        delay(1000)
        println("완료")  // ✅ 정상 실행됨!
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow(
            leftTitle = "coroutineScope",
            leftItems = listOf("자식 실패 → 전부 취소", "기본 동작", "모 아니면 도"),
            leftColor = Color(0xFFFFCDD2),
            rightTitle = "supervisorScope",
            rightItems = listOf("자식 실패 → 그것만 취소", "독립적 실행", "개별 에러 처리 필요"),
            rightColor = Color(0xFFC8E6C9)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// ViewModel에서 supervisorScope 활용
class DashboardViewModel : ViewModel() {
    fun loadDashboard() {
        viewModelScope.launch {
            supervisorScope {
                // 각각 독립적으로 실패 가능
                val userJob = launch { loadUser() }
                val postsJob = launch { loadPosts() }
                val notificationsJob = launch { loadNotifications() }
                
                // User 로딩 실패해도 Posts, Notifications는 계속
            }
        }
    }
}
            """.trimIndent()
        )
    }
}

@Composable
private fun BestPracticesSection() {
    SectionCard(title = "5️⃣ 예외 처리 Best Practice") {
        HighlightBox(
            title = "✅ 권장 패턴",
            items = listOf(
                "Repository에서 try-catch + Result/sealed class 반환",
                "ViewModel에서 Result 처리 후 UI 상태 업데이트",
                "UI에서는 상태만 관찰 (예외 처리 X)",
                "마지막 방어선으로 Handler 설정"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// ✅ Repository: 예외를 Result로 감싸기
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun getUser(id: Int): Result<User> = 
        runCatching {
            withContext(Dispatchers.IO) {
                api.getUser(id)
            }
        }
}

// ✅ ViewModel: Result 처리
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    fun loadUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            repository.getUser(id)
                .onSuccess { user ->
                    _uiState.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message)
                }
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val user: User) : UiState()
    data class Error(val message: String?) : UiState()
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        WarningBox(
            text = "❌ CancellationException을 catch하지 마세요! 코루틴 취소를 방해합니다."
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// ❌ CancellationException 삼키기
try {
    suspendFunction()
} catch (e: Exception) {
    // CancellationException도 여기서 잡힘! 취소 안됨
}

// ✅ CancellationException은 다시 throw
try {
    suspendFunction()
} catch (e: CancellationException) {
    throw e  // 취소는 다시 던지기
} catch (e: Exception) {
    handleError(e)
}

// ✅ 더 간단하게: runCatching 사용
runCatching { suspendFunction() }
    .onFailure { /* CancellationException은 자동 전파 */ }
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: launch에서 예외 처리?" to
                    "A: 내부에서 try-catch 또는 CoroutineExceptionHandler. 바깥 try-catch는 소용없음",
            "Q: async에서 예외 처리?" to
                    "A: await() 호출 시 예외 발생. try-catch로 await() 감싸기",
            "Q: supervisorScope 언제?" to
                    "A: 자식 실패가 형제에게 영향 주면 안 될 때. Dashboard에서 독립적 로딩",
            "Q: Handler 어디에 설정?" to
                    "A: 루트 코루틴에만. 자식에 설정하면 무시됨. launch에만 동작",
            "Q: CancellationException?" to
                    "A: 절대 삼키지 말 것! catch하면 다시 throw. runCatching은 자동 전파"
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
private fun CoroutineExceptionScreenPreview() {
    CoroutineExceptionScreen()
}
