package scheduler

import com.cronutils.model.Cron
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import config.TaskConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KSuspendFunction0

/**
 * Scheduler class to manage and execute tasks based on their cron expressions.
 * It uses a Timer to schedule tasks and a CronParser to parse the cron expressions.
 */
class Scheduler {
    private val timer = Timer()
    private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    private val cronParser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(com.cronutils.model.CronType.UNIX))

    /**
     * Starts the scheduler by scheduling the provided tasks.
     * @param tasks List of TaskConfig containing task details.
     * @return Boolean indicating success or failure of scheduling tasks.
     */
    fun start(tasks: List<TaskConfig>): Boolean {
        try {
            tasks.forEach { (task, taskName, taskSchedule, taskParameters) ->
                logger.info("Scheduling Task ${taskName.get()}; Cron Expression - ${taskSchedule.get()}")
                logger.info("Task Parameters: ${taskParameters.get()}")
                val cron: Cron = cronParser.parse(taskSchedule.get())
                val executionTime = ExecutionTime.forCron(cron)
                scheduleTask(task, executionTime, taskName.get())
                logger.info("Task ${taskName.get()} Scheduled Successfully")
            }
        } catch (exception: Exception) {
            logger.error("Error scheduling tasks: $exception")
            return false
        }
        return true
    }

    /**
     * Schedules a task to be executed at the next execution time based on the cron expression.
     * @param task The task to be executed.
     * @param executionTime The ExecutionTime object representing the cron expression.
     * @param taskName The name of the task.
     */
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
                                    logger.info("Task $taskName Executed Succesfully")
                                } catch (exception: Exception) {
                                    logger.error("Error Executing Task $taskName: $exception")
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
