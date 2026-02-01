package io.github.bokchidevchan.android_study_2601.study.testing.coroutine

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ========================================================================
 * 📚 Coroutine Test - runTest, TestDispatcher, Turbine
 * ========================================================================
 *
 * 코루틴 테스트의 핵심:
 * 1. Virtual Time: delay()를 실제로 기다리지 않음
 * 2. TestDispatcher: 코루틴 실행을 제어
 * 3. Turbine: Flow 테스트를 쉽게
 *
 * 주요 컴포넌트:
 * - runTest: 코루틴 테스트 스코프 제공
 * - StandardTestDispatcher: 명시적 실행 제어
 * - UnconfinedTestDispatcher: 즉시 실행
 * - Turbine: Flow assertion 라이브러리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestExamples {

    // ========================================================================
    // 1. runTest 기본
    // ========================================================================

    @Test
    fun `runTest - 기본 사용법`() = runTest {
        // runTest 내부에서는 suspend 함수 직접 호출 가능
        val result = suspendFunction()
        assertEquals("Hello", result)
    }

    private suspend fun suspendFunction(): String {
        delay(1000)  // 실제로 기다리지 않음 (Virtual Time)
        return "Hello"
    }

    @Test
    fun `runTest - delay가 실제로 기다리지 않음`() = runTest {
        val startTime = currentTime  // Virtual Time

        delay(10_000)  // 10초

        val elapsed = currentTime - startTime
        assertEquals(10_000, elapsed)  // Virtual Time이 10초 흘렀지만
        // 실제 실행 시간은 거의 0
    }

    // ========================================================================
    // 2. StandardTestDispatcher vs UnconfinedTestDispatcher
    // ========================================================================

    @Test
    fun `StandardTestDispatcher - 명시적 실행 제어`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var result = ""

        // launch는 즉시 실행되지 않음
        launch(dispatcher) {
            result = "executed"
        }

        assertEquals("", result)  // 아직 실행 안 됨

        advanceUntilIdle()  // 모든 대기 중인 코루틴 실행

        assertEquals("executed", result)  // 이제 실행됨
    }

    @Test
    fun `UnconfinedTestDispatcher - 즉시 실행`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var result = ""

        launch(dispatcher) {
            result = "executed"
        }

        assertEquals("executed", result)  // 즉시 실행됨
    }

    @Test
    fun `advanceTimeBy - 특정 시간만큼 진행`() = runTest {
        var step = 0

        launch {
            delay(1000)
            step = 1
            delay(1000)
            step = 2
            delay(1000)
            step = 3
        }

        assertEquals(0, step)

        advanceTimeBy(500)
        assertEquals(0, step)  // 아직 1000ms 안 됨

        advanceTimeBy(600)  // 총 1100ms
        assertEquals(1, step)

        advanceTimeBy(1000)  // 총 2100ms
        assertEquals(2, step)

        advanceUntilIdle()
        assertEquals(3, step)
    }

    // ========================================================================
    // 3. Main Dispatcher 교체
    // ========================================================================

    @Test
    fun `setMain - Main Dispatcher 교체`() = runTest {
        // viewModelScope.launch는 Main dispatcher 사용
        // 테스트에서는 Main이 없으므로 교체 필요

        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        try {
            val viewModel = ArticleViewModel(mockk(relaxed = true))

            // viewModel 내부에서 Dispatchers.Main 사용해도 테스트 가능
            coEvery { viewModel.loadArticles() } returns Unit

        } finally {
            Dispatchers.resetMain()  // 항상 복원
        }
    }

    // ========================================================================
    // 4. 실제 ViewModel 테스트
    // ========================================================================

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModel 로딩 상태 테스트`() = runTest {
        // Arrange
        val mockRepository = mockk<ArticleRepository>()
        val articles = listOf(
            Article("1", "제목1", "내용1"),
            Article("2", "제목2", "내용2")
        )
        coEvery { mockRepository.getArticles() } coAnswers {
            delay(500)  // 네트워크 지연 시뮬레이션
            articles
        }

        val viewModel = ArticleViewModel(mockRepository)

        // Act & Assert
        assertEquals(ArticleViewModel.UiState.Loading, viewModel.state)

        // loadArticles 호출
        launch { viewModel.loadArticles() }

        // 아직 로딩 중
        advanceTimeBy(100)
        assertEquals(ArticleViewModel.UiState.Loading, viewModel.state)

        // 완료 후
        advanceUntilIdle()
        val state = viewModel.state as ArticleViewModel.UiState.Success
        assertEquals(2, state.articles.size)
    }

    @Test
    fun `ViewModel 에러 상태 테스트`() = runTest {
        // Arrange
        val mockRepository = mockk<ArticleRepository>()
        coEvery { mockRepository.getArticles() } throws RuntimeException("네트워크 오류")

        val viewModel = ArticleViewModel(mockRepository)

        // Act
        viewModel.loadArticles()

        // Assert
        val state = viewModel.state as ArticleViewModel.UiState.Error
        assertEquals("네트워크 오류", state.message)
    }

    // ========================================================================
    // 5. Turbine - Flow 테스트
    // ========================================================================

    @Test
    fun `Turbine - 기본 Flow 테스트`() = runTest {
        val flow = flowOf(1, 2, 3)

        flow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()  // Flow 완료 확인
        }
    }

    @Test
    fun `Turbine - delay가 있는 Flow`() = runTest {
        val flow = flow {
            emit(1)
            delay(1000)
            emit(2)
            delay(1000)
            emit(3)
        }

        flow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())  // Virtual Time이라 즉시
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Turbine - ArticleFlowProducer 테스트`() = runTest {
        val producer = ArticleFlowProducer()

        producer.produceArticles(count = 3, intervalMs = 100).test {
            val first = awaitItem()
            assertEquals("article_0", first.id)

            val second = awaitItem()
            assertEquals("article_1", second.id)

            val third = awaitItem()
            assertEquals("article_2", third.id)

            awaitComplete()
        }
    }

    @Test
    fun `Turbine - 에러 Flow 테스트`() = runTest {
        val producer = ArticleFlowProducer()

        producer.produceWithError(errorAtIndex = 2).test {
            awaitItem()  // index 0
            awaitItem()  // index 1

            val error = awaitError()  // index 2에서 에러
            assertTrue(error is RuntimeException)
            assertEquals("Error at index 2", error.message)
        }
    }

    @Test
    fun `Turbine - expectNoEvents로 이벤트 없음 확인`() = runTest {
        val flow = flow {
            delay(1000)
            emit(1)
        }

        flow.test {
            expectNoEvents()  // 아직 이벤트 없음 확인

            // Virtual Time 진행
            advanceTimeBy(1000)

            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Turbine - cancelAndIgnoreRemainingEvents로 조기 종료`() = runTest {
        val infiniteFlow = flow {
            var i = 0
            while (true) {
                emit(i++)
                delay(100)
            }
        }

        infiniteFlow.test {
            assertEquals(0, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())

            cancelAndIgnoreRemainingEvents()  // 나머지 무시하고 종료
        }
    }

    @Test
    fun `Turbine - turbineScope로 여러 Flow 동시 테스트`() = runTest {
        val flow1 = flowOf("a", "b", "c")
        val flow2 = flowOf(1, 2, 3)

        turbineScope {
            val turbine1 = flow1.testIn(backgroundScope)
            val turbine2 = flow2.testIn(backgroundScope)

            assertEquals("a", turbine1.awaitItem())
            assertEquals(1, turbine2.awaitItem())

            assertEquals("b", turbine1.awaitItem())
            assertEquals(2, turbine2.awaitItem())

            turbine1.cancelAndIgnoreRemainingEvents()
            turbine2.cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================================================
    // 6. Repository Flow 테스트
    // ========================================================================

    @Test
    fun `Repository Flow를 UseCase로 테스트`() = runTest {
        // Arrange
        val mockRepository = mockk<ArticleRepository>()
        val articlesFlow = flow {
            emit(listOf(Article("1", "첫번째", "내용1")))
            delay(500)
            emit(listOf(
                Article("1", "첫번째", "내용1"),
                Article("2", "두번째", "내용2")
            ))
        }
        coEvery { mockRepository.observeArticles() } returns articlesFlow

        val useCase = ObserveArticlesUseCase(mockRepository)

        // Act & Assert
        useCase().test {
            val first = awaitItem()
            assertEquals(1, first.size)

            val second = awaitItem()
            assertEquals(2, second.size)

            awaitComplete()
        }
    }

    // ========================================================================
    // 7. coVerify - suspend 함수 호출 검증
    // ========================================================================

    @Test
    fun `coVerify - suspend 함수 호출 검증`() = runTest {
        val mockRepository = mockk<ArticleRepository>()
        coEvery { mockRepository.getArticle(any()) } returns Article("1", "테스트", "내용")

        val viewModel = ArticleViewModel(mockRepository)
        viewModel.loadArticle("1")

        coVerify { mockRepository.getArticle("1") }
    }
}

/**
 * ========================================================================
 * 📚 Coroutine Test 요약
 * ========================================================================
 *
 * 1. runTest
 *    - 코루틴 테스트의 기본 빌더
 *    - Virtual Time 사용 (delay가 실제로 기다리지 않음)
 *    - currentTime으로 경과 시간 확인
 *
 * 2. TestDispatcher
 *    - StandardTestDispatcher: 명시적 실행 제어 (advanceUntilIdle 필요)
 *    - UnconfinedTestDispatcher: 즉시 실행
 *    - setMain/resetMain으로 Main Dispatcher 교체
 *
 * 3. 시간 제어
 *    - advanceTimeBy(ms): 특정 시간만큼 진행
 *    - advanceUntilIdle(): 모든 대기 작업 완료
 *    - runCurrent(): 현재 시점의 작업만 실행
 *
 * 4. Turbine
 *    - flow.test { ... }: Flow 테스트 시작
 *    - awaitItem(): 다음 이벤트 대기
 *    - awaitComplete(): 완료 대기
 *    - awaitError(): 에러 대기
 *    - expectNoEvents(): 이벤트 없음 확인
 *    - cancelAndIgnoreRemainingEvents(): 조기 종료
 */
