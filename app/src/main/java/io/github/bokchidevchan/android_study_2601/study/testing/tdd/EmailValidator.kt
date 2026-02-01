package io.github.bokchidevchan.android_study_2601.study.testing.tdd

/**
 * ========================================================================
 * 📚 TDD 실습 - Email Validator
 * ========================================================================
 *
 * TDD (Test-Driven Development) 사이클:
 *
 * 1. Red: 실패하는 테스트 작성
 * 2. Green: 테스트를 통과하는 최소한의 코드 작성
 * 3. Refactor: 코드 정리 (테스트는 계속 통과)
 *
 * 이 클래스는 TDD로 개발되었습니다.
 * 테스트 코드(EmailValidatorTest.kt)를 먼저 확인하세요.
 */

/**
 * 이메일 유효성 검사 결과
 */
sealed class EmailValidationResult {
    data object Valid : EmailValidationResult()
    data class Invalid(val reason: String) : EmailValidationResult()

    val isValid: Boolean get() = this is Valid
}

/**
 * 이메일 유효성 검사기
 *
 * TDD 개발 순서:
 * 1. 빈 문자열 검사 (Red → Green → Refactor)
 * 2. @ 기호 검사 (Red → Green → Refactor)
 * 3. 도메인 검사 (Red → Green → Refactor)
 * 4. 로컬 파트 검사 (Red → Green → Refactor)
 * 5. 특수 케이스 (Red → Green → Refactor)
 */
class EmailValidator {

    /**
     * 이메일 주소 유효성 검사
     *
     * @param email 검사할 이메일 주소
     * @return 검사 결과 (Valid 또는 Invalid with reason)
     */
    fun validate(email: String): EmailValidationResult {
        // 1단계: 빈 문자열 검사
        if (email.isBlank()) {
            return EmailValidationResult.Invalid("이메일을 입력해주세요")
        }

        // 2단계: @ 기호 검사
        if (!email.contains("@")) {
            return EmailValidationResult.Invalid("@를 포함해야 합니다")
        }

        // 여러 개의 @ 검사
        if (email.count { it == '@' } > 1) {
            return EmailValidationResult.Invalid("@는 하나만 포함해야 합니다")
        }

        val parts = email.split("@")
        val localPart = parts[0]
        val domainPart = parts[1]

        // 3단계: 로컬 파트 검사
        if (localPart.isEmpty()) {
            return EmailValidationResult.Invalid("@앞에 문자가 필요합니다")
        }

        // 4단계: 도메인 파트 검사
        if (domainPart.isEmpty()) {
            return EmailValidationResult.Invalid("도메인을 입력해주세요")
        }

        if (!domainPart.contains(".")) {
            return EmailValidationResult.Invalid("올바른 도메인 형식이 아닙니다")
        }

        // 도메인 TLD 검사
        val tld = domainPart.substringAfterLast(".")
        if (tld.isEmpty() || tld.length < 2) {
            return EmailValidationResult.Invalid("올바른 도메인 형식이 아닙니다")
        }

        // 5단계: 허용되지 않는 문자 검사
        val validLocalPartRegex = Regex("^[a-zA-Z0-9._%+-]+$")
        if (!validLocalPartRegex.matches(localPart)) {
            return EmailValidationResult.Invalid("허용되지 않는 문자가 포함되어 있습니다")
        }

        return EmailValidationResult.Valid
    }

    /**
     * 간단한 유효성 검사 (Boolean 반환)
     */
    fun isValid(email: String): Boolean = validate(email).isValid
}

/**
 * ========================================================================
 * 📚 TDD 개발 과정 예시
 * ========================================================================
 *
 * Step 1: Red (실패하는 테스트 작성)
 * ```kotlin
 * @Test
 * fun `빈_이메일은_Invalid`() {
 *     val result = validator.validate("")
 *     assertFalse(result.isValid)
 * }
 * ```
 * → 테스트 실행 → 실패 (validate 함수가 없음)
 *
 * Step 2: Green (최소한의 코드로 통과)
 * ```kotlin
 * fun validate(email: String): EmailValidationResult {
 *     return EmailValidationResult.Invalid("이메일을 입력해주세요")
 * }
 * ```
 * → 테스트 실행 → 통과!
 *
 * Step 3: Refactor (코드 정리)
 * ```kotlin
 * fun validate(email: String): EmailValidationResult {
 *     if (email.isBlank()) {
 *         return EmailValidationResult.Invalid("이메일을 입력해주세요")
 *     }
 *     return EmailValidationResult.Valid
 * }
 * ```
 *
 * 다음 기능으로 넘어가서 반복...
 */

/**
 * 패스워드 검증기 - TDD 연습용
 */
class PasswordValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * 패스워드 유효성 검사
     *
     * 규칙:
     * - 최소 8자 이상
     * - 대문자 포함
     * - 소문자 포함
     * - 숫자 포함
     * - 특수문자 포함
     */
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < 8) {
            errors.add("8자 이상이어야 합니다")
        }

        if (!password.any { it.isUpperCase() }) {
            errors.add("대문자를 포함해야 합니다")
        }

        if (!password.any { it.isLowerCase() }) {
            errors.add("소문자를 포함해야 합니다")
        }

        if (!password.any { it.isDigit() }) {
            errors.add("숫자를 포함해야 합니다")
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("특수문자를 포함해야 합니다")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
