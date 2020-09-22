package api.client

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandilibreClientTest {
    @Test
    fun testGet() {
        runBlocking {
            val client = CandilibreClient("https", "jsonplaceholder.typicode.com", "", "testToken")
            val response = client.get<TestGetResponse>("todos/1")
            val expected = TestGetResponse(
                1,
                1,
                "delectus aut autem",
                false
            )
            assertEquals(expected, response)
        }
    }

    @Serializable
    private data class TestGetResponse(
        val userId: Int? = null,
        val id: Int? = null,
        val title: String? = null,
        val completed: Boolean? = null
    )
}