package io.github.bokchidevchan.android_study_2601.study.networking.client

import android.util.Log
import io.github.bokchidevchan.android_study_2601.study.networking.api.JsonPlaceholderApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG = "NetworkingStudy"

/**
 * ========================================================================
 * 🔧 OkHttp 클라이언트 설정
 * ========================================================================
 *
 * OkHttp의 핵심 기능들:
 *
 * 1. Connection Pooling
 *    - TCP 연결 재사용으로 핸드셰이크 비용 절감
 *    - 기본 5개 연결, 5분 유지
 *
 * 2. Interceptors
 *    - Application Interceptor: 앱 레벨 (리다이렉트 전)
 *    - Network Interceptor: 네트워크 레벨 (실제 요청/응답)
 *
 * 3. Timeout 설정
 *    - Connect: TCP 연결 타임아웃
 *    - Read: 데이터 읽기 타임아웃
 *    - Write: 데이터 쓰기 타임아웃
 */
object NetworkClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // ========================================================================
    // Interceptors
    // ========================================================================

    /**
     * 📝 로깅 Interceptor
     * HTTP 요청/응답을 로그로 출력
     */
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, "OkHttp: $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * 🔐 인증 토큰 주입 Interceptor
     * 모든 요청에 Authorization 헤더를 자동 추가
     *
     * 실제 앱에서는:
     * - TokenManager, SharedPreferences 등에서 토큰을 가져옴
     * - 토큰이 없으면 로그인 화면으로 이동
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 토큰 추가 (실제로는 TokenManager 등에서 가져옴)
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer dummy_token_for_study")
            .addHeader("X-Custom-Header", "Android-Study-App")
            .build()

        Log.d(TAG, "AuthInterceptor: 헤더 추가됨 → ${newRequest.headers}")

        chain.proceed(newRequest)
    }

    /**
     * 🔄 토큰 갱신 Interceptor (401 에러 처리)
     * 인증 만료 시 토큰을 갱신하고 요청 재시도
     *
     * 실제 구현 시 주의사항:
     * - 동시 요청 시 토큰 갱신 중복 방지 (synchronized 또는 Mutex)
     * - 갱신 실패 시 로그아웃 처리
     * - 무한 루프 방지 (갱신 요청 자체의 401 처리)
     */
    private val tokenRefreshInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            Log.d(TAG, "TokenRefreshInterceptor: 401 감지! 토큰 갱신 시도...")

            // 실제 구현에서는:
            // 1. response.close()
            // 2. 토큰 갱신 API 호출 (동기적으로)
            // 3. 새 토큰으로 원래 요청 재시도
            //
            // synchronized(this) {
            //     val currentToken = tokenManager.getToken()
            //     if (currentToken == originalToken) {
            //         // 아직 갱신 안됨 → 갱신 수행
            //         val newToken = authApi.refreshToken().execute().body()
            //         tokenManager.saveToken(newToken)
            //     }
            // }
            //
            // val newRequest = chain.request().newBuilder()
            //     .header("Authorization", "Bearer ${tokenManager.getToken()}")
            //     .build()
            // return chain.proceed(newRequest)
        }

        response
    }

    // ========================================================================
    // OkHttp Client
    // ========================================================================

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        // Timeout 설정
        .connectTimeout(10, TimeUnit.SECONDS)  // TCP 연결
        .readTimeout(30, TimeUnit.SECONDS)     // 응답 읽기
        .writeTimeout(30, TimeUnit.SECONDS)    // 요청 쓰기

        // Application Interceptors (순서대로 실행)
        .addInterceptor(authInterceptor)       // 1. 토큰 주입
        .addInterceptor(loggingInterceptor)    // 2. 로깅

        // Network Interceptors (실제 네트워크 요청 시 실행)
        .addNetworkInterceptor(tokenRefreshInterceptor)

        // Certificate Pinning (보안 강화 - MITM 공격 방지)
        // 실제 앱에서는 서버 인증서의 공개키 해시를 여기에 설정
        // .certificatePinner(
        //     CertificatePinner.Builder()
        //         .add("jsonplaceholder.typicode.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        //         .build()
        // )

        .build()

    // ========================================================================
    // Retrofit Instance
    // ========================================================================

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        // Kotlinx.Serialization 사용 시:
        // .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    // ========================================================================
    // API Instance
    // ========================================================================

    val api: JsonPlaceholderApi = retrofit.create(JsonPlaceholderApi::class.java)
}
