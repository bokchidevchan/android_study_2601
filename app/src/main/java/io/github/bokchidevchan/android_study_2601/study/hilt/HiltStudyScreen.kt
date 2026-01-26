package io.github.bokchidevchan.android_study_2601.study.hilt

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.bokchidevchan.android_study_2601.study.hilt.presentation.PurchaseUiState
import io.github.bokchidevchan.android_study_2601.study.hilt.presentation.PurchaseViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * ========================================================================
 * 📚 학습 목표: Dagger Hilt 이해하기
 * ========================================================================
 *
 * 🔑 핵심 개념:
 *
 * 1. 왜 쓰는가? (The Why)
 *    - 컴포넌트 생명주기 자동 관리
 *    - 컴파일 타임 안전성 (런타임 에러 방지)
 *    - 테스트 용이성 (Mock 교체 간편)
 *
 * 2. 어떻게 쓰는가? (The How)
 *    - @HiltAndroidApp: 앱 진입점
 *    - @AndroidEntryPoint: Activity/Fragment 주입 활성화
 *    - @HiltViewModel: ViewModel 주입
 *    - @Module + @Provides/@Binds: 의존성 제공
 *
 * 3. 주의사항 (Trade-offs)
 *    - 빌드 시간 증가 (KSP로 완화)
 *    - 의존성 그래프 불투명성
 *    - Scope 오용 시 메모리 누수
 *
 * 📁 파일 구조:
 *    hilt/
 *    ├── di/                    - DI 모듈
 *    │   └── RepositoryModule.kt
 *    ├── domain/                - 인터페이스
 *    │   └── UserRepository.kt
 *    ├── data/                  - 구현체
 *    │   └── ApiUserRepository.kt
 *    ├── presentation/          - ViewModel
 *    │   └── PurchaseViewModel.kt
 *    └── HiltStudyScreen.kt     - UI
 *
 * ========================================================================
 */

@Composable
fun HiltStudyScreen(
    modifier: Modifier = Modifier,
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HiltStudyContent(
        modifier = modifier,
        uiState = uiState,
        onLoadBalance = viewModel::loadBalance,
        onPurchase = viewModel::purchaseItem
    )
}

@Composable
private fun HiltStudyContent(
    modifier: Modifier = Modifier,
    uiState: PurchaseUiState,
    onLoadBalance: () -> Unit,
    onPurchase: (String, Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        Text(
            text = "Dagger Hilt",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "의존성 주입(DI)과 테스트 용이성",
            fontSize = 14.sp,
            color = Color.Gray
        )

        // 예제 1: 왜 쓰는가?
        WhyUseHiltSection()

        // 예제 2: 기본 사용법
        HowToUseHiltSection()

        // 예제 3: 실제 동작 테스트
        LiveDemoSection(
            uiState = uiState,
            onLoadBalance = onLoadBalance,
            onPurchase = onPurchase
        )

        // 예제 4: 테스트 코드
        TestingSection()

        // 예제 5: 주의사항
        CautionSection()
    }
}

// ========================================================================
// 섹션 1: 왜 쓰는가?
// ========================================================================

@Composable
private fun WhyUseHiltSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "왜 Hilt를 쓰는가?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 이유 1
            ReasonCard(
                number = "1",
                title = "생명주기 자동 관리",
                description = "Android 컴포넌트(Activity, Fragment)의 생명주기에 맞춰 의존성 자동 관리",
                color = Color(0xFFBBDEFB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 이유 2
            ReasonCard(
                number = "2",
                title = "컴파일 타임 안전성",
                description = "순환 참조나 누락된 의존성을 빌드 단계에서 감지 (런타임 Crash 방지)",
                color = Color(0xFFBBDEFB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 이유 3
            ReasonCard(
                number = "3",
                title = "테스트 용이성",
                description = "인터페이스 기반 설계로 Mock 객체 쉽게 교체 가능",
                color = Color(0xFFBBDEFB)
            )
        }
    }
}

@Composable
private fun ReasonCard(
    number: String,
    title: String,
    description: String,
    color: Color
) {
    Card(colors = CardDefaults.cardColors(containerColor = color)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = number,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

// ========================================================================
// 섹션 2: 어떻게 쓰는가?
// ========================================================================

@Composable
private fun HowToUseHiltSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "어떻게 쓰는가?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 단계 1
            Text(text = "1. Application 클래스", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            CodeBlock(
                code = """
@HiltAndroidApp
class MyApp : Application()
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 단계 2
            Text(text = "2. Module 정의 (@Binds vs @Provides)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            CodeBlock(
                code = """
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // @Binds: 인터페이스 ↔ 구현체 매핑 (권장)
    @Binds
    abstract fun bindUserRepository(
        impl: ApiUserRepository
    ): UserRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // @Provides: 외부 라이브러리/복잡한 생성
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com")
            .build()
    }
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 단계 3
            Text(text = "3. ViewModel에서 사용", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            CodeBlock(
                code = """
@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel()
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 단계 4
            Text(text = "4. Compose에서 획득", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            CodeBlock(
                code = """
@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    // viewModel 사용
}
                """.trimIndent()
            )
        }
    }
}

// ========================================================================
// 섹션 3: 실제 동작 데모
// ========================================================================

@Composable
private fun LiveDemoSection(
    uiState: PurchaseUiState,
    onLoadBalance: () -> Unit,
    onPurchase: (String, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실제 동작 테스트",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "Hilt가 주입한 Repository를 통해 동작",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 잔액 표시
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "현재 잔액", fontSize = 14.sp, color = Color.Gray)
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        val formattedBalance = NumberFormat.getNumberInstance(Locale.KOREA)
                            .format(uiState.balance)
                        Text(
                            text = "${formattedBalance}원",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoadBalance,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("잔액 조회")
                }

                Button(
                    onClick = { onPurchase("potion", 1000) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text("1,000원 아이템 구매")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onPurchase("sword", 3000) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("검 구매 (3,000원)")
                }

                Button(
                    onClick = { onPurchase("shield", 15000) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("방패 (15,000원)")
                }
            }

            // 결과 메시지
            uiState.purchaseResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.contains("성공")) Color(0xFFA5D6A7) else Color(0xFFFFCDD2)
                    )
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // 에러 메시지
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))) {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
        }
    }
}

// ========================================================================
// 섹션 4: 테스트 코드
// ========================================================================

@Composable
private fun TestingSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mock으로 테스트하기",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "실제 네트워크 없이 비즈니스 로직 테스트",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "MockK를 사용한 테스트", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            CodeBlock(
                code = """
@Test
fun `잔액이 부족할 때 구매 실패`() = runTest {
    // Given: Mock 설정
    val mockRepository = mockk<UserRepository>()
    coEvery { mockRepository.getBalance() } returns 100
    coEvery { mockRepository.purchaseItem(any(), any()) } returns
        Result.success(PurchaseResult.InsufficientBalance(500, 100))

    val viewModel = PurchaseViewModel(mockRepository)

    // When: 구매 시도
    viewModel.purchaseItem("sword", 500)

    // Then: 잔액 부족 메시지
    val state = viewModel.uiState.value
    assertTrue(state.purchaseResult?.contains("잔액 부족") == true)
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Fake Repository를 사용한 테스트", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            CodeBlock(
                code = """
@Test
fun `구매 성공 시 잔액 감소`() = runTest {
    // Given: Fake Repository (실제 동작하는 간단한 구현체)
    val fakeRepository = FakeUserRepository().apply {
        fakeBalance = 10000
    }
    val viewModel = PurchaseViewModel(fakeRepository)

    // When: 3000원 아이템 구매
    viewModel.purchaseItem("sword", 3000)

    // Then: 잔액이 7000원으로 감소
    assertEquals(7000, viewModel.uiState.value.balance)
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mock vs Stub vs Spy vs Fake
            Text(text = "테스트 대역(Test Double) 비교", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestDoubleCard(
                    modifier = Modifier.weight(1f),
                    title = "Stub",
                    description = "준비된 값만 반환",
                    example = "getBalance() returns 100",
                    color = Color(0xFFE1BEE7)
                )
                TestDoubleCard(
                    modifier = Modifier.weight(1f),
                    title = "Mock",
                    description = "호출 여부/횟수 검증",
                    example = "verify { save() }",
                    color = Color(0xFFCE93D8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestDoubleCard(
                    modifier = Modifier.weight(1f),
                    title = "Spy",
                    description = "실제 객체 + 일부 감시",
                    example = "spyk(realObj)",
                    color = Color(0xFFB39DDB)
                )
                TestDoubleCard(
                    modifier = Modifier.weight(1f),
                    title = "Fake",
                    description = "실제 동작하는 간단 구현",
                    example = "FakeRepository()",
                    color = Color(0xFF9575CD)
                )
            }
        }
    }
}

@Composable
private fun TestDoubleCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    example: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = description, fontSize = 10.sp)
            Text(
                text = example,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.DarkGray
            )
        }
    }
}

// ========================================================================
// 섹션 5: 주의사항
// ========================================================================

@Composable
private fun CautionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "주의사항 (Trade-offs)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            CautionItem(
                icon = "⏱️",
                title = "빌드 시간 증가",
                description = "Annotation Processing으로 인해 빌드 속도 영향. KSP 사용으로 완화 가능."
            )

            CautionItem(
                icon = "🔍",
                title = "의존성 그래프 불투명",
                description = "자동화로 인해 객체 주입 경로 파악이 어려울 수 있음. IDE 플러그인 활용 권장."
            )

            CautionItem(
                icon = "💾",
                title = "Scope 오용 시 메모리 누수",
                description = "SingletonComponent에 너무 많은 객체 할당 시 앱 종료까지 메모리 점유."
            )

            CautionItem(
                icon = "📚",
                title = "학습 비용",
                description = "커스텀 스코프, Multi-binding 등 고급 기능은 Dagger 로우레벨 지식 필요."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scope 가이드
            Text(text = "Scope 선택 가이드", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            CodeBlock(
                code = """
// 앱 전체에서 하나 (설정, 네트워크 클라이언트)
@Singleton @InstallIn(SingletonComponent::class)

// Activity 재생성에도 유지 (ViewModel)
@ActivityRetainedScoped @InstallIn(ActivityRetainedComponent::class)

// Activity 단위 (권한 관리)
@ActivityScoped @InstallIn(ActivityComponent::class)

// Fragment 단위 (화면별 상태)
@FragmentScoped @InstallIn(FragmentComponent::class)
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun CautionItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = description, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

// ========================================================================
// 공통 컴포넌트
// ========================================================================

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
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
// Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
private fun HiltStudyContentPreview() {
    MaterialTheme {
        HiltStudyContent(
            uiState = PurchaseUiState(balance = 10000),
            onLoadBalance = {},
            onPurchase = { _, _ -> }
        )
    }
}
