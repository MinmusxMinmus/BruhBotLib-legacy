package shared

import java.io.Serializable
import java.time.Instant

/**
 * Describes a notable event during command execution. This is used for both normal events and errors / exceptions that
 * might occur, indicated by the subclass.
 */
abstract class ExecutionEventBase(val timestamp: Instant = Instant.now(), open val info: String) : Serializable

/**
 * Describes a standard event during command execution. This can be as simple as beginning a new loop, finishing processing
 * of a certain task, or anything else worth mentioning.
 */
data class ExecutionEvent(override val info: String) : ExecutionEventBase(info = info)

/**
 * Describes an error during command execution. Said error can be caused by an exception (caught or uncaught) or it can
 * be caused by a failed condition. The error can be fatal and force to stop command execution, or it can be dealt with
 * using an alternative solution, or even just aknowledged and ignored henceforth.
 */
data class ExecutionError(override val info: String,
                          val exception: Exception?) : ExecutionEventBase(info = info)