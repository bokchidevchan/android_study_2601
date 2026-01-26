package io.github.bokchidevchan.android_study_2601.study.hilt.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.bokchidevchan.android_study_2601.study.hilt.data.ApiUserRepository
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.UserRepository
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * ========================================================================
 * 📚 Hilt Module - 의존성 제공 방법
 * ========================================================================
 *
 * @Module: 의존성 제공 방법을 정의하는 클래스
 * @InstallIn: 어떤 컴포넌트(스코프)에 설치할지 지정
 *
 * Hilt 컴포넌트 계층 구조:
 *
 * SingletonComponent (앱 전체)
 *     ↓
 * ActivityRetainedComponent (ViewModel 수명)
 *     ↓
 * ViewModelComponent (ViewModel)
 *     ↓
 * ActivityComponent (Activity)
 *     ↓
 * FragmentComponent (Fragment)
 *     ↓
 * ViewComponent (View)
 */

// ========================================================================
// 방법 1: @Binds 사용 (권장)
// ========================================================================

/**
 * @Binds를 사용한 인터페이스-구현체 바인딩
 *
 * 장점:
 * - 코드 생성량 감소 → 빌드 속도 향상
 * - Dagger가 구현체 생성자만 알면 되므로 효율적
 * - 보일러플레이트 코드 최소화
 *
 * 조건:
 * - 반드시 abstract class/interface여야 함
 * - 메서드는 abstract여야 함
 * - 파라미터는 딱 하나 (구현체)
 * - 반환 타입은 인터페이스
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * UserRepository 인터페이스를 요청하면 ApiUserRepository 구현체를 제공
     *
     * 내부 동작:
     * 1. Hilt가 ApiUserRepository의 @Inject constructor 발견
     * 2. 생성자 파라미터들을 재귀적으로 해결
     * 3. 인스턴스 생성 후 UserRepository 타입으로 제공
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        apiUserRepository: ApiUserRepository
    ): UserRepository
}

// ========================================================================
// 방법 2: @Provides 사용 (외부 라이브러리/복잡한 생성 로직)
// ========================================================================

/**
 * @Provides를 사용해야 하는 경우:
 * 1. 외부 라이브러리 클래스 (Retrofit, OkHttp 등)
 * 2. 생성자에 @Inject를 붙일 수 없는 경우
 * 3. 복잡한 생성 로직이 필요한 경우
 * 4. Builder 패턴을 사용하는 경우
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * @Provides 메서드 규칙:
     * - 반환 타입이 제공할 의존성의 타입
     * - 파라미터는 Hilt가 주입 (다른 의존성 필요시)
     * - @Singleton으로 단일 인스턴스 보장 가능
     */
    @Provides
    @Singleton
    fun provideBaseUrl(): String {
        return "https://api.example.com/"
    }

    /**
     * 다른 의존성을 파라미터로 받아 사용
     */
    @Provides
    @Singleton
    fun provideApiConfig(baseUrl: String): ApiConfig {
        return ApiConfig(
            baseUrl = baseUrl,
            timeout = 30_000L,
            retryCount = 3
        )
    }
}

/**
 * API 설정 데이터 클래스
 */
data class ApiConfig(
    val baseUrl: String,
    val timeout: Long,
    val retryCount: Int
)

// ========================================================================
// 방법 3: @Qualifier 사용 (같은 타입, 다른 인스턴스)
// ========================================================================

/**
 * 같은 타입의 여러 인스턴스를 구분할 때 사용
 *
 * 예: 여러 개의 OkHttpClient가 필요한 경우
 * - 일반 API용
 * - 인증 API용 (토큰 인터셉터 포함)
 * - 로깅용
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicBaseUrl

@Module
@InstallIn(SingletonComponent::class)
object UrlModule {

    @Provides
    @AuthBaseUrl
    fun provideAuthBaseUrl(): String = "https://auth.example.com/"

    @Provides
    @PublicBaseUrl
    fun providePublicBaseUrl(): String = "https://api.example.com/"
}

/**
 * 사용 예시:
 *
 * class AuthRepository @Inject constructor(
 *     @AuthBaseUrl private val authUrl: String,
 *     @PublicBaseUrl private val publicUrl: String
 * )
 */
