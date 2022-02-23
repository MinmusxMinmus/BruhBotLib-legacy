package requirements.message

import net.dv8tion.jda.api.entities.Message
import requirements.Requirement
import java.time.OffsetDateTime
import java.time.temporal.TemporalUnit

abstract class MessageRequirement(protected val message: Message): Requirement

class RequireMessageTimeSent(message: Message, private val time: OffsetDateTime, private val leeway: Long, private val leewayType: TemporalUnit): MessageRequirement(message) {
    override fun check() = message.timeCreated < time.plus(leeway, leewayType) && message.timeCreated > time.minus(leeway, leewayType)
}

class RequireMessageTimeEdited(message: Message, private val time: OffsetDateTime, private val leeway: Long, private val leewayType: TemporalUnit): MessageRequirement(message) {
    override fun check() = message.timeEdited!! < time.plus(leeway, leewayType) && message.timeEdited!! > time.minus(leeway, leewayType)
}
