package parameters

import parameters.parsers.ParameterParser
import parameters.types.ParameterType

interface Parameter {
    fun type(): ParameterType
    fun parser(): ParameterParser
}