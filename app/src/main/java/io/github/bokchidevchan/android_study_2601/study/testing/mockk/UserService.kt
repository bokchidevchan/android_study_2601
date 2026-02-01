package io.github.bokchidevchan.android_study_2601.study.testing.mockk

/**
 * ========================================================================
 * 📚 MockK 심화 - 테스트 대상 클래스들
 * ========================================================================
 */

// 사용자 데이터
data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int
)

// 이벤트 데이터
data class UserEvent(
    val type: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 사용자 저장소 인터페이스
 */
interface UserRepository {
    fun findById(id: String): User?
    fun findAll(): List<User>
    fun save(user: User): User
    fun delete(id: String): Boolean
    fun existsById(id: String): Boolean
    suspend fun findByIdAsync(id: String): User?
}

/**
 * 이벤트 발행기 인터페이스
 */
interface EventPublisher {
    fun publish(event: UserEvent)
    fun publishAll(events: List<UserEvent>)
}

/**
 * 로거 인터페이스
 */
interface Logger {
    fun info(message: String)
    fun error(message: String, throwable: Throwable? = null)
    fun debug(message: String)
}

/**
 * 사용자 서비스 - 테스트 대상
 *
 * 여러 의존성을 가지고 있어 MockK의 다양한 기능을 테스트하기 좋음
 */
class UserService(
    private val userRepository: UserRepository,
    private val eventPublisher: EventPublisher,
    private val logger: Logger
) {

    /**
     * 사용자 조회
     */
    fun getUser(id: String): User? {
        logger.debug("사용자 조회: $id")
        return userRepository.findById(id)
    }

    /**
     * 사용자 생성
     */
    fun createUser(name: String, email: String, age: Int): User {
        logger.info("사용자 생성 시작: $name")

        val user = User(
            id = generateId(),
            name = name,
            email = email,
            age = age
        )

        val savedUser = userRepository.save(user)

        eventPublisher.publish(
            UserEvent(
                type = "USER_CREATED",
                userId = savedUser.id,
                metadata = mapOf("name" to name, "email" to email)
            )
        )

        logger.info("사용자 생성 완료: ${savedUser.id}")
        return savedUser
    }

    /**
     * 사용자 삭제
     */
    fun deleteUser(id: String): Boolean {
        logger.info("사용자 삭제 시작: $id")

        if (!userRepository.existsById(id)) {
            logger.error("사용자를 찾을 수 없음: $id")
            return false
        }

        val deleted = userRepository.delete(id)

        if (deleted) {
            eventPublisher.publish(
                UserEvent(type = "USER_DELETED", userId = id)
            )
        }

        return deleted
    }

    /**
     * 모든 성인 사용자 조회
     */
    fun getAdultUsers(): List<User> {
        return userRepository.findAll().filter { it.age >= 18 }
    }

    /**
     * 사용자 이메일 변경
     */
    fun updateEmail(id: String, newEmail: String): User? {
        val user = userRepository.findById(id) ?: return null

        val updatedUser = user.copy(email = newEmail)
        userRepository.save(updatedUser)

        eventPublisher.publish(
            UserEvent(
                type = "EMAIL_UPDATED",
                userId = id,
                metadata = mapOf("oldEmail" to user.email, "newEmail" to newEmail)
            )
        )

        return updatedUser
    }

    /**
     * 배치 사용자 생성
     */
    fun createUsers(users: List<Triple<String, String, Int>>): List<User> {
        val createdUsers = users.map { (name, email, age) ->
            val user = User(id = generateId(), name = name, email = email, age = age)
            userRepository.save(user)
        }

        eventPublisher.publishAll(
            createdUsers.map { user ->
                UserEvent(type = "USER_CREATED", userId = user.id)
            }
        )

        return createdUsers
    }

    private fun generateId(): String = "user_${System.currentTimeMillis()}"
}

/**
 * Suspend 함수를 포함한 서비스
 */
class AsyncUserService(
    private val userRepository: UserRepository
) {
    suspend fun getUserAsync(id: String): User? {
        return userRepository.findByIdAsync(id)
    }
}
