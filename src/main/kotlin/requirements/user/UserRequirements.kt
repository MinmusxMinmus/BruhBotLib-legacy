package requirements.user

import net.dv8tion.jda.api.entities.User
import requirements.Requirement
import requirements.RequirementInformation

abstract class UserRequirement(): Requirement

class RequireUser(private val userId: Long): UserRequirement() {
    override fun check(information: RequirementInformation) = (information.get("user") as User).idLong == userId
}