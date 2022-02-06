package parameters.parsers

import parameters.types.*
import parameters.values.*

interface ParameterParser {
    fun getType(): ParameterType
    fun parse(): ParameterResult
}

class WildcardParameterParser(private val param: Any? = null): ParameterParser {
    override fun getType(): WildcardParameterType = WildcardParameterType()
    override fun parse(): ParameterResult = WildcardParameterValue(param)
}

abstract class ParameterParserFromString(protected val param: String): ParameterParser

open class StringParameterParser(param: String): ParameterParserFromString(param) {
    override fun getType(): StringParameterType = StringParameterType()
    override fun parse(): ParameterResult = StringParameterValue(param)
}

open class BooleanParameterParser(param: String): ParameterParserFromString(param) {
    override fun getType(): ParameterType = BooleanParameterType()
    override fun parse(): ParameterResult {
        return try { BooleanParameterValue(param.toBooleanStrict()) }
        catch (e: IllegalArgumentException) { BadParameterError() }
        catch (e: Exception) { ExceptionError(e) }
    }
}

open class NumberParameterParser(param: String): ParameterParserFromString(param) {
    override fun getType(): ParameterType = NumberParameterType()
    override fun parse(): ParameterResult {
        return try { IntegerParameterValue(param.toInt()) }
        catch (e: NumberFormatException) {
            try { DecimalParameterValue(param.toDouble()) }
            catch (e: NumberFormatException) { BadParameterError() }
            catch (e: Exception) { ExceptionError(e) }
        }
        catch (e: Exception) { ExceptionError(e) }
    }
}

open class IntegerParameterParser(param: String): NumberParameterParser(param) {
    override fun getType(): ParameterType = IntegerParameterType()
    override fun parse(): ParameterResult {
        return try { IntegerParameterValue(param.toInt()) }
        catch (e: NumberFormatException) { BadParameterError() }
        catch (e: Exception) { ExceptionError(e) }
    }
}

open class DecimalParameterParser(param: String): NumberParameterParser(param) {
    override fun getType(): ParameterType = DecimalParameterType()
    override fun parse(): ParameterResult {
        return try { DecimalParameterValue(param.toDouble()) }
        catch (e: NumberFormatException) { BadParameterError() }
        catch (e: Exception) { ExceptionError(e) }
    }
}