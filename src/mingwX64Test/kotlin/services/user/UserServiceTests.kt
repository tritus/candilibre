package services.user

import kotlin.test.Test
import kotlin.test.assertEquals

class UserServiceTests {
    @Test
    fun testGetUserId() {
        val token =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjVmMmQwYmNmZThiN2FiMDAxM2EyZjhmNyIsImxldmVsIjowLCJpYXQiOjE2MDEwMzg3MjIsImV4cCI6MTYwMTI5NzkyMn0.508iyEzW44sIQufm3srI151BIcvNYRrs1NdSLuCrzGs"
        val userId = UserService.getUserId(token)
        assertEquals("5f2d0bcfe8b7ab0013a2f8f7", userId)
    }
}