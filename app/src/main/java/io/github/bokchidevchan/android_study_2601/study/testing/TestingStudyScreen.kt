package io.github.bokchidevchan.android_study_2601.study.testing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 Android Testing 학습 - 메인 화면
 * ========================================================================
 *
 * 테스트 학습 내용:
 * 1. Unit Test 기초 (JUnit, Assertion, Lifecycle)
 * 2. MockK 심화 (relaxed, capture, slot, spy)
 * 3. Coroutine Test (runTest, TestDispatcher, Turbine)
 * 4. Compose UI Test (Semantics Tree, Finder/Assertion/Action)
 * 5. TDD 실습 (Red-Green-Refactor)
 *
 * 테스트 피라미드:
 * - 70% Unit Test: 빠르고 안정적
 * - 20% Integration Test: 모듈 간 통합
 * - 10% E2E/UI Test: 실제 사용자 시나리오
 */
@Composable
fun TestingStudyScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        item {
            Text(
                text = "Android Testing 학습",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "테스트 코드 작성법과 모범 사례를 학습합니다",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 테스트 피라미드 섹션
        item {
            TestPyramidCard()
        }

        // 학습 카테고리
        items(testingCategories) { category ->
            TestingCategoryCard(category)
        }

        // 테스트 철학 섹션
        item {
            TestingPhilosophyCard()
        }
    }
}

@Composable
private fun TestPyramidCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔺 테스트 피라미드",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            PyramidRow("E2E/UI", "10%", Color(0xFFE57373))
            PyramidRow("Integration", "20%", Color(0xFFFFB74D))
            PyramidRow("Unit", "70%", Color(0xFF81C784))

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Unit Test가 가장 많고, 빠르며, 안정적입니다.\n" +
                        "위로 갈수록 느리고 불안정하지만 실제 사용자 경험에 가깝습니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PyramidRow(name: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "  ",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Text(text = name)
        }
        Text(text = percentage, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TestingCategoryCard(category: TestingCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = category.emoji + " " + category.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.description,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "주요 내용:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            category.topics.forEach { topic ->
                Text(
                    text = "• $topic",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📁 ${category.testPath}",
                fontSize = 11.sp,
                color = Color(0xFF1565C0)
            )
        }
    }
}

@Composable
private fun TestingPhilosophyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💡 언제 테스트를 작성해야 할까?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val criteria = listOf(
                "변경 비용이 높은 코드" to "결제, 인증 등 핵심 비즈니스 로직",
                "복잡도가 높은 코드" to "조건 분기가 많거나 상태가 복잡한 경우",
                "문서화가 필요한 코드" to "테스트가 살아있는 명세서 역할",
                "리팩토링 예정인 코드" to "테스트가 안전망 역할"
            )

            criteria.forEach { (title, desc) ->
                Text(
                    text = "✓ $title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "   $desc",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
            ) {
                Text(
                    text = "\"모든 코드에 테스트를 작성하는 것은 비효율적입니다.\n" +
                            "ROI를 고려하여 핵심 로직에 집중하세요.\"",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// ========================================================================
// Data Models
// ========================================================================

data class TestingCategory(
    val emoji: String,
    val title: String,
    val description: String,
    val topics: List<String>,
    val testPath: String
)

private val testingCategories = listOf(
    TestingCategory(
        emoji = "1️⃣",
        title = "Unit Test 기초",
        description = "JUnit 4 기반의 단위 테스트 작성법을 학습합니다.",
        topics = listOf(
            "@Test, @Before, @After, @BeforeClass, @AfterClass",
            "assertEquals, assertTrue, assertThrows",
            "테스트 라이프사이클과 인스턴스 격리"
        ),
        testPath = "app/src/test/.../testing/basics/"
    ),
    TestingCategory(
        emoji = "2️⃣",
        title = "MockK 심화",
        description = "Kotlin MockK 라이브러리를 활용한 Mocking 기법입니다.",
        topics = listOf(
            "every, verify, coEvery, coVerify",
            "relaxed mock - 기본값 자동 반환",
            "slot, capture - 인자 캡처",
            "spyk - 부분 Mocking"
        ),
        testPath = "app/src/test/.../testing/mockk/"
    ),
    TestingCategory(
        emoji = "3️⃣",
        title = "Coroutine Test",
        description = "비동기 코드 테스트와 Flow 검증 방법입니다.",
        topics = listOf(
            "runTest - Virtual Time 기반 테스트",
            "StandardTestDispatcher vs UnconfinedTestDispatcher",
            "advanceTimeBy, advanceUntilIdle",
            "Turbine - Flow 테스트 라이브러리"
        ),
        testPath = "app/src/test/.../testing/coroutine/"
    ),
    TestingCategory(
        emoji = "4️⃣",
        title = "Compose UI Test",
        description = "Semantics Tree 기반의 Compose UI 테스트입니다.",
        topics = listOf(
            "Finder: onNodeWithText, onNodeWithTag",
            "Assertion: assertIsDisplayed, assertIsEnabled",
            "Action: performClick, performTextInput",
            "Stateless Composable 분리 (Humble Object Pattern)"
        ),
        testPath = "app/src/androidTest/.../testing/compose/"
    ),
    TestingCategory(
        emoji = "5️⃣",
        title = "TDD 실습",
        description = "Test-Driven Development 사이클을 실습합니다.",
        topics = listOf(
            "Red: 실패하는 테스트 작성",
            "Green: 테스트 통과하는 최소 코드",
            "Refactor: 코드 정리",
            "EmailValidator, PasswordValidator 예제"
        ),
        testPath = "app/src/test/.../testing/tdd/"
    )
)

// ========================================================================
// Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
private fun TestingStudyScreenPreview() {
    MaterialTheme {
        TestingStudyScreen()
    }
}

/**
 * ========================================================================
 * 📚 테스트 학습 가이드
 * ========================================================================
 *
 * 🏃 테스트 실행 방법:
 *
 * 1. Unit Test (JVM)
 *    ./gradlew test
 *    또는 Android Studio에서 test 폴더 우클릭 → Run Tests
 *
 * 2. Instrumented Test (Android Device)
 *    ./gradlew connectedAndroidTest
 *    또는 androidTest 폴더 우클릭 → Run Tests
 *
 * 3. 특정 테스트만 실행
 *    ./gradlew test --tests "*.CalculatorTest"
 *
 * 📁 프로젝트 구조:
 *
 * app/
 * ├── src/
 * │   ├── main/java/.../study/testing/
 * │   │   ├── basics/          # Calculator.kt
 * │   │   ├── mockk/           # UserService.kt
 * │   │   ├── coroutine/       # AsyncRepository.kt
 * │   │   ├── compose/         # CounterScreen.kt
 * │   │   └── tdd/             # EmailValidator.kt
 * │   │
 * │   ├── test/java/.../study/testing/      # Unit Tests
 * │   │   ├── basics/          # CalculatorTest.kt
 * │   │   ├── mockk/           # MockKAdvancedTest.kt
 * │   │   ├── coroutine/       # CoroutineTestExamples.kt
 * │   │   └── tdd/             # EmailValidatorTest.kt
 * │   │
 * │   └── androidTest/java/.../study/testing/  # Instrumented Tests
 * │       └── compose/         # ComposeUITest.kt
 *
 * 📖 학습 순서:
 *
 * 1. basics/ - JUnit 기초부터 시작
 * 2. mockk/ - 의존성 Mocking 학습
 * 3. coroutine/ - 비동기 테스트
 * 4. compose/ - UI 테스트
 * 5. tdd/ - TDD 실습
 *
 * 각 파일의 주석을 읽으며 학습하세요!
 */
