package requirements.guild

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import requirements.Requirement
import requirements.RequirementInformation

abstract class GuildRequirement(): Requirement

class RequireMember(private val user: User) : GuildRequirement() {
    override fun check(information: RequirementInformation) = (information.get("guild") as Guild).isMember(user)
}

class RequireGuild(private val guildId: Long) : GuildRequirement() {
    override fun check(information: RequirementInformation) = (information.get("guild") as Guild).idLong == guildId
}