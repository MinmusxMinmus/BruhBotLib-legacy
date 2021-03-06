package simpleCommands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import shared.*
import java.io.Serializable

/**
 * Defines all information required to use its corresponding command. Includes the name of the command, a small
 * description of both the command and the parameters it requires, and the permission the command requires to be executed.
 */
data class SimpleCommandDeclaration(val name: String,
                                    val description: String,
                                    val parameters: List<Pair<String, ParameterType>>,
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
 *
 * This command class cannot handle complex logic, such as an interactive menu that allows the user to input multiple
 * times. The only information required to execute the command should be contained in the [trigger] message sent at
 * the beginning.
 */
abstract class SimpleCommand(protected val trigger: Message): Logging {
    companion object : Logging {
        val logger = logger()
    }
    // Shortcuts
    val channel get() = trigger.channel
    val author get() = trigger.author
    val guild get() = trigger.guild

    protected val arguments by lazy     {
        val cmdname = declaration().name
        logger.info("Parsing arguments of command '$cmdname'")
        val args = trigger.contentRaw.substring(trigger.contentRaw.indexOf(cmdname) + cmdname.length).trim()
        logger.debug("Argument string: '$args'")
        ArgumentParser().parse(args, declaration().parameters.map { it.second }).also {
            logger.info("Arguments parsed successfully")
        }
    }
    protected val events = mutableListOf<ExecutionEventBase>()

    val success get() = events.none { it is ExecutionError }
    val failure get() = executed && !events.isEmpty() && events.last() is ExecutionError
    var executed = false
        private set

    fun execute() {
        // Permission check
        logger.debug("Checking command permissions")
        if (!declaration().permission.validatePermission(trigger)) {
            logger.warn("Command failed permission check")
            events.add(ExecutionError(info = "Permission check failed. Command cannot execute.", exception = null))
            execWhenBadPerms()
            return
        }

        // Argument check
        logger.debug("Checking command arguments")
        if (arguments.size != declaration().parameters.size || arguments.any { it is ParameterError }) {
            logger.warn("Command failed argument check")
            events.add(ExecutionError(info = "Argument check failed. Command cannot execute.", exception = null))
            execWhenBadArgs()
            return
        }

        // Actual command
        try {
            logger.debug("Executing command")
            execCommand()
            logger.info("Command finished execution successfully")
        } catch (e: Exception) {
            logger.warn("Command failed execution due to unknown exception")
            logger.warn("Trace: ${e.stackTraceToString()}")
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
     * Returns the command's [SimpleCommandDeclaration].
     */
    abstract fun declaration(): SimpleCommandDeclaration
}