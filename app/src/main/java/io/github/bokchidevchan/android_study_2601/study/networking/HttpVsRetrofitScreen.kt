package io.github.bokchidevchan.android_study_2601.study.networking

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * ========================================================================
 * 📚 학습 목표: HttpURLConnection vs Retrofit 이해하기
 * ========================================================================
 *
 * 🔑 핵심 개념:
 *
 * 1. HttpURLConnection (Low-level)
 *    - Java 표준 API
 *    - InputStream/OutputStream 직접 제어
 *    - 모든 것을 수동으로 처리 (파싱, 에러 핸들링, 스레드 등)
 *
 * 2. Retrofit (High-level)
 *    - Square사의 Type-safe HTTP 클라이언트
 *    - 어노테이션 기반 선언적 API 정의
 *    - Dynamic Proxy로 런타임에 구현체 생성
 *
 * 3. OkHttp (Retrofit의 엔진)
 *    - Connection Pooling: TCP 연결 재사용
 *    - Interceptors: 요청/응답 가로채기
 *    - Transparent GZIP: 자동 압축
 *
 * 4. JSON Serialization
 *    - Gson: 리플렉션 기반, 유연하지만 무거움
 *    - Kotlinx.Serialization: 컴파일 타임 코드 생성, 타입 안전
 *
 * ========================================================================
 */

private const val TAG = "NetworkingStudy"

// ========================================================================
// 📦 데이터 모델 (Gson용)
// ========================================================================

/**
 * Gson은 @SerializedName으로 JSON 키를 매핑
 * 리플렉션을 사용하므로 런타임에 동작
 */
data class GsonUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String
)

// ========================================================================
// 📦 데이터 모델 (Kotlinx.Serialization용)
// ========================================================================

/**
 * Kotlinx.Serialization은 @Serializable로 컴파일 타임에 직렬화 코드 생성
 * 리플렉션 없이 동작하여 성능이 좋고 타입 안전
 *
 * ⚠️ 주의: non-null 필드에 null이 오면 예외 발생 (Gson은 null 주입함!)
 */
@Serializable
data class KotlinxUser(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("username") val username: String,
    @SerialName("email") val email: String
)

// ========================================================================
// 🌐 Retrofit API 인터페이스
// ========================================================================

/**
 * Retrofit의 마법: 인터페이스만 정의하면 구현체는 Dynamic Proxy가 자동 생성!
 *
 * 내부 동작:
 * 1. Retrofit.create(ApiService::class.java) 호출
 * 2. Java의 Proxy.newProxyInstance() 사용
 * 3. 메서드 호출 시 어노테이션 분석 → HTTP 요청 생성
 */
interface JsonPlaceholderApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): GsonUser

    @GET("users")
    suspend fun getUsers(): List<GsonUser>
}

// ========================================================================
// 🔧 OkHttp 클라이언트 설정
// ========================================================================

/**
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

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, "OkHttp: $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * 🔐 인증 토큰 주입 Interceptor
     * 모든 요청에 Authorization 헤더를 자동 추가
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
     */
    private val tokenRefreshInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            Log.d(TAG, "TokenRefreshInterceptor: 401 감지! 토큰 갱신 시도...")

            // 실제 구현에서는:
            // 1. response.close()
            // 2. 토큰 갱신 API 호출
            // 3. 새 토큰으로 원래 요청 재시도
            // val newToken = refreshToken()
            // val newRequest = chain.request().newBuilder()
            //     .header("Authorization", "Bearer $newToken")
            //     .build()
            // return chain.proceed(newRequest)
        }

        response
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)           // Application Interceptor
        .addInterceptor(loggingInterceptor)        // Logging
        .addNetworkInterceptor(tokenRefreshInterceptor) // Network Interceptor
        // Certificate Pinning (보안 강화)
        // .certificatePinner(
        //     CertificatePinner.Builder()
        //         .add("jsonplaceholder.typicode.com", "sha256/...")
        //         .build()
        // )
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: JsonPlaceholderApi = retrofit.create(JsonPlaceholderApi::class.java)
}

// ========================================================================
// 🎭 Dynamic Proxy 학습용 예제
// ========================================================================

/**
 * Retrofit이 내부적으로 사용하는 Dynamic Proxy 패턴을 직접 구현해보기
 *
 * Dynamic Proxy란?
 * - 런타임에 인터페이스의 구현체를 동적으로 생성
 * - 모든 메서드 호출을 InvocationHandler로 가로챔
 * - Retrofit은 이를 이용해 어노테이션을 분석하고 HTTP 요청으로 변환
 */
interface SimpleApi {
    fun getData(): String
    fun getUser(id: Int): String
}

class SimpleApiInvocationHandler : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        val methodName = method?.name ?: "unknown"
        val params = args?.joinToString() ?: "없음"

        Log.d(TAG, "🎭 Dynamic Proxy 호출됨!")
        Log.d(TAG, "  - 메서드: $methodName")
        Log.d(TAG, "  - 파라미터: $params")

        // Retrofit은 여기서 어노테이션을 분석하여 HTTP 요청을 생성함
        return "[$methodName] 호출됨 (파라미터: $params)"
    }
}

/**
 * Dynamic Proxy로 인터페이스 구현체 생성
 * Retrofit.create()가 내부적으로 하는 일!
 */
fun createSimpleApi(): SimpleApi {
    return Proxy.newProxyInstance(
        SimpleApi::class.java.classLoader,
        arrayOf(SimpleApi::class.java),
        SimpleApiInvocationHandler()
    ) as SimpleApi
}

// ========================================================================
// 🎨 메인 화면
// ========================================================================

@Composable
fun HttpVsRetrofitScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        Text(
            text = "🌐 HttpURLConnection vs Retrofit",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "저수준 API와 고수준 추상화의 차이를 비교합니다",
            fontSize = 14.sp,
            color = Color.Gray
        )

        // 예제 1: HttpURLConnection
        HttpUrlConnectionExample()

        // 예제 2: Retrofit
        RetrofitExample()

        // 예제 3: Dynamic Proxy
        DynamicProxyExample()

        // 예제 4: JSON 직렬화 비교
        JsonSerializationComparison()

        // 예제 5: Interceptor 설명
        InterceptorExplanation()

        // Trade-off 설명
        TradeOffComparison()
    }
}

// ========================================================================
// 🔬 예제 1: HttpURLConnection (Low-level)
// ========================================================================

@Composable
fun HttpUrlConnectionExample() {
    var result by remember { mutableStateOf("버튼을 눌러 테스트하세요") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFCDD2) // 빨간색 계열
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "❌ HttpURLConnection (Low-level)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "직접 스트림을 열고, 바이트를 읽고, 파싱해야 함",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 코드 예시
            CodeBlock(
                code = """
// 1. URL 객체 생성
val url = URL("https://api.example.com/users/1")

// 2. 연결 열기
val conn = url.openConnection() as HttpURLConnection
conn.requestMethod = "GET"
conn.connectTimeout = 10000
conn.readTimeout = 10000

// 3. 응답 코드 확인
if (conn.responseCode == 200) {
    // 4. InputStream에서 바이트 읽기
    val reader = BufferedReader(
        InputStreamReader(conn.inputStream)
    )
    val response = reader.readText()
    reader.close()

    // 5. JSON 파싱 (직접!)
    val user = Gson().fromJson(response, User::class.java)
}

// 6. 연결 닫기 (잊으면 메모리 누수!)
conn.disconnect()
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        result = fetchWithHttpUrlConnection()
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("HttpURLConnection으로 요청")
                }
            }

            if (result.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private suspend fun fetchWithHttpUrlConnection(): String = withContext(Dispatchers.IO) {
    try {
        val startTime = System.currentTimeMillis()

        val url = URL("https://jsonplaceholder.typicode.com/users/1")
        val connection = url.openConnection() as HttpURLConnection

        connection.apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json")
        }

        Log.d(TAG, "HttpURLConnection: 연결 시작...")

        val responseCode = connection.responseCode
        Log.d(TAG, "HttpURLConnection: 응답 코드 = $responseCode")

        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()

            Log.d(TAG, "HttpURLConnection: 원본 응답 = $response")

            // Gson으로 파싱
            val user = Gson().fromJson(response, GsonUser::class.java)

            val elapsed = System.currentTimeMillis() - startTime

            connection.disconnect()

            """
            ✅ 성공! (${elapsed}ms)
            ID: ${user.id}
            Name: ${user.name}
            Email: ${user.email}
            """.trimIndent()
        } else {
            connection.disconnect()
            "❌ 에러: HTTP $responseCode"
        }
    } catch (e: Exception) {
        Log.e(TAG, "HttpURLConnection 에러", e)
        "❌ 예외: ${e.message}"
    }
}

// ========================================================================
// 🔬 예제 2: Retrofit (High-level)
// ========================================================================

@Composable
fun RetrofitExample() {
    var result by remember { mutableStateOf("버튼을 눌러 테스트하세요") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFC8E6C9) // 초록색 계열
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ Retrofit (High-level)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "인터페이스 선언만으로 네트워크 통신 완성!",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 코드 예시
            CodeBlock(
                code = """
// 1. 인터페이스 선언 (구현은 Retrofit이 Dynamic Proxy로!)
interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
}

// 2. Retrofit 인스턴스 생성
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// 3. API 인스턴스 생성 (Dynamic Proxy!)
val api = retrofit.create(ApiService::class.java)

// 4. 사용 (끝!)
val user = api.getUser(1)
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        result = fetchWithRetrofit()
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Retrofit으로 요청")
                }
            }

            if (result.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private suspend fun fetchWithRetrofit(): String = withContext(Dispatchers.IO) {
    try {
        val startTime = System.currentTimeMillis()

        Log.d(TAG, "Retrofit: 요청 시작...")

        val user = NetworkClient.api.getUser(1)

        val elapsed = System.currentTimeMillis() - startTime

        Log.d(TAG, "Retrofit: 응답 = $user")

        """
        ✅ 성공! (${elapsed}ms)
        ID: ${user.id}
        Name: ${user.name}
        Email: ${user.email}

        🎯 훨씬 간단하죠?
        """.trimIndent()
    } catch (e: Exception) {
        Log.e(TAG, "Retrofit 에러", e)
        "❌ 예외: ${e.message}"
    }
}

// ========================================================================
// 🔬 예제 3: Dynamic Proxy 이해하기
// ========================================================================

@Composable
fun DynamicProxyExample() {
    var result by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎭 Dynamic Proxy 이해하기",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "Retrofit이 인터페이스만으로 동작하는 비밀",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            CodeBlock(
                code = """
// Retrofit.create() 내부 동작 (간략화)
fun <T> create(service: Class<T>): T {
    return Proxy.newProxyInstance(
        service.classLoader,
        arrayOf(service),
        object : InvocationHandler {
            override fun invoke(
                proxy: Any,
                method: Method,
                args: Array<Any>?
            ): Any {
                // 1. 메서드의 어노테이션 분석
                val getAnnotation = method.getAnnotation(GET::class.java)
                val path = getAnnotation?.value

                // 2. HTTP 요청 생성
                val request = buildRequest(path, args)

                // 3. OkHttp로 실행
                return okHttpClient.execute(request)
            }
        }
    ) as T
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val proxy = createSimpleApi()
                    val r1 = proxy.getData()
                    val r2 = proxy.getUser(42)
                    result = "결과 1: $r1\n결과 2: $r2\n\n(Logcat에서 상세 로그 확인)"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dynamic Proxy 테스트")
            }

            if (result.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ========================================================================
// 🔬 예제 4: JSON 직렬화 비교
// ========================================================================

@Composable
fun JsonSerializationComparison() {
    var result by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📦 Gson vs Kotlinx.Serialization",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "JSON 직렬화 라이브러리 비교",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gson 설명
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE0B2)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔴 Gson (리플렉션 기반)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = """
• 런타임에 리플렉션으로 객체 분석
• 유연하지만 성능 오버헤드 있음
• ⚠️ Kotlin non-null 타입 무시 → null 주입 위험!
                        """.trimIndent(),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kotlinx.Serialization 설명
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFC8E6C9)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🟢 Kotlinx.Serialization (코드 생성)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = """
• 컴파일 타임에 직렬화 코드 생성
• 리플렉션 없이 빠르고 안전
• ✅ Kotlin 타입 시스템 완벽 지원
                        """.trimIndent(),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    result = compareJsonSerialization()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("직렬화 비교 테스트")
            }

            if (result.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun compareJsonSerialization(): String {
    val json = """{"id":1,"name":"John","username":"john_doe","email":"john@example.com"}"""

    // Gson
    val gsonStartTime = System.nanoTime()
    repeat(1000) {
        Gson().fromJson(json, GsonUser::class.java)
    }
    val gsonTime = (System.nanoTime() - gsonStartTime) / 1_000_000

    // Kotlinx.Serialization
    val kotlinxJson = Json { ignoreUnknownKeys = true }
    val kotlinxStartTime = System.nanoTime()
    repeat(1000) {
        kotlinxJson.decodeFromString<KotlinxUser>(json)
    }
    val kotlinxTime = (System.nanoTime() - kotlinxStartTime) / 1_000_000

    // null 안전성 테스트
    val nullJson = """{"id":1,"name":null,"username":"john","email":"john@example.com"}"""

    val gsonNullResult = try {
        val user = Gson().fromJson(nullJson, GsonUser::class.java)
        "name = ${user.name} (null이 주입됨!)"
    } catch (e: Exception) {
        "예외: ${e.message}"
    }

    val kotlinxNullResult = try {
        kotlinxJson.decodeFromString<KotlinxUser>(nullJson)
        "성공 (예상 밖)"
    } catch (e: Exception) {
        "✅ 예외 발생 (타입 안전!)"
    }

    return """
📊 1000회 파싱 성능 비교:
• Gson: ${gsonTime}ms
• Kotlinx: ${kotlinxTime}ms

🔒 null 안전성 테스트 (non-null에 null 주입):
• Gson: $gsonNullResult
• Kotlinx: $kotlinxNullResult
    """.trimIndent()
}

// ========================================================================
// 🔬 예제 5: Interceptor 설명
// ========================================================================

@Composable
fun InterceptorExplanation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔧 OkHttp Interceptor",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "요청/응답을 가로채서 처리하는 미들웨어",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interceptor 종류
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE1BEE7)
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Application",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• 리다이렉트 전\n• 캐시 전\n• 1번만 호출",
                            fontSize = 10.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFCE93D8)
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Network",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• 실제 네트워크\n• 리다이렉트마다\n• 여러 번 호출",
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 활용 예시
            Text(
                text = "💡 실전 활용 예시",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            CodeBlock(
                code = """
// 1. 토큰 자동 주입
val authInterceptor = Interceptor { chain ->
    val newRequest = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer ${'$'}token")
        .build()
    chain.proceed(newRequest)
}

// 2. 토큰 갱신 (401 처리)
val refreshInterceptor = Interceptor { chain ->
    val response = chain.proceed(chain.request())
    if (response.code == 401) {
        val newToken = refreshToken()
        // 새 토큰으로 재시도
    }
    response
}

// 3. Certificate Pinning (MITM 방지)
val pinner = CertificatePinner.Builder()
    .add("api.example.com", "sha256/...")
    .build()
                """.trimIndent()
            )
        }
    }
}

// ========================================================================
// 📊 Trade-off 비교
// ========================================================================

@Composable
fun TradeOffComparison() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ Trade-off 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비교 테이블
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // 헤더
                    Row {
                        TableCell("구분", isHeader = true, width = 80)
                        TableCell("HttpURLConnection", isHeader = true, width = 140)
                        TableCell("Retrofit + OkHttp", isHeader = true, width = 140)
                    }

                    // 데이터
                    Row {
                        TableCell("제어권", width = 80)
                        TableCell("완벽한 로우레벨 제어", width = 140)
                        TableCell("프레임워크 범위 내", width = 140)
                    }

                    Row {
                        TableCell("복잡도", width = 80)
                        TableCell("스레드, 커넥션 풀 직접 구현", width = 140)
                        TableCell("설정 몇 줄로 해결", width = 140)
                    }

                    Row {
                        TableCell("성능", width = 80)
                        TableCell("최적화 시 미세하게 가벼움", width = 140)
                        TableCell("검증된 최적화 적용됨", width = 140)
                    }

                    Row {
                        TableCell("유지보수", width = 80)
                        TableCell("코드 중복, 휴먼 에러 위험", width = 140)
                        TableCell("표준화로 협업 용이", width = 140)
                    }

                    Row {
                        TableCell("사용 시기", width = 80)
                        TableCell("특수 프로토콜, 극한 최적화", width = 140)
                        TableCell("99% 실무 상황", width = 140)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9C4)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "👨‍💼 시니어 개발자의 조언",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = """
"직접 구현을 택하는 경우는 '라이브러리 크기가 극도로 제한된 환경'이거나
'특수한 프로토콜(Custom TCP)'을 사용해야 할 때뿐입니다.

그 외엔 안정성과 생산성이 검증된 Retrofit + OkHttp 조합이 압도적입니다."
                        """.trimIndent(),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    isHeader: Boolean = false,
    width: Int
) {
    Box(
        modifier = Modifier
            .size(width.dp, 40.dp)
            .background(
                if (isHeader) Color(0xFFE0E0E0) else Color.White
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ========================================================================
// 🎨 공통 컴포넌트
// ========================================================================

@Composable
fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF263238),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFF80CBC4),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 14.sp
        )
    }
}

// ========================================================================
// 🎨 Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
fun HttpVsRetrofitScreenPreview() {
    MaterialTheme {
        HttpVsRetrofitScreen()
    }
}
