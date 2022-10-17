import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MyThreadLocal<T>(
    delay: Long = TimeUnit.MINUTES.toMillis(1),
) {
    private val threadNameToValueMap = ConcurrentHashMap<String, T>()
    private val deadTreadCleaner = DeadTreadCleaner(delay)

    init {
        deadTreadCleaner.scheduleCleanTask()
    }

    private fun getKey(): String = Thread.currentThread().name

    fun set(t: T) {
        threadNameToValueMap[getKey()] = t
    }

    fun get(): T? = threadNameToValueMap[getKey()]

    fun remove(): T? = threadNameToValueMap.remove(getKey())

    private inner class DeadTreadCleaner(
        private val delay: Long
    ) {
        private var executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        private val cleanTask: () -> Unit = { clean() }

        private fun clean() {
            val allThreadsMap = Thread.getAllStackTraces().keys.associateBy { it.name }
            threadNameToValueMap.keys.forEach { threadName ->
                if (threadName !in allThreadsMap) {
                    threadNameToValueMap.remove(threadName)
                    println("tread $threadName died")
                }
            }
            scheduleCleanTask()
        }

        fun scheduleCleanTask() {
            try {
                executorService.schedule(cleanTask, delay, TimeUnit.MILLISECONDS)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}