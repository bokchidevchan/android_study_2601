package io.github.bokchidevchan.android_study_2601.study.kotlin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * 📚 Kotlin 심화 학습 - 메인 화면
 * ========================================================================
 *
 * 학습 주제:
 * 1. 함수형 프로그래밍 (Functional Programming)
 * 2. 객체지향 프로그래밍 (Object-Oriented Programming)
 * 3. 제네릭 (Generics)
 *
 * Kotlin은 멀티 패러다임 언어로, 함수형과 객체지향을 모두 지원합니다.
 * 상황에 맞게 적절한 패러다임을 선택하는 것이 중요합니다.
 */
@Composable
fun KotlinStudyScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
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
            text = "🎯 Kotlin 심화 학습",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "함수형, 객체지향, 제네릭 프로그래밍의 핵심 개념과 실전 예제",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 1. 함수형 프로그래밍
        KotlinTopicCard(
            emoji = "λ",
            title = "함수형 프로그래밍",
            subtitle = "Functional Programming",
            description = "순수 함수, 불변성, 고차 함수, 컬렉션 연산, Scope Functions",
            color = Color(0xFFE3F2FD),
            highlights = listOf(
                "순수 함수 vs 비순수 함수",
                "map, filter, fold, reduce",
                "let, run, with, apply, also",
                "함수 합성과 체이닝"
            ),
            onClick = { onNavigate("FunctionalProgramming") }
        )
        
        // 2. 객체지향 프로그래밍
        KotlinTopicCard(
            emoji = "🏛️",
            title = "객체지향 프로그래밍",
            subtitle = "Object-Oriented Programming",
            description = "캡슐화, 상속, 다형성, 추상화, SOLID 원칙",
            color = Color(0xFFFFF3E0),
            highlights = listOf(
                "캡슐화와 접근 제어",
                "인터페이스와 의존성 역전",
                "Sealed Class와 타입 안전성",
                "Delegation 패턴"
            ),
            onClick = { onNavigate("ObjectOriented") }
        )
        
        // 3. 제네릭
        KotlinTopicCard(
            emoji = "🔧",
            title = "제네릭",
            subtitle = "Generics",
            description = "타입 파라미터, 공변성/반공변성, reified, 실전 패턴",
            color = Color(0xFFE8F5E9),
            highlights = listOf(
                "out (공변성) vs in (반공변성)",
                "타입 제약 (Upper Bound)",
                "reified 타입 파라미터",
                "Result, Repository 패턴"
            ),
            onClick = { onNavigate("Generics") }
        )
        
        // 패러다임 비교 요약
        ParadigmComparisonCard()
    }
}

@Composable
private fun KotlinTopicCard(
    emoji: String,
    title: String,
    subtitle: String,
    description: String,
    color: Color,
    highlights: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            highlights.forEach { highlight ->
                Text(
                    text = "• $highlight",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun ParadigmComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📋 패러다임 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            ComparisonRow("핵심", "불변성, 순수함수", "캡슐화, 다형성", "타입 파라미터화")
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("데이터", "불변 데이터 변환", "객체 내 상태 관리", "타입 안전성 보장")
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("재사용", "고차함수 합성", "상속/위임", "타입 독립적 로직")
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("키워드", "val, map, fold", "class, interface", "<T>, out, in")
        }
    }
}

@Composable
private fun ComparisonRow(
    category: String,
    functional: String,
    oop: String,
    generics: String
) {
    Column {
        Text(
            text = category,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "함수형: $functional",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "객체지향: $oop",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "제네릭: $generics",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KotlinStudyScreenPreview() {
    KotlinStudyScreen(onNavigate = {})
}
