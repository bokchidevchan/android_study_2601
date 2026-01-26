package io.github.bokchidevchan.android_study_2601.study.hilt

import app.cash.turbine.test
import io.github.bokchidevchan.android_study_2601.study.hilt.data.FakeUserRepository
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.PurchaseResult
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.User
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.UserRepository
import io.github.bokchidevchan.android_study_2601.study.hilt.presentation.PurchaseViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ========================================================================
 * PurchaseViewModel Unit Test
 * ========================================================================
 *
 * 이 테스트 파일은 다음을 보여줍니다:
 *
 * 1. Mock - 행위 검증 (호출 여부 체크)
 * 2. Stub - 미리 정해진 응답 반환
 * 3. Spy - 실제 객체 + 부분 모킹
 * 4. Fake - 실제 동작하는 간단한 구현체
 *
 * ========================================================================
 * Mock vs Stub vs Spy vs Fake 비교
 * ========================================================================
 *
 * ┌──────────┬────────────────────────────────────────────────────────────┐
 * │   종류   │                         설명                               │
 * ├──────────┼────────────────────────────────────────────────────────────┤
 * │  Mock    │ 호출 여부/횟수/인자 검증에 초점                            │
 * │          │ "이 메서드가 호출되었는가?"                                │
 * ├──────────┼────────────────────────────────────────────────────────────┤
 * │  Stub    │ 미리 정의된 응답을 반환                                    │
 * │          │ "호출하면 이 값을 반환해라"                                │
 * ├──────────┼────────────────────────────────────────────────────────────┤
 * │  Spy     │ 실제 객체를 감싸서 일부만 모킹                             │
 * │          │ "대부분 실제 동작, 일부만 가짜"                            │
 * ├──────────┼────────────────────────────────────────────────────────────┤
 * │  Fake    │ 실제 동작하는 간단한 구현체                                │
 * │          │ "상태를 가지고 실제처럼 동작"                              │
 * └──────────┴────────────────────────────────────────────────────────────┘
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Main dispatcher를 테스트용으로 교체
        // viewModelScope.launch가 Main dispatcher를 사용하기 때문
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================================================
    // 1. Mock + Stub 사용 예시
    // ========================================================================

    /**
     * Mock을 사용한 테스트 - 호출 여부 검증
     *
     * MockK에서는 mockk<T>()로 Mock 객체를 생성하고
     * coEvery로 suspend 함수의 반환값을 정의합니다 (= Stubbing)
     */
    @Test
    fun `잔액 조회 성공 - Mock과 Stub 사용`() = runTest {
        // Given - Mock 객체 생성 및 Stubbing
        val mockRepository = mockk<UserRepository>()

        // Stub: getBalance() 호출 시 5000 반환
        coEvery { mockRepository.getBalance() } returns 5000

        // When - ViewModel 생성 (init에서 loadBalance 호출)
        val viewModel = PurchaseViewModel(mockRepository)
        advanceUntilIdle() // 모든 코루틴 완료 대기

        // Then - 상태 검증
        assertEquals(5000, viewModel.uiState.value.balance)
        assertEquals(false, viewModel.uiState.value.isLoading)

        // Mock 검증: getBalance가 정확히 1번 호출되었는지 확인
        coVerify(exactly = 1) { mockRepository.getBalance() }
    }

    /**
     * Stub을 사용한 에러 시나리오 테스트
     */
    @Test
    fun `잔액 조회 실패 - 에러 발생 시 에러 상태 업데이트`() = runTest {
        // Given - Mock 객체에 에러 발생 설정
        val mockRepository = mockk<UserRepository>()

        // Stub: getBalance() 호출 시 예외 발생
        coEvery { mockRepository.getBalance() } throws RuntimeException("네트워크 오류")

        // When
        val viewModel = PurchaseViewModel(mockRepository)
        advanceUntilIdle()

        // Then
        assertEquals("네트워크 오류", viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    /**
     * 구매 성공 테스트 - 복잡한 Stub 설정
     */
    @Test
    fun `아이템 구매 성공 - 잔액 차감 확인`() = runTest {
        // Given
        val mockRepository = mockk<UserRepository>()
        coEvery { mockRepository.getBalance() } returns 10000
        coEvery { mockRepository.purchaseItem("item-001", 3000) } returns
                Result.success(PurchaseResult.Success(remainingBalance = 7000))

        val viewModel = PurchaseViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.purchaseItem("item-001", 3000)
        advanceUntilIdle()

        // Then
        assertEquals(7000, viewModel.uiState.value.balance)
        assertTrue(viewModel.uiState.value.purchaseResult?.contains("구매 성공") == true)

        // 검증: purchaseItem이 정확한 파라미터로 호출되었는지
        coVerify { mockRepository.purchaseItem("item-001", 3000) }
    }

    // ========================================================================
    // 2. Spy 사용 예시
    // ========================================================================

    /**
     * Spy 사용 - 실제 객체의 일부만 모킹
     *
     * Spy는 실제 구현체를 감싸서 특정 메서드만 오버라이드합니다.
     * 테스트하고 싶은 부분만 제어하고 나머지는 실제 동작을 사용할 때 유용합니다.
     */
    @Test
    fun `Spy를 사용한 부분 모킹 - getBalance만 모킹`() = runTest {
        // Given - 실제 FakeUserRepository를 Spy로 감싸기
        val realRepository = FakeUserRepository()
        realRepository.fakeBalance = 10000
        val spyRepository = spyk(realRepository)

        // Spy: getBalance만 다른 값 반환하도록 오버라이드
        // 나머지 메서드(purchaseItem 등)는 실제 FakeUserRepository 동작 사용
        coEvery { spyRepository.getBalance() } returns 99999

        val viewModel = PurchaseViewModel(spyRepository)
        advanceUntilIdle()

        // Then - getBalance는 Spy 값 사용
        assertEquals(99999, viewModel.uiState.value.balance)

        // When - purchaseItem은 실제 FakeUserRepository 동작 사용
        viewModel.purchaseItem("item-001", 1000)
        advanceUntilIdle()

        // FakeUserRepository의 실제 동작 확인
        // purchaseItem 호출 후 잔액 감소
        // Spy를 통해 호출되므로 realRepository의 fakeBalance가 변경됨
        assertEquals(9000, spyRepository.fakeBalance)
    }

    // ========================================================================
    // 3. Fake 사용 예시
    // ========================================================================

    /**
     * Fake를 사용한 상태 기반 테스트
     *
     * Fake는 실제 동작하는 간단한 구현체입니다.
     * Mock/Stub처럼 설정이 필요 없이 실제처럼 동작합니다.
     */
    @Test
    fun `Fake를 사용한 테스트 - 실제 동작 시뮬레이션`() = runTest {
        // Given - Fake 객체 생성
        val fakeRepository = FakeUserRepository()
        fakeRepository.fakeBalance = 5000 // 초기 잔액 설정

        val viewModel = PurchaseViewModel(fakeRepository)
        advanceUntilIdle()

        // Then - Fake의 실제 동작으로 잔액 조회
        assertEquals(5000, viewModel.uiState.value.balance)

        // When - 구매 실행
        viewModel.purchaseItem("item-001", 2000)
        advanceUntilIdle()

        // Then - Fake 내부 상태 변경 확인
        assertEquals(3000, fakeRepository.fakeBalance)
        assertEquals(3000, viewModel.uiState.value.balance)
    }

    /**
     * Fake를 사용한 잔액 부족 시나리오
     */
    @Test
    fun `Fake로 잔액 부족 테스트`() = runTest {
        // Given
        val fakeRepository = FakeUserRepository()
        fakeRepository.fakeBalance = 1000 // 적은 잔액

        val viewModel = PurchaseViewModel(fakeRepository)
        advanceUntilIdle()

        // When - 잔액보다 큰 금액 구매 시도
        viewModel.purchaseItem("expensive-item", 5000)
        advanceUntilIdle()

        // Then - 잔액 부족 메시지 확인
        assertTrue(viewModel.uiState.value.purchaseResult?.contains("잔액 부족") == true)
        // 잔액은 변하지 않음
        assertEquals(1000, fakeRepository.fakeBalance)
    }

    /**
     * Fake를 사용한 에러 시나리오 테스트
     */
    @Test
    fun `Fake의 에러 플래그로 실패 테스트`() = runTest {
        // Given - Fake에 에러 발생 설정
        val fakeRepository = FakeUserRepository()
        fakeRepository.shouldFail = true
        fakeRepository.failureException = RuntimeException("서버 점검 중")

        // When
        val viewModel = PurchaseViewModel(fakeRepository)
        advanceUntilIdle()

        // Then
        assertEquals("서버 점검 중", viewModel.uiState.value.error)
    }

    // ========================================================================
    // 4. Turbine을 사용한 Flow 테스트
    // ========================================================================

    /**
     * Turbine을 사용한 StateFlow 테스트
     *
     * Turbine은 Flow를 순차적으로 테스트하기 좋은 라이브러리입니다.
     */
    @Test
    fun `Turbine으로 상태 변화 순서 테스트`() = runTest {
        // Given
        val fakeRepository = FakeUserRepository()
        fakeRepository.fakeBalance = 10000

        val viewModel = PurchaseViewModel(fakeRepository)
        advanceUntilIdle() // init 완료 대기

        // Flow 테스트 시작 - 구매 액션만 테스트
        viewModel.uiState.test {
            // 현재 상태 (로딩 완료 후)
            val current = awaitItem()
            assertEquals(10000, current.balance)
            assertEquals(false, current.isLoading)

            // 구매 실행
            viewModel.purchaseItem("item", 3000)

            // 로딩 중 상태
            val purchasing = awaitItem()
            assertEquals(true, purchasing.isLoading)

            // 구매 완료 상태
            val purchased = awaitItem()
            assertEquals(false, purchased.isLoading)
            assertEquals(7000, purchased.balance)
            assertTrue(purchased.purchaseResult?.contains("구매 성공") == true)

            // 취소 (더 이상 이벤트 없음)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================================================
    // 5. 왜 Mock/Fake가 필요한가?
    // ========================================================================

    /**
     * "그냥 구현하면 되잖아?" 라는 질문에 대한 답
     *
     * 실제 구현체 없이 테스트하는 이유:
     *
     * 1. 속도: 네트워크/DB 호출 없이 빠른 테스트
     *    - 실제 API 호출: 200-500ms
     *    - Mock 테스트: 1-5ms
     *
     * 2. 격리: 외부 의존성과 분리
     *    - 서버 장애가 테스트 실패를 유발하지 않음
     *    - CI/CD에서 안정적인 테스트 실행
     *
     * 3. 제어: 다양한 시나리오 테스트 가능
     *    - 네트워크 에러
     *    - 타임아웃
     *    - 특정 데이터 상태
     *
     * 4. 반복 가능: 동일한 입력 → 동일한 출력
     *    - 실제 서버는 데이터가 변할 수 있음
     *    - Mock은 항상 동일한 응답
     */
    @Test
    fun `이 테스트는 네트워크 없이도 실행됩니다`() = runTest {
        // 실제 네트워크 호출 없이 다양한 시나리오를 빠르게 테스트
        val scenarios = listOf(
            100 to "소액",
            10000 to "일반",
            1000000 to "고액",
            0 to "잔액 없음"
        )

        scenarios.forEach { (balance, description) ->
            val fake = FakeUserRepository()
            fake.fakeBalance = balance

            val viewModel = PurchaseViewModel(fake)
            advanceUntilIdle()

            assertEquals(
                "시나리오 '$description' 실패",
                balance,
                viewModel.uiState.value.balance
            )
        }
    }
}

/**
 * ========================================================================
 * 테스트 더블 선택 가이드
 * ========================================================================
 *
 * 1. Mock을 사용하세요:
 *    - 메서드 호출 여부/횟수를 검증하고 싶을 때
 *    - 특정 파라미터로 호출되었는지 확인하고 싶을 때
 *
 * 2. Stub을 사용하세요:
 *    - 특정 응답값이 필요할 때
 *    - 에러 상황을 시뮬레이션할 때
 *
 * 3. Spy를 사용하세요:
 *    - 실제 객체의 일부 동작만 변경하고 싶을 때
 *    - 레거시 코드와 함께 테스트할 때
 *
 * 4. Fake를 사용하세요:
 *    - 상태 기반 테스트가 필요할 때
 *    - 복잡한 비즈니스 로직을 테스트할 때
 *    - Mock 설정이 너무 복잡할 때
 */
