package model

import net.dv8tion.jda.api.entities.Message
import java.io.Serializable

/**
 * The simplest way to interact with the bot.
 *
 * A command takes the form of a message sent in a guild, formatted in a specific fashion to be readable by the bot,
 * that invokes execution of a specific task.
 */
abstract class Command(val trigger: Message) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
    /**
     * Returns the name of the command. This equates to the name users must type to execute the command.
     */
    abstract fun getName() : String
    /**
     * Returns a simple description of the command. This will be displayed in the command help section.
     */
    abstract fun getDescription(): String
    /**
     * Returns the command's task object.
     */
    abstract fun getTask(): BBTask
}

interface BBTask : Serializable {
    fun isFinished(): Boolean
    fun execute()
}