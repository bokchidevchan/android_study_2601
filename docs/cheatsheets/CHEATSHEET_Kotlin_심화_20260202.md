# Kotlin 심화 치트시트

> 함수형 / 객체지향 / 제네릭 핵심 정리 | 2026-02-02

---

## 1. 핵심 개념 한눈에

| 패러다임 | 핵심 원칙 | 면접 포인트 |
|----------|----------|-------------|
| **함수형** | 불변성, 순수함수, 고차함수 | "테스트 용이, 병렬처리 안전, 상태 추적 쉬움" |
| **객체지향** | 캡슐화, 다형성, 추상화 | "변경에 유연, 코드 재사용, 테스트 가능한 설계" |
| **제네릭** | 타입 파라미터화, Variance | "타입 안전성 + 코드 재사용 동시에" |

---

## 2. 함수형 프로그래밍

### 순수 함수 vs 비순수 함수

```kotlin
// ❌ 비순수 - 외부 상태 의존
var taxRate = 0.1
fun calcPrice(price: Int) = price * (1 + taxRate)

// ✅ 순수 - 입력만으로 결과 결정
fun calcPrice(price: Int, taxRate: Double) = price * (1 + taxRate)
```

> **면접**: "순수 함수는 같은 입력에 항상 같은 출력. 테스트/캐싱/병렬처리에 유리"

### Scope Functions 비교

| 함수 | 수신 객체 | 반환 | 용도 |
|------|----------|------|------|
| `let` | it | Lambda 결과 | null 체크 + 변환 |
| `run` | this | Lambda 결과 | 객체 스코프에서 연산 |
| `with` | this | Lambda 결과 | 동일 객체 여러 작업 |
| `apply` | this | 객체 자신 | 객체 초기화 |
| `also` | it | 객체 자신 | 디버깅/로깅 |

```kotlin
// let - null 체크
user?.let { println("${it.name}님 환영") }

// apply - 객체 초기화
val user = User().apply {
    name = "홍길동"
    age = 25
}

// also - 체이닝 중 로깅
products.filter { it.price > 1000 }
    .also { println("필터링: ${it.size}개") }
    .map { it.name }
```

### 컬렉션 연산 핵심

```kotlin
products
    .filter { it.category == "전자기기" }  // 조건 필터
    .map { it.name }                        // 변환
    .sortedBy { it.length }                 // 정렬
    
val total = products.fold(0) { acc, p -> acc + p.price }  // 누적
val grouped = products.groupBy { it.category }            // 그룹화
```

> **면접 Q**: "fold vs reduce?"  
> **A**: fold는 초기값 지정, reduce는 첫 요소가 초기값. 빈 리스트에 reduce는 에러

---

## 3. 객체지향 프로그래밍

### 캡슐화 패턴

```kotlin
class BankAccount(private var _balance: Int = 0) {
    val balance: Int get() = _balance  // 읽기 전용 노출
    
    fun deposit(amount: Int): Result<Int> {
        return if (amount > 0) {
            _balance += amount
            Result.success(_balance)
        } else {
            Result.failure(IllegalArgumentException("양수만 가능"))
        }
    }
}
```

> **면접**: "private 필드 + public 메서드로 일관된 상태 보장"

### 의존성 역전 (DIP)

```kotlin
// 인터페이스 정의
interface PaymentProcessor {
    suspend fun process(amount: Int): PaymentResult
}

// ViewModel은 인터페이스에만 의존
class CheckoutViewModel(
    private val payment: PaymentProcessor  // 구현체 아닌 인터페이스
) { ... }

// 프로덕션: KakaoPayProcessor
// 테스트: FakePaymentProcessor
```

> **면접**: "고수준 모듈이 저수준에 의존 X, 둘 다 추상화에 의존. 테스트/확장 용이"

### Sealed Class vs Enum

| 구분 | enum class | sealed class |
|------|------------|--------------|
| 데이터 | 상태만 표현 | 상태 + 데이터 |
| 인스턴스 | 싱글톤 | 인스턴스 생성 가능 |
| when | else 불필요 | else 불필요 |

```kotlin
sealed class NetworkState<out T> {
    data object Loading : NetworkState<Nothing>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class Error(val message: String) : NetworkState<Nothing>()
}

// when에서 모든 케이스 강제 - 누락 시 컴파일 에러
when (state) {
    is NetworkState.Loading -> showLoading()
    is NetworkState.Success -> showData(state.data)
    is NetworkState.Error -> showError(state.message)
}
```

### Delegation (by 키워드)

```kotlin
interface Logger {
    fun log(message: String)
}

class UserService(logger: Logger) : Logger by logger {
    fun createUser(name: String) {
        log("사용자 생성: $name")  // 위임된 메서드
    }
}
```

> **면접**: "상속 대신 컴포지션. 기존 구현 재사용하면서 결합도 낮춤"

---

## 4. 제네릭

### Variance (공변성/반공변성)

| 키워드 | 이름 | 규칙 | 예시 |
|--------|------|------|------|
| `out` | 공변성 | 생산만 (반환) | `Producer<out T>` |
| `in` | 반공변성 | 소비만 (받기) | `Consumer<in T>` |

```kotlin
// out - 생산자 (읽기 전용)
interface Producer<out T> {
    fun produce(): T       // ✅ 반환 가능
    // fun consume(t: T)   // ❌ 받기 불가
}

val dogProducer: Producer<Dog> = ...
val animalProducer: Producer<Animal> = dogProducer  // ✅ 공변

// in - 소비자 (쓰기 전용)
interface Consumer<in T> {
    fun consume(t: T)      // ✅ 받기 가능
    // fun produce(): T    // ❌ 반환 불가
}

val animalConsumer: Consumer<Animal> = ...
val dogConsumer: Consumer<Dog> = animalConsumer  // ✅ 반공변
```

> **면접 기억법**: "PECS - Producer는 Extends(out), Consumer는 Super(in)"

### reified 타입 파라미터

```kotlin
// ❌ 일반 제네릭 - 런타임에 타입 정보 없음 (Type Erasure)
fun <T> isType(value: Any): Boolean {
    // return value is T  // 컴파일 에러!
}

// ✅ inline + reified - 런타임에도 타입 정보 유지
inline fun <reified T> isType(value: Any): Boolean {
    return value is T  // OK!
}

isType<String>("hello")  // true
isType<Int>("hello")     // false

// 실용 예시: JSON 파싱
inline fun <reified T> String.parseJson(): T {
    return Gson().fromJson(this, T::class.java)
}
```

> **면접**: "inline 함수에서만 사용. T::class.java, is/as 연산자 사용 가능"

### 실전 패턴: Result 타입

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

// 사용
fetchUser()
    .map { it.name }
    .flatMap { validateName(it) }
```

---

## 5. 면접 Q&A 총정리

### 함수형

| 질문 | 핵심 답변 |
|------|----------|
| 순수 함수란? | 같은 입력 → 같은 출력, 부수 효과 없음. 테스트/병렬처리 용이 |
| let vs apply? | let은 it으로 접근, 결과 반환. apply는 this, 객체 자신 반환 |
| fold vs reduce? | fold는 초기값 필수, reduce는 첫 요소가 초기값. 빈 리스트 reduce는 에러 |

### 객체지향

| 질문 | 핵심 답변 |
|------|----------|
| 캡슐화란? | private 필드 + public 메서드로 내부 구현 숨기고 일관된 상태 보장 |
| DIP란? | 고수준/저수준 모듈이 모두 인터페이스에 의존. 테스트/확장 용이 |
| sealed vs enum? | enum은 상태만, sealed는 상태+데이터. when에서 모든 케이스 강제 |
| 컴포지션 vs 상속? | 상속은 강한 결합, 컴포지션은 느슨한 결합. has-a면 컴포지션 |

### 제네릭

| 질문 | 핵심 답변 |
|------|----------|
| Type Erasure란? | 컴파일 후 제네릭 타입 정보 삭제. List<String>과 List<Int>는 런타임에 동일 |
| out vs in? | out은 공변성(생산만), in은 반공변성(소비만). PECS 기억 |
| reified는 언제? | 런타임에 타입 정보 필요할 때. inline 함수에서만 가능 |
| *는 뭔가요? | 타입 인자 모를 때 사용. 읽기는 Any?, 쓰기는 불가 |

---

## 6. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| apply에서 it 사용 | apply는 this, also가 it |
| 빈 리스트에 reduce | fold 사용하거나 reduceOrNull |
| out 위치에서 T 받기 | out은 생산만, 소비하려면 in 또는 무공변 |
| 일반 제네릭에서 is T | inline + reified 사용 |
| sealed class when에서 else | 불필요. else 쓰면 새 타입 추가 시 컴파일 에러 못 받음 |
| ViewModel에서 View 참조 | StateFlow로 데이터만 노출 (메모리 누수 방지) |

---

## 7. 한 줄 요약

```
함수형: "데이터를 변환하는 파이프라인. 불변 + 순수함수 = 예측 가능"
객체지향: "책임을 캡슐화하고 인터페이스로 소통. 변경에 유연"
제네릭: "타입을 파라미터화. out은 생산, in은 소비, reified는 런타임"
```

---

*Generated from conversation on 2026-02-02*
