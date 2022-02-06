package parameters.types

interface ParameterType {
    fun getName(): String
    fun getDescription(): String
}

open class StringParameterType: ParameterType {
    override fun getName(): String = "String"
    override fun getDescription(): String = "A string of text."
}

abstract class KeywordParameterType: StringParameterType() {
    protected val keywords: MutableCollection<StringParameterType> = mutableSetOf()
    override fun getName(): String = "Keyword"
    override fun getDescription(): String = "One of ${keywords.size} different predefined keywords."
}

open class NumberParameterType: ParameterType {
    override fun getName(): String = "Number"
    override fun getDescription(): String = "Any type of number."
}

open class IntegerParameterType: NumberParameterType() {
    override fun getName(): String = "Integer"
    override fun getDescription(): String = "A number with no decimal part."
}

open class DecimalParameterType: NumberParameterType() {
    override fun getName(): String = "Decimal"
    override fun getDescription(): String = "A number with decimal part."
}

open class BooleanParameterType: ParameterType {
    override fun getName(): String = "Boolean"
    override fun getDescription(): String = "A true/false value."
}

class WildcardParameterType: ParameterType {
    override fun getName(): String = "Anything"
    override fun getDescription(): String = "A parameter that can be anything"
}