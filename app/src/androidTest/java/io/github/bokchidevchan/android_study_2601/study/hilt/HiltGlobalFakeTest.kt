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
 * 📚 @BindValue 사용 예제 - 다른 잔액으로 테스트
 * ========================================================================
 *
 * 이 테스트는 HiltIntegrationTest와 다른 잔액(50000)을 사용합니다.
 *
 * @BindValue의 장점:
 * - 각 테스트 클래스마다 다른 Fake 설정 가능
 * - 테스트 시나리오에 맞는 데이터로 테스트
 *
 * ┌────────────────────────────────────────────────────────────────┐
 * │  HiltIntegrationTest        │  HiltGlobalFakeTest             │
 * ├────────────────────────────────────────────────────────────────┤
 * │  fakeBalance = 99,999       │  fakeBalance = 50,000           │
 * │  고액 테스트                 │  일반 금액 테스트               │
 * └────────────────────────────────────────────────────────────────┘
 */
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class HiltGlobalFakeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * 이 테스트 클래스에서만 사용할 Fake
     * HiltIntegrationTest(99999)와 다른 잔액(50000) 설정
     */
    @BindValue
    @JvmField
    val fakeRepository: UserRepository = FakeUserRepository().apply {
        fakeBalance = 50000
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * 이 테스트의 @BindValue 잔액(50000)이 표시되는지 확인
     */
    @Test
    fun 테스트별_다른_Fake_데이터() {
        // Given: 이 테스트의 @BindValue에서 fakeBalance = 50000 설정

        // When: Hilt 화면으로 이동
        composeRule.onNodeWithText("Hilt DI").performClick()
        composeRule.onNodeWithText("Hilt 기초").performClick()

        // Then: 이 테스트의 Fake 잔액이 표시됨
        composeRule.onNodeWithText("50,000", substring = true).assertIsDisplayed()
    }
}

/**
 * ========================================================================
 * 📚 @TestInstallIn 개념 설명 (코드 예시)
 * ========================================================================
 *
 * @TestInstallIn은 프로덕션 모듈을 테스트 모듈로 "전역 교체"합니다.
 * 모든 테스트에 자동 적용되므로 개별 테스트에서 제거할 수 없습니다.
 *
 * 사용 예시 (만약 모든 테스트에서 동일한 Fake를 쓴다면):
 *
 * ```kotlin
 * @Module
 * @TestInstallIn(
 *     components = [SingletonComponent::class],
 *     replaces = [RepositoryModule::class]  // 프로덕션 모듈 대체
 * )
 * object FakeRepositoryModule {
 *     @Provides
 *     @Singleton
 *     fun provideFakeUserRepository(): UserRepository {
 *         return FakeUserRepository().apply { fakeBalance = 10000 }
 *     }
 * }
 * ```
 *
 * @TestInstallIn vs @BindValue 선택 기준:
 *
 * @TestInstallIn 사용:
 * - 모든 테스트에서 동일한 Fake 사용
 * - 설정 한 번으로 모든 테스트에 적용
 *
 * @BindValue 사용:
 * - 테스트마다 다른 Fake 설정 필요
 * - 특정 시나리오 테스트 (잔액 부족, 에러 상황 등)
 */
