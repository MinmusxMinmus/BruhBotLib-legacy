package shared

import java.io.Serializable
import java.lang.NumberFormatException

// TODO add optional parameter support. Optional parameters should technically just be assuming that if the splitting went wrong, the parameter wasn't included

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
sealed class SimpleParameterType(name: String, val separationPolicy: SeparationPolicy) : ParameterType(name, null), Logging {
    companion object: Logging {
        private val logger = logger()
    }

    override fun removeFromParams(remainingParams: String) = when (separationPolicy) {
        SeparationPolicy.QUOTATION_MARKS -> parseQuotations(remainingParams)
        SeparationPolicy.OPTIONAL_QUOTATION_MARKS -> if (remainingParams.startsWith('"')) parseQuotations(remainingParams) else parseSpaces(remainingParams)
        SeparationPolicy.SPACES -> parseSpaces(remainingParams)
    }

    private fun parseQuotations(str: String): Pair<String, String> {
        logger.debug("Parsing parameter '$name' from string '$str' (quotation marks)")
        // Unreadable if it doesn't start with quotation marks
        if (!str.startsWith('"')) {
            logger.warn("Parameter '$name' had the first quotation mark missing")
            error = MissingFirstQuotation()
            return Pair("", str)
        }

        // Remove first quotation marks
        val ret = str.substring(1)
        logger.debug("Looking for the next valid quotation mark in string '$ret'")

        // Search for the end of the parameter
        var currentIndex = 0
        while (true) {
            var valid = true
            val i = ret.indexOf('"', startIndex = currentIndex)
            logger.debug("Plausible ending quotation mark found at index $i")

            // If -1 it means there's no more quotation marks
            // If 0 it means the parameter is an empty string
            if (i < 1) {
                if (i == -1) {
                    logger.warn("Unable to find ending quotation mark (no more quotation marks found)")
                    error = MissingLastQuotation()
                } else
                    logger.debug("Ending quotation mark found right after the beginning one: this must be an empty parameter")
                return "" to str
            }

            // If escaped, better luck next time
            if (ret[i - 1] == '\\') {
                logger.debug("Index contained an escaped quotation mark. Looking for the next one...")
                currentIndex = i
                valid = false
            }

            // If it was a valid quotation mark, we got it
            // If it wasn't:
            //  If that's it, this is unreadable
            //  Otherwise put the next non-quotation mark in currentIndex and continue searching
            if (valid) {
                logger.debug("Ending quotation mark at $currentIndex confirmed")
                val arg = str.substring(0, currentIndex).trim()
                logger.info("Parameter '$name' parsed as '$arg'")
                return arg to str.removePrefix(arg).trim()
            } else {
                if (ret.length == i + 1) {
                    logger.warn("Unable to find ending quotation mark (last possible ending quotation mark found and discarded)")
                    error = MissingLastQuotation()
                    return "" to str
                }
                currentIndex++
            }
        }
    }
    private fun parseSpaces(str: String): Pair<String, String> {
        logger.debug("Parsing parameter '$name' from string '$str' (spaces)")

        // Everything is the param
        if (str.indexOf(' ') == -1) {
            logger.debug("No following space characters found: the entire string must be the parameter")
            logger.info("Parameter '$name' parsed as '$str'")
            return str to ""
        }

        // Get param
        val space = str.indexOf(' ')
        val arg = str.substring(0, space)
        val rest = str.substring(space, str.length).trim()
        logger.info("Parameter '$name' parsed as '$arg'")
        return arg to rest
    }
}

// Possible parameter types
class WildcardType : ParameterType("Anything", null) {
    override fun badParameterMessage() = "There's no way to get this message"
    override fun getParameterValue(value: String) = StringValue(value)
    override fun removeFromParams(remainingParams: String) = Pair(remainingParams, "")
    override fun validate(param: String) = true
}
class StringParameter(val canContainSpaces: Boolean) : SimpleParameterType("String (${if (canContainSpaces) "Quotation mark/Space-separated" else "Quotation mark-separated"})", if (canContainSpaces) SeparationPolicy.QUOTATION_MARKS else SeparationPolicy.OPTIONAL_QUOTATION_MARKS) {
    override fun validate(param: String) = true
    override fun badParameterMessage() = "There's no way to get this message"
    override fun getParameterValue(value: String) = StringValue(value)
}
class KeywordParameter(val words: Set<String>) : SimpleParameterType(words.joinToString(separator = "\", \"", prefix = " Keywords (\"", postfix = "\")"), SeparationPolicy.OPTIONAL_QUOTATION_MARKS) {
    override fun validate(param: String) = words.contains(param.trim())
    override fun badParameterMessage() = "Invalid keyword. Possible values are: ${words.joinToString(separator = "\", \"", prefix = "\"", postfix = "\"") { it }}"
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

/**
 * The second part of parameter implementing, this class holds the value of a correct parameter.
 */
sealed class ParameterValue(val valueStr: String): Serializable

// Possible parameter values
class StringValue(valueStr: String) : ParameterValue(valueStr)
class IntegerValue(val valueInt: Int) : ParameterValue(valueInt.toString())
class DecimalValue(val valueDouble: Double): ParameterValue(valueDouble.toString())