package model

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import remote.model.*
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Defines all information required to use its corresponding command. Includes the name of the command, a small
 * description of both the command and the parameters it requires, a reference to the command class it belongs to, and
 * the permission the command requires to be executed.
 */
data class CommandDeclaration(val name: String,
                              val description: String,
                              val parameters: List<ParameterType>,
                              val command: KClass<out Command>,
                              val permission: CommandPermission) : Serializable

/**
 * Valuable information about a command at any point of execution.
 */
data class CommandInformation(val channel: MessageChannel,
                              val author: User,
                              val guild: Guild,
                              val success: Boolean,
                              val failure: Boolean,
                              val errorMessage: String,
                              val arguments: List<ParameterValue>,
                              val events: List<ExecutionEventBase>) : Serializable

/**
 * The simplest way to interact with the bot.
 *
 * A command takes the form of a message sent in a guild, formatted in a specific fashion to be readable by the bot,
 * that invokes execution of a specific task. Said task may log the different events it undergoes.
 */
abstract class Command(protected val trigger: Message) : Serializable {
    // Shortcuts
    val channel get() = trigger.channel
    val author get() = trigger.author
    val guild get() = trigger.guild

    protected val arguments by lazy { ArgumentParser().parse(trigger.contentRaw, declaration().parameters) }
    protected val events = mutableListOf<ExecutionEventBase>()

    val success get() = events.none { it is ExecutionError }
    val failure get() = executed && !events.isEmpty() && events.last() is ExecutionError
    var executed = false
        private set

    fun execute() {
        // Permission check
        if (!declaration().permission.validatePermission(trigger)) {
            events.add(ExecutionError(info = "Permission check failed. Command cannot execute.", exception = null))
            execWhenBadPerms()
            return
        }

        // Argument check
        if (arguments.any { it is ParameterError }) {
            events.add(ExecutionError(info = "Argument check failed. Command cannot execute.", exception = null))
            execWhenBadArgs()
            return
        }

        // Actual command
        try {
            execCommand()
        } catch (e: Exception) {
            events.add(ExecutionError(info = "Unknown exception caused termination.", exception = e))
        } finally {
            executed = true
        }
    }

    /**
     * Contains the command's execution code. This is where modules should insert all of the command logic.
     */
    protected abstract fun execCommand()

    /**
     * Contains the code executed when bad permissions have been detected. This method will usually have a message
     * telling the user that he can't execute the command for X reason.
     */
    protected abstract fun execWhenBadPerms()

    /**
     * Contains the code executed when bad arguments have been introduced. This method will usually have the bot send a
     * message to the invoker explaining what went wrong.
     */
    protected abstract fun execWhenBadArgs()

    fun details() = CommandInformation(channel, author, guild, success, failure, events.last().info, arguments, events.toList())

    /**
     * Returns the command's [CommandDeclaration].
     */
    abstract fun declaration(): CommandDeclaration
}