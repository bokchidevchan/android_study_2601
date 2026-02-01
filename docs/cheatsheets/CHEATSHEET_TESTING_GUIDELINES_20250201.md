# 테스트 코드 작성 기준 치트시트

> 최근 대화 기반 핵심 정리 | 2025-02-01

---

## 1. 핵심 개념

| 개념 | 설명 | 면접 포인트 |
|------|------|------------|
| **테스트 피라미드** | 70% Unit, 20% Integration, 10% E2E | 빠르고 안정적인 테스트 우선 |
| **ROI (Return on Investment)** | 테스트 작성 비용 vs 버그 방지 효과 | 모든 코드 테스트 ≠ 좋은 테스트 |
| **회귀 테스트** | 기존 기능이 깨지지 않았는지 확인 | 버그 수정 시 테스트 먼저 작성 |
| **테스트 더블** | Mock, Stub, Spy, Fake | 외부 의존성 격리 |

---

## 2. 테스트 작성 판단 기준

### ✅ 반드시 테스트 작성

```kotlin
// 1. 비즈니스 로직이 있는 ViewModel
@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    fun purchase(item: Item) {
        if (balance < item.price) {           // ← 분기 로직
            _state.value = InsufficientBalance
        } else {
            balance -= item.price              // ← 상태 변경
            _state.value = Success
        }
    }
}

// 2. 복잡한 계산/변환 로직
fun calculateDiscount(price: Int, userLevel: Level): Int {
    return when (userLevel) {
        Level.VIP -> (price * 0.8).toInt()
        Level.GOLD -> (price * 0.9).toInt()
        else -> price
    }
}

// 3. Edge cases
@Test
fun `잔액이 0일 때 구매 실패`() { ... }

@Test  
fun `네트워크 에러 시 에러 메시지 표시`() { ... }
```

### ❌ 테스트 스킵 가능

```kotlin
// 1. 단순 getter/setter - 비즈니스 로직 없음
data class User(val name: String, val email: String)

// 2. 단순 위임 - 테스트할 로직 없음
fun save(user: User) = repository.save(user)

// 3. 프레임워크 코드 - Android가 보장
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

// 4. 라이브러리 동작 - 이미 테스트됨
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}
```

---

## 3. ROI 판단 체크리스트

### 테스트 작성 전 자문

| 질문 | YES = 테스트 작성 |
|------|------------------|
| 비즈니스 로직이 있는가? | ✅ |
| 버그 시 심각한 영향? (결제, 인증) | ✅ |
| 자주 변경되는 코드인가? | ✅ |
| 조건 분기가 많은가? | ✅ |

**3개 이상 YES → 테스트 필수**

### ROI 매트릭스

|  | 변경 빈도 높음 | 변경 빈도 낮음 |
|--|---------------|---------------|
| **위험도 높음** | 🔴 필수 | 🟡 권장 |
| **위험도 낮음** | 🟡 권장 | 🟢 선택 |

---

## 4. 테스트 피라미드

```
          /\
         /  \      10% E2E (UI Test)
        /    \     - 느림, 불안정
       /──────\    - 핵심 플로우만
      /        \  
     /          \  20% Integration
    /            \ - 컴포넌트 상호작용
   /──────────────\
  /                \ 70% Unit Test
 /                  \- 빠름, 안정적
/____________________\- 비즈니스 로직
```

| 종류 | 속도 | 위치 | 대상 |
|------|------|------|------|
| Unit | ⚡ 밀리초 | `test/` | ViewModel, UseCase, Utility |
| Integration | 🐢 초 | `androidTest/` | Repository + DB |
| E2E | 🐌 분 | `androidTest/` | 전체 사용자 플로우 |

---

## 5. 비교표: 테스트 대상

| 구분 | 테스트 ✅ | 테스트 ❌ |
|------|----------|----------|
| **ViewModel** | 상태 변경, 분기 로직 | - |
| **Repository** | 캐시 vs 네트워크 선택 | 단순 API 호출 |
| **UseCase** | 비즈니스 규칙 | 단순 위임 |
| **Activity** | - | 프레임워크 코드 |
| **UI** | 동작 (클릭 → 결과) | 레이아웃 세부사항 |

---

## 6. 면접 Q&A

| 질문 | 핵심 답변 |
|------|----------|
| **언제 테스트를 작성하나요?** | 비즈니스 로직, 고위험 영역(결제/인증), 자주 변경되는 코드, 버그 수정 시 |
| **모든 코드에 테스트해야 하나요?** | 아니요. ROI 고려. getter/setter, 프레임워크 코드는 스킵 |
| **테스트 피라미드란?** | 70% Unit, 20% Integration, 10% E2E. 빠르고 안정적인 테스트 우선 |
| **100% 커버리지가 좋은가요?** | 아니요. 커버리지 숫자보다 핵심 로직 테스트 품질이 중요 |
| **Unit vs Integration 차이?** | Unit은 단일 클래스 격리 테스트, Integration은 여러 컴포넌트 상호작용 테스트 |
| **버그 수정 시 테스트는?** | 먼저 실패하는 테스트 작성 → 수정 → 테스트 통과 확인 (TDD) |

---

## 7. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| 모든 코드에 테스트 작성 | ROI 판단 → 핵심 로직만 |
| 구현 세부사항 테스트 | 공개 API/동작만 테스트 |
| 느린 테스트에 의존 | Unit 테스트 70% 유지 |
| 테스트 없이 버그 수정 | 먼저 실패 테스트 작성 |
| UI 레이아웃 테스트 | 동작/상호작용만 테스트 |
| 외부 라이브러리 테스트 | 우리 코드만 테스트 |

---

## 8. 관련 치트시트

- [Hilt vs 수동 DI 비교](./CHEATSHEET_HILT_VS_MANUAL_DI_20250201.md) — 테스트 용이성 관점에서 DI 비교

---

## 9. 요약: 한 문장 정리

```
┌────────────────────────────────────────────────────────────┐
│                                                             │
│  "비즈니스 로직이 있고, 버그 시 영향이 크며,                  │
│   자주 변경되는 코드만 테스트하라"                           │
│                                                             │
│  테스트 = 모든 코드 커버 ❌                                  │
│  테스트 = ROI 높은 핵심 로직 ✅                              │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

---

*Generated from conversation on 2025-02-01*
