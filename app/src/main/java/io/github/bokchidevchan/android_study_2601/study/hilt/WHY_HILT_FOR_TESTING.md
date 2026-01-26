# 왜 Hilt가 테스트에 필요한가?

## "그냥 생성자에 Fake 넣으면 되잖아?"

정확한 지적입니다. **Unit Test에서는 Hilt가 필요 없습니다.**

```kotlin
// Unit Test - Hilt 필요 없음
val viewModel = PurchaseViewModel(FakeUserRepository())
```

## 그렇다면 Hilt는 왜 쓰는가?

### 1. Unit Test vs Integration Test

| 구분 | Unit Test | Integration Test |
|------|-----------|------------------|
| 위치 | `test/` 폴더 | `androidTest/` 폴더 |
| 테스트 대상 | 클래스 하나 | Activity → ViewModel → Repository 전체 |
| 객체 생성 | 개발자가 직접 | **시스템이 생성** |
| Hilt 필요 | ❌ 불필요 | ✅ 필수 |

### 2. 핵심 문제: "누가 객체를 만드는가?"

```kotlin
// Unit Test에서는 개발자가 직접 생성
val viewModel = PurchaseViewModel(FakeUserRepository())

// Integration Test에서는...
launchActivity<MainActivity>()  // 시스템이 Activity 생성
// → MainActivity 내부의 ViewModel은?
// → ViewModel이 사용하는 Repository는?
// → 우리가 직접 주입할 방법이 없음!
```

### 3. Hilt의 해결책

```kotlin
@HiltAndroidTest
class IntegrationTest {

    // 이 Fake가 앱 전체에 자동 주입됨!
    @BindValue
    val fakeRepo: UserRepository = FakeUserRepository()

    @Test
    fun 전체_앱_흐름_테스트() {
        // Activity 실행 → ViewModel → fakeRepo 사용
        launchActivity<MainActivity>()

        // 실제 앱처럼 동작하지만, 네트워크 호출은 없음
    }
}
```

## 정리

```
┌─────────────────────────────────────────────────────────────┐
│  "Hilt 없이 테스트하면 되잖아?"                              │
│                                                             │
│  Unit Test:     ✅ 맞습니다. Hilt 없이 하세요.              │
│  Integration Test: ❌ Hilt 없으면 Fake 주입 불가능          │
└─────────────────────────────────────────────────────────────┘
```

### Hilt의 진짜 가치

1. **Unit Test**: Hilt 불필요 (직접 Mock/Fake 주입)
2. **실제 앱**: 복잡한 객체 그래프 자동 조립
3. **Integration Test**: 앱 전체를 Fake로 테스트 가능하게 함

## 파일 구조

```
androidTest/
└── study/hilt/
    ├── HiltTestRunner.kt       # Hilt 테스트용 러너
    ├── FakeRepositoryModule.kt # @TestInstallIn으로 전역 Fake 설정
    └── HiltIntegrationTest.kt  # @BindValue로 개별 테스트 Fake 설정

test/
└── study/hilt/
    └── PurchaseViewModelTest.kt # Unit Test (Hilt 불필요)
```

## 관련 어노테이션

| 어노테이션 | 용도 |
|-----------|------|
| `@HiltAndroidTest` | 통합 테스트 클래스 표시 |
| `@TestInstallIn` | 프로덕션 모듈을 테스트 모듈로 교체 (전역) |
| `@UninstallModules` | 특정 모듈 비활성화 |
| `@BindValue` | 특정 의존성을 테스트 클래스에서만 교체 |
