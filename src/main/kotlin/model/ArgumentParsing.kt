package remote.model

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