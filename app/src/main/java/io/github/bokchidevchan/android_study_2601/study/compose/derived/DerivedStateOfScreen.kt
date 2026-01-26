package io.github.bokchidevchan.android_study_2601.study.compose.derived

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.launch

/**
 * ========================================================================
 * 📚 학습 목표: derivedStateOf vs remember(key) 완벽 이해하기
 * ========================================================================
 *
 * 실무에서 derivedStateOf를 쓰는 상황:
 * "입력 데이터는 초당 수십 번 변하는데,
 *  내 UI는 특정 조건에서만 한두 번 바뀌면 될 때"
 *
 * derivedStateOf는 내부적으로 "버퍼링" 역할을 합니다:
 * 1. Source State가 변함
 * 2. derivedStateOf 내부 계산 수행
 * 3. 결과값이 이전과 다른지 비교
 * 4. 다를 때만 리컴포지션 트리거!
 *
 * ========================================================================
 */

@Composable
fun DerivedStateOfScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📚 derivedStateOf vs remember(key)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "상태 변화 빈도를 줄이는 최적화 기법",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 개념 비교
        ConceptComparisonCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 스크롤 위치에 따른 UI 제어 (가장 흔한 케이스)
        ScrollPositionExampleCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 검색어 필터링
        SearchFilterExampleCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 폼 유효성 검사
        FormValidationExampleCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. derivedStateOf vs remember(key) 비교 실습
        ComparisonExampleCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 6. 트레이드오프
        TradeOffCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 7. 면접 질문
        InterviewQuestionsCard()
    }
}

@Composable
fun ConceptComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔑 핵심 차이점",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // derivedStateOf
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "✅ derivedStateOf",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• 계산 결과가 바뀔 때만 Recomposition",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 입력은 자주 변하지만 출력은 드물게 변할 때",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 예: 스크롤 위치 → 버튼 노출 여부",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // remember(key)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "📝 remember(key)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• key가 바뀌면 무조건 Recomposition",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 단순 연산 또는 key 변경 = 결과 변경일 때",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 예: userId 변경 → 사용자 정보 다시 계산",
                        fontSize = 11.sp,
                        color = Color.DarkGray
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
// derivedStateOf: 버퍼링 역할
val showButton by remember {
    derivedStateOf {
        scrollState.firstVisibleItemIndex > 0
    }
}
// index가 0→1→2→3→4→5 변해도
// showButton은 false→true 딱 1번만 변경!

// remember(key): 키 변경 시 재계산
val greeting = remember(userName) {
    "Hello, ${"$"}userName!"
}
// userName 바뀔 때마다 재계산
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
fun ScrollPositionExampleCard() {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // ✅ derivedStateOf: 스크롤 위치가 수천 번 변해도
    // showButton은 true/false 변경 시에만 Recomposition!
    val showScrollToTopButton by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 2 }
    }

    // 리컴포지션 카운터 (derivedStateOf 효과 확인용)
    var derivedRecomposeCount by remember { mutableIntStateOf(0) }
    SideEffect { derivedRecomposeCount++ }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📱 실습 1: 스크롤 위치에 따른 버튼 노출",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "가장 흔한 실무 케이스 (인스타그램, 배달의민족 등)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "현재 인덱스: ${scrollState.firstVisibleItemIndex}",
                    fontSize = 13.sp
                )
                Text(
                    text = "Parent Recompose: $derivedRecomposeCount",
                    fontSize = 13.sp,
                    color = Color.Blue
                )
            }

            Text(
                text = "버튼 노출: $showScrollToTopButton",
                fontSize = 13.sp,
                color = if (showScrollToTopButton) Color.Green else Color.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 스크롤 가능한 리스트
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(50) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFA5D6A7)
                            )
                        ) {
                            Text(
                                text = "아이템 $index",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // FAB 버튼 (derivedStateOf 덕분에 효율적)
                if (showScrollToTopButton) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, "위로")
                    }
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
// ✅ derivedStateOf 사용
val showButton by remember {
    derivedStateOf {
        scrollState.firstVisibleItemIndex > 2
    }
}
// index가 0→1→2→3→4→5 변해도
// showButton은 false→true 한 번만!

// ❌ 이렇게 하면 안 됨!
val showButton = scrollState.firstVisibleItemIndex > 2
// 스크롤 할 때마다 60fps로 리컴포지션!
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
fun SearchFilterExampleCard() {
    var searchQuery by remember { mutableStateOf("") }
    val allItems = remember {
        listOf(
            "김철수", "이영희", "박지민", "최민준", "정서연",
            "강도윤", "조하은", "윤지호", "임서준", "한지아",
            "김민서", "이준혁", "박소율", "최예준", "정다온"
        )
    }

    // ✅ derivedStateOf: 필터링 결과가 같으면 하위 컴포저블 스킵
    val filteredItems by remember {
        derivedStateOf {
            Log.d("DerivedStateOf", "필터링 계산 수행: '$searchQuery'")
            if (searchQuery.isEmpty()) {
                allItems
            } else {
                allItems.filter { it.contains(searchQuery) }
            }
        }
    }

    var filterRecomposeCount by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 실습 2: 검색어 필터링",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "수천 명의 직원 목록에서 이름 검색",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("이름 검색") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "검색 결과: ${filteredItems.size}명",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            // 필터링된 결과 표시
            FilteredListDisplay(
                items = filteredItems,
                onRecompose = { filterRecomposeCount++ }
            )

            Text(
                text = "하위 컴포저블 Recompose: $filterRecomposeCount",
                fontSize = 12.sp,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// 검색어가 바뀌어서 계산은 다시 수행하지만,
// 결과 리스트의 내용이 같다면 하위 스킵!
val filteredList by remember {
    derivedStateOf {
        allItems.filter {
            it.name.contains(searchQuery)
        }
    }
}

// 💡 "김" 입력 후 "김철" 입력 시
// 결과가 같으면 리스트 UI 재렌더링 X
                    """.trimIndent(),
                    color = Color(0xFFFFCC80),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun FilteredListDisplay(items: List<String>, onRecompose: () -> Unit) {
    SideEffect { onRecompose() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        items.take(5).forEach { name ->
            Text(text = "• $name", fontSize = 12.sp)
        }
        if (items.size > 5) {
            Text(text = "... 외 ${items.size - 5}명", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FormValidationExampleCard() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ✅ derivedStateOf: 입력할 때마다가 아닌,
    // 유효성 상태(true/false)가 바뀔 때만 버튼 리컴포지션
    val isFormValid by remember {
        derivedStateOf {
            email.contains("@") && email.contains(".") && password.length >= 8
        }
    }

    var buttonRecomposeCount by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ 실습 3: 폼 유효성 검사",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "회원가입 버튼 활성화 상태 관리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("example@email.com") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호 (8자 이상)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "유효성: ${if (isFormValid) "✅ 통과" else "❌ 미충족"}",
                    color = if (isFormValid) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )

                SubmitButton(
                    enabled = isFormValid,
                    onRecompose = { buttonRecomposeCount++ }
                )
            }

            Text(
                text = "버튼 Recompose: $buttonRecomposeCount",
                fontSize = 12.sp,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
// 글자 하나하나 칠 때마다 리컴포지션 방지!
val isFormValid by remember {
    derivedStateOf {
        email.contains("@") &&
        password.length >= 8
    }
}

// 버튼은 true↔false 변경 시에만 다시 그림
// 이메일 "a@b.c" 입력 중에도
// 조건 충족 전까지 버튼 리컴포지션 X
                    """.trimIndent(),
                    color = Color(0xFFF8BBD9),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SubmitButton(enabled: Boolean, onRecompose: () -> Unit) {
    SideEffect { onRecompose() }

    Button(
        onClick = { /* 가입 처리 */ },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE91E63),
            disabledContainerColor = Color.Gray
        )
    ) {
        Text("가입하기")
    }
}

@Composable
fun ComparisonExampleCard() {
    var counter by remember { mutableIntStateOf(0) }

    // ❌ remember(key): key가 바뀔 때마다 재계산 + 리컴포지션
    val isEvenByRemember = remember(counter) {
        Log.d("Remember", "remember(counter) 계산: $counter")
        counter % 2 == 0
    }

    // ✅ derivedStateOf: 결과가 바뀔 때만 리컴포지션
    val isEvenByDerived by remember {
        derivedStateOf {
            Log.d("DerivedStateOf", "derivedStateOf 계산: $counter")
            counter % 2 == 0
        }
    }

    var rememberChildRecompose by remember { mutableIntStateOf(0) }
    var derivedChildRecompose by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1BEE7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚔️ 실습 4: remember(key) vs derivedStateOf 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "카운터: $counter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // remember(key) 결과
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("remember(counter)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("짝수: $isEvenByRemember", fontSize = 12.sp)
                        RememberChildDisplay(
                            isEven = isEvenByRemember,
                            onRecompose = { rememberChildRecompose++ }
                        )
                        Text(
                            "Child Recompose: $rememberChildRecompose",
                            fontSize = 10.sp,
                            color = Color.Red
                        )
                    }
                }

                // derivedStateOf 결과
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("derivedStateOf", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("짝수: $isEvenByDerived", fontSize = 12.sp)
                        DerivedChildDisplay(
                            isEven = isEvenByDerived,
                            onRecompose = { derivedChildRecompose++ }
                        )
                        Text(
                            "Child Recompose: $derivedChildRecompose",
                            fontSize = 10.sp,
                            color = Color.Green
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { counter++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("카운터 +1")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 0→1→2→3→4 증가 시:\n" +
                       "• remember: 매번 Child 리컴포지션 (5회)\n" +
                       "• derived: true→false→true 변경 시에만 (3회)",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun RememberChildDisplay(isEven: Boolean, onRecompose: () -> Unit) {
    SideEffect { onRecompose() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isEven) Color.Green else Color.Red,
                CircleShape
            )
    )
}

@Composable
fun DerivedChildDisplay(isEven: Boolean, onRecompose: () -> Unit) {
    SideEffect { onRecompose() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isEven) Color.Green else Color.Red,
                CircleShape
            )
    )
}

@Composable
fun TradeOffCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ 트레이드오프",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 언제 derivedStateOf
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "✅ derivedStateOf 사용",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(text = "• 입력 빈도 높음 + 출력 변화 낮음", fontSize = 12.sp)
                    Text(text = "• 스크롤 위치 → Boolean", fontSize = 12.sp)
                    Text(text = "• 검색어 → 필터링 결과", fontSize = 12.sp)
                    Text(text = "• 여러 입력 → 유효성 Boolean", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 언제 remember(key)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "📝 remember(key) 사용",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(text = "• key 변경 = 결과 변경 (1:1 대응)", fontSize = 12.sp)
                    Text(text = "• 단순 연산: val a = b + c", fontSize = 12.sp)
                    Text(text = "• 객체 교체: userId → userInfo", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 비용
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "⚠️ derivedStateOf 비용",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• 객체 생성 오버헤드\n" +
                               "• 내부 결과값 비교 오버헤드\n" +
                               "• 단순 연산에는 오히려 손해!",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InterviewQuestionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧐 면접 질문",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            InterviewQuestionItem(
                question = "Q1. derivedStateOf 없이 remember(scrollState.value)를 쓰면?",
                answer = "scrollState.value가 1px 단위로 변할 때마다 remember 블록이 재실행되고, " +
                        "모든 UI가 매 프레임(1/60초)마다 다시 그려집니다. " +
                        "CPU 점유율 상승, 배터리 소모, 애니메이션 끊김 발생."
            )

            Spacer(modifier = Modifier.height(8.dp))

            InterviewQuestionItem(
                question = "Q2. derivedStateOf 내부에서 일반 var 변수를 참조하면?",
                answer = "Compose는 State 객체의 변화만 추적합니다. " +
                        "일반 변수가 바뀌어도 derivedStateOf는 모릅니다. " +
                        "반드시 MutableState나 State 객체를 참조해야 합니다."
            )

            Spacer(modifier = Modifier.height(8.dp))

            InterviewQuestionItem(
                question = "Q3. derivedStateOf와 remember의 key를 동시에 사용하는 경우?",
                answer = "derivedStateOf 내부에서 참조하는 객체 자체가 통째로 바뀔 때. " +
                        "remember(newList) { derivedStateOf { ... } } 형태로 " +
                        "내부 관찰 대상을 갱신해줘야 합니다."
            )
        }
    }
}

@Composable
fun InterviewQuestionItem(question: String, answer: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFB2EBF2), RoundedCornerShape(8.dp))
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
fun DerivedStateOfScreenPreview() {
    MaterialTheme {
        DerivedStateOfScreen()
    }
}
