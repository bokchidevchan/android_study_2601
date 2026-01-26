package io.github.bokchidevchan.android_study_2601.study.hilt.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.PurchaseResult
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ========================================================================
 * 📚 @HiltViewModel - Hilt로 ViewModel 주입
 * ========================================================================
 *
 * @HiltViewModel의 역할:
 * 1. ViewModelComponent에 ViewModel 등록
 * 2. SavedStateHandle 자동 제공 (필요시)
 * 3. hiltViewModel() 컴포저블 함수로 쉽게 획득
 *
 * 내부 동작:
 * - Hilt가 HiltViewModelFactory 생성
 * - ViewModelProvider가 이 팩토리를 사용해 ViewModel 생성
 * - 생성자의 의존성들은 Hilt가 자동 주입
 *
 * ⚠️ 주의:
 * - 반드시 @Inject constructor 사용
 * - Activity/Fragment는 @AndroidEntryPoint 필수
 */
@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val userRepository: UserRepository
    // SavedStateHandle이 필요하면 파라미터에 추가만 하면 됨
    // private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ========================================================================
    // UI State
    // ========================================================================

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
    }

    // ========================================================================
    // Actions
    // ========================================================================

    /**
     * 잔액 조회
     */
    fun loadBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val balance = userRepository.getBalance()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        balance = balance
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "잔액 조회 실패"
                    )
                }
            }
        }
    }

    /**
     * 아이템 구매
     *
     * 이 메서드가 테스트하기 좋은 이유:
     * - userRepository가 인터페이스이므로 Mock/Fake로 교체 가능
     * - 비즈니스 로직만 테스트 (실제 네트워크 불필요)
     */
    fun purchaseItem(itemId: String, price: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, purchaseResult = null) }

            val result = userRepository.purchaseItem(itemId, price)

            result.fold(
                onSuccess = { purchaseResult ->
                    when (purchaseResult) {
                        is PurchaseResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    balance = purchaseResult.remainingBalance,
                                    purchaseResult = "구매 성공! 남은 잔액: ${purchaseResult.remainingBalance}원"
                                )
                            }
                        }
                        is PurchaseResult.InsufficientBalance -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    purchaseResult = "잔액 부족! 필요: ${purchaseResult.required}원, 현재: ${purchaseResult.current}원"
                                )
                            }
                        }
                        is PurchaseResult.ItemNotFound -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "아이템을 찾을 수 없습니다: ${purchaseResult.itemId}"
                                )
                            }
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "구매 실패"
                        )
                    }
                }
            )
        }
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 구매 결과 메시지 클리어
     */
    fun clearPurchaseResult() {
        _uiState.update { it.copy(purchaseResult = null) }
    }
}

/**
 * UI 상태 데이터 클래스
 */
data class PurchaseUiState(
    val isLoading: Boolean = false,
    val balance: Int = 0,
    val error: String? = null,
    val purchaseResult: String? = null
)
