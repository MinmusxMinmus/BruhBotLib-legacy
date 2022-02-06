package parameters.values

import parameters.types.*

interface ParameterResult {
    fun isError(): Boolean
}

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