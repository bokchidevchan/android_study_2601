package io.github.bokchidevchan.android_study_2601.study.recomposition

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList

/**
 * ========================================================================
 * 📚 학습 목표: Stability & Recomposition 최적화 이해하기
 * ========================================================================
 *
 * ⚠️ 중요: Compose Compiler 2.0+ (Kotlin 2.0)에서는
 * Strong Skipping Mode가 기본 활성화되어 대부분의 최적화가 자동으로 이루어집니다.
 *
 * 따라서 이 화면에서는 "이론적 개념"을 설명하고,
 * 실제 Stability를 확인하는 방법(Compose Compiler Metrics)을 안내합니다.
 *
 * ========================================================================
 */

@Composable
fun StabilityRecompositionScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📚 Stability & Recomposition",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Compose 성능 최적화의 핵심 개념",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 핵심 개념
        ConceptCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 2. copy()와 객체 교체 메커니즘 실습
        CopyMechanismCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Stability 3단계
        StabilityLevelsCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 왜 List는 Unstable?
        ListProblemCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 해결책
        SolutionCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Compose Compiler Metrics
        MetricsCard()
    }
}

/**
 * copy()를 통한 객체 교체 확인용 데이터 클래스
 */
@Immutable
data class Developer(val name: String, val level: Int)

/**
 * copy()를 통한 객체 교체 메커니즘 실습 카드
 */
@Composable
fun CopyMechanismCard() {
    var developer by remember { mutableStateOf(Developer("복치", 1)) }
    var recomposeCount by remember { mutableIntStateOf(0) }
    recomposeCount++

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 실습: copy()는 객체를 교체합니다",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "이름: ${developer.name}", fontSize = 14.sp)
            Text(text = "레벨: ${developer.level}", fontSize = 14.sp)

            Text(
                text = "객체 주소(Hash): ${System.identityHashCode(developer)}",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(text = "현재 카드 리컴포지션 횟수: $recomposeCount", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { developer = developer.copy(level = developer.level + 1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("copy()로 레벨업", fontSize = 11.sp)
                }

                Button(
                    onClick = { developer = developer.copy() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("같은 값 copy()", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// 1. copy() 실행 시 새로운 주소 할당
// 2. Compose가 주소 변경 감지
// 3. Recomposition 수행!
developer = developer.copy(level = 2)
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔑 핵심 원리",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Compose는 파라미터가 '변하지 않았다'고 판단하면\n" +
                       "Recomposition을 스킵합니다.\n\n" +
                       "이 판단의 핵심이 바로 'Stability(안정성)'입니다.",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
@Composable
fun Parent() {
    var count by remember { mutableIntStateOf(0) }

    // count가 바뀌면 Parent는 Recomposition
    // 하지만 Child의 파라미터가 안 바뀌면?
    Child(name = "고정값")  // ← 스킵 가능!
}
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun StabilityLevelsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Stability 3단계",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Immutable
            StabilityItem(
                emoji = "🟢",
                title = "Immutable (불변)",
                description = "생성 후 절대 변하지 않음",
                examples = "String, Int, @Immutable data class",
                result = "→ Recomposition 무조건 스킵 가능",
                color = Color(0xFFC8E6C9)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stable
            StabilityItem(
                emoji = "🟡",
                title = "Stable (안정)",
                description = "변할 수 있지만, Compose에게 알려줌",
                examples = "MutableState<T>, @Stable class",
                result = "→ equals() 비교 후 스킵 가능",
                color = Color(0xFFFFF9C4)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unstable
            StabilityItem(
                emoji = "🔴",
                title = "Unstable (불안정)",
                description = "언제 변할지 모름",
                examples = "List, Map, Set, 일반 class",
                result = "→ 매번 Recomposition (성능 저하!)",
                color = Color(0xFFFFCDD2)
            )
        }
    }
}

@Composable
fun StabilityItem(
    emoji: String,
    title: String,
    description: String,
    examples: String,
    result: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "$emoji $title",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(text = description, fontSize = 12.sp)
            Text(
                text = "예: $examples",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
            Text(
                text = result,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ListProblemCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🤔 왜 List는 Unstable일까?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// List는 인터페이스일 뿐!
val items: List<String> = mutableListOf("A")

// 실제 구현체가 MutableList일 수 있음
(items as MutableList).add("B")  // 가능!

// Compose 입장:
// "이 List가 언제 바뀔지 몰라!"
// → Unstable로 판단
// → 매번 Recomposition
                    """.trimIndent(),
                    color = Color(0xFFFFCC80),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 List 타입만 봐서는 내부가 변할 수 있는지 알 수 없음!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SolutionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ 해결책",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 해결책 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "1️⃣ ImmutableList 사용",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF263238), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = """
// kotlinx-collections-immutable
val items: ImmutableList<String> =
    persistentListOf("A", "B")
                            """.trimIndent(),
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 해결책 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "2️⃣ @Immutable 어노테이션",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF263238), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = """
@Immutable
data class User(
    val id: Int,
    val name: String,
    val tags: ImmutableList<String>
)
                            """.trimIndent(),
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 해결책 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "3️⃣ @Stable 어노테이션",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF263238), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = """
@Stable
class Counter {
    var value by mutableIntStateOf(0)
        private set
    fun increment() { value++ }
}
                            """.trimIndent(),
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 실제 Stability 확인하는 방법",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Compose Compiler Metrics를 활성화하면\n" +
                       "각 클래스/함수의 Stability를 확인할 수 있습니다.",
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// build.gradle.kts
composeCompiler {
    reportsDestination =
        layout.buildDirectory.dir("compose_compiler")
    metricsDestination =
        layout.buildDirectory.dir("compose_compiler")
}

// 빌드 후 확인
// app/build/compose_compiler/
//   - app_debug-classes.txt  ← Stability 정보
//   - app_debug-composables.txt
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "📝 classes.txt 예시:",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
stable class User {
  stable val id: Int
  stable val name: String
  unstable val tags: List<String>  // ❌ 문제!
}
                    """.trimIndent(),
                    color = Color(0xFFFFCC80),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// 예시 데이터 클래스들 (설명용)
@Immutable
data class StableUser(
    val id: Int,
    val name: String,
    val tags: ImmutableList<String>
)

@Preview(showBackground = true)
@Composable
fun StabilityRecompositionScreenPreview() {
    MaterialTheme {
        StabilityRecompositionScreen()
    }
}
