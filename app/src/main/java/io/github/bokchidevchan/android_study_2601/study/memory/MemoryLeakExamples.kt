package io.github.bokchidevchan.android_study_2601.study.memory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * ========================================================================
 * 📚 학습 목표: Android 메모리 누수 패턴 이해하기
 * ========================================================================
 *
 * 🔑 7가지 주요 메모리 누수 패턴:
 *
 * 1. Static Reference to Activity/Context
 *    - Companion object에 Activity 참조 저장
 *    - 해결: applicationContext 사용 또는 WeakReference
 *
 * 2. Inner Class Holding Activity Reference
 *    - 익명 내부 클래스가 외부 Activity 참조
 *    - 해결: static inner class + WeakReference
 *
 * 3. Handler/Runnable Leaks
 *    - postDelayed 콜백이 Activity 캡처
 *    - 해결: onDestroy에서 removeCallbacksAndMessages(null)
 *
 * 4. Listener/Callback Not Unregistered
 *    - register 후 unregister 누락
 *    - 해결: Lifecycle-aware 또는 onDestroy에서 해제
 *
 * 5. Singleton Holding Context
 *    - 싱글톤에 Activity context 저장
 *    - 해결: applicationContext 사용
 *
 * 6. ViewModel Holding View Reference
 *    - ViewModel이 View/Activity 직접 참조
 *    - 해결: StateFlow/LiveData로 데이터만 노출
 *
 * 7. Coroutine Scope Leaks
 *    - GlobalScope 사용으로 Activity 캡처
 *    - 해결: viewModelScope, lifecycleScope 사용
 *
 * ========================================================================
 */

// ========================================================================
// 🔴 PATTERN 1: Static Reference to Activity/Context
// ========================================================================

/**
 * ❌ BAD: Static reference to Activity causes memory leak
 *
 * 문제점:
 * - companion object는 static이므로 앱 전체 수명 동안 유지
 * - Activity가 destroy되어도 static 참조로 인해 GC되지 않음
 * - Activity는 View 계층 전체를 참조하므로 거대한 메모리 누수 발생
 */
object BadStaticReference {
    // ❌ MEMORY LEAK: Activity reference stored in static field
    var currentActivity: Context? = null
    
    fun setActivity(activity: Context) {
        // Activity를 static 필드에 저장 -> 메모리 누수!
        currentActivity = activity
        Log.d("MemoryLeak", "Activity stored in static field - LEAK!")
    }
    
    fun doSomething() {
        // currentActivity는 destroy된 Activity를 참조할 수 있음
        currentActivity?.let {
            Log.d("MemoryLeak", "Using potentially leaked context")
        }
    }
}

/**
 * ✅ GOOD: Application context or WeakReference
 */
object GoodStaticReference {
    // ✅ SAFE: Application context lives for entire app lifetime
    private var appContext: Context? = null
    
    // ✅ SAFE: WeakReference allows GC
    private var activityRef: WeakReference<Context>? = null
    
    fun init(context: Context) {
        // Application context 사용 - 메모리 누수 없음
        appContext = context.applicationContext
    }
    
    fun setActivityWeak(activity: Context) {
        // WeakReference 사용 - GC 허용
        activityRef = WeakReference(activity)
    }
    
    fun doSomethingSafe() {
        // WeakReference.get()은 GC되었으면 null 반환
        activityRef?.get()?.let { activity ->
            Log.d("MemoryLeak", "Safely using activity if still alive")
        }
    }
}

// ========================================================================
// 🔴 PATTERN 2: Inner Class Holding Activity Reference
// ========================================================================

/**
 * ❌ BAD: Anonymous inner class holds implicit reference to outer class
 *
 * 문제점:
 * - object : Runnable은 익명 내부 클래스
 * - 내부 클래스는 외부 클래스(Activity)에 대한 암시적 참조를 가짐
 * - Handler 큐에 오래 대기하면 Activity가 GC되지 않음
 */
class BadInnerClass(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun startLeakyOperation() {
        // ❌ MEMORY LEAK: Anonymous class captures 'this' which holds context
        val runnable = object : Runnable {
            override fun run() {
                // 이 람다는 외부 BadInnerClass 인스턴스를 캡처
                // BadInnerClass는 context(Activity)를 참조
                doSomethingWithContext()
            }
        }
        
        // 5분 후 실행 - 그 동안 Activity가 destroy되면 누수!
        handler.postDelayed(runnable, 300000)
        Log.d("MemoryLeak", "Posted delayed runnable - potential LEAK!")
    }
    
    private fun doSomethingWithContext() {
        Log.d("MemoryLeak", "Doing something with context: $context")
    }
}

/**
 * ✅ GOOD: Static inner class with WeakReference + proper cleanup
 */
class GoodInnerClass(context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private val contextRef = WeakReference(context)
    private val updateRunnable = UpdateRunnable(contextRef)
    
    fun startSafeOperation() {
        handler.postDelayed(updateRunnable, 300000)
        Log.d("MemoryLeak", "Posted safe delayed runnable")
    }
    
    // ✅ IMPORTANT: Call this in onDestroy()
    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        Log.d("MemoryLeak", "Cleaned up handler callbacks")
    }
    
    // ✅ SAFE: Static-like class (no implicit reference to outer class)
    private class UpdateRunnable(
        private val contextRef: WeakReference<Context>
    ) : Runnable {
        override fun run() {
            // WeakReference가 null이면 Activity가 GC됨 - 안전하게 무시
            contextRef.get()?.let { context ->
                Log.d("MemoryLeak", "Safely using context: $context")
            } ?: Log.d("MemoryLeak", "Context was garbage collected - no leak!")
        }
    }
}

// ========================================================================
// 🔴 PATTERN 3: Handler/Runnable Leaks
// ========================================================================

/**
 * ❌ BAD: Handler without cleanup
 */
class BadHandlerUsage(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun startRepeatingTask() {
        // ❌ MEMORY LEAK: Lambda captures 'this' and 'context'
        handler.postDelayed({
            Log.d("MemoryLeak", "Repeating task with context: $context")
            startRepeatingTask() // 재귀 호출 - 영원히 실행
        }, 1000)
    }
    
    // onDestroy에서 호출 안 하면 누수!
    // fun cleanup() { handler.removeCallbacksAndMessages(null) }
}

/**
 * ✅ GOOD: Handler with proper lifecycle management
 */
class GoodHandlerUsage(context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private val contextRef = WeakReference(context)
    
    private val repeatingRunnable = object : Runnable {
        override fun run() {
            contextRef.get()?.let { ctx ->
                Log.d("MemoryLeak", "Safe repeating task: $ctx")
                handler.postDelayed(this, 1000)
            }
            // Context가 GC되면 자동으로 중단됨
        }
    }
    
    fun startRepeatingTask() {
        handler.post(repeatingRunnable)
    }
    
    // ✅ MUST call in onDestroy()
    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
    }
}

// ========================================================================
// 🔴 PATTERN 4: Listener/Callback Not Unregistered
// ========================================================================

/**
 * ❌ BAD: Broadcast receiver not unregistered
 */
class BadListenerUsage(private val context: Context) {
    
    // ❌ MEMORY LEAK: Receiver holds context reference
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            Log.d("MemoryLeak", "Received broadcast in: $context")
        }
    }
    
    fun registerReceiver() {
        // 등록은 하지만 해제 안 함 -> 누수!
        context.registerReceiver(receiver, IntentFilter("SOME_ACTION"))
        Log.d("MemoryLeak", "Registered receiver - potential LEAK!")
    }
    
    // unregisterReceiver() 호출 누락!
}

/**
 * ✅ GOOD: Proper listener lifecycle management
 */
class GoodListenerUsage(private val context: Context) {
    
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            Log.d("MemoryLeak", "Received broadcast safely")
        }
    }
    
    private var isRegistered = false
    
    fun registerReceiver() {
        if (!isRegistered) {
            context.registerReceiver(receiver, IntentFilter("SOME_ACTION"))
            isRegistered = true
        }
    }
    
    // ✅ MUST call in onDestroy() or onStop()
    fun unregisterReceiver() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(receiver)
                isRegistered = false
                Log.d("MemoryLeak", "Unregistered receiver - no leak!")
            } catch (e: IllegalArgumentException) {
                // Already unregistered
            }
        }
    }
}

// ========================================================================
// 🔴 PATTERN 5: Singleton Holding Context
// ========================================================================

/**
 * ❌ BAD: Singleton stores Activity context
 */
object BadSingleton {
    // ❌ MEMORY LEAK: Singleton holds Activity context forever
    private var context: Context? = null
    
    fun init(ctx: Context) {
        // Activity context 저장 -> 영원히 유지 -> 누수!
        context = ctx
        Log.d("MemoryLeak", "Singleton storing Activity context - LEAK!")
    }
    
    fun doWork() {
        context?.let { ctx ->
            Log.d("MemoryLeak", "Using leaked context: $ctx")
        }
    }
}

/**
 * ✅ GOOD: Singleton with Application context
 */
object GoodSingleton {
    // ✅ SAFE: Application context lives for entire app lifetime
    private lateinit var appContext: Context
    
    fun init(context: Context) {
        // 항상 applicationContext 사용
        appContext = context.applicationContext
        Log.d("MemoryLeak", "Singleton using Application context - safe!")
    }
    
    fun doWork() {
        Log.d("MemoryLeak", "Using app context: $appContext")
    }
}

// ========================================================================
// 🔴 PATTERN 6: ViewModel Holding View Reference
// ========================================================================

/**
 * ❌ BAD: ViewModel storing View/Context reference
 *
 * 문제점:
 * - ViewModel은 Configuration Change를 살아남음
 * - 화면 회전 시 Activity/View는 destroy되고 새로 생성됨
 * - ViewModel이 이전 View 참조를 유지하면 이전 Activity가 GC되지 않음
 */
class BadViewModel : ViewModel() {
    // ❌ MEMORY LEAK: ViewModel survives config changes, View doesn't
    private var textViewText: String? = null
    private var storedContext: Context? = null
    
    fun setContext(context: Context) {
        // ViewModel이 Activity 참조 저장 -> 회전 시 누수!
        storedContext = context
        Log.d("MemoryLeak", "ViewModel storing Context - LEAK!")
    }
    
    fun updateText(text: String) {
        textViewText = text
        // storedContext 사용 -> 이전 Activity 참조 유지
        storedContext?.let {
            Log.d("MemoryLeak", "Using stored context: $it")
        }
    }
}

/**
 * ✅ GOOD: ViewModel exposing only data via StateFlow
 */
class GoodViewModel : ViewModel() {
    // ✅ SAFE: ViewModel only holds data, not View references
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun updateText(newText: String) {
        // View가 알아서 observe하고 업데이트
        _text.value = newText
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(1000) // 시뮬레이션
            _uiState.value = UiState.Success("Data loaded!")
        }
    }
    
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val error: String) : UiState()
    }
}

// ========================================================================
// 🔴 PATTERN 7: Coroutine Scope Leaks
// ========================================================================

/**
 * ❌ BAD: Using GlobalScope
 *
 * 문제점:
 * - GlobalScope는 앱 전체 수명 동안 유지
 * - 코루틴 내 람다가 Activity 캡처하면 누수
 * - 취소 불가능
 */
class BadCoroutineUsage(private val context: Context) {
    
    fun startLeakyCoroutine() {
        // ❌ MEMORY LEAK: GlobalScope never cancelled
        GlobalScope.launch {
            delay(300000) // 5분
            // 이 람다는 context(Activity)를 캡처
            Log.d("MemoryLeak", "GlobalScope with context: $context")
        }
        Log.d("MemoryLeak", "Started GlobalScope coroutine - LEAK!")
    }
}

/**
 * ✅ GOOD: Using lifecycle-aware coroutine scopes
 */
class GoodCoroutineUsage(context: Context) {
    
    private val contextRef = WeakReference(context)
    
    // ✅ Custom scope with manual cancellation
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    fun startSafeCoroutine() {
        scope.launch {
            delay(300000)
            contextRef.get()?.let { ctx ->
                Log.d("MemoryLeak", "Safe coroutine with context: $ctx")
            }
        }
    }
    
    // ✅ MUST call in onDestroy()
    fun cleanup() {
        scope.cancel()
        Log.d("MemoryLeak", "Coroutine scope cancelled - no leak!")
    }
}

/**
 * ✅ BEST: Using viewModelScope (automatically cancelled)
 */
class BestCoroutineViewModel : ViewModel() {
    
    fun loadDataSafely() {
        // ✅ SAFE: viewModelScope cancelled when ViewModel cleared
        viewModelScope.launch {
            delay(300000)
            Log.d("MemoryLeak", "viewModelScope coroutine - auto cancelled!")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // viewModelScope는 자동으로 취소됨
        Log.d("MemoryLeak", "ViewModel cleared, coroutines auto-cancelled")
    }
}

// ========================================================================
// 📊 메모리 누수 감지 도구
// ========================================================================

/**
 * LeakCanary 감지 순서:
 *
 * 1. ObjectWatcher가 destroyed 객체를 WeakReference로 감시
 * 2. 5초 후 GC 강제 실행
 * 3. WeakReference가 clear되지 않으면 "retained" 상태
 * 4. Retained 객체 5개 누적 시 Heap Dump 생성
 * 5. Shark 분석기가 GC Root → Leaking Object 경로 추적
 * 6. 알림으로 Leak Trace 제공
 *
 * Leak Trace 읽기:
 * ┬───
 * │ GC Root: System class
 * │
 * ├─ com.example.BadSingleton class
 * │    ↓ static BadSingleton.context
 * │                         ~~~~~~~  <- 누수 원인!
 * ╰→ com.example.MainActivity instance
 *     Leaking: YES (Activity destroyed but still referenced)
 */
object LeakDetectionGuide {
    const val SETUP = """
        // build.gradle.kts
        dependencies {
            debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
        }
        
        // 끝! 추가 코드 불필요
    """
    
    const val LOGCAT_TAG = "LeakCanary"
}

// ========================================================================
// 🛠️ Android Studio Memory Profiler 사용법
// ========================================================================

/**
 * Memory Profiler 사용 순서:
 *
 * 1. 앱 실행 (Debug 모드)
 * 2. View > Tool Windows > Profiler
 * 3. 기기/프로세스 선택
 * 4. Memory 타임라인 클릭
 *
 * 누수 감지 방법:
 * 1. Activity 진입
 * 2. 뒤로가기로 Activity 종료
 * 3. 🗑️ (Force GC) 버튼 클릭
 * 4. 📷 (Dump Java heap) 버튼 클릭
 * 5. "MainActivity" 검색
 * 6. 인스턴스가 있으면 = 누수!
 * 7. References 탭에서 누가 참조하는지 확인
 *
 * 핵심 메트릭:
 * - Shallow Size: 객체 자체 크기
 * - Retained Size: 이 객체가 GC되면 해제되는 총 크기
 */
object MemoryProfilerGuide {
    const val STEPS = """
        1. Run app in Debug mode
        2. Open Profiler: View > Tool Windows > Profiler
        3. Select device and process
        4. Click on Memory timeline
        5. Perform actions that should release memory
        6. Click 🗑️ Force GC
        7. Click 📷 Dump Java heap
        8. Search for your Activity/Fragment class
        9. If instances exist after destroy = LEAK!
        10. Check References tab to find who's holding it
    """
}
