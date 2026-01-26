package io.github.bokchidevchan.android_study_2601.study.networking.api

import io.github.bokchidevchan.android_study_2601.study.networking.model.GsonUser
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * ========================================================================
 * 🌐 Retrofit API 인터페이스
 * ========================================================================
 *
 * Retrofit의 마법: 인터페이스만 정의하면 구현체는 Dynamic Proxy가 자동 생성!
 *
 * 내부 동작:
 * 1. Retrofit.create(ApiService::class.java) 호출
 * 2. Java의 Proxy.newProxyInstance() 사용
 * 3. 메서드 호출 시 어노테이션 분석 → HTTP 요청 생성
 *
 * 어노테이션 종류:
 * - @GET, @POST, @PUT, @DELETE, @PATCH: HTTP 메서드
 * - @Path: URL 경로 변수 치환
 * - @Query: 쿼리 파라미터
 * - @Body: 요청 본문 (JSON)
 * - @Header: 헤더 추가
 * - @FormUrlEncoded + @Field: 폼 데이터
 */
interface JsonPlaceholderApi {

    /**
     * 단일 사용자 조회
     * GET https://jsonplaceholder.typicode.com/users/{id}
     *
     * @Path("id")는 URL의 {id} 부분을 치환
     */
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): GsonUser

    /**
     * 전체 사용자 목록 조회
     * GET https://jsonplaceholder.typicode.com/users
     */
    @GET("users")
    suspend fun getUsers(): List<GsonUser>
}
