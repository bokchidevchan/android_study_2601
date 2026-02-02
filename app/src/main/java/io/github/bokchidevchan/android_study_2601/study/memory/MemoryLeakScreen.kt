package io.github.bokchidevchan.android_study_2601.study.memory

import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MemoryLeakScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var leakDemoCount by remember { mutableIntStateOf(0) }
    var lastAction by remember { mutableStateOf("") }
    
    DisposableEffect(Unit) {
        GoodSingleton.init(context)
        
        onDispose {
            Log.d("MemoryLeakScreen", "Screen disposed - cleanup triggered")
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        ToolsOverviewSection()
        LeakPatternsSection()
        
        InteractiveDemoSection(
            leakCount = leakDemoCount,
            lastAction = lastAction,
            onBadAction = { pattern ->
                leakDemoCount++
                lastAction = "❌ $pattern - 누수 발생!"
                triggerBadPattern(context, pattern)
            },
            onGoodAction = { pattern ->
                lastAction = "✅ $pattern - 안전!"
                triggerGoodPattern(context, pattern)
            }
        )
        
        MemoryProfilerSection()
        LeakCanarySection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "🧠 메모리 누수 디버깅",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Android 메모리 누수 패턴과 디버깅 도구 학습",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolsOverviewSection() {
    SectionCard(title = "🛠️ 디버깅 도구") {
        ToolItem(
            name = "Memory Profiler",
            description = "Android Studio 내장. Heap Dump 캡처 및 분석",
            usage = "View > Tool Windows > Profiler > Memory"
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolItem(
            name = "LeakCanary",
            description = "Square 오픈소스. 자동 누수 감지 및 알림",
            usage = "debugImplementation 한 줄로 설정 완료"
        )
    }
}

@Composable
private fun ToolItem(name: String, description: String, usage: String) {
    Column {
        Text(
            text = name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "→ $usage",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun LeakPatternsSection() {
    SectionCard(title = "🔴 7가지 누수 패턴") {
        val patterns = listOf(
            LeakPattern(
                number = 1,
                title = "Static Reference",
                cause = "companion object에 Activity 저장",
                fix = "applicationContext 또는 WeakReference 사용"
            ),
            LeakPattern(
                number = 2,
                title = "Inner Class",
                cause = "익명 클래스가 외부 Activity 암시적 참조",
                fix = "static inner class + WeakReference"
            ),
            LeakPattern(
                number = 3,
                title = "Handler/Runnable",
                cause = "postDelayed 콜백이 Activity 캡처",
                fix = "onDestroy()에서 removeCallbacksAndMessages(null)"
            ),
            LeakPattern(
                number = 4,
                title = "Listener 미해제",
                cause = "register 후 unregister 누락",
                fix = "onDestroy()에서 해제 또는 Lifecycle-aware"
            ),
            LeakPattern(
                number = 5,
                title = "Singleton Context",
                cause = "싱글톤에 Activity context 저장",
                fix = "context.applicationContext 사용"
            ),
            LeakPattern(
                number = 6,
                title = "ViewModel → View",
                cause = "ViewModel이 View/Activity 참조",
                fix = "StateFlow/LiveData로 데이터만 노출"
            ),
            LeakPattern(
                number = 7,
                title = "Coroutine Scope",
                cause = "GlobalScope 사용",
                fix = "viewModelScope, lifecycleScope 사용"
            )
        )
        
        patterns.forEach { pattern ->
            PatternItem(pattern)
            if (pattern.number < 7) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PatternItem(pattern: LeakPattern) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${pattern.number}.",
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE53935)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pattern.title,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "원인: ${pattern.cause}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "해결: ${pattern.fix}",
                fontSize = 13.sp,
                color = Color(0xFF43A047)
            )
        }
    }
}

@Composable
private fun InteractiveDemoSection(
    leakCount: Int,
    lastAction: String,
    onBadAction: (String) -> Unit,
    onGoodAction: (String) -> Unit
) {
    SectionCard(title = "🎮 인터랙티브 데모") {
        Text(
            text = "Logcat에서 'MemoryLeak' 태그로 필터링하세요",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("누수 발생 횟수: $leakCount", fontWeight = FontWeight.Bold)
        }
        
        if (lastAction.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastAction,
                fontSize = 14.sp,
                color = if (lastAction.startsWith("❌")) Color(0xFFE53935) else Color(0xFF43A047)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DemoButtonRow(
            label = "Static Reference",
            onBad = { onBadAction("Static Reference") },
            onGood = { onGoodAction("Static Reference") }
        )
        
        DemoButtonRow(
            label = "Singleton Context",
            onBad = { onBadAction("Singleton Context") },
            onGood = { onGoodAction("Singleton Context") }
        )
        
        DemoButtonRow(
            label = "Handler Leak",
            onBad = { onBadAction("Handler") },
            onGood = { onGoodAction("Handler") }
        )
    }
}

@Composable
private fun DemoButtonRow(
    label: String,
    onBad: () -> Unit,
    onGood: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Button(
            onClick = onBad,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935)
            ),
            modifier = Modifier.weight(0.6f)
        ) {
            Text("❌ BAD", fontSize = 12.sp)
        }
        OutlinedButton(
            onClick = onGood,
            modifier = Modifier.weight(0.6f)
        ) {
            Text("✅ GOOD", fontSize = 12.sp)
        }
    }
}

@Composable
private fun MemoryProfilerSection() {
    SectionCard(title = "📊 Memory Profiler 사용법") {
        val steps = listOf(
            "1. Debug 모드로 앱 실행",
            "2. View > Tool Windows > Profiler",
            "3. Memory 타임라인 클릭",
            "4. 테스트할 Activity 진입 후 종료",
            "5. 🗑️ Force GC 클릭",
            "6. 📷 Dump Java heap 클릭",
            "7. 종료된 Activity 이름 검색",
            "8. 인스턴스가 있으면 = 누수!"
        )
        
        steps.forEach { step ->
            Text(
                text = step,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF1E1E1E),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = """
핵심 메트릭:
• Shallow Size: 객체 자체 크기
• Retained Size: GC 시 해제될 총 크기
  → Retained Size가 크면 심각한 누수
                """.trimIndent(),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
private fun LeakCanarySection() {
    SectionCard(title = "🐤 LeakCanary 설정") {
        Text(
            text = "한 줄 설정으로 자동 누수 감지!",
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CodeBlock(
            code = """
// build.gradle.kts
dependencies {
    debugImplementation(
        "com.squareup.leakcanary:leakcanary-android:2.14"
    )
}
// 끝! 추가 코드 불필요
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "동작 원리:",
            fontWeight = FontWeight.SemiBold
        )
        
        val steps = listOf(
            "1. ObjectWatcher가 destroyed 객체 감시",
            "2. 5초 후 GC 강제 실행",
            "3. WeakReference 미해제 = retained",
            "4. Retained 5개 → Heap Dump",
            "5. Shark 분석기가 경로 추적",
            "6. 알림으로 Leak Trace 제공"
        )
        
        steps.forEach { step ->
            Text(
                text = step,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Square 앱에서 OOM 크래시 94% 감소!",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF43A047)
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: 메모리 누수란?" to 
                "A: 사용 완료된 객체가 GC되지 않아 메모리 점유가 계속되는 현상",
            "Q: 가장 흔한 원인?" to 
                "A: Activity 참조가 static 필드, 싱글톤, 장시간 콜백에 유지됨",
            "Q: 어떻게 감지?" to 
                "A: LeakCanary (자동), Memory Profiler (수동 Heap Dump)",
            "Q: 어떻게 예방?" to 
                "A: applicationContext 사용, WeakReference, Lifecycle-aware 컴포넌트",
            "Q: ViewModel이 View 참조하면?" to 
                "A: 화면 회전 시 이전 Activity 누수. StateFlow로 데이터만 노출해야 함"
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
            .background(
                Color(0xFF1E1E1E),
                RoundedCornerShape(8.dp)
            )
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

private data class LeakPattern(
    val number: Int,
    val title: String,
    val cause: String,
    val fix: String
)

private fun triggerBadPattern(context: android.content.Context, pattern: String) {
    when (pattern) {
        "Static Reference" -> BadStaticReference.setActivity(context)
        "Singleton Context" -> BadSingleton.init(context)
        "Handler" -> BadHandlerUsage(context).startRepeatingTask()
    }
}

private fun triggerGoodPattern(context: android.content.Context, pattern: String) {
    when (pattern) {
        "Static Reference" -> GoodStaticReference.setActivityWeak(context)
        "Singleton Context" -> GoodSingleton.init(context)
        "Handler" -> {
            val handler = GoodHandlerUsage(context)
            handler.startRepeatingTask()
            handler.cleanup()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoryLeakScreenPreview() {
    MemoryLeakScreen()
}
