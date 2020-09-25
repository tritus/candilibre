package services.api.client

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class CandilibreClientTest {
    @Test
    fun testGet() {
        runBlocking {
            val client = HttpClient("https", "jsonplaceholder.typicode.com", "", "testToken")
            val response = client.get<List<TodoTestObject>>("todos", "id" to "1")
            val expected = listOf(
                TodoTestObject(
                    1,
                    1,
                    "delectus aut autem",
                    false
                )
            )
            val expectedContentTypeHeader = "application/json"
            assertEquals(expected, response.body)
            assertEquals(expectedContentTypeHeader, response.headers["Content-Type"])
        }
    }

    @Test
    fun testPatch() {
        runBlocking {
            val client = HttpClient("https", "jsonplaceholder.typicode.com", "", "testToken")
            val body = TodoTestObject(
                1,
                1,
                "testTitle",
                true
            )
            val response = client.patch<TodoTestObject, TodoTestObject>("posts/1", body)
            val expected = TodoTestObject(
                1,
                1,
                "testTitle",
                true
            )
            assertEquals(expected, response)
        }
    }

    @Serializable
    private data class TodoTestObject(
        val userId: Int? = null,
        val id: Int? = null,
        val title: String? = null,
        val completed: Boolean? = null
    )
}