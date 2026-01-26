package io.github.bokchidevchan.android_study_2601.study.hilt

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.github.bokchidevchan.android_study_2601.MainActivity
import io.github.bokchidevchan.android_study_2601.study.hilt.data.FakeUserRepository
import io.github.bokchidevchan.android_study_2601.study.hilt.di.RepositoryModule
import io.github.bokchidevchan.android_study_2601.study.hilt.domain.UserRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * ========================================================================
 * 📚 Hilt 통합 테스트 - 이것이 Hilt의 진짜 가치!
 * ========================================================================
 *
 * 이 테스트가 보여주는 것:
 *
 * 1. 실제 MainActivity를 실행하지만
 * 2. 네트워크 호출 없이 FakeRepository 사용
 * 3. UI 클릭 → ViewModel → Repository 전체 흐름 테스트
 *
 * Hilt 없이는 불가능한 이유:
 * - MainActivity는 시스템이 생성 (우리가 생성자 호출 불가)
 * - MainActivity 내부에서 hiltViewModel()로 ViewModel 획득
 * - ViewModel은 Hilt가 주입한 Repository 사용
 * - 이 체인에 Fake를 끼워넣으려면 Hilt의 도움이 필수!
 */
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)  // 프로덕션 모듈 제거 → @BindValue로 대체
class HiltIntegrationTest {

    /**
     * HiltAndroidRule - Hilt 테스트 환경 초기화
     *
     * 이 Rule이 하는 일:
     * 1. 테스트용 Hilt 컴포넌트 생성
     * 2. @BindValue로 선언된 의존성 주입
     * 3. @Inject 필드에 의존성 주입
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Compose 테스트 Rule
     * MainActivity를 실행하고 UI 테스트 가능하게 함
     */
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * ========================================================================
     * @BindValue - 특정 의존성을 이 테스트 클래스에서만 교체
     * ========================================================================
     *
     * @TestInstallIn vs @BindValue:
     *
     * @TestInstallIn:
     * - 모든 테스트 클래스에 적용
     * - 전역적인 Fake 설정
     *
     * @BindValue:
     * - 이 테스트 클래스에서만 적용
     * - 테스트별로 다른 설정 가능
     * - 더 유연한 제어
     *
     * ⚠️ 주의: @BindValue 사용 시 같은 타입을 제공하는 모듈을 @UninstallModules로 제거해야 함!
     * 그렇지 않으면 DuplicateBindings 오류 발생
     */
    @BindValue
    @JvmField
    val fakeRepository: UserRepository = FakeUserRepository().apply {
        fakeBalance = 99999  // 이 테스트에서만 사용할 잔액
    }

    @Before
    fun setup() {
        // Hilt 의존성 주입 실행
        hiltRule.inject()
    }

    /**
     * 통합 테스트: 전체 앱 흐름 테스트
     *
     * 테스트 시나리오:
     * 1. 앱 실행 (MainActivity)
     * 2. Hilt 카테고리 선택
     * 3. Hilt 기초 화면 진입
     * 4. 잔액이 FakeRepository의 값(99999)으로 표시되는지 확인
     *
     * 핵심:
     * - 실제 Activity, ViewModel, Repository 체인 전체가 동작
     * - 하지만 네트워크 호출은 없음 (FakeRepository 사용)
     */
    @Test
    fun 앱_전체_흐름_테스트_Fake_Repository_사용() {
        // Given: 앱이 실행됨 (MainActivity)

        // When: Hilt 카테고리 선택
        composeRule.onNodeWithText("Hilt DI").performClick()

        // Then: Hilt 카테고리 화면이 표시됨
        composeRule.onNodeWithText("Hilt 기초").assertIsDisplayed()

        // When: Hilt 기초 화면 진입
        composeRule.onNodeWithText("Hilt 기초").performClick()

        // Then: FakeRepository의 잔액이 표시됨
        // @BindValue로 주입한 FakeRepository의 fakeBalance = 99999
        composeRule.onNodeWithText("99,999", substring = true).assertIsDisplayed()
    }

    /**
     * 구매 버튼 클릭 테스트
     */
    @Test
    fun 구매_버튼_클릭_시_잔액_감소() {
        // Given: Hilt 기초 화면으로 이동
        composeRule.onNodeWithText("Hilt DI").performClick()
        composeRule.onNodeWithText("Hilt 기초").performClick()

        // 초기 잔액 확인
        composeRule.onNodeWithText("99,999", substring = true).assertIsDisplayed()

        // When: 1000원 아이템 구매
        composeRule.onNodeWithText("1,000원 아이템 구매").performClick()

        // Then: 잔액이 98,999원으로 감소
        // 실제로 UI가 업데이트되는지 확인
        composeRule.waitForIdle()
        composeRule.onNodeWithText("98,999", substring = true).assertIsDisplayed()
    }
}

/**
 * ========================================================================
 * 📚 왜 이게 중요한가? - Hilt 없는 세계 vs Hilt 있는 세계
 * ========================================================================
 *
 * ❌ Hilt 없이 통합 테스트를 하려면:
 *
 * ```kotlin
 * // 1. 테스트용 Application 클래스 직접 생성
 * class TestApplication : Application() {
 *     val fakeRepository = FakeUserRepository()
 *     // ...모든 의존성을 수동으로 관리
 * }
 *
 * // 2. 모든 Activity에서 Application 캐스팅
 * class MainActivity : ComponentActivity() {
 *     val repository = (application as TestApplication).fakeRepository
 *     // ...
 * }
 *
 * // 3. 프로덕션 코드에 테스트 로직이 섞임 (끔찍!)
 * ```
 *
 * ✅ Hilt와 함께라면:
 *
 * ```kotlin
 * @HiltAndroidTest
 * class MyTest {
 *     @BindValue
 *     val fakeRepo: UserRepository = FakeUserRepository()
 *
 *     // 끝! 프로덕션 코드는 건드리지 않음
 * }
 * ```
 *
 * ========================================================================
 * 핵심 정리
 * ========================================================================
 *
 * Q: "Unit Test에서는 그냥 생성자에 Fake 넣으면 되잖아. 왜 Hilt?"
 *
 * A: Unit Test ≠ Integration Test
 *
 *    Unit Test (test/):
 *    - 클래스 하나만 테스트
 *    - 개발자가 직접 객체 생성 가능
 *    - Hilt 없어도 됨 (오히려 없는 게 빠름)
 *
 *    Integration Test (androidTest/):
 *    - Activity/Fragment/ViewModel 전체 테스트
 *    - 시스템이 객체를 생성 (우리가 제어 불가)
 *    - Hilt 없이는 Fake 주입이 거의 불가능
 *    - @BindValue, @TestInstallIn으로 우아하게 해결
 *
 * 결론:
 * Hilt는 "단위 테스트를 쉽게 해주는 도구"가 아니라
 * "실제 앱처럼 동작하는 테스트 환경을 쉽게 만들어주는 도구"입니다.
 */
