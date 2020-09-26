package services.api.client

import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutinesTests {

    @Test
    fun testCancelSuspend() {
        runBlocking {
            var result = -1
            val job = GlobalScope.launch { result = heavyCalculus() }
            job.cancelAndJoin()
            assertEquals(result, -1)
        }
    }

    @Test
    fun testMultithreading() {
        runBlocking {
            var result = -1
            val job = GlobalScope.launch { result = neverEndingFun() }
            job.cancelAndJoin()
            assertEquals(result, -1)
        }
    }

    @Test
    fun testCancelDelay() {
        runBlocking {
            var result = -1
            val job = GlobalScope.launch {
                delay(1000000)
                result = 20
            }
            job.cancelAndJoin()
            assertEquals(result, -1)
        }
    }

    suspend fun heavyCalculus(): Int = suspendCancellableCoroutine<Int> { continuation ->
        val i = 1000
        var result = 1
        repeat(i) {
            repeat(i) {
                repeat(i) {
                    result = 23
                }
            }
        }
        result = 42
        continuation.resume(result)
    }

    suspend fun neverEndingFun(): Int = coroutineScope {
        suspendCancellableCoroutine<Int> { continuation ->
            var stop = false
            while (!stop && isActive) {
            }
            continuation.resume(20)
        }
    }
}