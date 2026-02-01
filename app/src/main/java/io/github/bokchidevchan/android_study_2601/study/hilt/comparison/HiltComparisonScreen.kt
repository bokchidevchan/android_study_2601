package io.github.bokchidevchan.android_study_2601.study.hilt.comparison

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HiltComparisonScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hilt 사용 전/후 비교",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "같은 기능을 두 가지 방식으로 구현한 코드 비교",
            fontSize = 14.sp,
            color = Color.Gray
        )

        OverviewSection()
        DependencyCreationComparison()
        ViewModelComparison()
        TestingComparison()
        SummaryTable()
        ConclusionSection()
    }
}

@Composable
private fun OverviewSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 학습 목표",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val goals = listOf(
                "의존성 주입(DI)이 왜 필요한지 이해",
                "Hilt가 어떤 보일러플레이트를 제거하는지 확인",
                "테스트가 어떻게 더 쉬워지는지 비교"
            )

            goals.forEachIndexed { index, goal ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "${index + 1}.",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.width(24.dp)
                    )
                    Text(text = goal, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DependencyCreationComparison() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1️⃣ 의존성 생성 방식 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonCodeCard(
                    title = "❌ Hilt 없이",
                    titleColor = Color(0xFFD32F2F),
                    code = """
object ManualContainer {
    private var _logger: Logger? = null
    private var _repository: UserRepository? = null
    
    val logger: Logger
        get() {
            if (_logger == null) {
                _logger = AndroidLogger()
            }
            return _logger!!
        }
    
    val repository: UserRepository
        get() {
            if (_repository == null) {
                // ⚠️ 순서 중요! logger 먼저!
                _repository = ApiRepository(logger)
            }
            return _repository!!
        }
    
    // 테스트용 교체 메서드 필요
    fun setLogger(mock: Logger) {
        _logger = mock
    }
    
    fun reset() {
        _logger = null
        _repository = null
    }
}
                    """.trimIndent(),
                    problems = listOf(
                        "싱글톤 관리 직접 구현",
                        "의존성 순서 신경써야 함",
                        "테스트용 setter 필요",
                        "스레드 안전성 직접 처리"
                    )
                )

                ComparisonCodeCard(
                    title = "✅ Hilt 사용",
                    titleColor = Color(0xFF388E3C),
                    code = """
@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return AndroidLogger()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRepository(
        impl: ApiRepository
    ): UserRepository
}

// 구현체
class ApiRepository @Inject constructor(
    private val logger: Logger  // 자동 주입!
) : UserRepository
                    """.trimIndent(),
                    benefits = listOf(
                        "@Singleton으로 선언적 관리",
                        "의존성 순서 자동 해결",
                        "@TestInstallIn으로 쉬운 교체",
                        "스레드 안전성 보장"
                    )
                )
            }
        }
    }
}

@Composable
private fun ViewModelComparison() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2️⃣ ViewModel 생성 방식 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonCodeCard(
                    title = "❌ Hilt 없이",
                    titleColor = Color(0xFFD32F2F),
                    code = """
// ViewModel
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() { ... }

// Factory 직접 구현 필요!
class UserViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        return UserViewModel(
            ManualContainer.repository
        ) as T
    }
}

// Activity에서 사용
class MyActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory()  // Factory 전달!
    }
}
                    """.trimIndent(),
                    problems = listOf(
                        "ViewModel마다 Factory 필요",
                        "의존성 추가 시 Factory도 수정",
                        "보일러플레이트 코드 많음"
                    )
                )

                ComparisonCodeCard(
                    title = "✅ Hilt 사용",
                    titleColor = Color(0xFF388E3C),
                    code = """
// ViewModel - 이게 끝!
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() { ... }


// Activity에서 사용
@AndroidEntryPoint
class MyActivity : ComponentActivity() {
    // Factory 필요 없음!
}

// Compose에서 사용
@Composable
fun MyScreen(
    viewModel: UserViewModel = hiltViewModel()
) {
    // 바로 사용!
}
                    """.trimIndent(),
                    benefits = listOf(
                        "Factory 자동 생성",
                        "의존성 추가해도 변경 없음",
                        "코드 간결함"
                    )
                )
            }
        }
    }
}

@Composable
private fun TestingComparison() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3️⃣ 테스트 시 Mock 교체 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonCodeCard(
                    title = "❌ Hilt 없이",
                    titleColor = Color(0xFFD32F2F),
                    code = """
class UserViewModelTest {
    
    @Before
    fun setUp() {
        // 전역 상태 변경
        ManualContainer.setLogger(mockk())
        ManualContainer.setRepository(mockk())
    }
    
    @After
    fun tearDown() {
        // 반드시 리셋!
        // 안 하면 다른 테스트에 영향
        ManualContainer.reset()
    }
    
    @Test
    fun testSomething() {
        val viewModel = UserViewModel(
            ManualContainer.repository
        )
        // ...
    }
}
                    """.trimIndent(),
                    problems = listOf(
                        "전역 상태 변경 필요",
                        "테스트 간 간섭 위험",
                        "매번 reset() 호출 필요"
                    )
                )

                ComparisonCodeCard(
                    title = "✅ Hilt 사용",
                    titleColor = Color(0xFF388E3C),
                    code = """
// 테스트용 모듈
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
@Module
abstract class FakeRepositoryModule {
    @Binds
    abstract fun bindRepo(
        fake: FakeUserRepository
    ): UserRepository
}

// 테스트
@HiltAndroidTest
class UserViewModelTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Test
    fun testSomething() {
        // FakeRepository가 자동 주입됨!
        // 전역 상태 변경 없음
    }
}
                    """.trimIndent(),
                    benefits = listOf(
                        "선언적으로 교체",
                        "테스트 격리 보장",
                        "클린업 자동 처리"
                    )
                )
            }
        }
    }
}

@Composable
private fun ComparisonCodeCard(
    title: String,
    titleColor: Color,
    code: String,
    problems: List<String> = emptyList(),
    benefits: List<String> = emptyList()
) {
    Card(
        modifier = Modifier.width(320.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFF80CBC4),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 12.sp
                )
            }

            if (problems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                problems.forEach { problem ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "⚠️",
                            fontSize = 10.sp,
                            modifier = Modifier.width(16.dp)
                        )
                        Text(
                            text = problem,
                            fontSize = 10.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            if (benefits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                benefits.forEach { benefit ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "✅",
                            fontSize = 10.sp,
                            modifier = Modifier.width(16.dp)
                        )
                        Text(
                            text = benefit,
                            fontSize = 10.sp,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 한눈에 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val comparisons = listOf(
                Triple("의존성 생성", "직접 new/object", "@Inject constructor"),
                Triple("싱글톤 관리", "if-null 체크", "@Singleton"),
                Triple("Factory", "직접 구현", "자동 생성"),
                Triple("의존성 그래프", "순서 관리", "자동 생성"),
                Triple("에러 발견", "런타임 크래시", "컴파일 타임"),
                Triple("테스트 교체", "전역 상태 변경", "@TestInstallIn"),
                Triple("생명주기", "직접 관리", "자동 관리"),
                Triple("빌드 시간", "빠름", "약간 느림"),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "항목",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "수동 DI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Hilt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF388E3C)
                    )
                }

                comparisons.forEach { (item, manual, hilt) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = item,
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = manual,
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                        Text(
                            text = hilt,
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConclusionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 결론: 언제 무엇을 쓸까?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC5CAE9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "수동 DI가 적합한 경우",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 작은 프로젝트 / 프로토타입\n• 빠른 빌드가 중요할 때\n• 의존성이 적을 때 (5개 미만)",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Hilt가 적합한 경우",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 중/대규모 프로젝트\n• 테스트가 중요한 프로젝트\n• 팀 프로젝트 (일관성 필요)\n• 장기 유지보수 예정",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 Tip: 대부분의 실무 Android 프로젝트에서는 Hilt 사용을 권장합니다. 초기 학습 비용이 있지만, 장기적으로 코드 품질과 테스트 용이성에서 큰 이점을 얻을 수 있습니다.",
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HiltComparisonScreenPreview() {
    MaterialTheme {
        HiltComparisonScreen()
    }
}
