# Android 메모리 누수 디버깅 치트시트

> 면접 대비용 핵심 정리

---

## 🔍 핵심 개념

### 메모리 누수란?
- 사용 완료된 객체가 GC되지 않아 메모리 점유가 계속되는 현상
- Android에서 가장 흔한 원인: **Activity/Context 참조가 해제되지 않음**

### 왜 문제인가?
- 메모리 사용량 지속 증가
- OOM(OutOfMemoryError) 크래시
- 앱 성능 저하 (GC 빈번 발생)

---

## 🛠️ 디버깅 도구

### 1. Android Studio Memory Profiler
```
사용법:
1. Debug 모드로 앱 실행
2. View > Tool Windows > Profiler
3. Memory 타임라인 클릭

누수 감지:
4. 테스트할 Activity 진입 후 종료
5. 🗑️ Force GC 클릭
6. 📷 Dump Java heap 클릭
7. 종료된 Activity 이름 검색
8. 인스턴스가 있으면 = 누수!

핵심 메트릭:
• Shallow Size: 객체 자체 크기
• Retained Size: GC 시 해제될 총 크기 (이게 크면 심각)
```

### 2. LeakCanary
```kotlin
// build.gradle.kts - 한 줄 설정!
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

// 추가 코드 불필요. 자동으로 동작.
```

**동작 원리:**
1. ObjectWatcher가 destroyed 객체 감시 (WeakReference)
2. 5초 후 GC 강제 실행
3. WeakReference 미해제 = retained
4. Retained 5개 → Heap Dump 생성
5. Shark 분석기가 GC Root → Leaking Object 경로 추적
6. 알림으로 Leak Trace 제공

---

## 🔴 7가지 메모리 누수 패턴

### 1. Static Reference to Activity
```kotlin
// ❌ BAD
object Manager {
    var activity: Activity? = null  // static 필드에 Activity 저장
}

// ✅ GOOD
object Manager {
    private lateinit var appContext: Context
    
    fun init(context: Context) {
        appContext = context.applicationContext  // Application context 사용
    }
}
```

### 2. Inner Class Holding Activity
```kotlin
// ❌ BAD - 익명 클래스가 Activity 암시적 참조
handler.postDelayed(object : Runnable {
    override fun run() {
        updateUI()  // Activity 캡처
    }
}, 60000)

// ✅ GOOD - WeakReference + 정리
private class UpdateRunnable(activity: Activity) : Runnable {
    private val ref = WeakReference(activity)
    override fun run() {
        ref.get()?.updateUI()
    }
}

override fun onDestroy() {
    handler.removeCallbacksAndMessages(null)
}
```

### 3. Handler/Runnable Leaks
```kotlin
// ❌ BAD
Handler().postDelayed({ updateUI() }, 300000)

// ✅ GOOD - lifecycleScope 사용
lifecycleScope.launch {
    delay(300000)
    updateUI()
}
```

### 4. Listener Not Unregistered
```kotlin
// ❌ BAD - unregister 누락
locationManager.requestLocationUpdates(provider, listener)

// ✅ GOOD
override fun onDestroy() {
    locationManager.removeUpdates(listener)
}
```

### 5. Singleton Holding Context
```kotlin
// ❌ BAD
object Db {
    private var ctx: Context? = null
    fun init(context: Context) { ctx = context }  // Activity 저장
}

// ✅ GOOD
object Db {
    private lateinit var appContext: Context
    fun init(context: Context) { 
        appContext = context.applicationContext 
    }
}
```

### 6. ViewModel Holding View
```kotlin
// ❌ BAD - ViewModel이 View 참조
class MyViewModel : ViewModel() {
    var textView: TextView? = null  // 회전 시 이전 Activity 누수
}

// ✅ GOOD - 데이터만 노출
class MyViewModel : ViewModel() {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()
}
```

### 7. Coroutine Scope Leaks
```kotlin
// ❌ BAD - GlobalScope 사용
GlobalScope.launch {
    delay(300000)
    updateUI()  // Activity 캡처
}

// ✅ GOOD - 생명주기 인식 스코프
lifecycleScope.launch { ... }  // Activity 종료 시 취소
viewModelScope.launch { ... }  // ViewModel 클리어 시 취소
```

---

## 📝 면접 Q&A

### Q: 메모리 누수가 뭔가요?
> 사용 완료된 객체가 GC되지 않아 메모리를 계속 점유하는 현상입니다.
> Android에서는 주로 Activity 참조가 static 필드, 싱글톤, 장시간 콜백에 유지되어 발생합니다.

### Q: 메모리 누수를 어떻게 감지하나요?
> 1. **LeakCanary**: debugImplementation 한 줄로 자동 감지
> 2. **Memory Profiler**: Activity 종료 후 Force GC → Heap Dump → 인스턴스 검색

### Q: 가장 흔한 누수 원인은?
> 1. Static 필드에 Activity 참조 저장
> 2. Handler/Runnable에서 Activity 캡처
> 3. 리스너 등록 후 해제 누락

### Q: ViewModel이 View를 참조하면 안 되는 이유?
> ViewModel은 Configuration Change(화면 회전)를 살아남습니다.
> View 참조를 유지하면 이전 Activity가 GC되지 않아 누수됩니다.
> 해결: StateFlow/LiveData로 데이터만 노출하고 View가 observe합니다.

### Q: applicationContext vs Activity context?
> - **Activity context**: Activity 수명 동안만 유효
> - **Application context**: 앱 전체 수명 동안 유효
> 
> 싱글톤이나 static 필드에는 반드시 applicationContext 사용!

### Q: GlobalScope를 쓰면 안 되는 이유?
> GlobalScope는 앱 전체 수명 동안 유지됩니다.
> 코루틴이 Activity를 캡처하면 취소되지 않아 누수됩니다.
> 해결: lifecycleScope, viewModelScope 사용

---

## ⚡ Quick Reference

| 패턴 | 원인 | 해결 |
|------|------|------|
| Static Reference | companion object에 Activity | applicationContext |
| Inner Class | 익명 클래스가 외부 참조 | static class + WeakReference |
| Handler | postDelayed가 Activity 캡처 | removeCallbacksAndMessages |
| Listener | unregister 누락 | onDestroy에서 해제 |
| Singleton | Activity context 저장 | applicationContext |
| ViewModel→View | View 직접 참조 | StateFlow/LiveData |
| Coroutine | GlobalScope | lifecycleScope |

---

## 🔧 예방 체크리스트

- [ ] static 필드에 Activity/View 참조 없음
- [ ] 익명 클래스에서 장시간 Activity 캡처 없음
- [ ] Handler 콜백 onDestroy에서 정리
- [ ] 모든 리스너 등록/해제 쌍 확인
- [ ] 싱글톤은 applicationContext만 사용
- [ ] ViewModel은 View 참조 안 함
- [ ] GlobalScope 사용 안 함 (lifecycleScope 사용)
- [ ] LeakCanary 활성화됨
