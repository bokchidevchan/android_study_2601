package io.github.bokchidevchan.android_study_2601.study.immutable

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
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
import kotlinx.collections.immutable.toImmutableList

/**
 * ========================================================================
 * 📚 학습 목표: Strong Skipping Mode와 ImmutableList 이해하기
 * ========================================================================
 *
 * Strong Skipping Mode (Compose Compiler 2.0+, Kotlin 2.0):
 * - Unstable 인자라도 **인스턴스 동일성(===)**을 만족하면 스킵
 * - List<T>를 쓴다고 무조건 성능 저하가 발생하지 않음
 *
 * 핵심 질문: 그렇다면 ImmutableList는 언제 써야 할까?
 *
 * ========================================================================
 */

@Composable
fun StrongSkippingModeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📚 Strong Skipping Mode",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "ImmutableList vs List 언제 써야 할까?",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 개념 설명
        ConceptOverviewCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Case A: 인스턴스가 절대 변하지 않는 경우
        CaseACard()

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Case B: 데이터가 바뀔 때만 새 인스턴스
        CaseBCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Case C: 데이터는 같은데 인스턴스만 새로 생성
        CaseCCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 위험한 함정: MutableList 캐스팅
        DangerousTrapCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 6. 실무 가이드라인
        PracticalGuidelineCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 7. 면접 질문
        InterviewQuestionsCard()
    }
}

@Composable
fun ConceptOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔑 Strong Skipping Mode란?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Compose Compiler 2.0+ (Kotlin 2.0)부터 기본 활성화",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 기존 방식
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "❌ 기존 방식 (Strong Skipping OFF)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• List<T>는 Unstable → 무조건 Recomposition\n" +
                               "• ImmutableList 강제 사용 필요",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strong Skipping 방식
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "✅ Strong Skipping Mode (기본값)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Unstable이라도 === 동일하면 스킵!\n" +
                               "• List<T>도 주소가 같으면 OK",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// Strong Skipping의 비교 로직
if (oldList === newList) {
    // 주소가 같음 → 스킵!
    skip()
} else if (oldList == newList) {
    // equals() 비교 (ImmutableList만 가능)
    skip()
} else {
    recompose()
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

// ========================================================================
// Case A: 인스턴스가 절대 변하지 않는 경우
// ========================================================================

@Composable
fun CaseACard() {
    // 컴포지션 외부에서 한 번만 생성된 리스트 (절대 변하지 않음)
    val staticList = remember { listOf("Apple", "Banana", "Cherry") }
    var otherState by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📦 Case A: 인스턴스가 절대 변하지 않는 경우",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "결론: List 사용 (ImmutableList 불필요)",
                fontSize = 12.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Other State: $otherState", fontSize = 14.sp)

            // 리스트를 받는 자식 컴포저블
            CaseAChildList(items = staticList)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { otherState++ }) {
                Text("다른 상태 변경 (리스트는 그대로)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// ✅ 리스트 주소가 항상 동일 → 스킵됨
val staticList = remember { listOf(...) }

// otherState가 바뀌어도 staticList 주소는 그대로
// → CaseAChildList는 Recomposition 스킵!

// ❌ 굳이 toImmutableList() 할 필요 없음
// → O(N) 변환 비용만 낭비
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
fun CaseAChildList(items: List<String>) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    recomposeCount++

    SideEffect {
        Log.d("StrongSkipping", "CaseA Child Recomposed: $recomposeCount")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = "Child Recomposition: $recomposeCount",
                fontWeight = FontWeight.Bold,
                color = if (recomposeCount > 1) Color.Red else Color.Green
            )
            items.forEach { Text(text = "• $it", fontSize = 12.sp) }
        }
    }
}

// ========================================================================
// Case B: 데이터가 바뀔 때만 새 인스턴스 (건강한 상태 관리)
// ========================================================================

@Composable
fun CaseBCard() {
    var fruits by remember { mutableStateOf(listOf("Apple", "Banana")) }
    var listRecomposeCount by remember { mutableIntStateOf(0) }
    var immutableListRecomposeCount by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 Case B: 데이터가 바뀔 때만 새 인스턴스",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "결론: List 사용 (어차피 Recomposition 해야 함)",
                fontSize = 12.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // List 사용
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("List<String>", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        CaseBChildList(
                            items = fruits,
                            onRecompose = { listRecomposeCount++ }
                        )
                        Text(
                            "Recompose: $listRecomposeCount",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }
                }

                // ImmutableList 사용
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("ImmutableList", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        CaseBChildImmutableList(
                            items = fruits.toImmutableList(),
                            onRecompose = { immutableListRecomposeCount++ }
                        )
                        Text(
                            "Recompose: $immutableListRecomposeCount",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { fruits = fruits + "Fruit${fruits.size + 1}" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("과일 추가 (데이터 변경)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// 데이터가 바뀌면 어차피 Recomposition 필요!

// List: 변환 비용 없음, 즉시 Recomposition
// ImmutableList:
//   1. toImmutableList() 변환 O(N)
//   2. equals() 비교 O(N) (실패)
//   3. 그제서야 Recomposition

// → 순수 오버헤드만 증가!
                    """.trimIndent(),
                    color = Color(0xFFFFCC80),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun CaseBChildList(items: List<String>, onRecompose: () -> Unit) {
    SideEffect { onRecompose() }
    Text(text = "${items.size}개 아이템", fontSize = 12.sp)
}

@Composable
fun CaseBChildImmutableList(items: ImmutableList<String>, onRecompose: () -> Unit) {
    SideEffect { onRecompose() }
    Text(text = "${items.size}개 아이템", fontSize = 12.sp)
}

// ========================================================================
// Case C: 데이터는 같은데 인스턴스만 새로 생성
// ========================================================================

@Composable
fun CaseCCard() {
    var trigger by remember { mutableIntStateOf(0) }

    // 매번 새 인스턴스지만 내용은 동일!
    val fruits = listOf("Apple", "Banana", "Cherry")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ Case C: 데이터는 같은데 인스턴스만 새로 생성",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "결론: ImmutableList가 유리할 수 있음 (하지만 주의!)",
                fontSize = 12.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Trigger: $trigger", fontSize = 14.sp)
            Text(
                text = "리스트 주소: ${System.identityHashCode(fruits)}",
                fontSize = 12.sp,
                color = Color.Magenta
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaseCChildList(items = fruits, label = "List")
                CaseCChildImmutableList(
                    items = fruits.toImmutableList(),
                    label = "ImmutableList"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { trigger++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trigger 증가 (리스트 내용은 그대로)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// ⚠️ 매번 새 인스턴스 생성 (remember 없이)
val fruits = listOf("A", "B", "C")

// List: 주소 다름 → 매번 Recomposition
// ImmutableList: equals()로 스킵 가능

// 하지만! O(N) 비교 비용 발생
// 리스트가 크면 비교 시간 > 렌더링 시간

// 💡 더 나은 해결책:
// remember { listOf(...) } 사용!
                    """.trimIndent(),
                    color = Color(0xFFCE93D8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun CaseCChildList(items: List<String>, label: String) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    recomposeCount++

    Box(
        modifier = Modifier
            .background(Color(0xFFE1BEE7), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(
                text = "Recompose: $recomposeCount",
                fontSize = 11.sp,
                color = if (recomposeCount > 1) Color.Red else Color.Green
            )
        }
    }
}

@Composable
fun CaseCChildImmutableList(items: ImmutableList<String>, label: String) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    recomposeCount++

    Box(
        modifier = Modifier
            .background(Color(0xFFE1BEE7), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(
                text = "Recompose: $recomposeCount",
                fontSize = 11.sp,
                color = if (recomposeCount > 1) Color.Red else Color.Green
            )
        }
    }
}

// ========================================================================
// 위험한 함정: MutableList 캐스팅
// ========================================================================

@Composable
fun DangerousTrapCard() {
    // ⚠️ 위험! MutableList를 List로 캐스팅
    val mutableList = remember { mutableListOf("초기값") }
    var uiTrigger by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚨 위험한 함정: MutableList 캐스팅",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Red
            )

            Text(
                text = "Strong Skipping을 맹신하면 발생하는 버그!",
                fontSize = 12.sp,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 현재 상태 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "실제 리스트 내용:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    mutableList.forEachIndexed { index, item ->
                        Text(text = "${index + 1}. $item", fontSize = 12.sp)
                    }
                    Text(
                        text = "리스트 주소: ${System.identityHashCode(mutableList)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 자식 컴포저블 (List로 받음)
            DangerousChildList(items = mutableList)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // ⚠️ 원본을 직접 수정! (주소는 그대로)
                        mutableList.add("추가됨 ${mutableList.size}")
                        Log.d("StrongSkipping", "리스트 수정됨: $mutableList")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("원본 수정 (버그!)", fontSize = 11.sp)
                }

                Button(
                    onClick = { uiTrigger++ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("UI 갱신", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// ❌ 절대 하면 안 되는 패턴!
val mutableList = mutableListOf("A")

// 원본 수정 (주소는 그대로!)
mutableList.add("B")

// Strong Skipping:
// "주소가 같네? 변경 없음!" → 스킵
// → UI에 "B"가 안 보임! (상태 불일치)

// ✅ 해결책: 항상 새 리스트 생성
items = items + "B"  // 새 인스턴스!
                    """.trimIndent(),
                    color = Color(0xFFFF8A80),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 '원본 수정' 버튼을 눌러도 Child가 업데이트 안 됨!\n" +
                       "'UI 갱신' 버튼을 눌러야 비로소 보임 (위험한 버그)",
                fontSize = 12.sp,
                color = Color.Red,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DangerousChildList(items: List<String>) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    recomposeCount++

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEF9A9A), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = "Child가 보는 리스트 (Recompose: $recomposeCount)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            items.forEachIndexed { index, item ->
                Text(text = "${index + 1}. $item", fontSize = 12.sp)
            }
        }
    }
}

// ========================================================================
// 실무 가이드라인
// ========================================================================

@Composable
fun PracticalGuidelineCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ 실무 가이드라인",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 가이드라인 1
            GuidelineItem(
                number = "1",
                title = "기본은 List<T> 사용",
                description = "Strong Skipping Mode가 기본이므로, 대부분의 경우 List로 충분"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 가이드라인 2
            GuidelineItem(
                number = "2",
                title = "remember로 인스턴스 고정",
                description = "매번 새 인스턴스 생성 대신 remember { listOf(...) } 사용"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 가이드라인 3
            GuidelineItem(
                number = "3",
                title = "불변성 원칙 준수",
                description = "MutableList를 List로 캐스팅하지 말 것. 항상 새 리스트 생성"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 가이드라인 4
            GuidelineItem(
                number = "4",
                title = "측정 중심 최적화",
                description = "프로파일러로 Hot Spot을 찾고, 그곳에만 ImmutableList 적용"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 가이드라인 5
            GuidelineItem(
                number = "5",
                title = "대용량 리스트는 LazyColumn + key",
                description = "10,000개 아이템은 ImmutableList의 O(N) 비교보다 LazyColumn이 효율적"
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
// ✅ 권장 패턴
@Composable
fun Screen(viewModel: ViewModel) {
    // StateFlow에서 직접 수집 (인스턴스 재사용)
    val items by viewModel.items.collectAsState()

    // items가 바뀔 때만 새 인스턴스
    // → Strong Skipping으로 자연스럽게 최적화
    ItemList(items = items)
}

// ✅ 불변 업데이트
fun addItem(item: Item) {
    _items.value = _items.value + item  // 새 리스트!
}
                    """.trimIndent(),
                    color = Color(0xFF80CBC4),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun GuidelineItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = number,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF2E7D32),
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = description, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

// ========================================================================
// 면접 질문
// ========================================================================

@Composable
fun InterviewQuestionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧐 면접 질문",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            InterviewQuestion(
                question = "Q1. Strong Skipping이 활성화된 상태에서, List<T>의 요소(T)가 Unstable한 객체라면?",
                answer = "리스트 자체의 주소가 같더라도, 내부 요소가 변할 수 있는 가변 객체라면 " +
                        "Compose는 보수적으로 동작할 수 있습니다. " +
                        "리스트의 안정성은 내용물의 안정성에도 전이됩니다."
            )

            Spacer(modifier = Modifier.height(8.dp))

            InterviewQuestion(
                question = "Q2. ImmutableList의 equals()는 O(N)입니다. 10,000개 리스트를 여러 단계로 전달하면?",
                answer = "각 단계마다 equals()를 호출하여, '바뀌었는지 체크하는 시간'이 " +
                        "'실제 렌더링 시간'보다 길어지는 역설적 상황이 발생합니다. " +
                        "이럴 때는 derivedStateOf나 페이징 처리를 고려해야 합니다."
            )

            Spacer(modifier = Modifier.height(8.dp))

            InterviewQuestion(
                question = "Q3. Strong Skipping을 전적으로 신뢰하고 List<T>만 사용했을 때 가장 위험한 부작용은?",
                answer = "MutableList를 List로 캐스팅하고 원본을 수정하는 경우입니다. " +
                        "주소(===)가 동일하므로 화면이 갱신되지 않는 '상태 불일치' 버그가 발생합니다. " +
                        "불변성은 성능 최적화 이전에 '예측 가능한 상태'를 만드는 아키텍처 원칙입니다."
            )
        }
    }
}

@Composable
fun InterviewQuestion(question: String, answer: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFE082), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = question,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A: $answer",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StrongSkippingModeScreenPreview() {
    MaterialTheme {
        StrongSkippingModeScreen()
    }
}
