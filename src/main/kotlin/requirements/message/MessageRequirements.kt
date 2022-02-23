package requirements.message

import net.dv8tion.jda.api.entities.Message
import requirements.Requirement
import requirements.RequirementInformation
import java.time.OffsetDateTime
import java.time.temporal.TemporalUnit

abstract class MessageRequirement(): Requirement

class RequireMessageTimeSent(private val time: OffsetDateTime, private val leeway: Long, private val leewayType: TemporalUnit): MessageRequirement() {
    override fun check(information: RequirementInformation) = (information.get("message") as Message).timeCreated < time.plus(leeway, leewayType) && (information.get("message") as Message).timeCreated > time.minus(leeway, leewayType)
}

class RequireMessageTimeEdited(private val time: OffsetDateTime, private val leeway: Long, private val leewayType: TemporalUnit): MessageRequirement() {
    override fun check(information: RequirementInformation) = (information.get("message") as Message).timeEdited!! < time.plus(leeway, leewayType) && (information.get("message") as Message).timeEdited!! > time.minus(leeway, leewayType)
}
