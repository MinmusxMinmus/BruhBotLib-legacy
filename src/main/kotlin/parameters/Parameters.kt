package parameters

import parameters.parsers.*
import parameters.types.*
import parameters.values.StringParameterValue

interface Parameter {
    fun type(): ParameterType
    fun parser(): ParameterParser
}

open class StringParameter: Parameter {
    override fun type(): StringParameterType = StringParameterType()
    override fun parser(): StringParameterParser = StringParameterParser()
}

open class BooleanParameter: Parameter {
    override fun type(): BooleanParameterType = BooleanParameterType()
    override fun parser(): BooleanParameterParser = BooleanParameterParser()
}

open class NumberParameter: Parameter {
    override fun type(): NumberParameterType = NumberParameterType()
    override fun parser(): NumberParameterParser = NumberParameterParser()
}

open class IntegerParameter: NumberParameter() {
    override fun type(): IntegerParameterType = IntegerParameterType()
    override fun parser(): IntegerParameterParser = IntegerParameterParser()
}

open class DecimalParameter: NumberParameter() {
    override fun type(): DecimalParameterType = DecimalParameterType()
    override fun parser(): DecimalParameterParser = DecimalParameterParser()
}

