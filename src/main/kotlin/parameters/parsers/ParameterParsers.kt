package parameters.parsers

import parameters.types.ParameterType
import parameters.values.ParameterResult

interface ParameterParser {
    fun parse(): ParameterResult
    fun getType(): ParameterType
}