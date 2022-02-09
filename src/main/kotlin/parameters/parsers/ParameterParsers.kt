package parameters.parsers

import parameters.types.*
import parameters.values.*

/*
ParameterParser classes transform objects into their ParameterValue representations. The parsers will get all their
required data from their own custom functions.
 */
interface ParameterParser {
    fun getType(): ParameterType
    fun parse(): ParameterResult
}

class WildcardParameterParser(private val param: Any? = null): ParameterParser {
    override fun getType(): WildcardParameterType = WildcardParameterType()
    override fun parse(): ParameterResult = WildcardParameterValue(param)
}

abstract class ParameterParserFromString: ParameterParser {
    protected lateinit var param: String

    fun parameter(param: String) {
        this.param = param
    }
}

open class StringParameterParser(): ParameterParserFromString() {
    override fun getType(): StringParameterType = StringParameterType()
    override fun parse(): ParameterResult = StringParameterValue(param)
}

open class BooleanParameterParser(): ParameterParserFromString() {
    override fun getType(): ParameterType = BooleanParameterType()
    override fun parse(): ParameterResult {
        return try { BooleanParameterValue(param.toBooleanStrict()) }
        catch (e: IllegalArgumentException) { BadParameterError() }
        catch (e: Exception) { ExceptionError(e) }
    }
}

open class NumberParameterParser(): ParameterParserFromString() {
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

open class IntegerParameterParser(): NumberParameterParser() {
    override fun getType(): ParameterType = IntegerParameterType()
    override fun parse(): ParameterResult {
        return try { IntegerParameterValue(param.toInt()) }
        catch (e: NumberFormatException) { BadParameterError() }
        catch (e: Exception) { ExceptionError(e) }
    }
}

open class DecimalParameterParser(): NumberParameterParser() {
    override fun getType(): ParameterType = DecimalParameterType()
    override fun parse(): ParameterResult {
        return try { DecimalParameterValue(param.toDouble()) }
        catch (e: NumberFormatException) { BadParameterError() }
        catch (e: Exception) { ExceptionError(e) }
    }
}