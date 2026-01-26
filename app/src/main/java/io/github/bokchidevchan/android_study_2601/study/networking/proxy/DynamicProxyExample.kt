package io.github.bokchidevchan.android_study_2601.study.networking.proxy

import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

private const val TAG = "NetworkingStudy"

/**
 * ========================================================================
 * 🎭 Dynamic Proxy 학습용 예제
 * ========================================================================
 *
 * Retrofit이 내부적으로 사용하는 Dynamic Proxy 패턴을 직접 구현해보기
 *
 * Dynamic Proxy란?
 * - 런타임에 인터페이스의 구현체를 동적으로 생성
 * - 모든 메서드 호출을 InvocationHandler로 가로챔
 * - Retrofit은 이를 이용해 어노테이션을 분석하고 HTTP 요청으로 변환
 *
 * 왜 사용하나?
 * - 컴파일 타임에 구현체를 작성할 필요 없음
 * - 어노테이션 기반으로 동적 동작 가능
 * - 보일러플레이트 코드 제거
 */

// ========================================================================
// 예제 인터페이스
// ========================================================================

/**
 * 간단한 API 인터페이스
 * 실제 구현 없이 Dynamic Proxy가 처리
 */
interface SimpleApi {
    fun getData(): String
    fun getUser(id: Int): String
}

// ========================================================================
// InvocationHandler 구현
// ========================================================================

/**
 * 모든 메서드 호출을 가로채는 핸들러
 *
 * Retrofit은 여기서:
 * 1. 메서드의 어노테이션(@GET, @POST 등) 분석
 * 2. 파라미터의 어노테이션(@Path, @Query 등) 분석
 * 3. HTTP 요청 객체 생성
 * 4. OkHttp로 실행
 * 5. 응답을 반환 타입으로 변환
 */
class SimpleApiInvocationHandler : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        val methodName = method?.name ?: "unknown"
        val params = args?.joinToString() ?: "없음"
        val returnType = method?.returnType?.simpleName ?: "unknown"

        Log.d(TAG, "🎭 ═══════════════════════════════════════")
        Log.d(TAG, "🎭 Dynamic Proxy 호출됨!")
        Log.d(TAG, "🎭 ───────────────────────────────────────")
        Log.d(TAG, "🎭   메서드 이름: $methodName")
        Log.d(TAG, "🎭   파라미터: $params")
        Log.d(TAG, "🎭   반환 타입: $returnType")
        Log.d(TAG, "🎭 ═══════════════════════════════════════")

        // Retrofit은 여기서 어노테이션을 분석하여 HTTP 요청을 생성함
        // 예: @GET("users/{id}") → GET /users/42
        return "[$methodName] 호출됨 (파라미터: $params)"
    }
}

// ========================================================================
// Proxy 생성 함수
// ========================================================================

/**
 * Dynamic Proxy로 인터페이스 구현체 생성
 *
 * 이것이 Retrofit.create()가 내부적으로 하는 일!
 *
 * ```kotlin
 * // Retrofit 사용
 * val api = retrofit.create(ApiService::class.java)
 *
 * // 내부적으로는 이렇게 동작
 * val api = Proxy.newProxyInstance(
 *     ApiService::class.java.classLoader,
 *     arrayOf(ApiService::class.java),
 *     ServiceMethodInvocationHandler(retrofit)
 * ) as ApiService
 * ```
 */
fun createSimpleApi(): SimpleApi {
    return Proxy.newProxyInstance(
        SimpleApi::class.java.classLoader,
        arrayOf(SimpleApi::class.java),
        SimpleApiInvocationHandler()
    ) as SimpleApi
}

// ========================================================================
// Retrofit 스타일 예제 (더 상세한 버전)
// ========================================================================

/**
 * Retrofit 스타일의 어노테이션 (학습용)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET(val path: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Path(val name: String)

/**
 * 어노테이션이 있는 API 인터페이스
 */
interface AnnotatedApi {
    @GET("users/{id}")
    fun getUser(@Path("id") userId: Int): String
}

/**
 * 어노테이션을 분석하는 InvocationHandler
 * Retrofit의 실제 동작을 간략화한 버전
 */
class AnnotationAwareInvocationHandler(
    private val baseUrl: String
) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        // 1. 메서드의 @GET 어노테이션 분석
        val getAnnotation = method?.getAnnotation(GET::class.java)
        var path = getAnnotation?.path ?: ""

        // 2. 파라미터의 @Path 어노테이션 분석
        method?.parameters?.forEachIndexed { index, parameter ->
            val pathAnnotation = parameter.getAnnotation(Path::class.java)
            if (pathAnnotation != null && args != null) {
                // {id} → 실제 값으로 치환
                path = path.replace("{${pathAnnotation.name}}", args[index].toString())
            }
        }

        val fullUrl = "$baseUrl$path"

        Log.d(TAG, "🔍 ═══════════════════════════════════════")
        Log.d(TAG, "🔍 어노테이션 분석 결과")
        Log.d(TAG, "🔍 ───────────────────────────────────────")
        Log.d(TAG, "🔍   HTTP Method: GET")
        Log.d(TAG, "🔍   Full URL: $fullUrl")
        Log.d(TAG, "🔍 ═══════════════════════════════════════")

        // 실제 Retrofit은 여기서 OkHttp를 사용해 HTTP 요청 수행
        return "GET $fullUrl → 응답 데이터"
    }
}

/**
 * 어노테이션 기반 API 생성
 */
fun createAnnotatedApi(baseUrl: String): AnnotatedApi {
    return Proxy.newProxyInstance(
        AnnotatedApi::class.java.classLoader,
        arrayOf(AnnotatedApi::class.java),
        AnnotationAwareInvocationHandler(baseUrl)
    ) as AnnotatedApi
}
