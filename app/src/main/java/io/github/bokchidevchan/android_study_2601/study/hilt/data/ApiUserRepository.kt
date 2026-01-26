package io.github.bokchidevchan.android_study_2601.study.hilt.data

import io.github.bokchidevchan.android_study_2601.study.hilt.domain.PurchaseResult
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.User
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.UserRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ========================================================================
 * 📚 Data Layer - Repository 구현체
 * ========================================================================
 *
 * @Inject constructor의 의미:
 * - Hilt에게 "이 클래스의 인스턴스를 만들 때 이 생성자를 사용해라"고 알림
 * - 생성자의 파라미터들도 Hilt가 자동으로 주입
 *
 * 실제 앱에서는:
 * - Retrofit API 인터페이스를 주입받아 네트워크 호출
 * - Room DAO를 주입받아 로컬 캐싱
 */
class ApiUserRepository @Inject constructor(
    // 실제 앱에서는 UserApi를 주입받음
    // private val userApi: UserApi
) : UserRepository {

    // 학습용 더미 데이터
    private var currentBalance = 10000

    override suspend fun getBalance(): Int {
        // 실제로는: return userApi.fetchBalance()
        delay(500) // 네트워크 지연 시뮬레이션
        return currentBalance
    }

    override suspend fun getUser(userId: String): User {
        // 실제로는: return userApi.getUser(userId)
        delay(300)
        return User(
            id = userId,
            name = "홍길동",
            email = "hong@example.com",
            balance = currentBalance
        )
    }

    override suspend fun purchaseItem(itemId: String, price: Int): Result<PurchaseResult> {
        delay(500)

        return try {
            if (currentBalance >= price) {
                currentBalance -= price
                Result.success(PurchaseResult.Success(currentBalance))
            } else {
                Result.success(
                    PurchaseResult.InsufficientBalance(
                        required = price,
                        current = currentBalance
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * ========================================================================
 * 📚 테스트용 Fake Repository
 * ========================================================================
 *
 * 왜 Fake를 쓰는가?
 *
 * Mock vs Fake:
 * - Mock: 호출 여부/횟수 검증에 초점 (Mockito, MockK)
 * - Fake: 실제 동작하는 간단한 구현체 (테스트 전용)
 *
 * Fake의 장점:
 * - Mock 설정(stubbing) 없이 자연스러운 테스트
 * - 상태 기반 테스트에 적합
 * - 실제 동작과 유사한 시나리오 테스트 가능
 */
class FakeUserRepository : UserRepository {

    // 테스트에서 직접 조작 가능한 상태
    var fakeBalance = 10000
    var shouldFail = false
    var failureException: Exception = RuntimeException("Fake error")

    override suspend fun getBalance(): Int {
        if (shouldFail) throw failureException
        return fakeBalance
    }

    override suspend fun getUser(userId: String): User {
        if (shouldFail) throw failureException
        return User(
            id = userId,
            name = "Test User",
            email = "test@example.com",
            balance = fakeBalance
        )
    }

    override suspend fun purchaseItem(itemId: String, price: Int): Result<PurchaseResult> {
        if (shouldFail) return Result.failure(failureException)

        return if (fakeBalance >= price) {
            fakeBalance -= price
            Result.success(PurchaseResult.Success(fakeBalance))
        } else {
            Result.success(
                PurchaseResult.InsufficientBalance(
                    required = price,
                    current = fakeBalance
                )
            )
        }
    }
}
