package requirements.channel

import net.dv8tion.jda.api.entities.AbstractChannel
import net.dv8tion.jda.api.entities.ChannelType
import requirements.Requirement

abstract class ChannelRequirement(protected val channel: AbstractChannel): Requirement

class RequireChannelName(channel: AbstractChannel, private val compareTo: String, private val check: ChannelNameCheck): ChannelRequirement(channel) {
    enum class ChannelNameCheck {
        FULL_NAME,
        CONTAINS,
        NOT_CONTAINS,
        REGEX;

        internal fun check(a: String, b: String) = when (this) {
            FULL_NAME -> a == b
            CONTAINS -> a.contains(b)
            NOT_CONTAINS -> !a.contains(b)
            REGEX -> a.matches(b.toRegex())
        }
    }

    override fun check() = check.check(channel.name, compareTo)
}

class RequireChannelType(channel: AbstractChannel, private val type: ChannelType) : ChannelRequirement(channel) {
    override fun check() = channel.type == type
}

class RequireChannel(channel: AbstractChannel, private val channelId: Long) : ChannelRequirement(channel) {
    override fun check() = channel.idLong == channelId
}