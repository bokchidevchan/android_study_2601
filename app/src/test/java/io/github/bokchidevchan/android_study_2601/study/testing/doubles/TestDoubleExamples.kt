package io.github.bokchidevchan.android_study_2601.study.testing.doubles

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ========================================================================
 * 📚 테스트 더블 (Test Double) 비교 예제
 * ========================================================================
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │                    테스트 더블 선택                       │
 * ├─────────────────────────────────────────────────────────┤
 * │  "이 메서드가 호출되었나?" ──────────────► Mock          │
 * │  "호출하면 이 값을 반환해라" ─────────────► Stub         │
 * │  "대부분 실제, 일부만 가짜" ──────────────► Spy          │
 * │  "상태를 가진 간이 버전" ─────────────────► Fake         │
 * └─────────────────────────────────────────────────────────┘
 */
class TestDoubleExamples {

    // ========================================================================
    // 테스트 대상 코드 (System Under Test)
    // ========================================================================

    interface UserRepository {
        fun findById(id: String): User?
        fun save(user: User)
        fun delete(id: String)
        fun findAll(): List<User>
        suspend fun fetchFromNetwork(id: String): User?
    }

    interface EmailSender {
        fun send(to: String, subject: String, body: String): Boolean
    }

    interface Logger {
        fun log(message: String)
        fun error(message: String)
    }

    data class User(
        val id: String,
        val name: String,
        val email: String
    )

    class UserService(
        private val repository: UserRepository,
        private val emailSender: EmailSender,
        private val logger: Logger
    ) {
        fun getUser(id: String): User? {
            logger.log("사용자 조회: $id")
            return repository.findById(id)
        }

        fun createUser(user: User): Boolean {
            repository.save(user)
            logger.log("사용자 생성: ${user.id}")

            val sent = emailSender.send(
                to = user.email,
                subject = "가입을 환영합니다!",
                body = "${user.name}님, 가입해주셔서 감사합니다."
            )

            if (!sent) {
                logger.error("이메일 발송 실패: ${user.email}")
            }

            return sent
        }

        fun deleteUser(id: String) {
            val user = repository.findById(id)
            if (user != null) {
                repository.delete(id)
                logger.log("사용자 삭제: $id")
            }
        }

        fun getAllUserCount(): Int {
            return repository.findAll().size
        }
    }

    // ========================================================================
    // 1. STUB - "호출하면 이 값을 반환해라"
    // ========================================================================

    /**
     * Stub: 미리 정의된 응답을 반환
     *
     * 용도: 테스트에 필요한 데이터를 제공
     * 질문: "이 입력에 대해 이 출력을 반환해라"
     *
     * MockK: every { } returns 값
     */
    @Test
    fun `Stub - 미리 정의된 응답 반환`() {
        // Arrange
        val stubRepository = mockk<UserRepository>()
        val stubEmailSender = mockk<EmailSender>()
        val stubLogger = mockk<Logger>(relaxed = true)

        // Stub 설정: findById 호출 시 이 User를 반환해라
        every { stubRepository.findById("user-1") } returns User(
            id = "user-1",
            name = "홍길동",
            email = "hong@test.com"
        )

        // 없는 사용자는 null 반환
        every { stubRepository.findById("unknown") } returns null

        val service = UserService(stubRepository, stubEmailSender, stubLogger)

        // Act & Assert
        val user = service.getUser("user-1")
        assertEquals("홍길동", user?.name)

        val notFound = service.getUser("unknown")
        assertEquals(null, notFound)

        // Stub은 "무엇이 반환되는가"에 집중
        // 호출 여부는 검증하지 않음 (그건 Mock의 역할)
    }

    @Test
    fun `Stub - 다양한 반환 패턴`() {
        val stub = mockk<UserRepository>()

        // 패턴 1: 고정 값 반환
        every { stub.findById("1") } returns User("1", "A", "a@test.com")

        // 패턴 2: 순차적 반환 (첫 호출, 두 번째 호출...)
        every { stub.findById("2") } returns User("2", "B", "b@test.com") andThen null

        // 패턴 3: 예외 발생
        every { stub.findById("error") } throws RuntimeException("DB 오류")

        // 패턴 4: 조건부 반환 (any, match 등)
        every { stub.findById(match { it.startsWith("vip-") }) } returns User("vip", "VIP", "vip@test.com")

        // 검증
        assertEquals("A", stub.findById("1")?.name)
        assertEquals("B", stub.findById("2")?.name)  // 첫 번째 호출
        assertEquals(null, stub.findById("2"))        // 두 번째 호출
        assertEquals("VIP", stub.findById("vip-123")?.name)
    }

    // ========================================================================
    // 2. MOCK - "이 메서드가 호출되었나?"
    // ========================================================================

    /**
     * Mock: 호출 여부와 횟수를 검증
     *
     * 용도: 특정 상호작용이 발생했는지 확인
     * 질문: "이 메서드가 호출되었나? 몇 번? 어떤 인자로?"
     *
     * MockK: verify { }
     */
    @Test
    fun `Mock - 메서드 호출 여부 검증`() {
        // Arrange
        val mockRepository = mockk<UserRepository>(relaxed = true)
        val mockEmailSender = mockk<EmailSender>()
        val mockLogger = mockk<Logger>(relaxed = true)

        every { mockEmailSender.send(any(), any(), any()) } returns true

        val service = UserService(mockRepository, mockEmailSender, mockLogger)
        val newUser = User("new-1", "김철수", "kim@test.com")

        // Act
        service.createUser(newUser)

        // Assert - Mock 검증: 호출되었는가?
        verify { mockRepository.save(newUser) }  // save가 호출됨
        verify { mockLogger.log("사용자 생성: new-1") }  // 로그 기록됨
        verify {
            mockEmailSender.send(
                to = "kim@test.com",
                subject = "가입을 환영합니다!",
                body = "김철수님, 가입해주셔서 감사합니다."
            )
        }

        // 호출되지 않음 검증
        verify(exactly = 0) { mockLogger.error(any()) }
    }

    @Test
    fun `Mock - 호출 횟수 검증`() {
        val mockLogger = mockk<Logger>(relaxed = true)

        // Act
        mockLogger.log("첫 번째")
        mockLogger.log("두 번째")
        mockLogger.log("세 번째")

        // Assert
        verify(exactly = 3) { mockLogger.log(any()) }
        verify(atLeast = 2) { mockLogger.log(any()) }
        verify(atMost = 5) { mockLogger.log(any()) }
    }

    @Test
    fun `Mock - 호출 순서 검증`() {
        val mockLogger = mockk<Logger>(relaxed = true)

        // Act
        mockLogger.log("시작")
        mockLogger.log("처리중")
        mockLogger.log("완료")

        // Assert - 순서대로 호출됨
        io.mockk.verifyOrder {
            mockLogger.log("시작")
            mockLogger.log("처리중")
            mockLogger.log("완료")
        }
    }

    @Test
    fun `Mock - 인자 캡처`() {
        val mockRepository = mockk<UserRepository>(relaxed = true)

        // 인자 캡처용 slot
        val userSlot = slot<User>()
        every { mockRepository.save(capture(userSlot)) } returns Unit

        // Act
        mockRepository.save(User("123", "이영희", "lee@test.com"))

        // Assert - 캡처된 인자 검증
        val captured = userSlot.captured
        assertEquals("123", captured.id)
        assertEquals("이영희", captured.name)
        assertEquals("lee@test.com", captured.email)
    }

    // ========================================================================
    // 3. SPY - "대부분 실제, 일부만 가짜"
    // ========================================================================

    /**
     * Spy: 실제 객체를 감싸서 일부 메서드만 오버라이드
     *
     * 용도: 실제 동작은 유지하면서 특정 부분만 제어
     * 질문: "진짜처럼 동작하되, 이 부분만 가짜로"
     *
     * MockK: spyk(realObject)
     */
    @Test
    fun `Spy - 실제 객체 기반 부분 모킹`() {
        // 실제 구현체
        val realCalculator = RealCalculator()

        // Spy로 감싸기
        val spyCalculator = spyk(realCalculator)

        // 실제 메서드는 그대로 동작
        assertEquals(15, spyCalculator.add(10, 5))
        assertEquals(5, spyCalculator.subtract(10, 5))

        // 특정 메서드만 오버라이드
        every { spyCalculator.multiply(any(), any()) } returns 999

        assertEquals(999, spyCalculator.multiply(10, 5))  // 오버라이드된 값
        assertEquals(2, spyCalculator.divide(10, 5))     // 실제 동작
    }

    @Test
    fun `Spy - 호출 검증도 가능`() {
        val realCalculator = RealCalculator()
        val spyCalculator = spyk(realCalculator)

        // Act
        spyCalculator.add(1, 2)
        spyCalculator.add(3, 4)

        // Assert - Spy도 호출 검증 가능
        verify(exactly = 2) { spyCalculator.add(any(), any()) }
    }

    class RealCalculator {
        fun add(a: Int, b: Int) = a + b
        fun subtract(a: Int, b: Int) = a - b
        fun multiply(a: Int, b: Int) = a * b
        fun divide(a: Int, b: Int) = a / b
    }

    // ========================================================================
    // 4. FAKE - "상태를 가진 간이 버전"
    // ========================================================================

    /**
     * Fake: 실제 동작하는 간단한 구현체
     *
     * 용도: 실제 DB 대신 메모리 저장소 등
     * 질문: "진짜처럼 동작하는 가짜 구현"
     *
     * 특징: 상태를 가지고, 실제 로직 수행
     */
    @Test
    fun `Fake - 메모리 기반 가짜 구현체`() {
        // Fake 구현체 사용
        val fakeRepository = FakeUserRepository()
        val stubEmailSender = mockk<EmailSender> {
            every { send(any(), any(), any()) } returns true
        }
        val stubLogger = mockk<Logger>(relaxed = true)

        val service = UserService(fakeRepository, stubEmailSender, stubLogger)

        // Act - 실제처럼 CRUD 동작
        service.createUser(User("1", "홍길동", "hong@test.com"))
        service.createUser(User("2", "김철수", "kim@test.com"))
        service.createUser(User("3", "이영희", "lee@test.com"))

        // Assert - 상태 기반 검증
        assertEquals(3, service.getAllUserCount())
        assertEquals("홍길동", service.getUser("1")?.name)

        // 삭제 후 상태 변경 확인
        service.deleteUser("2")
        assertEquals(2, service.getAllUserCount())
        assertEquals(null, service.getUser("2"))
    }

    @Test
    fun `Fake - 직접 상태 설정 가능`() {
        val fakeRepository = FakeUserRepository()

        // Fake의 장점: 내부 상태 직접 설정
        fakeRepository.addUser(User("preset-1", "사전설정", "preset@test.com"))
        fakeRepository.addUser(User("preset-2", "미리설정", "preset2@test.com"))

        assertEquals(2, fakeRepository.findAll().size)
        assertEquals("사전설정", fakeRepository.findById("preset-1")?.name)
    }

    /**
     * Fake 구현체 - 메모리 기반 Repository
     */
    class FakeUserRepository : UserRepository {
        private val users = mutableMapOf<String, User>()

        override fun findById(id: String): User? = users[id]

        override fun save(user: User) {
            users[user.id] = user
        }

        override fun delete(id: String) {
            users.remove(id)
        }

        override fun findAll(): List<User> = users.values.toList()

        override suspend fun fetchFromNetwork(id: String): User? = users[id]

        // Fake 전용: 테스트 설정용 메서드
        fun addUser(user: User) {
            users[user.id] = user
        }

        fun clear() {
            users.clear()
        }
    }

    // ========================================================================
    // 5. 종합 비교 - 같은 시나리오, 다른 접근
    // ========================================================================

    /**
     * 시나리오: 사용자 생성 테스트
     *
     * 각 테스트 더블로 어떻게 접근하는지 비교
     */
    @Test
    fun `종합 - Stub으로 테스트 (반환값 중심)`() {
        val stubRepo = mockk<UserRepository>(relaxed = true)
        val stubEmail = mockk<EmailSender>()
        val stubLogger = mockk<Logger>(relaxed = true)

        // Stub: 이메일 발송 성공/실패 시나리오 테스트
        every { stubEmail.send(any(), any(), any()) } returns true

        val service = UserService(stubRepo, stubEmail, stubLogger)
        val result = service.createUser(User("1", "A", "a@test.com"))

        // Stub 관점: 반환값만 확인
        assertTrue(result)
    }

    @Test
    fun `종합 - Mock으로 테스트 (호출 검증 중심)`() {
        val mockRepo = mockk<UserRepository>(relaxed = true)
        val mockEmail = mockk<EmailSender>()
        val mockLogger = mockk<Logger>(relaxed = true)

        every { mockEmail.send(any(), any(), any()) } returns true

        val service = UserService(mockRepo, mockEmail, mockLogger)
        service.createUser(User("1", "홍길동", "hong@test.com"))

        // Mock 관점: 상호작용 검증
        verify { mockRepo.save(any()) }
        verify { mockEmail.send("hong@test.com", any(), any()) }
        verify { mockLogger.log(match { it.contains("생성") }) }
        verify(exactly = 0) { mockLogger.error(any()) }
    }

    @Test
    fun `종합 - Fake로 테스트 (상태 검증 중심)`() {
        val fakeRepo = FakeUserRepository()
        val stubEmail = mockk<EmailSender> {
            every { send(any(), any(), any()) } returns true
        }
        val stubLogger = mockk<Logger>(relaxed = true)

        val service = UserService(fakeRepo, stubEmail, stubLogger)

        // 초기 상태
        assertEquals(0, fakeRepo.findAll().size)

        // 사용자 생성
        service.createUser(User("1", "홍길동", "hong@test.com"))

        // Fake 관점: 상태 변화 검증
        assertEquals(1, fakeRepo.findAll().size)
        assertEquals("홍길동", fakeRepo.findById("1")?.name)
    }

    // ========================================================================
    // 6. Coroutine과 함께 사용
    // ========================================================================

    @Test
    fun `coEvery, coVerify - suspend 함수용`() = runTest {
        val mockRepo = mockk<UserRepository>()

        // suspend 함수는 coEvery
        coEvery { mockRepo.fetchFromNetwork("1") } returns User("1", "네트워크유저", "net@test.com")

        val result = mockRepo.fetchFromNetwork("1")

        assertEquals("네트워크유저", result?.name)

        // suspend 함수 검증은 coVerify
        coVerify { mockRepo.fetchFromNetwork("1") }
    }
}

/**
 * ========================================================================
 * 📚 테스트 더블 선택 가이드
 * ========================================================================
 *
 * | 상황 | 선택 | 이유 |
 * |------|------|------|
 * | 반환값 제어 필요 | Stub | 원하는 값 반환 |
 * | 호출 여부 검증 | Mock | verify로 확인 |
 * | 일부만 오버라이드 | Spy | 실제 로직 유지 |
 * | 상태 기반 테스트 | Fake | 실제처럼 동작 |
 *
 * 면접 답변:
 * "Stub은 반환값, Mock은 호출 검증, Spy는 부분 모킹,
 *  Fake는 상태를 가진 가짜 구현입니다."
 */
