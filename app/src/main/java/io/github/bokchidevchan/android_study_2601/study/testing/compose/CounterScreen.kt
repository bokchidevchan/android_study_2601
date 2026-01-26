package io.github.bokchidevchan.android_study_2601.study.testing.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ========================================================================
 * 📚 Compose UI Test - Stateful vs Stateless 분리
 * ========================================================================
 *
 * 테스트하기 좋은 Compose 설계:
 *
 * 1. Stateless Composable: 데이터와 콜백만 받음 (테스트 쉬움)
 * 2. Stateful Composable: 상태를 관리 (ViewModel 연결)
 *
 * 핵심 원칙: "Humble Object Pattern"
 * - UI 로직을 최대한 Stateless로 분리
 * - ViewModel의 상태만 전달받아 렌더링
 */

// ========================================================================
// ViewModel
// ========================================================================

data class CounterUiState(
    val count: Int = 0,
    val message: String = "",
    val isLoading: Boolean = false
)

class CounterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    fun increment() {
        _uiState.value = _uiState.value.copy(
            count = _uiState.value.count + 1,
            message = "증가됨"
        )
    }

    fun decrement() {
        _uiState.value = _uiState.value.copy(
            count = _uiState.value.count - 1,
            message = "감소됨"
        )
    }

    fun reset() {
        _uiState.value = CounterUiState()
    }

    fun setCount(value: Int) {
        _uiState.value = _uiState.value.copy(count = value)
    }
}

// ========================================================================
// Stateful Composable (ViewModel 연결)
// ========================================================================

/**
 * Stateful 버전 - ViewModel을 직접 사용
 *
 * 테스트 시 ViewModel Mock이 필요해서 복잡함
 */
@Composable
fun CounterScreen(
    viewModel: CounterViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CounterContent(
        count = uiState.count,
        message = uiState.message,
        onIncrement = viewModel::increment,
        onDecrement = viewModel::decrement,
        onReset = viewModel::reset
    )
}

// ========================================================================
// Stateless Composable (테스트 용이)
// ========================================================================

/**
 * Stateless 버전 - 데이터와 콜백만 받음
 *
 * 테스트 시 원하는 상태를 직접 주입 가능
 * ViewModel 없이도 테스트 가능
 */
@Composable
fun CounterContent(
    count: Int,
    message: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 카운터 표시
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("counter_card"),  // testTag로 식별
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Counter",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 카운터 값 - contentDescription으로 접근성 제공
                Text(
                    text = count.toString(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (count >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.semantics {
                        contentDescription = "카운터 값: $count"
                    }
                )

                // 메시지 표시
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.testTag("message_text")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onDecrement,
                modifier = Modifier.testTag("decrement_button")
            ) {
                Text("-1")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onReset,
                modifier = Modifier.testTag("reset_button")
            ) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onIncrement,
                modifier = Modifier.testTag("increment_button")
            ) {
                Text("+1")
            }
        }
    }
}

// ========================================================================
// 입력 폼 예제 (테스트 대상)
// ========================================================================

/**
 * 입력 폼 - 텍스트 입력 테스트용
 */
@Composable
fun InputFormContent(
    name: String,
    email: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitEnabled: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("이름") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("name_input"),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("이메일") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input"),
            singleLine = true
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.testTag("error_message")
            )
        }

        Button(
            onClick = onSubmit,
            enabled = isSubmitEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_button")
        ) {
            Text("제출")
        }
    }
}

/**
 * Stateful 입력 폼
 */
@Composable
fun InputFormScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isValidEmail = email.contains("@") && email.contains(".")
    val isSubmitEnabled = name.isNotBlank() && isValidEmail

    InputFormContent(
        name = name,
        email = email,
        onNameChange = { name = it },
        onEmailChange = {
            email = it
            errorMessage = if (!it.contains("@") && it.isNotEmpty()) {
                "올바른 이메일 형식이 아닙니다"
            } else null
        },
        onSubmit = { /* 제출 로직 */ },
        isSubmitEnabled = isSubmitEnabled,
        errorMessage = errorMessage
    )
}

// ========================================================================
// Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
private fun CounterContentPreview() {
    MaterialTheme {
        CounterContent(
            count = 5,
            message = "테스트",
            onIncrement = {},
            onDecrement = {},
            onReset = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InputFormContentPreview() {
    MaterialTheme {
        InputFormContent(
            name = "홍길동",
            email = "hong@test.com",
            onNameChange = {},
            onEmailChange = {},
            onSubmit = {},
            isSubmitEnabled = true,
            errorMessage = null
        )
    }
}
