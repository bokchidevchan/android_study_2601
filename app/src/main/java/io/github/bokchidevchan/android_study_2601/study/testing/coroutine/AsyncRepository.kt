package io.github.bokchidevchan.android_study_2601.study.testing.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * ========================================================================
 * 📚 Coroutine Test - 테스트 대상 클래스들
 * ========================================================================
 */

data class Article(
    val id: String,
    val title: String,
    val content: String
)

/**
 * 기사 저장소 인터페이스
 */
interface ArticleRepository {
    suspend fun getArticle(id: String): Article?
    suspend fun getArticles(): List<Article>
    suspend fun saveArticle(article: Article): Article
    fun observeArticles(): Flow<List<Article>>
}

/**
 * 네트워크 서비스 인터페이스
 */
interface NetworkService {
    suspend fun fetchData(): String
    suspend fun fetchWithDelay(delayMs: Long): String
}

/**
 * 기사 ViewModel (테스트 대상)
 */
class ArticleViewModel(
    private val repository: ArticleRepository
) {
    sealed class UiState {
        data object Loading : UiState()
        data class Success(val articles: List<Article>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private var _state: UiState = UiState.Loading
    val state: UiState get() = _state

    suspend fun loadArticles() {
        _state = UiState.Loading
        try {
            val articles = repository.getArticles()
            _state = UiState.Success(articles)
        } catch (e: Exception) {
            _state = UiState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun loadArticle(id: String): Article? {
        return repository.getArticle(id)
    }
}

/**
 * Flow를 반환하는 UseCase
 */
class ObserveArticlesUseCase(
    private val repository: ArticleRepository
) {
    operator fun invoke(): Flow<List<Article>> {
        return repository.observeArticles()
    }
}

/**
 * 지연이 있는 서비스 (delay 테스트용)
 */
class DelayedService(
    private val networkService: NetworkService
) {
    suspend fun fetchDataWithRetry(maxRetries: Int = 3): String {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                delay(1000L * (attempt + 1))  // 점진적 대기
                return networkService.fetchData()
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: RuntimeException("Unknown error")
    }

    suspend fun fetchWithTimeout(timeoutMs: Long): String {
        return networkService.fetchWithDelay(timeoutMs)
    }
}

/**
 * Flow를 발행하는 클래스
 */
class ArticleFlowProducer {

    /**
     * 일정 간격으로 기사 발행
     */
    fun produceArticles(count: Int, intervalMs: Long = 100): Flow<Article> = flow {
        repeat(count) { index ->
            delay(intervalMs)
            emit(Article(
                id = "article_$index",
                title = "제목 $index",
                content = "내용 $index"
            ))
        }
    }

    /**
     * 에러를 발생시키는 Flow
     */
    fun produceWithError(errorAtIndex: Int): Flow<Article> = flow {
        repeat(5) { index ->
            if (index == errorAtIndex) {
                throw RuntimeException("Error at index $errorAtIndex")
            }
            delay(100)
            emit(Article("$index", "Title $index", "Content"))
        }
    }
}
