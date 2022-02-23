package requirements.channel

import net.dv8tion.jda.api.entities.AbstractChannel
import net.dv8tion.jda.api.entities.ChannelType
import requirements.Requirement
import requirements.RequirementInformation

abstract class ChannelRequirement(): Requirement

class RequireChannelName(channel: AbstractChannel, private val compareTo: String, private val check: ChannelNameCheck): ChannelRequirement() {
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

    override fun check(information: RequirementInformation) = check.check((information.get("") as AbstractChannel).name, compareTo)
}

class RequireChannelType(private val type: ChannelType) : ChannelRequirement() {
    override fun check(information: RequirementInformation) = information.get("") == type
}

class RequireChannel(private val channelId: Long) : ChannelRequirement() {
    override fun check(information: RequirementInformation) = information.get("") == channelId
}