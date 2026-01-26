package io.github.bokchidevchan.android_study_2601.study.testing.basics

import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

/**
 * ========================================================================
 * 📚 JUnit 기본 - Unit Test의 기초
 * ========================================================================
 *
 * 테스트의 3A 패턴 (AAA Pattern):
 * - Arrange: 테스트 환경 준비
 * - Act: 테스트 대상 실행
 * - Assert: 결과 검증
 *
 * 테스트 명명 규칙:
 * - 한글 사용 가능 (백틱으로 감싸기)
 * - "무엇을_어떤상황에서_어떻게되는지" 형식 권장
 */
class CalculatorTest {

    // ========================================================================
    // Test Lifecycle - 테스트 생명주기
    // ========================================================================

    companion object {
        /**
         * @BeforeClass: 테스트 클래스 전체에서 딱 한 번 실행
         * - 무거운 리소스 초기화 (DB 연결, 파일 로드 등)
         * - static 메서드여야 함 (companion object 내부)
         */
        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            println("=== 테스트 클래스 시작 (1회) ===")
        }

        /**
         * @AfterClass: 테스트 클래스 전체 종료 후 딱 한 번 실행
         * - 리소스 정리
         */
        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            println("=== 테스트 클래스 종료 (1회) ===")
        }
    }

    private lateinit var calculator: Calculator

    /**
     * @Before: 각 테스트 메서드 실행 전에 매번 실행
     * - 테스트 간 격리 보장
     * - 매번 새로운 객체로 테스트
     */
    @Before
    fun setUp() {
        println(">> @Before: 테스트 준비")
        calculator = Calculator()
    }

    /**
     * @After: 각 테스트 메서드 실행 후에 매번 실행
     * - 테스트 후 정리 작업
     */
    @After
    fun tearDown() {
        println("<< @After: 테스트 정리")
    }

    // ========================================================================
    // 기본 Assertion 메서드
    // ========================================================================

    @Test
    fun `assertEquals - 두 값이 같은지 확인`() {
        // Arrange
        val a = 10
        val b = 5

        // Act
        val result = calculator.add(a, b)

        // Assert
        assertEquals(15, result)
        assertEquals("메시지도 추가 가능", 15, result)
    }

    @Test
    fun `assertNotEquals - 두 값이 다른지 확인`() {
        val result = calculator.add(2, 3)
        assertNotEquals(0, result)
    }

    @Test
    fun `assertTrue와 assertFalse - 불리언 검증`() {
        assertTrue(calculator.isEven(4))
        assertTrue("4는 짝수여야 함", calculator.isEven(4))

        assertFalse(calculator.isEven(5))
        assertFalse("5는 홀수여야 함", calculator.isEven(5))
    }

    @Test
    fun `assertNull과 assertNotNull - null 검증`() {
        val nullValue: String? = null
        val nonNullValue: String? = "Hello"

        assertNull(nullValue)
        assertNotNull(nonNullValue)
    }

    @Test
    fun `assertSame과 assertNotSame - 참조 동일성 검증`() {
        val list1 = listOf(1, 2, 3)
        val list2 = list1  // 같은 참조
        val list3 = listOf(1, 2, 3)  // 다른 참조, 같은 값

        assertSame(list1, list2)  // 같은 객체 참조
        assertNotSame(list1, list3)  // 다른 객체 참조

        assertEquals(list1, list3)  // 값은 같음
    }

    @Test
    fun `assertArrayEquals - 배열 비교`() {
        val expected = arrayOf(1, 2, 3)
        val actual = arrayOf(1, 2, 3)

        assertArrayEquals(expected, actual)
    }

    // ========================================================================
    // 예외 테스트
    // ========================================================================

    /**
     * expected 파라미터로 예외 타입 지정
     * - 간단하지만 예외 메시지 검증 불가
     */
    @Test(expected = IllegalArgumentException::class)
    fun `0으로 나누면 예외 발생 - expected 사용`() {
        calculator.divide(10, 0)
    }

    /**
     * assertThrows로 예외 검증 (JUnit 4.13+)
     * - 예외 메시지까지 검증 가능
     */
    @Test
    fun `0으로 나누면 예외 발생 - assertThrows 사용`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            calculator.divide(10, 0)
        }

        assertEquals("0으로 나눌 수 없습니다", exception.message)
    }

    /**
     * Kotlin의 runCatching 활용
     */
    @Test
    fun `음수 팩토리얼은 예외 발생`() {
        val result = runCatching { calculator.factorial(-1) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    // ========================================================================
    // 다양한 테스트 케이스
    // ========================================================================

    @Test
    fun `덧셈 - 양수 + 양수`() {
        assertEquals(15, calculator.add(10, 5))
    }

    @Test
    fun `덧셈 - 양수 + 음수`() {
        assertEquals(5, calculator.add(10, -5))
    }

    @Test
    fun `덧셈 - 음수 + 음수`() {
        assertEquals(-15, calculator.add(-10, -5))
    }

    @Test
    fun `덧셈 - 0 더하기`() {
        assertEquals(10, calculator.add(10, 0))
        assertEquals(0, calculator.add(0, 0))
    }

    @Test
    fun `소수 판별 - 경계값 테스트`() {
        // Edge cases
        assertFalse("0은 소수가 아님", calculator.isPrime(0))
        assertFalse("1은 소수가 아님", calculator.isPrime(1))
        assertTrue("2는 소수", calculator.isPrime(2))
        assertTrue("3은 소수", calculator.isPrime(3))

        // 일반 케이스
        assertFalse("4는 소수가 아님", calculator.isPrime(4))
        assertTrue("5는 소수", calculator.isPrime(5))
        assertTrue("7은 소수", calculator.isPrime(7))
        assertFalse("9는 소수가 아님", calculator.isPrime(9))
        assertTrue("11은 소수", calculator.isPrime(11))
    }

    @Test
    fun `팩토리얼 계산`() {
        assertEquals(1L, calculator.factorial(0))
        assertEquals(1L, calculator.factorial(1))
        assertEquals(2L, calculator.factorial(2))
        assertEquals(6L, calculator.factorial(3))
        assertEquals(120L, calculator.factorial(5))
        assertEquals(3628800L, calculator.factorial(10))
    }

    // ========================================================================
    // 특수 어노테이션
    // ========================================================================

    /**
     * @Ignore: 테스트 일시 무시
     * - 미완성 테스트나 임시로 건너뛸 때 사용
     * - 실행 시 스킵됨 (노란색으로 표시)
     */
    @Ignore("아직 구현되지 않은 기능")
    @Test
    fun `미구현 기능 테스트`() {
        fail("이 테스트는 실행되지 않아야 함")
    }

    /**
     * @Test(timeout = ms): 시간 제한
     * - 지정 시간 내에 완료되지 않으면 실패
     */
    @Test(timeout = 1000)  // 1초 제한
    fun `성능 테스트 - 1초 내 완료`() {
        repeat(1000) {
            calculator.isPrime(97)
        }
    }
}

/**
 * ========================================================================
 * 📚 Test Lifecycle 시연 - 실행 순서 확인용
 * ========================================================================
 */
class CounterLifecycleTest {

    companion object {
        private var instanceCount = 0

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            println("\n[BeforeClass] 테스트 클래스 초기화")
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            println("[AfterClass] 테스트 클래스 정리")
            println("생성된 테스트 인스턴스 수: $instanceCount")
        }
    }

    private val counter = Counter()
    private val instanceId = ++instanceCount

    init {
        println("  [init] 테스트 인스턴스 #$instanceId 생성")
    }

    @Before
    fun before() {
        println("  [Before #$instanceId] counter.count = ${counter.count}")
    }

    @After
    fun after() {
        println("  [After #$instanceId] counter.count = ${counter.count}")
    }

    @Test
    fun `테스트1 - counter에 5 추가`() {
        println("    [Test1 #$instanceId] 실행")
        counter.add(5)
        assertEquals(5, counter.count)
    }

    @Test
    fun `테스트2 - counter에 10 추가`() {
        println("    [Test2 #$instanceId] 실행")
        counter.add(10)
        assertEquals(10, counter.count)  // 5가 아닌 10! (각 테스트는 새 인스턴스)
    }

    @Test
    fun `테스트3 - counter 초기값 확인`() {
        println("    [Test3 #$instanceId] 실행")
        assertEquals(0, counter.count)  // 항상 0으로 시작
    }
}

/**
 * ========================================================================
 * 📚 테스트 실행 순서 출력 예시
 * ========================================================================
 *
 * [BeforeClass] 테스트 클래스 초기화
 *   [init] 테스트 인스턴스 #1 생성
 *   [Before #1] counter.count = 0
 *     [Test1 #1] 실행
 *   [After #1] counter.count = 5
 *   [init] 테스트 인스턴스 #2 생성
 *   [Before #2] counter.count = 0
 *     [Test2 #2] 실행
 *   [After #2] counter.count = 10
 *   [init] 테스트 인스턴스 #3 생성
 *   [Before #3] counter.count = 0
 *     [Test3 #3] 실행
 *   [After #3] counter.count = 0
 * [AfterClass] 테스트 클래스 정리
 * 생성된 테스트 인스턴스 수: 3
 *
 * 핵심 포인트:
 * - 각 @Test마다 새로운 테스트 인스턴스가 생성됨
 * - 테스트 간 상태가 공유되지 않음 (격리)
 * - @BeforeClass/@AfterClass는 클래스당 1회만 실행
 */
