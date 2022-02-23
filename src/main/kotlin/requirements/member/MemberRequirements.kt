package requirements.member

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import requirements.Requirement

abstract class MemberRequirement(protected val member: Member) : Requirement

class RequireRole(member: Member, private val role: Role) : MemberRequirement(member) {
    override fun check() =member.roles.contains(role)
}

class RequireAdmin(member: Member) : MemberRequirement(member) {
    override fun check() = member.permissions.contains(Permission.ADMINISTRATOR)
}