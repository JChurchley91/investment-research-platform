package config

import kotlin.reflect.KProperty0
import kotlin.reflect.KSuspendFunction0

/**
 * Configuration class for tasks in the application.
 * Contains properties and methods to configure tasks.
 */
data class TaskConfig(
    val taskFunction: KSuspendFunction0<Unit>,
    val taskName: KProperty0<String>,
    val taskSchedule: KProperty0<String>,
    val taskParameters: KProperty0<List<Any>?>,
)
