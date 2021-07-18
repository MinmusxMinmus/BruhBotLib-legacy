package remote.model

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import java.io.Serializable

/*
Examples of permissions:
- Only if the user has administrator permissions
- Only the server owner
- Only this specific dude
- Only if the message contains a certain pattern (Why would you do this?)
- Only in this specific server
- Only if X part of the message fulfills the conditions stored in persistence
 */

/**
 * The most general way of checking for permissions. Override this method in your modules to create different,
 * completely customized permissions for your command.
 */
sealed class CommandPermission(val name: String) : Serializable {
    abstract fun validatePermission(message: Message) : Boolean
}
/**
 * Checks for multiple types of permissions. All of the given permissions must apply for the command to be greenlit.
 */
sealed class AllPermissions(private val restrictions: Set<CommandPermission>) : CommandPermission("Only messages that validate specific restrictions") {
    override fun validatePermission(message: Message) = restrictions.all { it.validatePermission(message) }
}
/**
 * Checks for multiple types of permissions. Any of the given permissions must apply for the command to be greenlit.
 */
sealed class AnyPermission(private val restrictions: Set<CommandPermission>) : CommandPermission("Only messages that validate specific restrictions") {
    override fun validatePermission(message: Message) = restrictions.any { it.validatePermission(message) }
}

/**
 * Checks for a certain permission in the message author. Modules can either use this implementation, or define classes with more
 * meaningful names.
 */
open class RolePermission(private val permission: Permission) : CommandPermission("Only messages whose author has a certain guild permission") {
    override fun validatePermission(message: Message): Boolean {
        // A null member means he left after executing this command. In that case, it's better that it doesn't run.
        // Otherwise, check if the member has a role with the corresponding permissions.
        return message.guild.getMemberById(message.author.id)?.roles?.any { it.permissions.contains(permission) } ?: false
    }
}
/**
 * Checks for multiple role permissions in the message author. All of the permissions must be present for the command to be greenlit.
 */
sealed class AllRolePermissions(permissions : Set<Permission>) : AllPermissions(permissions.mapTo(HashSet()) { RolePermission(it) })
/**
 * Checks for multiple role permissions in the message author. Any of the permissions must be present for the command to be greenlit.
 */
sealed class AnyRolePermissions(permissions : Set<Permission>) : AnyPermission(permissions.mapTo(HashSet()) { RolePermission(it) })

// Some common permissions
/**
 * Checks that the message author has administration permissions.
 */
class AdminPermission : RolePermission(Permission.ADMINISTRATOR)
/**
 * Greenlights all commands. Essentially a null permission checker.
 */
class NoPermission : CommandPermission("Public command") {
    override fun validatePermission(message: Message) = true
}