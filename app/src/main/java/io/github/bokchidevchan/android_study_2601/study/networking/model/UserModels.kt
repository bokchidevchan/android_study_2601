package io.github.bokchidevchan.android_study_2601.study.networking.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ========================================================================
 * 📦 데이터 모델 - JSON 직렬화 라이브러리별 비교
 * ========================================================================
 */

// ========================================================================
// Gson용 데이터 모델
// ========================================================================

/**
 * Gson은 @SerializedName으로 JSON 키를 매핑
 * 리플렉션을 사용하므로 런타임에 동작
 *
 * 장점:
 * - 유연함, 복잡한 JSON 구조도 처리 가능
 * - Java 호환성 좋음
 *
 * 단점:
 * - 리플렉션 오버헤드
 * - ⚠️ Kotlin non-null 타입 무시 → null 주입 위험!
 */
data class GsonUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String
)

// ========================================================================
// Kotlinx.Serialization용 데이터 모델
// ========================================================================

/**
 * Kotlinx.Serialization은 @Serializable로 컴파일 타임에 직렬화 코드 생성
 * 리플렉션 없이 동작하여 성능이 좋고 타입 안전
 *
 * 장점:
 * - 컴파일 타임 코드 생성 → 빠름
 * - ✅ Kotlin 타입 시스템 완벽 지원 (non-null 보장)
 * - Multiplatform 지원
 *
 * 단점:
 * - @Serializable 어노테이션 필수
 * - 복잡한 커스텀 직렬화는 설정 필요
 *
 * ⚠️ 주의: non-null 필드에 null이 오면 예외 발생 (이게 장점!)
 */
@Serializable
data class KotlinxUser(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("username") val username: String,
    @SerialName("email") val email: String
)
