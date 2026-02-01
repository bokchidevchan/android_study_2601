package io.github.bokchidevchan.android_study_2601.study.testing.tdd

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ========================================================================
 * 📚 TDD 실습 - EmailValidator 테스트
 * ========================================================================
 *
 * TDD (Test-Driven Development) 사이클:
 *
 * 1. Red: 실패하는 테스트 작성
 * 2. Green: 테스트를 통과하는 최소한의 코드 작성
 * 3. Refactor: 코드 정리 (테스트는 계속 통과)
 *
 * 이 테스트는 TDD 사이클을 보여주는 예제입니다.
 * 각 테스트는 EmailValidator의 기능을 하나씩 검증합니다.
 */
class EmailValidatorTest {

    private lateinit var validator: EmailValidator

    @Before
    fun setUp() {
        validator = EmailValidator()
    }

    // ========================================================================
    // Step 1: 빈 문자열 검사 (Red → Green → Refactor)
    // ========================================================================

    @Test
    fun `빈_이메일은_Invalid`() {
        // Given
        val email = ""

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertTrue(result is EmailValidationResult.Invalid)
        assertEquals("이메일을 입력해주세요", (result as EmailValidationResult.Invalid).reason)
    }

    @Test
    fun `공백만_있는_이메일은_Invalid`() {
        // Given
        val email = "   "

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
    }

    // ========================================================================
    // Step 2: @ 기호 검사 (Red → Green → Refactor)
    // ========================================================================

    @Test
    fun `골뱅이가_없으면_Invalid`() {
        // Given
        val email = "testexample.com"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("@를 포함해야 합니다", (result as EmailValidationResult.Invalid).reason)
    }

    @Test
    fun `골뱅이가_여러개면_Invalid`() {
        // Given
        val email = "test@@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("@는 하나만 포함해야 합니다", (result as EmailValidationResult.Invalid).reason)
    }

    // ========================================================================
    // Step 3: 로컬 파트 검사 (Red → Green → Refactor)
    // ========================================================================

    @Test
    fun `골뱅이_앞에_문자가_없으면_Invalid`() {
        // Given
        val email = "@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("@앞에 문자가 필요합니다", (result as EmailValidationResult.Invalid).reason)
    }

    // ========================================================================
    // Step 4: 도메인 파트 검사 (Red → Green → Refactor)
    // ========================================================================

    @Test
    fun `도메인이_없으면_Invalid`() {
        // Given
        val email = "test@"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("도메인을 입력해주세요", (result as EmailValidationResult.Invalid).reason)
    }

    @Test
    fun `도메인에_점이_없으면_Invalid`() {
        // Given
        val email = "test@examplecom"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("올바른 도메인 형식이 아닙니다", (result as EmailValidationResult.Invalid).reason)
    }

    @Test
    fun `TLD가_너무_짧으면_Invalid`() {
        // Given
        val email = "test@example.c"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("올바른 도메인 형식이 아닙니다", (result as EmailValidationResult.Invalid).reason)
    }

    // ========================================================================
    // Step 5: 특수 문자 검사 (Red → Green → Refactor)
    // ========================================================================

    @Test
    fun `로컬파트에_허용되지_않는_문자가_있으면_Invalid`() {
        // Given
        val email = "test!#\$@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertFalse(result.isValid)
        assertEquals("허용되지 않는 문자가 포함되어 있습니다", (result as EmailValidationResult.Invalid).reason)
    }

    // ========================================================================
    // 유효한 이메일 테스트
    // ========================================================================

    @Test
    fun `올바른_이메일은_Valid`() {
        // Given
        val email = "test@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertTrue(result.isValid)
        assertTrue(result is EmailValidationResult.Valid)
    }

    @Test
    fun `점이_포함된_로컬파트도_Valid`() {
        // Given
        val email = "test.user@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertTrue(result.isValid)
    }

    @Test
    fun `플러스가_포함된_로컬파트도_Valid`() {
        // Given
        val email = "test+tag@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertTrue(result.isValid)
    }

    @Test
    fun `하이픈이_포함된_로컬파트도_Valid`() {
        // Given
        val email = "test-user@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertTrue(result.isValid)
    }

    @Test
    fun `숫자가_포함된_로컬파트도_Valid`() {
        // Given
        val email = "test123@example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertTrue(result.isValid)
    }

    @Test
    fun `서브도메인이_있는_이메일도_Valid`() {
        // Given
        val email = "test@mail.example.com"

        // When
        val result = validator.validate(email)

        // Then
        assertTrue(result.isValid)
    }

    // ========================================================================
    // isValid 헬퍼 메서드 테스트
    // ========================================================================

    @Test
    fun `isValid_메서드_테스트`() {
        assertTrue(validator.isValid("test@example.com"))
        assertFalse(validator.isValid(""))
        assertFalse(validator.isValid("invalid"))
    }
}

/**
 * ========================================================================
 * 📚 PasswordValidator TDD 테스트
 * ========================================================================
 */
class PasswordValidatorTest {

    private lateinit var validator: PasswordValidator

    @Before
    fun setUp() {
        validator = PasswordValidator()
    }

    @Test
    fun `8자_미만이면_에러`() {
        val result = validator.validate("Abc1!")

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("8자 이상이어야 합니다"))
    }

    @Test
    fun `대문자가_없으면_에러`() {
        val result = validator.validate("abcdefgh1!")

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("대문자를 포함해야 합니다"))
    }

    @Test
    fun `소문자가_없으면_에러`() {
        val result = validator.validate("ABCDEFGH1!")

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("소문자를 포함해야 합니다"))
    }

    @Test
    fun `숫자가_없으면_에러`() {
        val result = validator.validate("Abcdefgh!")

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("숫자를 포함해야 합니다"))
    }

    @Test
    fun `특수문자가_없으면_에러`() {
        val result = validator.validate("Abcdefgh1")

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("특수문자를 포함해야 합니다"))
    }

    @Test
    fun `모든_조건을_만족하면_Valid`() {
        val result = validator.validate("Abcdefgh1!")

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `여러_조건_위반시_모든_에러_반환`() {
        val result = validator.validate("abc")

        assertFalse(result.isValid)
        assertEquals(4, result.errors.size)  // 길이, 대문자, 숫자, 특수문자
    }
}

/**
 * ========================================================================
 * 📚 TDD 개발 순서 정리
 * ========================================================================
 *
 * 1. 테스트 먼저 작성 (Red)
 *    - 실패하는 테스트를 작성한다
 *    - 컴파일조차 안 될 수 있다 (클래스/메서드가 없으므로)
 *
 * 2. 최소한의 코드로 통과 (Green)
 *    - 테스트를 통과하는 가장 간단한 코드를 작성
 *    - 하드코딩도 괜찮다
 *
 * 3. 리팩토링 (Refactor)
 *    - 중복 제거
 *    - 이름 개선
 *    - 구조 정리
 *    - 테스트는 계속 통과해야 함
 *
 * 4. 반복
 *    - 다음 요구사항에 대해 1-3 반복
 *
 * TDD의 장점:
 * - 요구사항이 테스트로 명확히 문서화됨
 * - 회귀 테스트가 자동으로 생김
 * - 테스트 가능한 설계가 자연스럽게 됨
 * - 리팩토링 시 안전망 역할
 *
 * TDD의 한계:
 * - 초기 개발 속도가 느릴 수 있음
 * - UI/외부 시스템 테스트는 어려움
 * - 요구사항이 불명확하면 테스트 작성이 어려움
 * - 100% TDD는 현실적으로 어려움 (균형 필요)
 */
