package remote

import model.Command
import java.rmi.Remote

/**
 * This interface serves to define all possible bot modules.
 *
 * A module is defined as an independent executable that can be remotely invoked by the bot, offering additional
 * functionality that is not necessary for basic execution. An example would be [BBCommands]: a module that only
 * offers additional commands to the bot.
 *
 * Any module must implement [registerCommands], a function which sends information about the commands the module
 * adds. Some modules may not add commands at all, in which case they would send an empty set.
 */
sealed interface BBModules : Remote {
    fun registerCommands() : Set<Command>
}

/**
 * Bot reaction module. The module evaluates received messages and sends the resulting actions to take.
 */
interface BBMessages : BBModules {

}

/**
 * Simple commands. The module offers an arrangement of common commands.
 */
interface BBCommands : BBModules {

}