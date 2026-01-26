package io.github.bokchidevchan.android_study_2601.study.hilt.domain

/**
 * ========================================================================
 * 📚 Domain Layer - Repository 인터페이스
 * ========================================================================
 *
 * 왜 인터페이스로 정의하는가?
 *
 * 1. 의존성 역전 원칙 (DIP)
 *    - 고수준 모듈(ViewModel)이 저수준 모듈(Repository 구현체)에 의존하지 않음
 *    - 둘 다 추상화(인터페이스)에 의존
 *
 * 2. 테스트 용이성
 *    - Mock 객체로 쉽게 교체 가능
 *    - 실제 네트워크/DB 없이 유닛 테스트 가능
 *
 * 3. 유연한 구현체 교체
 *    - ApiUserRepository (네트워크)
 *    - FakeUserRepository (테스트용)
 *    - CachedUserRepository (캐싱 레이어)
 */
interface UserRepository {
    /**
     * 사용자 잔액 조회
     * @return 잔액 (원 단위)
     */
    suspend fun getBalance(): Int

    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    suspend fun getUser(userId: String): User

    /**
     * 아이템 구매
     * @param itemId 아이템 ID
     * @param price 가격
     * @return 구매 성공 여부
     */
    suspend fun purchaseItem(itemId: String, price: Int): Result<PurchaseResult>
}

/**
 * 사용자 정보 데이터 클래스
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val balance: Int
)

/**
 * 구매 결과
 */
sealed class PurchaseResult {
    data class Success(val remainingBalance: Int) : PurchaseResult()
    data class InsufficientBalance(val required: Int, val current: Int) : PurchaseResult()
    data class ItemNotFound(val itemId: String) : PurchaseResult()
}
