internal object App {
    suspend fun runApp() {
        println("Starting the app")
        val worker1 = Worker()
        try {
            println("Starting the worker")
            worker1.doWork()
        } catch (e: Throwable) {
            println("Stopping the worker")
            worker1.stop()
            throw e
        }
    }
}