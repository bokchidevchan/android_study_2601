package io.github.bokchidevchan.android_study_2601.study.kotlin.oop

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
 * 📚 객체지향 프로그래밍 (Object-Oriented Programming)
 * ========================================================================
 *
 * 4대 원칙:
 * 1. 캡슐화 (Encapsulation) - 데이터 + 행위를 하나로, 접근 제어
 * 2. 상속 (Inheritance) - 코드 재사용, is-a 관계
 * 3. 다형성 (Polymorphism) - 같은 인터페이스, 다른 구현
 * 4. 추상화 (Abstraction) - 핵심만 노출, 복잡성 숨김
 */
@Composable
fun ObjectOrientedScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        EncapsulationSection()
        InheritancePolymorphismSection()
        InterfaceAndDIPSection()
        SealedClassSection()
        DelegationSection()
        DataClassSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "🏛️ 객체지향 프로그래밍",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Encapsulation, Inheritance, Polymorphism, Abstraction",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EncapsulationSection() {
    SectionCard(title = "1️⃣ 캡슐화 (Encapsulation)") {
        Text(
            text = "데이터와 행위를 하나로 묶고, 외부 접근을 제어",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "❌ 나쁜 예 - 외부에서 직접 상태 변경 가능",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )
        
        CodeBlock(
            code = """
class BankAccount {
    var balance: Int = 0  // 외부에서 직접 수정 가능!
}

val account = BankAccount()
account.balance = -1000  // 잘못된 상태 허용됨
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "✅ 좋은 예 - 메서드를 통해서만 상태 변경",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )
        
        CodeBlock(
            code = """
class BankAccount(
    private var _balance: Int = 0
) {
    val balance: Int get() = _balance  // 읽기 전용
    
    fun deposit(amount: Int): Result<Int> {
        return if (amount > 0) {
            _balance += amount
            Result.success(_balance)
        } else {
            Result.failure(
                IllegalArgumentException("양수만 가능")
            )
        }
    }
    
    fun withdraw(amount: Int): Result<Int> {
        return when {
            amount <= 0 -> Result.failure(
                IllegalArgumentException("양수만 가능")
            )
            amount > _balance -> Result.failure(
                IllegalArgumentException("잔액 부족")
            )
            else -> {
                _balance -= amount
                Result.success(_balance)
            }
        }
    }
}
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "접근 제어자",
            items = listOf(
                "private - 같은 클래스 내에서만",
                "protected - 같은 클래스 + 하위 클래스",
                "internal - 같은 모듈 내에서만",
                "public (기본) - 어디서든 접근 가능"
            )
        )
    }
}

@Composable
private fun InheritancePolymorphismSection() {
    SectionCard(title = "2️⃣ 상속과 다형성") {
        Text(
            text = "상속: 코드 재사용 | 다형성: 같은 타입, 다른 동작",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
// 추상 클래스 - 공통 인터페이스 정의
abstract class Shape {
    abstract val name: String
    abstract fun area(): Double
    abstract fun perimeter(): Double
    
    // 공통 메서드는 구현 제공
    fun describe(): String = 
        "${'$'}name - 넓이: ${'$'}{area()}"
}

class Circle(private val radius: Double) : Shape() {
    override val name = "원"
    override fun area() = Math.PI * radius * radius
    override fun perimeter() = 2 * Math.PI * radius
}

class Rectangle(
    private val width: Double,
    private val height: Double
) : Shape() {
    override val name = "직사각형"
    override fun area() = width * height
    override fun perimeter() = 2 * (width + height)
}

// 다형성 - 같은 타입으로 다양한 객체 처리
fun printAll(shapes: List<Shape>) {
    shapes.forEach { shape ->
        println(shape.describe())
    }
}

val shapes: List<Shape> = listOf(
    Circle(5.0),
    Rectangle(4.0, 6.0)
)
printAll(shapes)
// "원 - 넓이: 78.54"
// "직사각형 - 넓이: 24.0"
            """.trimIndent()
        )
    }
}

@Composable
private fun InterfaceAndDIPSection() {
    SectionCard(title = "3️⃣ 인터페이스와 의존성 역전 (DIP)") {
        Text(
            text = "고수준 모듈이 저수준 모듈에 의존하지 않고, 둘 다 추상화에 의존",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
// 인터페이스 (추상화)
interface PaymentProcessor {
    suspend fun process(amount: Int): PaymentResult
}

sealed class PaymentResult {
    data class Success(val txId: String) : PaymentResult()
    data class Failure(val reason: String) : PaymentResult()
}

// 구현체 1: 카카오페이
class KakaoPayProcessor : PaymentProcessor {
    override suspend fun process(amount: Int): PaymentResult {
        // 카카오페이 API 호출
        return PaymentResult.Success("KAKAO-123")
    }
}

// 구현체 2: 테스트용 Fake
class FakePaymentProcessor(
    private val shouldSucceed: Boolean = true
) : PaymentProcessor {
    override suspend fun process(amount: Int): PaymentResult {
        return if (shouldSucceed) {
            PaymentResult.Success("FAKE-123")
        } else {
            PaymentResult.Failure("테스트 실패")
        }
    }
}

// ViewModel - 인터페이스에만 의존 (DIP)
class CheckoutViewModel(
    private val payment: PaymentProcessor
) {
    suspend fun checkout(amount: Int) {
        when (val result = payment.process(amount)) {
            is PaymentResult.Success -> { /* 성공 */ }
            is PaymentResult.Failure -> { /* 실패 */ }
        }
    }
}

// 프로덕션: KakaoPayProcessor 주입
// 테스트: FakePaymentProcessor 주입
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "DIP의 장점",
            items = listOf(
                "테스트 용이 - Mock/Fake 쉽게 주입",
                "유연성 - 구현체 교체 용이",
                "결합도 감소 - 변경 영향 최소화",
                "Hilt와 찰떡궁합 - @Inject + @Module"
            )
        )
    }
}

@Composable
private fun SealedClassSection() {
    SectionCard(title = "4️⃣ Sealed Class - 타입 안전한 계층") {
        Text(
            text = "모든 하위 타입이 컴파일 타임에 알려짐 → when에서 else 불필요",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
sealed class NetworkState<out T> {
    data object Loading : NetworkState<Nothing>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class Error(
        val message: String, 
        val cause: Throwable? = null
    ) : NetworkState<Nothing>()
}

// 모든 케이스 강제 처리 (else 불필요)
fun <T> handleState(state: NetworkState<T>) {
    when (state) {
        is NetworkState.Loading -> showLoading()
        is NetworkState.Success -> showData(state.data)
        is NetworkState.Error -> showError(state.message)
    }
}

// 새 하위 타입 추가 시 → 컴파일 에러로 누락 방지
// sealed class NetworkState<out T> {
//     ...
//     data object Idle : NetworkState<Nothing>()  // 추가
// }
// → handleState()에서 Idle 처리 안 하면 컴파일 에러!
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ComparisonItem(
                title = "enum class",
                items = listOf("상태만 표현", "값 저장 불가", "싱글톤")
            )
            ComparisonItem(
                title = "sealed class",
                items = listOf("상태 + 데이터", "각 타입별 프로퍼티", "인스턴스 생성")
            )
        }
    }
}

@Composable
private fun ComparisonItem(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        items.forEach { item ->
            Text(text = "• $item", fontSize = 12.sp)
        }
    }
}

@Composable
private fun DelegationSection() {
    SectionCard(title = "5️⃣ Delegation 패턴 (by 키워드)") {
        Text(
            text = "인터페이스 구현을 다른 객체에 위임 → 보일러플레이트 제거",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
interface Logger {
    fun log(message: String)
}

class ConsoleLogger : Logger {
    override fun log(message: String) {
        println("[LOG] ${'$'}message")
    }
}

// by 키워드로 Logger 구현을 위임
class UserService(
    logger: Logger
) : Logger by logger {
    
    fun createUser(name: String) {
        log("사용자 생성: ${'$'}name")  // 위임된 메서드
        // 생성 로직...
    }
}

// 사용
val service = UserService(ConsoleLogger())
service.createUser("홍길동")
// 출력: [LOG] 사용자 생성: 홍길동
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "Delegation 활용",
            items = listOf(
                "상속보다 컴포지션 선호할 때",
                "여러 인터페이스 구현 시 코드 재사용",
                "기존 구현에 기능 추가 (데코레이터)",
                "Kotlin의 lazy, observable 등도 위임"
            )
        )
    }
}

@Composable
private fun DataClassSection() {
    SectionCard(title = "6️⃣ data class vs class") {
        Text(
            text = "data class: 값 객체 | class: 행위 중심 객체",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
// data class - 값 객체(Value Object)에 적합
// 자동 생성: equals, hashCode, toString, copy, componentN
data class Point(val x: Int, val y: Int)

val p1 = Point(1, 2)
val p2 = Point(1, 2)
p1 == p2       // true (구조적 동등성)
p1.copy(x = 5) // Point(x=5, y=2)

val (x, y) = p1  // 구조 분해

// class - 행위 중심 객체에 적합
class UserAccount(
    private val id: String
) {
    private var _status = Status.ACTIVE
    val status: Status get() = _status
    
    fun suspend() { _status = Status.SUSPENDED }
    fun activate() { _status = Status.ACTIVE }
}

enum class Status { ACTIVE, SUSPENDED }
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ComparisonItem(
                title = "data class 사용",
                items = listOf("DTO, 응답 모델", "불변 상태 표현", "Map 키로 사용")
            )
            ComparisonItem(
                title = "class 사용",
                items = listOf("비즈니스 로직 포함", "상태 변경 있음", "생명주기 관리")
            )
        }
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: 캡슐화란?" to 
                "A: 데이터와 행위를 하나로 묶고, private으로 내부 구현 숨김. 일관된 상태 보장",
            "Q: 다형성이란?" to 
                "A: 같은 인터페이스로 다양한 구현체 사용. List<Shape>으로 Circle, Rectangle 처리",
            "Q: 의존성 역전(DIP)이란?" to 
                "A: 고수준 모듈이 저수준에 의존 않고, 둘 다 인터페이스에 의존. 테스트/확장 용이",
            "Q: sealed class vs enum?" to 
                "A: enum은 상태만, sealed는 상태+데이터. sealed는 when에서 모든 케이스 강제",
            "Q: data class는 언제 쓰나?" to 
                "A: 값 객체(DTO, 응답 모델)에 사용. equals, hashCode 자동 생성. 불변 선호",
            "Q: 컴포지션 vs 상속?" to 
                "A: 상속은 강한 결합, 컴포지션은 느슨한 결합. 'has-a'면 컴포지션, 'is-a'면 상속"
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
private fun ObjectOrientedScreenPreview() {
    ObjectOrientedScreen()
}
