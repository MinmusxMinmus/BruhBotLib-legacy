package requirements.guild

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import requirements.Requirement

abstract class GuildRequirement(protected val guild: Guild): Requirement

class RequireMember(guild: Guild, private val user: User) : GuildRequirement(guild) {
    override fun check() = guild.isMember(user)
}

class RequireGuild(guild: Guild, private val guildId: Long) : GuildRequirement(guild) {
    override fun check() = guild.idLong == guildId
}