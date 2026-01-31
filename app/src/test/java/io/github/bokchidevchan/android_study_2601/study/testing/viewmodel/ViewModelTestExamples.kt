package io.github.bokchidevchan.android_study_2601.study.testing.viewmodel

import app.cash.turbine.test
import io.github.bokchidevchan.android_study_2601.study.testing.coroutine.Article
import io.github.bokchidevchan.android_study_2601.study.testing.coroutine.ArticleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ========================================================================
 * 📚 ViewModel 테스트 패턴 - 종합 예제
 * ========================================================================
 *
 * ViewModel 테스트의 핵심:
 * 1. Main Dispatcher 교체 (viewModelScope가 Main 사용)
 * 2. Turbine으로 StateFlow 상태 변화 검증
 * 3. Mock Repository로 다양한 시나리오 테스트
 *
 * 테스트 가능한 ViewModel 설계:
 * - 의존성 주입 (Repository, UseCase 등)
 * - Dispatcher 주입 (테스트에서 교체 가능)
 * - sealed class로 상태 정의
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTestExamples {

    // ========================================================================
    // 테스트 환경 설정
    // ========================================================================

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mockk<ArticleRepository>()
    private lateinit var viewModel: TestableArticleViewModel

    @Before
    fun setUp() {
        // Main Dispatcher를 테스트용으로 교체
        // viewModelScope.launch가 Main dispatcher를 사용하기 때문
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // 테스트 후 정리
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========================================================================
    // 1. 기본 상태 테스트
    // ========================================================================

    @Test
    fun `초기_상태는_Idle`() = runTest {
        // Given
        viewModel = TestableArticleViewModel(mockRepository)

        // Then
        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    // ========================================================================
    // 2. 로딩 → 성공 상태 테스트
    // ========================================================================

    @Test
    fun `기사_로딩_성공시_상태_변화_확인`() = runTest {
        // Given
        val articles = listOf(
            Article("1", "제목1", "내용1"),
            Article("2", "제목2", "내용2")
        )
        coEvery { mockRepository.getArticles() } coAnswers {
            delay(100)  // 네트워크 지연 시뮬레이션
            articles
        }

        viewModel = TestableArticleViewModel(mockRepository)

        // When & Then - Turbine으로 상태 변화 순서 검증
        viewModel.uiState.test {
            // 초기 상태
            assertEquals(UiState.Idle, awaitItem())

            // 로딩 시작
            viewModel.loadArticles()

            // Loading 상태
            assertEquals(UiState.Loading, awaitItem())

            // 성공 상태 (advanceUntilIdle로 코루틴 완료 대기)
            advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is UiState.Success)
            assertEquals(2, (successState as UiState.Success).articles.size)

            cancelAndIgnoreRemainingEvents()
        }

        // Repository 호출 검증
        coVerify(exactly = 1) { mockRepository.getArticles() }
    }

    // ========================================================================
    // 3. 에러 상태 테스트
    // ========================================================================

    @Test
    fun `기사_로딩_실패시_에러_상태`() = runTest {
        // Given
        coEvery { mockRepository.getArticles() } throws RuntimeException("네트워크 오류")

        viewModel = TestableArticleViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            assertEquals(UiState.Idle, awaitItem())

            viewModel.loadArticles()

            assertEquals(UiState.Loading, awaitItem())

            advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is UiState.Error)
            assertEquals("네트워크 오류", (errorState as UiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================================================
    // 4. 재시도 로직 테스트
    // ========================================================================

    @Test
    fun `재시도_버튼_클릭시_다시_로딩`() = runTest {
        // Given - 첫 번째 호출 실패, 두 번째 호출 성공
        val articles = listOf(Article("1", "제목", "내용"))

        coEvery { mockRepository.getArticles() } throws RuntimeException("오류") andThen articles

        viewModel = TestableArticleViewModel(mockRepository)

        // When - 첫 번째 시도 (실패)
        viewModel.loadArticles()
        advanceUntilIdle()

        // Then - 에러 상태 확인
        assertTrue(viewModel.uiState.value is UiState.Error)

        // When - 재시도
        viewModel.retry()
        advanceUntilIdle()

        // Then - 성공 상태 확인
        assertTrue(viewModel.uiState.value is UiState.Success)

        // 총 2번 호출됨
        coVerify(exactly = 2) { mockRepository.getArticles() }
    }

    // ========================================================================
    // 5. 빈 결과 테스트
    // ========================================================================

    @Test
    fun `기사가_없으면_Empty_상태`() = runTest {
        // Given
        coEvery { mockRepository.getArticles() } returns emptyList()

        viewModel = TestableArticleViewModel(mockRepository)

        // When
        viewModel.loadArticles()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is UiState.Empty)
    }

    // ========================================================================
    // 6. 새로고침 테스트 (Pull-to-refresh)
    // ========================================================================

    @Test
    fun `새로고침시_기존_데이터_유지하면서_로딩`() = runTest {
        // Given - 첫 로딩
        val initialArticles = listOf(Article("1", "초기", "내용"))
        val refreshedArticles = listOf(
            Article("1", "초기", "내용"),
            Article("2", "새글", "내용")
        )

        coEvery { mockRepository.getArticles() } returns initialArticles andThen refreshedArticles

        viewModel = TestableArticleViewModel(mockRepository)

        // 초기 로딩
        viewModel.loadArticles()
        advanceUntilIdle()

        // When - 새로고침
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as UiState.Success
        assertEquals(2, state.articles.size)
        assertEquals(false, state.isRefreshing)
    }

    // ========================================================================
    // 7. 단일 이벤트 테스트 (Navigation, Toast 등)
    // ========================================================================

    @Test
    fun `기사_클릭시_상세_화면으로_이동_이벤트`() = runTest {
        // Given
        val articles = listOf(Article("1", "제목", "내용"))
        coEvery { mockRepository.getArticles() } returns articles

        viewModel = TestableArticleViewModel(mockRepository)
        viewModel.loadArticles()
        advanceUntilIdle()

        // When
        viewModel.onArticleClick("1")

        // Then - 직접 값 검증 (StateFlow는 마지막 값 유지)
        val event = viewModel.events.value
        assertTrue(event is ViewModelEvent.NavigateToDetail)
        assertEquals("1", (event as ViewModelEvent.NavigateToDetail).articleId)
    }
}

// ========================================================================
// 테스트용 ViewModel (테스트 가능하게 설계)
// ========================================================================

/**
 * 테스트 가능한 ViewModel 설계 예시
 *
 * 핵심:
 * 1. 의존성을 생성자로 주입
 * 2. sealed class로 명확한 상태 정의
 * 3. 단일 이벤트용 Channel/SharedFlow 분리
 */
class TestableArticleViewModel(
    private val repository: ArticleRepository
) {
    // UI 상태
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 단일 이벤트 (Navigation, Toast 등)
    private val _events = MutableStateFlow<ViewModelEvent?>(null)
    val events: StateFlow<ViewModelEvent?> = _events.asStateFlow()

    suspend fun loadArticles() {
        _uiState.value = UiState.Loading

        try {
            val articles = repository.getArticles()
            _uiState.value = if (articles.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(articles)
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun retry() {
        loadArticles()
    }

    suspend fun refresh() {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }

        try {
            val articles = repository.getArticles()
            _uiState.value = UiState.Success(articles, isRefreshing = false)
        } catch (e: Exception) {
            // 새로고침 실패 시 기존 데이터 유지
            if (currentState is UiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = false)
            }
        }
    }

    fun onArticleClick(articleId: String) {
        _events.value = ViewModelEvent.NavigateToDetail(articleId)
    }
}

// ========================================================================
// UI 상태 정의
// ========================================================================

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data object Empty : UiState()
    data class Success(
        val articles: List<Article>,
        val isRefreshing: Boolean = false
    ) : UiState()
    data class Error(val message: String) : UiState()
}

// ========================================================================
// 이벤트 정의 (단일 소비 이벤트)
// ========================================================================

sealed class ViewModelEvent {
    data class NavigateToDetail(val articleId: String) : ViewModelEvent()
    data class ShowToast(val message: String) : ViewModelEvent()
}

/**
 * ========================================================================
 * 📚 ViewModel 테스트 요약
 * ========================================================================
 *
 * 1. 설정
 *    - Dispatchers.setMain(testDispatcher)
 *    - mockk으로 Repository 모킹
 *
 * 2. 상태 검증
 *    - Turbine의 flow.test { awaitItem() } 사용
 *    - advanceUntilIdle()로 코루틴 완료 대기
 *
 * 3. 테스트 시나리오
 *    - 초기 상태
 *    - 로딩 → 성공
 *    - 로딩 → 에러
 *    - 빈 결과
 *    - 재시도
 *    - 새로고침
 *    - 네비게이션 이벤트
 *
 * 4. 테스트 가능한 ViewModel 설계
 *    - 의존성 주입
 *    - sealed class 상태
 *    - 이벤트 분리
 */
