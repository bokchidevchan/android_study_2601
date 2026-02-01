# Hilt vs 수동 DI 치트시트

> 최근 대화 기반 핵심 정리 | 2025-02-01

---

## 1. 핵심 개념

| 개념 | 설명 | 면접 포인트 |
|------|------|------------|
| **의존성 주입 (DI)** | 객체가 필요한 의존성을 외부에서 주입받는 패턴 | "왜 DI?" → 테스트 용이성, 결합도 감소 |
| **Service Locator** | 전역 컨테이너에서 의존성을 가져오는 패턴 | Anti-pattern으로 보는 시각도 있음 (숨겨진 의존성) |
| **Hilt** | Dagger 기반 Android DI 프레임워크 | 생명주기 자동 관리 + 컴파일 타임 검증 |
| **@Inject constructor** | Hilt가 이 클래스의 인스턴스 생성법을 알게 됨 | 가장 기본적인 Hilt 사용법 |
| **@Module** | 의존성 제공 방법을 정의하는 클래스 | @Binds vs @Provides 차이 중요 |

---

## 2. 코드 비교

### 🔴 수동 DI (Service Locator 패턴)

```kotlin
// 1. 전역 컨테이너
object ManualContainer {
    private var _logger: Logger? = null
    private var _repository: UserRepository? = null
    
    val logger: Logger
        get() = _logger ?: AndroidLogger().also { _logger = it }
    
    val repository: UserRepository
        get() = _repository ?: ApiRepository(logger).also { _repository = it }
    
    // ⚠️ 테스트용 setter 필요
    fun setRepository(repo: UserRepository) { _repository = repo }
    fun reset() { _logger = null; _repository = null }
}

// 2. ViewModelFactory 직접 구현
class UserViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(ManualContainer.repository) as T
    }
}

// 3. Activity에서 사용
class MyActivity : ComponentActivity() {
    private val viewModel by viewModels { UserViewModelFactory() }
}
```

### 🟢 Hilt 사용

```kotlin
// 1. Module 정의
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindRepository(impl: ApiRepository): UserRepository
}

// 2. 구현체에 @Inject
class ApiRepository @Inject constructor(
    private val logger: Logger  // 자동 주입!
) : UserRepository

// 3. ViewModel - Factory 필요 없음!
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel()

// 4. Activity에서 사용
@AndroidEntryPoint
class MyActivity : ComponentActivity() {
    // Compose에서: val viewModel = hiltViewModel()
}
```

---

## 3. @Binds vs @Provides

| 구분 | @Binds | @Provides |
|------|--------|-----------|
| **용도** | 인터페이스 ↔ 구현체 바인딩 | 인스턴스 직접 생성 |
| **모듈 타입** | `abstract class` | `object` |
| **사용 시점** | 단순 매핑 | 외부 라이브러리, 빌더 패턴 |
| **성능** | 더 효율적 (코드 생성 적음) | 약간 비효율적 |

```kotlin
// @Binds - 인터페이스 ↔ 구현체
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bind(impl: ApiRepo): UserRepository
}

// @Provides - 외부 라이브러리
@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com")
        .build()
}
```

---

## 4. 테스트 시 Mock 교체

### 🔴 수동 DI

```kotlin
class ViewModelTest {
    @Before
    fun setUp() {
        ManualContainer.setRepository(mockk())  // 전역 상태 변경!
    }
    
    @After
    fun tearDown() {
        ManualContainer.reset()  // 필수! 안 하면 다른 테스트에 영향
    }
}
```

### 🟢 Hilt

```kotlin
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
@Module
abstract class FakeRepositoryModule {
    @Binds abstract fun bind(fake: FakeRepo): UserRepository
}

@HiltAndroidTest
class ViewModelTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    // FakeRepo가 자동 주입됨, 테스트 격리 보장
}
```

---

## 5. 한눈에 비교

| 항목 | 수동 DI | Hilt |
|------|---------|------|
| 의존성 생성 | `new` / `object` 직접 | `@Inject constructor` |
| 싱글톤 | `if (null)` 체크 | `@Singleton` |
| Factory | 직접 구현 | 자동 생성 |
| 의존성 순서 | 수동 관리 | 자동 해결 |
| 에러 발견 | **런타임** (크래시) | **컴파일 타임** (빌드 실패) |
| 테스트 교체 | 전역 상태 변경 | `@TestInstallIn` |
| 생명주기 | 수동 관리 | 자동 관리 |
| 빌드 시간 | 빠름 | 느림 (KSP로 개선) |

---

## 6. 면접 Q&A

| 질문 | 핵심 답변 |
|------|----------|
| **왜 DI를 쓰나요?** | 테스트 용이성 (Mock 교체), 결합도 감소, 코드 재사용성 |
| **Hilt vs Dagger?** | Hilt = Dagger + Android 생명주기 자동 관리. 보일러플레이트 감소 |
| **@Binds vs @Provides?** | Binds는 인터페이스-구현체 매핑, Provides는 인스턴스 직접 생성 |
| **Hilt 단점은?** | 빌드 시간 증가, 의존성 그래프 불투명, Scope 오용 시 메모리 누수 |
| **테스트에서 Hilt 장점?** | @TestInstallIn으로 선언적 교체, 테스트 격리 보장, 클린업 자동 |
| **언제 수동 DI?** | 작은 프로젝트, 프로토타입, 의존성 5개 미만, 빠른 빌드 필요 시 |

---

## 7. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| `@AndroidEntryPoint` 없이 주입 시도 | Activity/Fragment에 반드시 추가 |
| `@HiltViewModel` 없이 hiltViewModel() 호출 | ViewModel 클래스에 어노테이션 추가 |
| `@Inject` 없는 클래스를 @Binds로 바인딩 | 구현체에 `@Inject constructor` 추가 |
| SingletonComponent에 너무 많은 객체 | 적절한 Scope 사용 (ActivityScoped 등) |
| 테스트 후 전역 상태 reset 안 함 (수동 DI) | @After에서 반드시 reset() 호출 |

---

## 8. Scope 선택 가이드

```kotlin
// 앱 전체 (Retrofit, Room, SharedPrefs)
@Singleton @InstallIn(SingletonComponent::class)

// Activity 재생성에도 유지 (ViewModel 내부 Repository)
@ActivityRetainedScoped @InstallIn(ActivityRetainedComponent::class)

// Activity 단위 (권한 관리, Navigation)
@ActivityScoped @InstallIn(ActivityComponent::class)

// Fragment 단위 (화면별 상태)
@FragmentScoped @InstallIn(FragmentComponent::class)
```

---

## 9. 결론: 언제 무엇을?

```
┌─────────────────────────────────────────────────────┐
│  프로젝트 규모 / 특성에 따른 선택                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  작은 프로젝트 / 프로토타입                           │
│  의존성 5개 미만                     ──────► 수동 DI  │
│  빠른 빌드 우선                                      │
│                                                      │
│  중/대규모 프로젝트                                   │
│  테스트 중요                          ──────► Hilt   │
│  팀 협업 / 장기 유지보수                             │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

*Generated from conversation on 2025-02-01*
