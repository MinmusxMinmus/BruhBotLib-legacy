package parameters.values

import parameters.types.ParameterType

interface ParameterResult {
    fun isError(): Boolean
}

interface ParameterValue: ParameterResult {
    override fun isError(): Boolean = false
    fun type(): ParameterType
    fun value(): Any
}

interface ParameterError: ParameterResult {
    override fun isError(): Boolean = true
    fun errorMessage(): String
}