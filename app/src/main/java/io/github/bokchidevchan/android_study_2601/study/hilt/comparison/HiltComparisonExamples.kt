package io.github.bokchidevchan.android_study_2601.study.hilt.comparison

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ========================================================================
 * 📚 Hilt 사용 전/후 비교 예제
 * ========================================================================
 *
 * 이 파일은 같은 기능을 두 가지 방식으로 구현합니다:
 * 1. Hilt 없이 (수동 의존성 주입)
 * 2. Hilt 사용 (자동 의존성 주입)
 *
 * 🎯 학습 포인트:
 * - 왜 의존성 주입이 필요한가?
 * - Hilt가 어떤 보일러플레이트를 제거하는가?
 * - 테스트는 어떻게 더 쉬워지는가?
 *
 * ========================================================================
 */

// ========================================================================
// 공통: 도메인 레이어 (인터페이스)
// ========================================================================

/**
 * 사용자 정보를 가져오는 Repository 인터페이스
 * 
 * ✅ 핵심: 인터페이스로 정의하면 구현체 교체가 쉬움
 *    - 실제 앱: ApiUserRepository (서버 통신)
 *    - 테스트: FakeUserRepository (가짜 데이터)
 */
interface UserRepository {
    suspend fun getUser(id: String): User?
    suspend fun saveUser(user: User)
}

/**
 * 로깅을 담당하는 인터페이스
 */
interface Logger {
    fun log(message: String)
    fun error(message: String)
}

/**
 * 분석 이벤트를 추적하는 인터페이스
 */
interface AnalyticsTracker {
    fun trackEvent(event: String, params: Map<String, String> = emptyMap())
}

data class User(
    val id: String,
    val name: String,
    val email: String
)

// ========================================================================
// 공통: 데이터 레이어 (구현체)
// ========================================================================

/**
 * 실제 API 호출하는 Repository 구현체
 */
class ApiUserRepositoryImpl(
    private val logger: Logger
) : UserRepository {
    
    private val users = mutableMapOf<String, User>()
    
    override suspend fun getUser(id: String): User? {
        logger.log("API에서 사용자 조회: $id")
        // 실제로는 Retrofit/Ktor 등으로 API 호출
        return users[id]
    }
    
    override suspend fun saveUser(user: User) {
        logger.log("API에 사용자 저장: ${user.id}")
        users[user.id] = user
    }
}

/**
 * Android Logcat에 로그를 출력하는 Logger 구현체
 */
class AndroidLogger : Logger {
    override fun log(message: String) {
        android.util.Log.d("AppLogger", message)
    }
    
    override fun error(message: String) {
        android.util.Log.e("AppLogger", message)
    }
}

/**
 * Firebase Analytics를 사용하는 Tracker 구현체
 */
class FirebaseAnalyticsTracker(
    private val logger: Logger
) : AnalyticsTracker {
    override fun trackEvent(event: String, params: Map<String, String>) {
        logger.log("Analytics: $event - $params")
        // 실제로는 Firebase Analytics SDK 호출
    }
}


// ════════════════════════════════════════════════════════════════════════
// ❌ Hilt 없이 구현하기 (수동 의존성 주입)
// ════════════════════════════════════════════════════════════════════════

/**
 * ========================================================================
 * 방법 1: 수동 의존성 주입 (Service Locator 패턴)
 * ========================================================================
 * 
 * 문제점:
 * 1. 모든 의존성을 직접 생성하고 관리해야 함
 * 2. 의존성 순서에 주의해야 함 (Logger → Repository → ViewModel)
 * 3. 싱글톤 관리를 직접 해야 함
 * 4. 테스트 시 교체가 번거로움
 */

/**
 * 수동 의존성 컨테이너
 * 
 * 앱 전체에서 사용할 의존성들을 직접 생성하고 관리
 */
object ManualDependencyContainer {
    
    // 직접 싱글톤 관리
    private var _logger: Logger? = null
    private var _userRepository: UserRepository? = null
    private var _analyticsTracker: AnalyticsTracker? = null
    
    /**
     * Logger 제공 (싱글톤)
     * 
     * 문제: lazy 초기화, null 체크, 스레드 안전성 모두 직접 관리
     */
    val logger: Logger
        get() {
            if (_logger == null) {
                _logger = AndroidLogger()
            }
            return _logger!!
        }
    
    /**
     * UserRepository 제공 (싱글톤)
     * 
     * 문제: logger에 의존 → 순서 중요!
     */
    val userRepository: UserRepository
        get() {
            if (_userRepository == null) {
                _userRepository = ApiUserRepositoryImpl(logger)  // logger 먼저 필요!
            }
            return _userRepository!!
        }
    
    /**
     * AnalyticsTracker 제공 (싱글톤)
     */
    val analyticsTracker: AnalyticsTracker
        get() {
            if (_analyticsTracker == null) {
                _analyticsTracker = FirebaseAnalyticsTracker(logger)
            }
            return _analyticsTracker!!
        }
    
    /**
     * 테스트용 Mock으로 교체하는 메서드
     * 
     * ⚠️ 문제점:
     * - 전역 상태 변경 → 테스트 간 간섭 가능
     * - 모든 의존성에 대해 setter 필요
     * - reset 메서드도 별도로 필요
     */
    fun setLogger(logger: Logger) {
        _logger = logger
    }
    
    fun setUserRepository(repository: UserRepository) {
        _userRepository = repository
    }
    
    fun reset() {
        _logger = null
        _userRepository = null
        _analyticsTracker = null
    }
}

/**
 * 수동 ViewModel (Hilt 없음)
 * 
 * ⚠️ 문제점:
 * 1. 의존성을 직접 가져와야 함
 * 2. ViewModelFactory를 직접 구현해야 함
 */
class ManualUserViewModel(
    private val userRepository: UserRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            analyticsTracker.trackEvent("user_load", mapOf("user_id" to userId))
            _user.value = userRepository.getUser(userId)
            _isLoading.value = false
        }
    }
    
    fun saveUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.saveUser(user)
            analyticsTracker.trackEvent("user_save", mapOf("user_id" to user.id))
            _user.value = user
            _isLoading.value = false
        }
    }
}

/**
 * 수동 ViewModelFactory
 * 
 * ⚠️ 문제점:
 * - 모든 ViewModel마다 Factory 필요
 * - 보일러플레이트 코드 많음
 * - 의존성 추가 시 Factory도 수정 필요
 */
class ManualUserViewModelFactory : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManualUserViewModel::class.java)) {
            return ManualUserViewModel(
                userRepository = ManualDependencyContainer.userRepository,
                analyticsTracker = ManualDependencyContainer.analyticsTracker
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * 수동 의존성 주입으로 Activity에서 사용하는 예시
 * 
 * ```kotlin
 * class ManualActivity : ComponentActivity() {
 *     
 *     // 직접 Factory 생성
 *     private val viewModel: ManualUserViewModel by viewModels {
 *         ManualUserViewModelFactory()
 *     }
 *     
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContent {
 *             // viewModel 사용
 *         }
 *     }
 * }
 * ```
 */


// ════════════════════════════════════════════════════════════════════════
// ✅ Hilt 사용하여 구현하기 (자동 의존성 주입)
// ════════════════════════════════════════════════════════════════════════

/**
 * ========================================================================
 * 방법 2: Hilt를 사용한 의존성 주입
 * ========================================================================
 * 
 * 장점:
 * 1. 선언적으로 의존성 정의 (어노테이션)
 * 2. 의존성 그래프 자동 생성
 * 3. 생명주기 자동 관리
 * 4. 컴파일 타임 검증
 * 5. 테스트 시 쉬운 교체 (@TestInstallIn)
 */

// ────────────────────────────────────────────────────────────────────────
// Step 1: Module 정의 - 의존성 제공 방법 선언
// ────────────────────────────────────────────────────────────────────────

/**
 * Logger 모듈
 * 
 * @Provides: 직접 인스턴스를 생성하여 제공
 * - 외부 라이브러리 (Retrofit, Room 등)
 * - 빌더 패턴이 필요한 객체
 * - 복잡한 초기화가 필요한 객체
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {
    
    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return AndroidLogger()
    }
}

/**
 * Repository 모듈
 * 
 * @Binds: 인터페이스와 구현체를 바인딩
 * - 인터페이스 ↔ 구현체 매핑
 * - 더 효율적 (Provides보다 생성 코드 적음)
 * - abstract class/interface 필수
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModuleWithHilt {
    
    /**
     * UserRepository 인터페이스를 ApiUserRepositoryWithHilt로 바인딩
     * 
     * Hilt가 알아서:
     * 1. ApiUserRepositoryWithHilt의 @Inject constructor 찾음
     * 2. 필요한 Logger 의존성 주입
     * 3. 인스턴스 생성
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: ApiUserRepositoryWithHilt
    ): UserRepository
}

/**
 * Analytics 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    
    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: FirebaseAnalyticsTrackerWithHilt
    ): AnalyticsTracker
}

// ────────────────────────────────────────────────────────────────────────
// Step 2: 구현체에 @Inject constructor 추가
// ────────────────────────────────────────────────────────────────────────

/**
 * Hilt용 Repository 구현체
 * 
 * @Inject constructor: Hilt에게 이 클래스의 인스턴스 생성 방법을 알려줌
 * - Logger는 자동으로 주입됨 (LoggerModule에서 제공)
 */
class ApiUserRepositoryWithHilt @Inject constructor(
    private val logger: Logger
) : UserRepository {
    
    private val users = mutableMapOf<String, User>()
    
    override suspend fun getUser(id: String): User? {
        logger.log("[Hilt] API에서 사용자 조회: $id")
        return users[id]
    }
    
    override suspend fun saveUser(user: User) {
        logger.log("[Hilt] API에 사용자 저장: ${user.id}")
        users[user.id] = user
    }
}

/**
 * Hilt용 Analytics 구현체
 */
class FirebaseAnalyticsTrackerWithHilt @Inject constructor(
    private val logger: Logger
) : AnalyticsTracker {
    
    override fun trackEvent(event: String, params: Map<String, String>) {
        logger.log("[Hilt] Analytics: $event - $params")
    }
}

// ────────────────────────────────────────────────────────────────────────
// Step 3: ViewModel에 @HiltViewModel 추가
// ────────────────────────────────────────────────────────────────────────

/**
 * Hilt ViewModel
 * 
 * ✅ 장점:
 * - Factory 필요 없음!
 * - 생성자에 필요한 의존성 나열만 하면 됨
 * - Compose에서 hiltViewModel()로 바로 획득
 */
@HiltViewModel
class HiltUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            analyticsTracker.trackEvent("user_load", mapOf("user_id" to userId))
            _user.value = userRepository.getUser(userId)
            _isLoading.value = false
        }
    }
    
    fun saveUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.saveUser(user)
            analyticsTracker.trackEvent("user_save", mapOf("user_id" to user.id))
            _user.value = user
            _isLoading.value = false
        }
    }
}

/**
 * Hilt로 Activity에서 사용하는 예시
 * 
 * ```kotlin
 * @AndroidEntryPoint  // ← 이것만 추가!
 * class HiltActivity : ComponentActivity() {
 *     
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContent {
 *             val viewModel: HiltUserViewModel = hiltViewModel()  // ← Factory 필요 없음!
 *             // viewModel 사용
 *         }
 *     }
 * }
 * ```
 */


// ════════════════════════════════════════════════════════════════════════
// 📊 비교 요약
// ════════════════════════════════════════════════════════════════════════

/**
 * ┌─────────────────────┬────────────────────────┬────────────────────────┐
 * │        항목         │    Hilt 없이 (수동)     │     Hilt 사용 (자동)    │
 * ├─────────────────────┼────────────────────────┼────────────────────────┤
 * │ 의존성 생성         │ 직접 new/object 사용    │ @Inject constructor    │
 * │ 싱글톤 관리         │ 직접 if-null 체크       │ @Singleton 어노테이션   │
 * │ ViewModel Factory   │ 직접 구현 필요          │ 자동 생성               │
 * │ 의존성 그래프       │ 수동 순서 관리          │ 컴파일 타임 자동 생성   │
 * │ 에러 발견 시점      │ 런타임 (앱 크래시)      │ 컴파일 타임 (빌드 실패) │
 * │ 테스트 시 교체      │ 전역 상태 변경 필요     │ @TestInstallIn 사용     │
 * │ 생명주기 관리       │ 직접 관리               │ 자동 관리               │
 * │ 보일러플레이트      │ 많음                    │ 적음                    │
 * │ 빌드 시간           │ 빠름                    │ 약간 느림 (KSP로 개선)  │
 * │ 학습 비용           │ 낮음                    │ 중간                    │
 * └─────────────────────┴────────────────────────┴────────────────────────┘
 * 
 * 
 * 🎯 결론:
 * 
 * 작은 프로젝트/프로토타입:
 *   → 수동 DI도 충분 (빠른 빌드, 단순함)
 * 
 * 중/대규모 프로젝트:
 *   → Hilt 권장 (테스트 용이성, 유지보수성, 안전성)
 */
