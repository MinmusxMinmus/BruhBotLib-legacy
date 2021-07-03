package model

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import java.io.Serializable
import java.time.Instant
import kotlin.reflect.KClass

/**
 * Describes a notable event during command execution. This is used for both normal events and errors / exceptions that
 * might occur, indicated by the subclass.
 */
abstract class ExecutionEventBase(open val timestamp: Instant, open val info: String) : Serializable

data class ExecutionEvent(override val timestamp: Instant,
                          override val info: String) : ExecutionEventBase(timestamp, info)

data class ExecutionError(override val timestamp: Instant,
                          override val info: String,
                          val exception: Exception?) : ExecutionEventBase(timestamp, info)

/**
 * Defines all information required to use its corresponding command. Includes the name of the command, as well as a
 * small description of both the command and the parameters it requires. Also includes a reference to the command class
 * it belongs to.
 */
data class CommandDeclaration(val name: String,
                              val description: String,
                              val parameters: String,
                              val command: KClass<Command>) : Serializable

/**
 * Valuable information about a command at any point of execution.
 */
data class CommandInformation(val channel: MessageChannel,
                              val author: User,
                              val guild: Guild,
                              val success: Boolean,
                              val failure: Boolean,
                              val errorMessage: String,) : Serializable

/**
 * The simplest way to interact with the bot.
 *
 * A command takes the form of a message sent in a guild, formatted in a specific fashion to be readable by the bot,
 * that invokes execution of a specific task. Said task may log the different events it undergoes.
 */
abstract class Command(protected val trigger: Message) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    // Shortcuts
    val channel get() = trigger.channel
    val author get() = trigger.author
    val guild get() = trigger.guild

    protected val events = mutableListOf<ExecutionEventBase>()
    val success get() = events.none { it is ExecutionError }
    val failure get() = executed && events.last() is ExecutionError
    var executed = false
        private set

    fun execute() {
        try {
            exec()
        } catch (e: Exception) {
            events.add(ExecutionError(Instant.now(), "Unknown exception caused termination", e))
        } finally {
            executed = true
        }
    }
    fun details() = CommandInformation(channel, author, guild, success, failure, events.last().info)


    protected abstract fun exec()
    /**
     * Returns the command's [CommandDeclaration].
     */
    abstract fun declaration(): CommandDeclaration
}