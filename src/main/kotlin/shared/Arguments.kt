package shared

class ArgumentParser: Logging {
    companion object: Logging {
        val logger = logger()
    }

    fun parse(paramString: String, expected: List<ParameterType>): List<ParameterValue> {
        logger.info("Parsing parameters from string '$paramString'")
        var remainingParams = paramString
        val arguments = mutableListOf<ParameterValue>()
        expected.forEach {
            var wildcard = false
            logger.debug("Parsing parameter '${it.name}'")
            // Arguments have been consumed already
            if (remainingParams.isEmpty()) {
                if (it is WildcardType) {
                    logger.debug("Parameter string is empty, but current parameter is of type ${WildcardType::class.simpleName} so everything is fine")
                    arguments += it.getParameterValue(remainingParams)
                    wildcard = true
                } else {
                    logger.warn("Argument string is empty. No argument to parse, this usually means previous parameters parsed incorrectly.")
                    arguments += MissingParameter()
                    return@forEach
                }
            }
            if (!wildcard) {
                try {
                    remainingParams = remainingParams.trim()
                    val param = it.removeFromParams(remainingParams)
                    logger.debug("Argument '${it.name}' splits the argument string in the following manner:")
                    logger.debug("'${param.first}' - '${param.second}'")
                    if (!it.validate(param.first)) {
                        logger.warn("Argument '${it.name}' failed to validate string '${param.first}'. This usually means the argument string part was written incorrectly")
                        arguments += BadParameter(it)
                        return@forEach
                    }
                    logger.info("Argument '${it.name}' successfully parsed the argument string")
                    arguments += it.getParameterValue(param.first)
                    remainingParams = param.second
                } catch (e: Exception) {
                    logger.warn("Argument '${it.name}' threw an exception while parsing or validating argument string '${remainingParams}'")
                    logger.warn("Trace: ${e.stackTraceToString()}")
                    arguments += ExceptionThrown(e)
                    return@forEach
                }
            }
        }
        // If there's still text, it means the parameters were malformed
        if (!remainingParams.isEmpty()) {
            logger.warn("Argument string left '$remainingParams' after parsing all arguments. This usually means the user added more parameters than required.")
            arguments += GenericError("Missing parameters")
        }
        logger.info("Parsing of string '$paramString' ended")
        return arguments.toList()
    }
}

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
class ExceptionThrown(exception: Exception) : ParameterError("An exception was thrown during parameter parsing (${exception.message}).\nYou should probably let the bot owner know about this.")