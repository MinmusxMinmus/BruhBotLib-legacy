package requirements

interface Requirement {
    fun check(information: RequirementInformation): Boolean
    infix fun and(b: Requirement): Requirement = AllRequirements(setOf(this, b))
    infix fun or(b: Requirement): Requirement = AnyRequirements(setOf(this, b))
}

interface RequirementInformation {
    fun get(key: String): Any?
}

class NoRequirements: Requirement {
    override fun check(information: RequirementInformation): Boolean = true
}

class AnyRequirements(requirements: Collection<Requirement>): Requirement {
    private val reqs: Collection<Requirement> = requirements
    override fun check(information: RequirementInformation): Boolean = reqs.any { it.check(information) }
}

class AllRequirements(requirements: Collection<Requirement>): Requirement {
    val reqs: Collection<Requirement> = requirements
    override fun check(information: RequirementInformation): Boolean = reqs.all { it.check(information) }
}