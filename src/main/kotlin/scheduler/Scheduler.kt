package scheduler

import com.cronutils.model.Cron
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KProperty0
import kotlin.reflect.KSuspendFunction0

class Scheduler {
    private val timer = Timer()
    private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    private val cronParser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(com.cronutils.model.CronType.UNIX))

    fun start(tasks: List<Triple<KSuspendFunction0<Unit>, KProperty0<String>, KProperty0<String>>>) {
        try {
            tasks.forEach { (task, cronExpression, taskName) ->
                logger.info("Scheduling Task $taskName; Cron Expression - ${cronExpression.get()}")
                val cron: Cron = cronParser.parse(cronExpression.get())
                val executionTime = ExecutionTime.forCron(cron)
                scheduleTask(task, executionTime, taskName.get())
            }
        } catch (exception: Exception) {
            logger.error("Error scheduling tasks: $exception")
        }
    }

    private fun scheduleTask(
        task: KSuspendFunction0<Unit>,
        executionTime: ExecutionTime,
        taskName: String,
    ) {
        val nextExecution = executionTime.nextExecution(ZonedDateTime.now())
        if (nextExecution.isPresent) {
            val delay = nextExecution.get().toInstant().toEpochMilli() - System.currentTimeMillis()
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        runBlocking {
                            launch {
                                try {
                                    task()
                                    logger.info("Task $taskName executed successfully")
                                } catch (exception: Exception) {
                                    logger.error("Error executing task $taskName: $exception")
                                }
                            }
                        }
                        scheduleTask(task, executionTime, taskName) // Reschedule the task
                    }
                },
                delay,
            )
        }
    }
}
