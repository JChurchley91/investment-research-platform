package scheduler

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KProperty0
import kotlin.reflect.KSuspendFunction0

class Scheduler {
    private val timer = Timer()
    private val logger = LoggerFactory.getLogger(Scheduler::class.java)

    fun start(tasks: List<Triple<KSuspendFunction0<Unit>, KProperty0<Long>, KProperty0<String>>>) {
        try {
            tasks.forEach { (task, taskSchedule, taskName) ->
                logger.info("Scheduling task: ${taskName.get()}")
                timer.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            runBlocking {
                                launch {
                                    task()
                                }
                            }
                        }
                    },
                    0,
                    taskSchedule.get(),
                )
            }
        } catch (exception: Exception) {
            logger.error("Error scheduling tasks: $exception")
        }
    }
}
