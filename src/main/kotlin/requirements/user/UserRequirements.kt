package requirements.user

import net.dv8tion.jda.api.entities.User
import requirements.Requirement

abstract class UserRequirement(protected val user: User): Requirement

class RequireUser(user: User, private val userId: Long): UserRequirement(user) {
    override fun check() = user.idLong == userId
}