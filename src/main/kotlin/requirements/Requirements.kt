package requirements

interface Requirement {
    fun check(): Boolean
    infix fun and(b: Requirement): Requirement = AllRequirements(setOf(this, b))
    infix fun or(b: Requirement): Requirement = AnyRequirements(setOf(this, b))
}

class NoRequirements: Requirement {
    override fun check(): Boolean = true
}

class AnyRequirements(requirements: Collection<Requirement>): Requirement {
    private val reqs: Collection<Requirement> = requirements
    override fun check(): Boolean = reqs.any { it.check() }
}

class AllRequirements(requirements: Collection<Requirement>): Requirement {
    val reqs: Collection<Requirement> = requirements
    override fun check(): Boolean = reqs.all { it.check() }
}