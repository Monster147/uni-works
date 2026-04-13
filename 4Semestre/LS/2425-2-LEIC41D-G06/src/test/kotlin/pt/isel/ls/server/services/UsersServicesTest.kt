package pt.isel.ls.server.services

import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.data.memory.UserDataMem
import pt.isel.ls.server.data.memory.Users
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UsersServicesTest {
    private lateinit var userServices: UserServices

    @BeforeTest
    fun setup() {
        Users.clear()
        userServices = UserServices(UserDataMem())
    }

    @Test
    fun `test createNewUser`() {
        val (token, uid) = userServices.createNewUser("John Doe", "johnDoe@test.com", "testJohnDoe")
        assert(uid >= 0) { "User ID should be positive" }
    }

    @Test
    fun `test createNewUser with empty name`() {
        assertFailsWith<AppError> { userServices.createNewUser("", "johnDoe@test.com", "testJohnDoe") }
    }

    @Test
    fun `test createNewUser with empty email`() {
        assertFailsWith<AppError> { userServices.createNewUser("John Doe", "", "testJohnDoe") }
    }

    @Test
    fun `test createNewUser with duplicate email`() {
        userServices.createNewUser("Jane Doe", "janeDoe@test.com", "testJaneDoe")
        assertFailsWith<AppError> { userServices.createNewUser("Jane Doe", "janeDoe@test.com", "testJaneDoe") }
    }

    @Test
    fun `test getUserDetails`() {
        val (token, uid) = userServices.createNewUser("Jane Doe", "janeDoe@test.com", "testJaneDoe")
        val user = userServices.getUserDetails(uid)
        assertNotNull(user) { "User should not be null" }
        assert(user.uid == uid) { "User ID should match the generated ID" }
        assert(user.name == "Jane Doe") { "User name should match the generated name" }
        assert(user.email == "janeDoe@test.com") { "User email should match the generated email" }
    }

    @Test
    fun `test getUserDetails with invalid ID`() {
        assertFailsWith<AppError> { userServices.getUserDetails(-1) }
    }

    @Test
    fun `test getUserDetails with non-existent ID`() {
        val user = userServices.getUserDetails(1)
        assertNull(user)
    }
}
