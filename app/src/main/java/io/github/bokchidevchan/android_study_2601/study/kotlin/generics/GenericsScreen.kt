package io.github.bokchidevchan.android_study_2601.study.kotlin.generics

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
 * 📚 제네릭 프로그래밍 (Generics)
 * ========================================================================
 *
 * 핵심 개념:
 * 1. 타입 파라미터 - 타입을 파라미터화
 * 2. 타입 제약 - 사용 가능한 타입 제한
 * 3. Variance (공변성/반공변성) - 타입 계층 관계 유지
 * 4. Type Erasure - 런타임에 제네릭 타입 정보 삭제
 * 5. reified - 런타임에도 타입 정보 유지
 */
@Composable
fun GenericsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader()
        BasicGenericsSection()
        TypeConstraintsSection()
        VarianceSection()
        StarProjectionSection()
        ReifiedSection()
        PracticalPatternsSection()
        CheatSheetSection()
    }
}

@Composable
private fun ScreenHeader() {
    Column {
        Text(
            text = "🔧 제네릭 (Generics)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Type Parameters, Variance, Constraints, Reified",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BasicGenericsSection() {
    SectionCard(title = "1️⃣ 기본 제네릭 클래스") {
        Text(
            text = "타입을 파라미터로 받아 재사용 가능한 코드 작성",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
// 단일 타입 파라미터
class Box<T>(private val content: T) {
    fun get(): T = content
    
    fun <R> map(transform: (T) -> R): Box<R> {
        return Box(transform(content))
    }
}

val intBox = Box(42)
val stringBox = intBox.map { it.toString() }
// Box<String>

// 다중 타입 파라미터
class Pair<A, B>(val first: A, val second: B) {
    fun swap(): Pair<B, A> = Pair(second, first)
}

val pair = Pair("hello", 42)
val swapped = pair.swap()  // Pair<Int, String>

// 제네릭 함수
fun <T> List<T>.secondOrNull(): T? {
    return if (size >= 2) this[1] else null
}

listOf(1, 2, 3).secondOrNull()  // 2
listOf("a").secondOrNull()      // null
            """.trimIndent()
        )
    }
}

@Composable
private fun TypeConstraintsSection() {
    SectionCard(title = "2️⃣ 타입 제약 (Type Constraints)") {
        Text(
            text = "제네릭 타입에 제약을 걸어 특정 타입만 허용",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "상한 제약 (Upper Bound)",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        CodeBlock(
            code = """
// T는 Comparable을 구현해야 함
fun <T : Comparable<T>> findMax(list: List<T>): T? {
    return list.maxOrNull()
}

findMax(listOf(1, 5, 3))       // 5 ✅
findMax(listOf("a", "z", "m")) // "z" ✅

class Box<T>(val value: T)
// findMax(listOf(Box(1), Box(2)))
// ❌ 컴파일 에러 - Box는 Comparable 아님
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "다중 제약 (where 절)",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        CodeBlock(
            code = """
// T는 CharSequence이면서 Comparable이어야 함
fun <T> process(value: T) 
    where T : CharSequence, 
          T : Comparable<T> {
    if (value.isNotEmpty()) {
        println("값: ${'$'}value")
    }
}

process("hello")  // ✅ String은 둘 다 만족
// process(123)   // ❌ Int는 CharSequence 아님
            """.trimIndent()
        )
    }
}

@Composable
private fun VarianceSection() {
    SectionCard(title = "3️⃣ 공변성(out)과 반공변성(in)") {
        Text(
            text = "타입 계층 관계를 제네릭에서도 유지하는 방법",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        VarianceExplanationRow()
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "out (공변성) - 생산만 가능",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )
        
        CodeBlock(
            code = """
interface Producer<out T> {
    fun produce(): T       // ✅ T 반환 가능
    // fun consume(t: T)   // ❌ T 받기 불가
}

open class Animal
class Dog : Animal()

val dogProducer: Producer<Dog> = object : Producer<Dog> {
    override fun produce() = Dog()
}

// Dog는 Animal의 하위 타입
// → Producer<Dog>도 Producer<Animal>의 하위 타입
val animalProducer: Producer<Animal> = dogProducer  // ✅
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "in (반공변성) - 소비만 가능",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E88E5)
        )
        
        CodeBlock(
            code = """
interface Consumer<in T> {
    fun consume(t: T)      // ✅ T 받기 가능
    // fun produce(): T    // ❌ T 반환 불가
}

val animalConsumer: Consumer<Animal> = object : Consumer<Animal> {
    override fun consume(t: Animal) {
        println("동물 처리")
    }
}

// Animal은 Dog의 상위 타입
// → Consumer<Animal>은 Consumer<Dog>의 하위 타입 (역전!)
val dogConsumer: Consumer<Dog> = animalConsumer  // ✅
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "기억법: PECS",
            items = listOf(
                "Producer → out (Extends) - 생산자는 out",
                "Consumer → in (Super) - 소비자는 in",
                "List<out T> → 읽기 전용 (covariant)",
                "Comparable<in T> → 비교 대상 받기 (contravariant)"
            )
        )
    }
}

@Composable
private fun VarianceExplanationRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VarianceBox(
            title = "out (공변성)",
            symbol = "Dog → Animal",
            description = "Producer<Dog> → Producer<Animal>",
            color = Color(0xFF43A047)
        )
        VarianceBox(
            title = "in (반공변성)",
            symbol = "Animal → Dog",
            description = "Consumer<Animal> → Consumer<Dog>",
            color = Color(0xFF1E88E5)
        )
    }
}

@Composable
private fun VarianceBox(
    title: String,
    symbol: String,
    description: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        Text(text = symbol, fontSize = 12.sp)
        Text(text = description, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun StarProjectionSection() {
    SectionCard(title = "4️⃣ Star Projection (*)") {
        Text(
            text = "타입 인자를 모를 때 * 사용",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CodeBlock(
            code = """
// 타입 인자를 모르는 제네릭 처리
fun printListInfo(list: List<*>) {
    println("크기: ${'$'}{list.size}")
    
    // list[0]의 타입은 Any?로 취급됨
    list.firstOrNull()?.let {
        println("첫 요소: ${'$'}it")
    }
}

printListInfo(listOf(1, 2, 3))     // 크기: 3
printListInfo(listOf("a", "b"))    // 크기: 2

// MutableList<*>
fun clearList(list: MutableList<*>) {
    list.clear()      // ✅ 삭제는 가능
    // list.add(1)    // ❌ 추가 불가 - 타입 모름
}
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "* 프로젝션 규칙",
            items = listOf(
                "Foo<out T> → Foo<*> = Foo<out Any?>",
                "Foo<in T> → Foo<*> = Foo<in Nothing>",
                "읽기: Any?로 취급",
                "쓰기: 불가 (Nothing)"
            )
        )
    }
}

@Composable
private fun ReifiedSection() {
    SectionCard(title = "5️⃣ reified 타입 파라미터") {
        Text(
            text = "inline + reified로 런타임에 타입 정보 유지",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "❌ 일반 제네릭 - Type Erasure",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )
        
        CodeBlock(
            code = """
fun <T> isType(value: Any): Boolean {
    // return value is T  
    // ❌ 컴파일 에러 - 런타임에 T 정보 없음
    return false
}
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "✅ inline + reified - 타입 정보 유지",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF43A047)
        )
        
        CodeBlock(
            code = """
inline fun <reified T> isType(value: Any): Boolean {
    return value is T  // ✅ 런타임에도 T 타입 확인 가능
}

isType<String>("hello")  // true
isType<Int>("hello")     // false

// 실용적 예시: JSON 파싱
inline fun <reified T> String.parseJson(): T {
    val type = T::class.java
    return Gson().fromJson(this, type)
}

val user = jsonString.parseJson<User>()

// 실용적 예시: Intent Extra
inline fun <reified T> Activity.getExtra(key: String): T? {
    return intent.extras?.get(key) as? T
}

val userId = getExtra<String>("user_id")
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HighlightBox(
            title = "reified 제약",
            items = listOf(
                "inline 함수에서만 사용 가능",
                "클래스의 타입 파라미터에는 불가",
                "T::class.java로 Class 객체 얻기 가능",
                "is, as 연산자 사용 가능"
            )
        )
    }
}

@Composable
private fun PracticalPatternsSection() {
    SectionCard(title = "6️⃣ 실전 제네릭 패턴") {
        Text(
            text = "Result 타입",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        CodeBlock(
            code = """
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = 
        when (this) {
            is Success -> transform(data)
            is Error -> this
        }
}

// 사용
suspend fun fetchUser(): Result<User> { ... }

fetchUser()
    .map { it.name }
    .flatMap { validateName(it) }
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Repository 인터페이스",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        CodeBlock(
            code = """
interface Repository<T, ID> {
    suspend fun findById(id: ID): T?
    suspend fun findAll(): List<T>
    suspend fun save(entity: T): T
    suspend fun delete(id: ID)
}

data class User(val id: String, val name: String)

class UserRepository : Repository<User, String> {
    private val cache = mutableMapOf<String, User>()
    
    override suspend fun findById(id: String) = cache[id]
    override suspend fun findAll() = cache.values.toList()
    override suspend fun save(entity: User): User {
        cache[entity.id] = entity
        return entity
    }
    override suspend fun delete(id: String) { 
        cache.remove(id) 
    }
}
            """.trimIndent()
        )
    }
}

@Composable
private fun CheatSheetSection() {
    SectionCard(title = "📝 면접 치트시트") {
        val qnas = listOf(
            "Q: Type Erasure란?" to 
                "A: 컴파일 후 제네릭 타입 정보가 삭제됨. List<String>과 List<Int>는 런타임에 같음",
            "Q: out vs in?" to 
                "A: out은 공변성(생산만), in은 반공변성(소비만). PECS: Producer-out, Consumer-in",
            "Q: reified는 언제 쓰나?" to 
                "A: 런타임에 타입 정보 필요할 때. inline 함수에서만 가능. is/as, T::class 사용",
            "Q: *는 뭔가요?" to 
                "A: 타입 인자를 모를 때 사용. 읽기는 Any?, 쓰기는 불가(Nothing)",
            "Q: where 절은?" to 
                "A: 다중 타입 제약. T가 여러 인터페이스를 구현해야 할 때",
            "Q: 왜 제네릭을 쓰나?" to 
                "A: 타입 안전성 + 코드 재사용. 컴파일 타임에 타입 체크, 런타임 캐스팅 불필요"
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
private fun GenericsScreenPreview() {
    GenericsScreen()
}
