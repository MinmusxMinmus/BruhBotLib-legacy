package parameters

import parameters.parsers.*
import parameters.types.*

/*
A parameter is defined as any sort of resource that might be used as input by any sort of bot public service.
The Parameter interface requires two different objects associated to it: a ParameterType class defining the basic
information for the parameter, and a ParameterParser class to decode a specific object into a ParameterValue class
representing its value.
 */
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

