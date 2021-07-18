package remote

import model.CommandDeclaration
import model.SimpleCommand
import net.dv8tion.jda.api.entities.Message
import java.rmi.Remote
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry

/*
The objective is to allow easy module creation. Anyone that wants to create modules should just include this library,
then create an executable JAR that adds the corresponding factory to the registry for RMI. In the bot core, there should
be a command that allows you to add/remove modules (for example, !module add/remove modulename). The bot then adds or
removes the module's commands to its command pool (as well as whatever else modules are able to add), and voila!

TODO: Add the ability for a module to register new listeners
TODO: Add the ability for a module to declare its dependencies
TODO: Allow module conflicts to be resolved somewhere (CMD?), instead of discarding duplicates.
*/

/**
 * This interface serves to define all possible bot modules.
 *
 * A module is defined as an independent executable that can be remotely invoked by the bot, offering additional
 * functionality that is not necessary for basic execution. An example would be a module that only
 * offers additional commands to the bot.
 */
interface BBModule : Remote {
    /**
     * A function which sends information about the commands the module adds. Some modules may not add commands at all,
     * in which case they should send an empty set.
     */
    @Throws(RemoteException::class)
    fun commands(): Set<CommandDeclaration>

    /**
     * Returns a new instance of the requested command. If the command doesn't exist, return null.
     */
    @Throws(RemoteException::class)
    fun newCommand(declaration: CommandDeclaration, trigger: Message): SimpleCommand?

    /**
     * The module's name. Must be a unique identifier!
     */
    @Throws(RemoteException::class)
    fun name(): String
}