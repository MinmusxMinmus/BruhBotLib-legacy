package rmi

import simpleCommands.SimpleCommandDeclaration
import simpleCommands.SimpleCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import java.io.Serializable
import java.rmi.Remote
import java.rmi.RemoteException

/*
The objective is to allow easy module creation. Anyone that wants to create modules should just include this library,
then create an executable JAR that adds the corresponding BBModule to the registry for RMI. In the bot core, there should
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
    companion object {
        lateinit var jda: JDA
    }

    /**
     * The module's name. Must be a unique identifier!
     */
    @Throws(RemoteException::class)
    fun name(): String

    /**
     * The required intents for this command module.
     */
    @Throws(RemoteException::class)
    fun intents(): Collection<GatewayIntent>

    /**
     * Sends information about the commands the module adds. Some modules may not add commands at all, in which case
     * an empty set is sent.
     */
    @Throws(RemoteException::class)
    fun commands(): Set<SimpleCommandDeclaration>

    /**
     * Given a [declaration], returns the associated [SimpleCommand].
     */
    @Throws(RemoteException::class)
    fun getCommand(declaration: SimpleCommandDeclaration, trigger: Message): SimpleCommand


    /**
     * Executes the specified command, given the serialized trigger. If the command doesn't exist, does nothing.
     */
    @Throws(RemoteException::class)
    fun executeSimpleCommand(declaration: SimpleCommandDeclaration, message: MessageOrigin) {
        val msg = message.get(jda)?.complete()
        if (msg != null)
            getCommand(declaration, msg).execute()
    }

    /**
     * Builds the JDA instance required to retrieve Discord content.
     */
    @Throws(RemoteException::class)
    fun buildJDA(token: String) {
        jda = JDABuilder.createDefault(token, intents()).build().awaitReady()
    }
}

class MessageOrigin(private val messageID: Long, private val channelID: Long, private val isPrivateChannel: Boolean): Serializable {
    companion object {
        fun from(message: Message) = MessageOrigin(message.idLong, message.channel.idLong, message.isFromType(ChannelType.PRIVATE))
    }

    internal fun get(jda: JDA): RestAction<Message>? {
        val channel = if (isPrivateChannel) jda.privateChannelCache.getElementById(channelID) else jda.textChannelCache.getElementById(channelID)
        return channel?.retrieveMessageById(messageID)
    }
}