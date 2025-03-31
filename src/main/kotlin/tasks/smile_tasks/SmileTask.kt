package tasks.smile_tasks

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * Base class for Smile Tasks.
 * This class provides common functionality for tasks
 * that use the smile-kotlin library.
 *
 * @property taskName The name of the task.
 * @property taskSchedule The schedule of the task.
 */
open class SmileTask(
    val taskName: String,
    val taskSchedule: String,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val today: LocalDate = LocalDate.now().minusDays(1)
}
