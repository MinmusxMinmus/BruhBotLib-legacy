package parameters.values

import parameters.types.*

/*
Parsing a given object yields a ParameterResult object, representing the value of the given parameter. It is also
possible to receive an erroneous result, as a consequence of bad parsing.
 */
interface ParameterResult {
    fun isError(): Boolean
}

/*
Objects implementing ParameterValue represent final values of their respective ParameterType. They will only be created
through corresponding ParameterParser objects, and should ideally override equals() and hashCode().
The BruhBot lib offers a series of commonly used parameter values (strings, numbers, etc). To create new types of
parameters, one must inherit from one of the abstract implementations. Keep in mind that, in the end, every type of
parameter should be encodable in a string.
 */
interface ParameterValue: ParameterResult {
    override fun isError(): Boolean = false
    fun type(): ParameterType
    fun value(): Any?
}

open class StringParameterValue(protected val value: String): ParameterValue {
    override fun type(): ParameterType = StringParameterType()
    override fun value(): String = value
}

abstract class NumberParameterValue: ParameterValue {
    abstract override fun type(): NumberParameterType
}

open class IntegerParameterValue(protected val value: Int): NumberParameterValue() {
    override fun type(): IntegerParameterType = IntegerParameterType()
    override fun value(): Int = value
}

open class DecimalParameterValue(protected val value: Double): NumberParameterValue() {
    override fun type(): DecimalParameterType = DecimalParameterType()
    override fun value(): Double = value
}

open class BooleanParameterValue(protected val value: Boolean): ParameterValue {
    override fun type(): BooleanParameterType = BooleanParameterType()
    override fun value(): Boolean = value
}

class WildcardParameterValue(private val value: Any?): ParameterValue {
    override fun type(): WildcardParameterType = WildcardParameterType()
    override fun value(): Any? = value
}
/*
When parsing, perhaps the parser is incapable of obtaining a correct value. In that case, it returns a ParameterError
object. Similar to exceptions there's a couple generic errors defined, and the user is encouraged to declare their own
specific errors.
 */
interface ParameterError: ParameterResult {
    override fun isError(): Boolean = true
    fun errorMessage(): String
}

open class BadParameterError: ParameterError {
    override fun errorMessage(): String = "The supplied parameter is incorrect."
}

open class ExceptionError(protected val error: Exception): ParameterError {
    override fun errorMessage(): String = "An exception was catched while parsing this parameter."
}

open class ParsingError: ParameterError {
    override fun errorMessage(): String = "There was an error parsing this parameter."
}