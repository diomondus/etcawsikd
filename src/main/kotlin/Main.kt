import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun main() {
    // or you can check MyThreadLocalTest.kt
    val threadLocal = MyThreadLocal<Int>(1000)
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
    println(v.get() == null)
    exitProcess(0)
}
