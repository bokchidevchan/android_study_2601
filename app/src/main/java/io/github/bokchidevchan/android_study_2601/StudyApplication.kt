package io.github.bokchidevchan.android_study_2601

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ========================================================================
 * 📚 @HiltAndroidApp - Hilt의 시작점
 * ========================================================================
 *
 * 이 어노테이션이 하는 일:
 * 1. SingletonComponent(앱 전체 스코프)의 의존성 컨테이너 생성
 * 2. 앱 생명주기에 맞춰 컨테이너 관리
 * 3. 다른 안드로이드 컴포넌트(@AndroidEntryPoint)가 의존성을 받을 수 있게 함
 *
 * 내부 동작:
 * - 컴파일 타임에 Hilt가 Hilt_StudyApplication 클래스를 생성
 * - Application.onCreate()에서 컴포넌트 초기화
 *
 * ⚠️ 주의:
 * - 반드시 AndroidManifest.xml에 android:name=".StudyApplication" 추가 필요!
 * - 멀티 모듈 구조에서는 app 모듈에만 이 클래스가 있어야 함
 */
@HiltAndroidApp
class StudyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Hilt가 자동으로 의존성 그래프 초기화
        // 별도 초기화 코드 불필요!
    }
}
