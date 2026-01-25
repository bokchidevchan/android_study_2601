package io.github.bokchidevchan.android_study_2601.study.state

import android.os.Parcelable
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.parcelize.Parcelize

/**
 * ========================================================================
 * 📚 학습 목표: remember vs rememberSaveable 차이 이해하기
 * ========================================================================
 *
 * 🔑 핵심 개념:
 *
 * 1. remember
 *    - Composition 내부에 값을 저장
 *    - Recomposition 시에는 값 유지 ✅
 *    - Configuration Change(화면 회전) 시 값 소멸 ❌
 *    - 프로세스 종료 시 값 소멸 ❌
 *
 * 2. rememberSaveable
 *    - Bundle을 이용해 상태 저장
 *    - Recomposition 시에는 값 유지 ✅
 *    - Configuration Change(화면 회전) 시 값 유지 ✅
 *    - 프로세스 종료 후 복원 시 값 유지 ✅
 *
 * ⚠️ Trade-off:
 *    - rememberSaveable은 직렬화 비용 발생
 *    - Bundle 크기 제한 (약 1MB)
 *    - 모든 상태를 rememberSaveable에 넣으면 성능 저하
 *
 * 🧪 실험 방법:
 *    1. 각 카운터를 증가시킨다
 *    2. 화면을 회전시킨다 (에뮬레이터: Ctrl+← / Ctrl+→)
 *    3. remember 카운터는 0으로 초기화됨
 *    4. rememberSaveable 카운터는 값이 유지됨
 *
 * ========================================================================
 */

// ========================================================================
// 📦 복잡한 객체를 rememberSaveable에 저장하려면 Parcelable 구현 필요
// ========================================================================

/**
 * @Parcelize를 사용하면 Parcelable을 자동 구현해줌
 * rememberSaveable에서 복잡한 객체를 저장할 때 필요
 *
 * ⚠️ 주의: 큰 객체를 Parcelize하면 직렬화/역직렬화 비용 발생!
 */
@Parcelize
data class UserInput(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

/**
 * ❌ Bad Example: 큰 데이터를 rememberSaveable에 저장
 * 이런 식으로 하면 메모리 부담과 직렬화 비용 발생!
 */
@Parcelize
data class HeavyData(
    val items: List<String>, // 큰 리스트를 저장하면 Bundle 크기 초과 위험
    val metadata: Map<String, String> // 복잡한 구조는 직렬화 비용 증가
) : Parcelable

// ========================================================================
// 🎨 메인 화면
// ========================================================================

@Composable
fun RememberVsSaveableScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더
        Text(
            text = "🧪 remember vs rememberSaveable",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "화면을 회전시켜서 차이를 확인하세요!\n(에뮬레이터: Ctrl+← / Ctrl+→)",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 예제 1: 기본 비교
        BasicComparisonExample()

        Spacer(modifier = Modifier.height(16.dp))

        // 예제 2: 복잡한 객체 저장
        ComplexObjectExample()

        Spacer(modifier = Modifier.height(16.dp))

        // 예제 3: Trade-off 설명
        TradeOffExplanation()
    }
}

// ========================================================================
// 🔬 예제 1: 기본 비교 - remember vs rememberSaveable
// ========================================================================

@Composable
fun BasicComparisonExample() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 예제 1: 기본 카운터 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // remember 카운터 (화면 회전 시 초기화됨)
                RememberCounter(
                    modifier = Modifier.weight(1f)
                )

                // rememberSaveable 카운터 (화면 회전 후에도 유지됨)
                RememberSaveableCounter(
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * ❌ remember 사용 - Configuration Change 시 값 소멸
 *
 * 적합한 사용 케이스:
 * - 애니메이션 상태
 * - 스크롤 위치 (일부 경우)
 * - 임시 UI 상태 (토글, 호버 등)
 */
@Composable
fun RememberCounter(modifier: Modifier = Modifier) {
    // 🔴 remember: Composition 내부에만 저장
    // 화면 회전 시 Composition이 재생성되어 값이 초기화됨
    var count by remember { mutableIntStateOf(0) }

    // 로그로 상태 변화 확인
    Log.d("StateStudy", "RememberCounter recomposed, count = $count")

    CounterCard(
        title = "remember",
        subtitle = "화면 회전 시 초기화 ❌",
        count = count,
        onIncrement = { count++ },
        onReset = { count = 0 },
        backgroundColor = Color(0xFFFFCDD2), // 빨간색 계열
        modifier = modifier
    )
}

/**
 * ✅ rememberSaveable 사용 - Configuration Change 후에도 값 유지
 *
 * 적합한 사용 케이스:
 * - 사용자 입력 (텍스트 필드)
 * - 선택 상태 (체크박스, 라디오 버튼)
 * - 페이지 번호, 탭 인덱스
 * - 폼 데이터
 */
@Composable
fun RememberSaveableCounter(modifier: Modifier = Modifier) {
    // 🟢 rememberSaveable: Bundle에 저장되어 복원 가능
    // 화면 회전, 프로세스 종료 후에도 값이 유지됨
    var count by rememberSaveable { mutableIntStateOf(0) }

    // 로그로 상태 변화 확인
    Log.d("StateStudy", "RememberSaveableCounter recomposed, count = $count")

    CounterCard(
        title = "rememberSaveable",
        subtitle = "화면 회전 후 유지 ✅",
        count = count,
        onIncrement = { count++ },
        onReset = { count = 0 },
        backgroundColor = Color(0xFFC8E6C9), // 초록색 계열
        modifier = modifier
    )
}

@Composable
fun CounterCard(
    title: String,
    subtitle: String,
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$count",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onIncrement,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+1")
            }

            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("리셋")
            }
        }
    }
}

// ========================================================================
// 🔬 예제 2: 복잡한 객체 저장 (Parcelize 필요)
// ========================================================================

@Composable
fun ComplexObjectExample() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📦 예제 2: 복잡한 객체 저장",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Parcelable 객체도 rememberSaveable로 저장 가능",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ✅ Parcelable 객체를 rememberSaveable에 저장
            var userInput by rememberSaveable {
                mutableStateOf(UserInput(text = "초기값"))
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "저장된 UserInput 객체:",
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "• text: ${userInput.text}")
                    Text(text = "• timestamp: ${userInput.timestamp}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            userInput = UserInput(
                                text = "업데이트됨 #${(1..100).random()}"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("객체 업데이트")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 코드 설명
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
                    text = """
@Parcelize
data class UserInput(
    val text: String,
    val timestamp: Long
) : Parcelable

// 사용
var userInput by rememberSaveable {
    mutableStateOf(UserInput("초기값"))
}
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

// ========================================================================
// 📖 예제 3: Trade-off 설명
// ========================================================================

@Composable
fun TradeOffExplanation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ Trade-off: 언제 무엇을 사용할까?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // remember 사용 케이스
            TradeOffItem(
                emoji = "🔴",
                title = "remember 사용",
                items = listOf(
                    "애니메이션 상태",
                    "임시 UI 상태 (호버, 포커스)",
                    "파생 상태 (계산된 값)",
                    "휘발되어도 되는 데이터"
                ),
                backgroundColor = Color(0xFFFFCDD2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // rememberSaveable 사용 케이스
            TradeOffItem(
                emoji = "🟢",
                title = "rememberSaveable 사용",
                items = listOf(
                    "사용자 입력 텍스트",
                    "선택 상태 (체크박스, 탭)",
                    "스크롤 위치",
                    "폼 데이터"
                ),
                backgroundColor = Color(0xFFC8E6C9)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 주의사항
            TradeOffItem(
                emoji = "⚠️",
                title = "rememberSaveable 주의사항",
                items = listOf(
                    "Bundle 크기 제한 (~1MB)",
                    "직렬화/역직렬화 비용 발생",
                    "큰 리스트, 이미지 등은 저장 ❌",
                    "→ 대신 ViewModel + Repository 사용"
                ),
                backgroundColor = Color(0xFFFFF9C4)
            )
        }
    }
}

@Composable
fun TradeOffItem(
    emoji: String,
    title: String,
    items: List<String>,
    backgroundColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "$emoji $title",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// ========================================================================
// 🎨 Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
fun RememberVsSaveableScreenPreview() {
    MaterialTheme {
        RememberVsSaveableScreen()
    }
}
