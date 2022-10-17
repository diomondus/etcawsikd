import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.assertNull

internal class MyThreadLocalTest {

    @Test
    fun `test set-get-remove methods`() {
        // given
        val threadLocal = spyk(MyThreadLocal<Int>(1000))
        val threadName = "thread-1"

        // when
        try {
            val t1 = thread(start = true, name = threadName) {
                try {
                    threadLocal.set(1).also { println("$threadName set") }
                    sleep(1000)
                    threadLocal.get().also { println("$threadName get $it") }
                    sleep(2000)
                } catch (ignore: InterruptedException) {
                    println("$threadName interrupted")
                }
            }
            sleep(2000)
            t1.interrupt()
            sleep(2000)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        // and
        val v = AtomicReference(0)
        thread(start = true, name = threadName) {
            v.set(threadLocal.get())
        }.join()

        // then
        assertNull(v.get()) // check value was removed after thread-1 had been interrupted
        verify(exactly = 1) { threadLocal.set(1) }
        verify(exactly = 2) { threadLocal.get() }
    }

}