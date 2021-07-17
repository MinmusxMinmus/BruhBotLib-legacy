package remote.model

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
sealed class ParameterType(val name: String, var error: ParameterError?) {
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
abstract class SimpleParameterType(name: String, val separationPolicy: SeparationPolicy) : ParameterType(name, null) {
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
abstract class ParameterValue(val valueStr: String)

// Common parameter/value pairs
// Normal string parameters
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
class StringValue(valueStr: String) : ParameterValue(valueStr)

// Integer parameters
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
class IntegerValue(val valueInt: Int) : ParameterValue(valueInt.toString())

// Decimal parameters
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
class DecimalValue(val valueDouble: Double): ParameterValue(valueDouble.toString())

/**
 * Represents a mistake in parsing. The library offers some example errors, used when parsing parameter spaces.
 *
 * To implement your own parameter error, use the badParameterMessage function in [ParameterType].
 */
sealed class ParameterError(val userMessage: String) : ParameterValue("")

/**
 * Represents a generic error. You can add whatever text as the error message.
 */
class GenericError(description: String) : ParameterError(description)
/**
 * Represents a mistake in writing, where a user forgot to enter a parameter. This most likely will appear with badly
 * written parameters, as the parsing messes up and takes more text than needed.
 */
class MissingParameter : ParameterError("This argument is missing. Perhaps there's been issues reading the previous ones?")
/**
 * Represents a mistake in writing, where a user forgot to type the first quotation mark (or both).
 */
class MissingFirstQuotation : ParameterError("Argument is missing quotations. Make sure to surround the parameter with \"!")
/**
 * Represents a mistake in writing, where a user forgot to write the second quotation mark.
 */
class MissingLastQuotation : ParameterError("Argument is missing the last quotation. Make sure to surround the parameter with \"!")
/**
 * Represents a general mistake, dependent on the expected parameter type.
 */
class BadParameter(type: ParameterType) : ParameterError(type.badParameterMessage())
/**
 * Represents a general parsing mistake, where an unknown exception has been thrown by the parser.
 */
class ExceptionThrown(val exception: Exception) : ParameterError("An exception was thrown during parameter parsing.")

class ArgumentParser {
    fun parse(paramString: String, expected: List<ParameterType>): List<ParameterValue> {
        var remainingParams = paramString
        val arguments = mutableListOf<ParameterValue>()
        expected.forEach {
            // Arguments have been consumed already
            if (remainingParams.isEmpty()) {
                println("Arguments have been consumed. Missing parameter/s!")
                arguments += MissingParameter()
                return@forEach
            }
            try {
                val param = it.removeFromParams(remainingParams)
                if (!it.validate(param.first)) {
                    arguments += BadParameter(it)
                    return@forEach
                }
                arguments += it.getParameterValue(param.first)
                remainingParams = param.second
            } catch (e: Exception) {
                arguments += ExceptionThrown(e)
                return@forEach
            }
        }
        // If there's still text, it means the parameters were malformed
        if (!remainingParams.isEmpty()) {
            println("There's still text ($remainingParams), bad")
            arguments += GenericError("Missing parameters")
        }
        return arguments.toList()
    }
}