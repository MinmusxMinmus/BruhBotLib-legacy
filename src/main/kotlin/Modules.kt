package remote

import model.CommandDeclaration
import java.rmi.Remote

/*
The objective is to allow easy module creation. Anyone that wants to create modules should just include this library,
then create an executable JAR that adds the corresponding factory to the registry for RMI. In the bot core, there should
be a command that allows you to add/remove modules (for example, !module add/remove modulename). The bot then adds or
removes the module's commands to its command pool (as well as whatever else modules are able to add), and voila!

TODO: Add the ability for a module to register new listeners
 */

/**
 * This interface serves to define all possible bot modules.
 *
 * A module is defined as an independent executable that can be remotely invoked by the bot, offering additional
 * functionality that is not necessary for basic execution. An example would be a module that only
 * offers additional commands to the bot.
 *
 * Any module must implement [commands], a function which sends information about the commands the module
 * adds. Some modules may not add commands at all, in which case they would send an empty set.
 */
interface BBModule : Remote {
    fun commands(): Set<CommandDeclaration>
}