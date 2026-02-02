package io.github.bokchidevchan.android_study_2601.study.kotlin.functional

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ========================================================================
 * 📚 함수형 프로그래밍 (Functional Programming)
 * ========================================================================
 *
 * 핵심 원칙:
 * 1. 순수 함수 (Pure Function) - 같은 입력 → 항상 같은 출력
 * 2. 불변성 (Immutability) - 데이터를 변경하지 않고 새로 생성
 * 3. 고차 함수 (Higher-Order Function) - 함수를 인자/반환값으로 사용
 * 4. 함수 합성 - 작은 함수들을 조합하여 복잡한 로직 구현
 */
@Composable
fun FunctionalProgrammingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        PureFunctionSection()
        HigherOrderFunctionSection()
        CollectionOperationsSection()
        ScopeFunctionsSection()
        FunctionCompositionSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "λ 함수형 프로그래밍",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pure Functions, Immutability, Higher-Order Functions",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PureFunctionSection() {
    SectionCard(title = "1️⃣ 순수 함수 vs 비순수 함수") {
        Text(
            text = "순수 함수: 같은 입력 → 항상 같은 출력, 부수 효과 없음",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "❌ 비순수 함수 - 외부 상태에 의존",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )
        
        CodeBlock(
            code = """
var taxRate = 0.1  // 외부 상태

fun calculatePrice(price: Int): Double {
    return price * (1 + taxRate)
}

// taxRate이 바뀌면 같은 입력에도 다른 결과
taxRate = 0.2
calculatePrice(1000)  // 1100 → 1200으로 변함!
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "✅ 순수 함수 - 입력만으로 결과 결정",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )
        
        CodeBlock(
            code = """
fun calculatePrice(price: Int, taxRate: Double): Double {
    return price * (1 + taxRate)
}

// 같은 입력 → 항상 같은 출력
calculatePrice(1000, 0.1)  // 항상 1100
calculatePrice(1000, 0.1)  // 항상 1100
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "순수 함수의 장점",
            items = listOf(
                "테스트 용이 - Mock 없이 입력/출력만 검증",
                "병렬 처리 안전 - 공유 상태 없음",
                "캐싱 가능 - 같은 입력은 같은 결과 (Memoization)",
                "추론 용이 - 코드 흐름 예측 가능"
            )
        )
    }
}

@Composable
private fun HigherOrderFunctionSection() {
    SectionCard(title = "2️⃣ 고차 함수 (Higher-Order Function)") {
        Text(
            text = "함수를 인자로 받거나 함수를 반환하는 함수",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "함수를 매개변수로 받기",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        CodeBlock(
            code = """
fun processItems(
    items: List<String>,
    transform: (String) -> String  // 함수 타입 파라미터
): List<String> {
    return items.map { transform(it) }
}

val items = listOf("apple", "banana")

// 람다로 전달
processItems(items) { it.uppercase() }
// 결과: ["APPLE", "BANANA"]

// 함수 참조로 전달
items.map(String::length)
// 결과: [5, 6]
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "함수를 반환하기 (Currying)",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        CodeBlock(
            code = """
fun multiplier(factor: Int): (Int) -> Int {
    return { number -> number * factor }
}

val double = multiplier(2)
val triple = multiplier(3)

double(5)  // 10
triple(5)  // 15

// 체이닝
listOf(1, 2, 3).map(double)  // [2, 4, 6]
            """.trimIndent()
        )
    }
}

@Composable
private fun CollectionOperationsSection() {
    SectionCard(title = "3️⃣ 컬렉션 함수형 연산") {
        Text(
            text = "Kotlin 컬렉션의 강력한 함수형 API",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
data class Product(
    val name: String, 
    val price: Int, 
    val category: String
)

val products = listOf(
    Product("iPhone", 1200000, "전자기기"),
    Product("Galaxy", 1100000, "전자기기"),
    Product("책상", 300000, "가구")
)

// filter + map + sortedBy 체이닝
val result = products
    .filter { it.category == "전자기기" }
    .filter { it.price >= 1000000 }
    .map { "${'$'}{it.name}: ${'$'}{it.price}원" }
    .sortedByDescending { it.length }

// fold - 값을 누적하여 하나로
val total = products.fold(0) { acc, product -> 
    acc + product.price 
}
// 2,600,000

// groupBy - 카테고리별 그룹화
val byCategory = products.groupBy { it.category }
// Map<String, List<Product>>
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "주요 컬렉션 함수",
            items = listOf(
                "filter { } - 조건에 맞는 요소만 선택",
                "map { } - 각 요소를 변환",
                "fold(초기값) { } - 누적하여 단일 값 생성",
                "groupBy { } - 키 기준으로 그룹화",
                "flatMap { } - 중첩 컬렉션을 평탄화",
                "take(n) / drop(n) - 앞에서 n개 선택/제외"
            )
        )
    }
}

@Composable
private fun ScopeFunctionsSection() {
    SectionCard(title = "4️⃣ Scope Functions (let, run, with, apply, also)") {
        Text(
            text = "객체를 다룰 때 유용한 5가지 스코프 함수",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ScopeFunctionRow(
            name = "let",
            receiver = "it",
            returns = "Lambda 결과",
            useCase = "null 체크 + 변환"
        )
        
        CodeBlock(
            code = """
user?.let { 
    println("${'$'}{it.name}님 환영합니다!")
}
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ScopeFunctionRow(
            name = "apply",
            receiver = "this",
            returns = "객체 자신",
            useCase = "객체 초기화"
        )
        
        CodeBlock(
            code = """
val user = User().apply {
    name = "홍길동"
    email = "hong@test.com"
    age = 25
}
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ScopeFunctionRow(
            name = "also",
            receiver = "it",
            returns = "객체 자신",
            useCase = "디버깅/로깅"
        )
        
        CodeBlock(
            code = """
val result = products
    .filter { it.price > 500000 }
    .also { println("필터링: ${'$'}{it.size}개") }
    .map { it.name }
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ScopeFunctionRow(
            name = "run",
            receiver = "this",
            returns = "Lambda 결과",
            useCase = "객체 스코프에서 연산"
        )
        
        CodeBlock(
            code = """
val greeting = user.run {
    "안녕하세요, ${'$'}name님! (${'$'}age세)"
}
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ScopeFunctionRow(
            name = "with",
            receiver = "this",
            returns = "Lambda 결과",
            useCase = "동일 객체 여러 작업"
        )
        
        CodeBlock(
            code = """
val info = with(user) {
    "이름: ${'$'}name, 이메일: ${'$'}email"
}
            """.trimIndent()
        )
    }
}

@Composable
private fun ScopeFunctionRow(
    name: String,
    receiver: String,
    returns: String,
    useCase: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = "수신: $receiver", fontSize = 12.sp)
        Text(text = "반환: $returns", fontSize = 12.sp)
        Text(text = useCase, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun FunctionCompositionSection() {
    SectionCard(title = "5️⃣ 함수 합성 (Function Composition)") {
        Text(
            text = "작은 함수들을 조합하여 복잡한 로직 구현",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
// 작은 단위 함수들
val addTax: (Int) -> Int = { (it * 1.1).toInt() }
val applyDiscount: (Int) -> Int = { (it * 0.9).toInt() }
val formatPrice: (Int) -> String = { "${'$'}it원" }

// 합성 함수 정의
fun <A, B, C> compose(
    f: (B) -> C, 
    g: (A) -> B
): (A) -> C = { a -> f(g(a)) }

// 함수 합성
val calculate = compose(
    formatPrice, 
    compose(applyDiscount, addTax)
)

calculate(10000)  // "9900원"

// 또는 체이닝으로 표현
val result = 10000
    .let(addTax)
    .let(applyDiscount)
    .let(formatPrice)
// "9900원"
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "함수 합성의 장점",
            items = listOf(
                "단일 책임 - 각 함수는 한 가지 일만",
                "재사용성 - 함수를 다양하게 조합 가능",
                "테스트 용이 - 작은 함수 단위로 테스트",
                "가독성 - 데이터 흐름이 명확"
            )
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: 순수 함수란?" to 
                "A: 같은 입력에 항상 같은 출력, 부수 효과 없는 함수. 테스트/병렬처리 용이",
            "Q: 고차 함수란?" to 
                "A: 함수를 인자로 받거나 반환하는 함수. map, filter, fold 등",
            "Q: let vs apply?" to 
                "A: let은 it으로 접근, 변환 결과 반환. apply는 this로 접근, 객체 자신 반환",
            "Q: fold vs reduce?" to 
                "A: fold는 초기값 지정, reduce는 첫 요소가 초기값. 빈 리스트에 reduce는 에러",
            "Q: 왜 불변성이 중요한가?" to 
                "A: 상태 추적 용이, 병렬 처리 안전, 버그 감소. Compose에서 recomposition 최적화"
        )
        
        qnas.forEachIndexed { index, (q, a) ->
            Column {
                Text(
                    text = q,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = a,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (index < qnas.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(
                Color(0xFF1E1E1E),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFFE0E0E0)
        )
    }
}

@Composable
private fun HighlightBox(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF43A047).copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Text(
                text = "• $item",
                fontSize = 13.sp,
                color = Color(0xFF388E3C)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FunctionalProgrammingScreenPreview() {
    FunctionalProgrammingScreen()
}
