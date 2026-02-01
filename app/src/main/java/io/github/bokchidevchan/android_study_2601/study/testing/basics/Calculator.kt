package io.github.bokchidevchan.android_study_2601.study.testing.basics

/**
 * ========================================================================
 * 📚 Unit Test 기본 - 테스트 대상 클래스
 * ========================================================================
 *
 * 테스트하기 좋은 코드의 특징:
 * 1. 순수 함수 (Pure Function): 같은 입력 → 같은 출력
 * 2. 외부 의존성 없음: Context, 네트워크, DB 등에 의존하지 않음
 * 3. 명확한 책임: 하나의 클래스는 하나의 역할만
 */
class Calculator {

    /**
     * 덧셈
     */
    fun add(a: Int, b: Int): Int = a + b

    /**
     * 뺄셈
     */
    fun subtract(a: Int, b: Int): Int = a - b

    /**
     * 곱셈
     */
    fun multiply(a: Int, b: Int): Int = a * b

    /**
     * 나눗셈
     * @throws IllegalArgumentException 0으로 나눌 때
     */
    fun divide(a: Int, b: Int): Int {
        if (b == 0) {
            throw IllegalArgumentException("0으로 나눌 수 없습니다")
        }
        return a / b
    }

    /**
     * 나머지
     */
    fun modulo(a: Int, b: Int): Int {
        if (b == 0) {
            throw IllegalArgumentException("0으로 나눌 수 없습니다")
        }
        return a % b
    }

    /**
     * 짝수 판별
     */
    fun isEven(n: Int): Boolean = n % 2 == 0

    /**
     * 소수 판별
     */
    fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0 || n % 3 == 0) return false

        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false
            i += 6
        }
        return true
    }

    /**
     * 팩토리얼
     */
    fun factorial(n: Int): Long {
        require(n >= 0) { "음수의 팩토리얼은 정의되지 않습니다" }
        return if (n <= 1) 1L else n * factorial(n - 1)
    }
}

/**
 * ========================================================================
 * 📚 테스트 Lifecycle 이해를 위한 클래스
 * ========================================================================
 *
 * 테스트가 실행될 때 객체의 상태 변화를 관찰합니다.
 */
class Counter {
    var count: Int = 0
        private set

    fun increment() {
        count++
    }

    fun decrement() {
        count--
    }

    fun reset() {
        count = 0
    }

    fun add(value: Int) {
        count += value
    }
}
