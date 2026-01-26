package io.github.bokchidevchan.android_study_2601.study.hilt

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * ========================================================================
 * Hilt 통합 테스트용 커스텀 TestRunner
 * ========================================================================
 *
 * 왜 필요한가?
 *
 * 일반 앱에서는 StudyApplication(@HiltAndroidApp)이 Hilt를 초기화합니다.
 * 하지만 테스트에서는 HiltTestApplication을 사용해야 @TestInstallIn 등이 동작합니다.
 *
 * 설정 방법:
 * build.gradle.kts에서:
 * ```
 * defaultConfig {
 *     testInstrumentationRunner = "io.github.bokchidevchan.android_study_2601.study.hilt.HiltTestRunner"
 * }
 * ```
 *
 * 이렇게 하면 androidTest 실행 시 HiltTestApplication이 사용됩니다.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        // 원래 앱(StudyApplication) 대신 HiltTestApplication 사용
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
