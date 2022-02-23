package requirements.member

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import requirements.Requirement
import requirements.RequirementInformation

abstract class MemberRequirement() : Requirement

class RequireRole(private val role: Role) : MemberRequirement() {
    override fun check(information: RequirementInformation) = (information.get("member") as Member).roles.contains(role)
}

class RequireAdmin() : MemberRequirement() {
    override fun check(information: RequirementInformation) = (information.get("member") as Member).permissions.contains(Permission.ADMINISTRATOR)
}