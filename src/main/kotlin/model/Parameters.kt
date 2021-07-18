package remote.model

import java.io.Serializable
import java.lang.NumberFormatException

/**
 * Defines the separator policy used by simple parameters.
 *
 * [QUOTATION_MARKS] means that it forcefully goes between quotation marks. [OPTIONAL_QUOTATION_MARKS] refers to
 * parameters that can either use quotations as separators, or split at the first space. [SPACES] indicates single-word
 * parameters that use spaces as separators.
 *
 * It is important to note that when using [QUOTATION_MARKS] or [OPTIONAL_QUOTATION_MARKS] in quotation mode, if the
 * user wants to put a quotation mark inside the parameter it must be properly escaped (`\"`)
 */
enum class SeparationPolicy {
    QUOTATION_MARKS, OPTIONAL_QUOTATION_MARKS, SPACES
}

/**
 * Represents a parameter type. Modules must override this class if they want to implement custom types.
 *
 * Make sure to implement [badParameterMessage] with a user-friendly message explaining why the parameter they input was
 * wrongly formatted.
 *
 * The [error] field is included as a way to pass information between [removeFromParams] and [validate]. An error while
 * executing the first method can be stored in this variable, then checked in the second method to return fast.
 */
sealed class ParameterType(val name: String, var error: ParameterError?): Serializable {
    /**
     * Confirms that the string represents a valid instance of the parameter type.
     */
    abstract fun validate(param: String): Boolean
    abstract fun badParameterMessage(): String
    abstract fun getParameterValue(value: String): ParameterValue

    /**
     * Returns a pair of strings, the first one corresponding to the parameter string and the second one corresponding
     * to the rest of the parameters (trimmed). If the parameter is badly written or otherwise unable to be read, the first value
     * must be an empty string, and the second value must be [remainingParams].
     */
    abstract fun removeFromParams(remainingParams: String): Pair<String, String>
}
/**
 * Represents a simple parameter type. Simple parameters use a special [SeparationPolicy] to determine how they're split
 *
 */
sealed class SimpleParameterType(name: String, val separationPolicy: SeparationPolicy) : ParameterType(name, null) {
    override fun removeFromParams(remainingParams: String) = when (separationPolicy) {
        SeparationPolicy.QUOTATION_MARKS -> parseQuotations(remainingParams)
        SeparationPolicy.OPTIONAL_QUOTATION_MARKS -> if (remainingParams.startsWith('"')) parseQuotations(remainingParams) else parseSpaces(remainingParams)
        SeparationPolicy.SPACES -> parseSpaces(remainingParams)
    }

    private fun parseQuotations(str: String): Pair<String, String> {
        // Unreadable if it doesn't start with quotation marks
        if (!str.startsWith('"')) {
            error = MissingFirstQuotation()
            return Pair("", str)
        }

        // Remove first quotation marks
        val ret = str.substring(1)

        // Search for the end of the parameter
        var currentIndex = 0
        while (true) {
            val i = ret.indexOf('"', startIndex = currentIndex)

            // If -1 it means there's no more quotation marks
            // If 0 it means the parameter is an empty string
            if (i < 1) {
                if (i == -1) error = MissingLastQuotation()
                return Pair("", str)
            }

            // If escaped, better luck next time
            if (ret[i - 1] == '\\') currentIndex = i

            // If that's it, this is unreadable
            // Otherwise put the correct value in currentIndex
            if (ret.length == i + 1) {
                error = MissingLastQuotation()
                return Pair("", str)
            }
            currentIndex++
        }
    }
    private fun parseSpaces(str: String): Pair<String, String> {
        // Everything is the param
        if (str.indexOf(' ') == -1) return Pair(str, "")

        // Get param
        return  Pair(str.substring(0, str.indexOf(' ')), str.substring(str.indexOf(' '), str.length).trim())
    }
}

/**
 * The second part of parameter implementing, this class holds the value of a correct parameter.
 */
sealed class ParameterValue(val valueStr: String): Serializable

// Possible parameter types
class WildcardType : ParameterType("Anything", null) {
    override fun badParameterMessage() = "There's no way to get this message"
    override fun getParameterValue(value: String) = StringValue(value)
    override fun removeFromParams(remainingParams: String) = Pair(remainingParams, "")
    override fun validate(param: String) = true
}
class StringParameter : SimpleParameterType("String", SeparationPolicy.QUOTATION_MARKS) {
    override fun validate(param: String) = true
    override fun badParameterMessage() = "There's no way to get this message"
    override fun getParameterValue(value: String) = StringValue(value)
}
class KeywordParameter : SimpleParameterType("Keyword", SeparationPolicy.OPTIONAL_QUOTATION_MARKS) {
    override fun validate(param: String) = true
    override fun badParameterMessage() = "This literally can't show up"
    override fun getParameterValue(value: String) = StringValue(value)
}
class IntegerParameter : SimpleParameterType("Integer", SeparationPolicy.OPTIONAL_QUOTATION_MARKS) {
    override fun validate(param: String): Boolean {
        return try {
            param.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    override fun badParameterMessage() = "The argument cannot be interpreted as an integer of any sort."
    override fun getParameterValue(value: String) = IntegerValue(value.toInt())
}
class DecimalParameter : SimpleParameterType("Decimal number", SeparationPolicy.OPTIONAL_QUOTATION_MARKS) {
    override fun validate(param: String): Boolean {
        return try {
            param.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    override fun badParameterMessage() = "The argument cannot be interpreted as a decimal number of any sort."
    override fun getParameterValue(value: String) = DecimalValue(value.toDouble())
}

// Possible parameter values
class StringValue(valueStr: String) : ParameterValue(valueStr)
class IntegerValue(val valueInt: Int) : ParameterValue(valueInt.toString())
class DecimalValue(val valueDouble: Double): ParameterValue(valueDouble.toString())