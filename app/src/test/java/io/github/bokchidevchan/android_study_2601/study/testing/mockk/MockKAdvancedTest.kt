package io.github.bokchidevchan.android_study_2601.study.testing.mockk

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ========================================================================
 * 📚 MockK 심화 - relaxed, capture, slot, spy
 * ========================================================================
 *
 * MockK는 Kotlin을 위해 설계된 Mocking 프레임워크입니다.
 * Mockito보다 Kotlin 친화적이며, 코루틴 지원이 우수합니다.
 *
 * 주요 개념:
 * - Mock: 모든 메서드가 stub 필요
 * - Relaxed Mock: stub 없이도 기본값 반환
 * - Spy: 실제 객체를 감싸서 일부만 모킹
 * - Capture/Slot: 호출된 인자 캡처
 */
class MockKAdvancedTest {

    // ========================================================================
    // 어노테이션 기반 Mock 선언
    // ========================================================================

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var eventPublisher: EventPublisher

    @RelaxedMockK  // stub 없이도 기본값 반환
    lateinit var logger: Logger

    private lateinit var userService: UserService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)  // 어노테이션 초기화
        userService = UserService(userRepository, eventPublisher, logger)
    }

    @After
    fun tearDown() {
        unmockkAll()  // 모든 Mock 정리
    }

    // ========================================================================
    // 1. 기본 Stubbing
    // ========================================================================

    @Test
    fun `every - 기본 stubbing`() {
        // Arrange
        val expectedUser = User("1", "홍길동", "hong@test.com", 25)
        every { userRepository.findById("1") } returns expectedUser

        // Act
        val result = userService.getUser("1")

        // Assert
        assertEquals(expectedUser, result)
    }

    @Test
    fun `returnsMany - 여러 번 호출 시 다른 값 반환`() {
        every { userRepository.findById("1") } returnsMany listOf(
            User("1", "첫번째", "first@test.com", 20),
            User("1", "두번째", "second@test.com", 25),
            User("1", "세번째", "third@test.com", 30)
        )

        assertEquals("첫번째", userService.getUser("1")?.name)
        assertEquals("두번째", userService.getUser("1")?.name)
        assertEquals("세번째", userService.getUser("1")?.name)
        assertEquals("세번째", userService.getUser("1")?.name)  // 마지막 값 반복
    }

    @Test
    fun `answers - 동적 응답`() {
        every { userRepository.findById(any()) } answers {
            val id = firstArg<String>()
            User(id, "User_$id", "$id@test.com", 20)
        }

        val user1 = userService.getUser("abc")
        val user2 = userService.getUser("xyz")

        assertEquals("User_abc", user1?.name)
        assertEquals("User_xyz", user2?.name)
    }

    @Test
    fun `throws - 예외 발생`() {
        every { userRepository.findById("error") } throws RuntimeException("DB 오류")

        assertThrows(RuntimeException::class.java) {
            userService.getUser("error")
        }
    }

    // ========================================================================
    // 2. Relaxed Mock - stub 없이 기본값 반환
    // ========================================================================

    @Test
    fun `relaxedMock - stub 없이도 동작`() {
        // logger는 @RelaxedMockK이므로 stub 없이도 동작
        // void 함수는 아무것도 안 함
        // 반환값이 있으면 기본값 반환 (0, false, null, 빈 컬렉션 등)

        // 아무 설정 없이 사용
        logger.info("테스트 메시지")
        logger.debug("디버그 메시지")

        // 호출 확인만 가능
        verify { logger.info("테스트 메시지") }
    }

    @Test
    fun `relaxedMockk 함수로 생성`() {
        val relaxedRepo = mockk<UserRepository>(relaxed = true)

        // stub 없이 호출 가능
        // relaxed mock은 nullable 타입에도 기본 mock 객체를 반환
        val result = relaxedRepo.findById("1")
        // relaxed mock은 객체를 반환 (null이 아님)
        assertNotNull(result)

        val allUsers = relaxedRepo.findAll()
        assertTrue(allUsers.isEmpty())  // 빈 리스트 반환

        val exists = relaxedRepo.existsById("1")
        assertFalse(exists)  // false 반환
    }

    // ========================================================================
    // 3. Slot과 Capture - 인자 캡처
    // ========================================================================

    @Test
    fun `slot - 단일 인자 캡처`() {
        // Arrange
        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } answers { userSlot.captured }
        every { eventPublisher.publish(any()) } just Runs  // 이벤트 발행 stub 추가

        // Act
        val result = userService.createUser("홍길동", "hong@test.com", 25)

        // Assert - 캡처된 인자 검증
        assertTrue(userSlot.isCaptured)
        assertEquals("홍길동", userSlot.captured.name)
        assertEquals("hong@test.com", userSlot.captured.email)
        assertEquals(25, userSlot.captured.age)
    }

    @Test
    fun `slot - 이벤트 인자 캡처 및 검증`() {
        // Arrange
        val eventSlot = slot<UserEvent>()
        every { userRepository.save(any()) } answers { firstArg() }
        every { eventPublisher.publish(capture(eventSlot)) } just Runs

        // Act
        userService.createUser("테스트", "test@test.com", 30)

        // Assert - 발행된 이벤트 검증
        assertTrue(eventSlot.isCaptured)
        assertEquals("USER_CREATED", eventSlot.captured.type)
        assertEquals("테스트", eventSlot.captured.metadata["name"])
        assertEquals("test@test.com", eventSlot.captured.metadata["email"])
    }

    @Test
    fun `mutableListOf - 여러 번 호출 시 모든 인자 캡처`() {
        // Arrange
        val capturedUsers = mutableListOf<User>()
        every { userRepository.save(capture(capturedUsers)) } answers { firstArg() }

        val users = listOf(
            Triple("유저1", "user1@test.com", 20),
            Triple("유저2", "user2@test.com", 25),
            Triple("유저3", "user3@test.com", 30)
        )
        every { eventPublisher.publishAll(any()) } just Runs

        // Act
        userService.createUsers(users)

        // Assert - 모든 캡처된 인자 검증
        assertEquals(3, capturedUsers.size)
        assertEquals("유저1", capturedUsers[0].name)
        assertEquals("유저2", capturedUsers[1].name)
        assertEquals("유저3", capturedUsers[2].name)
    }

    // ========================================================================
    // 4. Verify - 호출 검증
    // ========================================================================

    @Test
    fun `verify - 호출 여부 확인`() {
        // Arrange
        every { userRepository.findById("1") } returns null

        // Act
        userService.getUser("1")

        // Assert
        verify { userRepository.findById("1") }
        verify { logger.debug(any()) }  // any()로 인자 무시
    }

    @Test
    fun `verify exactly - 정확한 호출 횟수`() {
        every { userRepository.findById(any()) } returns null

        userService.getUser("1")
        userService.getUser("2")
        userService.getUser("3")

        verify(exactly = 3) { userRepository.findById(any()) }
        verify(exactly = 1) { userRepository.findById("1") }
        verify(exactly = 1) { userRepository.findById("2") }
    }

    @Test
    fun `verify atLeast, atMost - 호출 횟수 범위`() {
        every { userRepository.findById(any()) } returns null

        repeat(5) { userService.getUser("$it") }

        verify(atLeast = 3) { userRepository.findById(any()) }
        verify(atMost = 10) { userRepository.findById(any()) }
    }

    @Test
    fun `verifyOrder - 호출 순서 검증`() {
        every { userRepository.existsById("1") } returns true
        every { userRepository.delete("1") } returns true
        every { eventPublisher.publish(any()) } just Runs

        userService.deleteUser("1")

        verifyOrder {
            userRepository.existsById("1")
            userRepository.delete("1")
            eventPublisher.publish(any())
        }
    }

    @Test
    fun `verifySequence - 정확한 호출 순서와 횟수`() {
        every { userRepository.findById("1") } returns User("1", "테스트", "test@test.com", 25)

        userService.getUser("1")

        verifySequence {
            logger.debug(any())
            userRepository.findById("1")
        }
    }

    @Test
    fun `confirmVerified - 모든 호출이 검증되었는지 확인`() {
        every { userRepository.findById("1") } returns null

        userService.getUser("1")

        verify { userRepository.findById("1") }
        verify { logger.debug(any()) }

        confirmVerified(userRepository)  // userRepository의 모든 호출이 verify됨
    }

    // ========================================================================
    // 5. Spy - 실제 객체 + 부분 모킹
    // ========================================================================

    @Test
    fun `spyk - 실제 객체를 감싸서 부분 모킹`() {
        // 실제 구현체
        val realCalculator = object {
            fun add(a: Int, b: Int) = a + b
            fun multiply(a: Int, b: Int) = a * b
        }

        // Spy로 감싸기
        val spyCalculator = spyk(realCalculator)

        // multiply만 모킹, add는 실제 동작
        every { spyCalculator.multiply(any(), any()) } returns 999

        assertEquals(15, spyCalculator.add(10, 5))  // 실제 동작
        assertEquals(999, spyCalculator.multiply(10, 5))  // 모킹된 값
    }

    // ========================================================================
    // 6. 코루틴 테스트
    // ========================================================================

    @Test
    fun `coEvery - suspend 함수 모킹`() = runTest {
        // Arrange
        val asyncService = AsyncUserService(userRepository)
        val expectedUser = User("1", "비동기유저", "async@test.com", 30)

        coEvery { userRepository.findByIdAsync("1") } returns expectedUser

        // Act
        val result = asyncService.getUserAsync("1")

        // Assert
        assertEquals(expectedUser, result)
        coVerify { userRepository.findByIdAsync("1") }
    }

    // ========================================================================
    // 7. Argument Matchers
    // ========================================================================

    @Test
    fun `any - 모든 인자 매칭`() {
        every { userRepository.findById(any()) } returns null

        assertNull(userService.getUser("아무거나"))
        assertNull(userService.getUser("다른거"))
    }

    @Test
    fun `match - 조건부 매칭`() {
        every {
            userRepository.findById(match { it.startsWith("admin") })
        } returns User("admin", "관리자", "admin@test.com", 30)

        every {
            userRepository.findById(match { !it.startsWith("admin") })
        } returns null

        assertNotNull(userService.getUser("admin_001"))
        assertNull(userService.getUser("user_001"))
    }

    @Test
    fun `allAny - 모든 인자를 any로`() {
        // 많은 파라미터가 있을 때 유용
        every { userRepository.save(any()) } answers { firstArg() }

        val user = User("1", "테스트", "test@test.com", 25)
        val saved = userRepository.save(user)

        assertEquals(user, saved)
    }
}

/**
 * ========================================================================
 * 📚 MockK 요약
 * ========================================================================
 *
 * 1. Mock 생성
 *    - mockk<Type>(): 일반 Mock (모든 메서드 stub 필요)
 *    - mockk<Type>(relaxed = true): 기본값 반환 Mock
 *    - spyk(realObject): 실제 객체 + 부분 모킹
 *
 * 2. Stubbing
 *    - every { ... } returns value
 *    - every { ... } throws exception
 *    - every { ... } answers { ... }
 *    - coEvery { ... }: suspend 함수용
 *
 * 3. Capture
 *    - val slot = slot<Type>()
 *    - capture(slot): 인자 캡처
 *    - slot.captured: 캡처된 값 접근
 *
 * 4. Verify
 *    - verify { ... }: 호출 확인
 *    - verify(exactly = n) { ... }: 정확한 횟수
 *    - verifyOrder { ... }: 순서 확인
 *    - coVerify { ... }: suspend 함수용
 *
 * 5. Matchers
 *    - any(): 모든 값
 *    - match { condition }: 조건부 매칭
 *    - eq(value): 정확한 값
 *    - isNull(), isNull(inverse = true)
 */
