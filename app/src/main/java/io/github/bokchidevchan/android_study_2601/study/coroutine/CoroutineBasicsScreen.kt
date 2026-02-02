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
 * 📚 코루틴 기초 (Coroutine Basics)
 * ========================================================================
 *
 * 핵심 개념:
 * 1. suspend - "비동기"가 아닌 "일시 중단 가능"
 * 2. CoroutineScope - 코루틴 생명주기 관리
 * 3. Job & Deferred - 코루틴 핸들
 * 4. launch vs async - 실행 방식의 차이
 * 5. Structured Concurrency - 부모-자식 관계
 *
 * 핵심: suspend 함수는 스레드를 블로킹하지 않고 "일시 중단"됩니다.
 */
@Composable
fun CoroutineBasicsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        SuspendSection()
        ScopeContextSection()
        JobDeferredSection()
        LaunchVsAsyncSection()
        StructuredConcurrencySection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "⚡ 코루틴 기초",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "suspend, CoroutineScope, Job, launch vs async",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuspendSection() {
    SectionCard(title = "1️⃣ suspend의 진짜 의미") {
        Text(
            text = "suspend ≠ 비동기, suspend = 일시 중단 가능",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "🔑 핵심 이해",
            items = listOf(
                "suspend는 '이 함수는 중간에 멈출 수 있다'는 표시",
                "스레드를 블로킹하지 않고 '중단점'에서 일시 정지",
                "컴파일러가 상태 머신(State Machine)으로 변환",
                "suspend 함수는 코루틴 또는 다른 suspend 함수에서만 호출 가능"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "❌ 잘못된 이해",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )

        CodeBlock(
            code = """
// suspend = 자동으로 백그라운드에서 실행? ❌ 아님!
suspend fun fetchData(): String {
    // 이 함수가 어떤 스레드에서 실행될지는
    // suspend 키워드가 결정하지 않음!
    return "data"
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "✅ 올바른 이해",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )

        CodeBlock(
            code = """
suspend fun fetchData(): String {
    // delay()는 중단점 - 여기서 일시 중단됨
    delay(1000)  // 스레드 블로킹 X, 코루틴만 중단
    return "data"
}

// 실제 스레드 지정은 Dispatcher가 담당
withContext(Dispatchers.IO) {
    fetchData()  // IO 스레드에서 실행
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow(
            leftTitle = "Thread.sleep()",
            leftItems = listOf("스레드 블로킹", "다른 작업 불가", "리소스 낭비"),
            leftColor = Color(0xFFFFCDD2),
            rightTitle = "delay()",
            rightItems = listOf("코루틴만 중단", "스레드는 다른 일 가능", "효율적"),
            rightColor = Color(0xFFC8E6C9)
        )
    }
}

@Composable
private fun ScopeContextSection() {
    SectionCard(title = "2️⃣ CoroutineScope & CoroutineContext") {
        Text(
            text = "Scope = 생명주기 관리, Context = 실행 환경 설정",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// CoroutineContext의 구성 요소
val context = Dispatchers.IO +      // 1. Dispatcher: 스레드 지정
              Job() +               // 2. Job: 생명주기 핸들
              CoroutineName("My") + // 3. Name: 디버깅용
              CoroutineExceptionHandler { _, e -> } // 4. 예외 처리

// CoroutineScope는 Context를 가진 "울타리"
class MyViewModel : ViewModel() {
    // viewModelScope: ViewModel 생명주기에 바인딩
    fun loadData() {
        viewModelScope.launch {
            // ViewModel이 clear되면 자동 취소!
        }
    }
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "Android에서 사용하는 Scope",
            items = listOf(
                "viewModelScope - ViewModel onCleared()에서 취소",
                "lifecycleScope - Activity/Fragment onDestroy()에서 취소",
                "rememberCoroutineScope() - Compose에서 사용",
                "GlobalScope - ❌ 절대 사용 금지 (메모리 누수)"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "❌ GlobalScope 사용 금지",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )

        CodeBlock(
            code = """
// ❌ 메모리 누수 발생!
GlobalScope.launch {
    // 앱 전체 생명주기 동안 살아있음
    // Activity가 종료되어도 계속 실행
    delay(10000)
    updateUI()  // 이미 Activity 죽었는데 UI 업데이트?
}

// ✅ 생명주기에 맞는 Scope 사용
viewModelScope.launch {
    delay(10000)
    updateUI()  // ViewModel 살아있을 때만 실행
}
            """.trimIndent()
        )
    }
}

@Composable
private fun JobDeferredSection() {
    SectionCard(title = "3️⃣ Job & Deferred") {
        Text(
            text = "코루틴 핸들: 취소, 대기, 상태 확인",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// Job: 코루틴의 생명주기 핸들
val job: Job = launch {
    repeat(1000) { i ->
        println("Job: ${'$'}i")
        delay(500)
    }
}

job.isActive      // 실행 중인가?
job.isCompleted   // 완료되었나?
job.isCancelled   // 취소되었나?
job.cancel()      // 취소!
job.join()        // 완료될 때까지 대기 (suspend)

// Deferred: 결과를 반환하는 Job
val deferred: Deferred<String> = async {
    delay(1000)
    "결과"  // 반환값 있음
}

val result = deferred.await()  // 결과 받기 (suspend)
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ComparisonRow(
            leftTitle = "Job (launch)",
            leftItems = listOf("반환값 없음", "fire-and-forget", "cancel(), join()"),
            leftColor = Color(0xFFE3F2FD),
            rightTitle = "Deferred (async)",
            rightItems = listOf("반환값 있음", "결과가 필요할 때", "await()로 결과 받기"),
            rightColor = Color(0xFFFFF3E0)
        )
    }
}

@Composable
private fun LaunchVsAsyncSection() {
    SectionCard(title = "4️⃣ launch vs async") {
        Text(
            text = "언제 무엇을 사용해야 하는가?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// launch: 결과가 필요 없을 때 (Fire-and-forget)
viewModelScope.launch {
    repository.saveData(data)  // 저장만 하면 됨
    analytics.logEvent("saved") // 로깅만 하면 됨
}

// async: 결과가 필요할 때
viewModelScope.launch {
    // 순차 실행 (느림)
    val user = fetchUser()     // 1초
    val posts = fetchPosts()   // 1초
    // 총 2초

    // 병렬 실행 (빠름)
    val userDeferred = async { fetchUser() }   // 동시에
    val postsDeferred = async { fetchPosts() } // 시작
    
    val user = userDeferred.await()
    val posts = postsDeferred.await()
    // 총 1초 (병렬이니까!)
}
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "🎯 선택 기준",
            items = listOf(
                "결과 필요 없음 → launch",
                "결과 필요, 순차 실행 → suspend 함수 직접 호출",
                "결과 필요, 병렬 실행 → async + await",
                "여러 결과 병렬로 받기 → awaitAll()"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 병렬 요청 패턴
suspend fun loadDashboard(): Dashboard = coroutineScope {
    val user = async { fetchUser() }
    val posts = async { fetchPosts() }
    val notifications = async { fetchNotifications() }
    
    Dashboard(
        user = user.await(),
        posts = posts.await(),
        notifications = notifications.await()
    )
}

// 더 간단하게: awaitAll
val (user, posts) = awaitAll(
    async { fetchUser() },
    async { fetchPosts() }
)
            """.trimIndent()
        )
    }
}

@Composable
private fun StructuredConcurrencySection() {
    SectionCard(title = "5️⃣ Structured Concurrency (구조화된 동시성)") {
        Text(
            text = "부모가 취소되면 자식도 취소, 자식이 끝나야 부모 완료",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// 구조화된 동시성의 규칙
viewModelScope.launch {  // 부모
    launch {  // 자식 1
        delay(1000)
        println("Child 1")
    }
    launch {  // 자식 2
        delay(2000)
        println("Child 2")
    }
    println("Parent waits...")
    // 부모는 모든 자식이 끝날 때까지 기다림!
}

// 규칙 1: 부모 취소 → 모든 자식 취소
// 규칙 2: 자식 실패 → 부모에게 전파 → 형제들도 취소
// 규칙 3: 부모는 자식들이 끝날 때까지 완료되지 않음
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HighlightBox(
            title = "왜 Structured Concurrency가 중요한가?",
            items = listOf(
                "메모리 누수 방지 - 고아 코루틴 없음",
                "예외 전파 - 실패가 숨겨지지 않음",
                "취소 전파 - 불필요한 작업 자동 정리",
                "코드 추론 용이 - 스코프 내 모든 작업 추적 가능"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = """
// coroutineScope vs supervisorScope
coroutineScope {
    launch { throw Exception() }  // 실패!
    launch { delay(1000) }        // 형제도 취소됨!
}

supervisorScope {
    launch { throw Exception() }  // 실패!
    launch { delay(1000) }        // 계속 실행! ✅
}
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: suspend의 의미?" to
                    "A: '비동기'가 아닌 '일시 중단 가능'. 컴파일러가 상태 머신으로 변환. 스레드 지정은 Dispatcher가 담당",
            "Q: launch vs async?" to
                    "A: launch는 Job 반환(fire-and-forget), async는 Deferred 반환(결과 필요). 병렬 실행 시 async+await",
            "Q: GlobalScope 왜 안 돼?" to
                    "A: 생명주기 바인딩 없음 → 메모리 누수. viewModelScope/lifecycleScope 사용",
            "Q: Job vs Deferred?" to
                    "A: Job은 핸들(cancel, join), Deferred는 Job + 결과(await). launch→Job, async→Deferred",
            "Q: Structured Concurrency?" to
                    "A: 부모-자식 관계. 부모 취소→자식 취소, 자식 실패→부모 전파, 부모는 자식 완료 대기"
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
private fun CoroutineBasicsScreenPreview() {
    CoroutineBasicsScreen()
}
