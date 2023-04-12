package com.suslanium.calculator.model

import android.util.Log
import com.suslanium.calculator.CalculatorButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorModel {

    companion object {
        private val OPERATORS = charArrayOf('+', '-', '×', '÷')
        private val BRACKETS = charArrayOf('(', ')')
        private const val DOT = '.'
        private const val PERCENT = '%'
        private val DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        private const val POSITIVE_INF = "Infinity"
        private const val NOT_A_NUMBER = "NaN"

        const val EMPTY_STRING = ""
        private const val OPERATOR_ERROR_MESSAGE = "Unknown operator"
        private const val ZERO_DIVISION_MESSAGE = "Division by zero"
        private const val TAG = "Calculator"

        private val NUMBER =
            "(?:(?:[${DIGITS.joinToString("")}\\$DOT]|E${OPERATORS[1]}[${DIGITS.joinToString("")}]|E[${
                DIGITS.joinToString(
                    ""
                )
            }])+|$POSITIVE_INF|$NOT_A_NUMBER)"
        private val MINUS = "[${OPERATORS[1]}]?"
        private val INNER_BRACKET_REGEX = Regex("\\([^\\(\\)]+\\)")
        private val PERCENT_REGEX = Regex("$NUMBER$PERCENT")
        private val MINUS_AFTER_OPERATOR =
            "(?:(?<=[${OPERATORS.joinToString("\\")}])[${OPERATORS[1]}]|)"
        private val FIRST_OPERATOR_REGEX =
            Regex("($MINUS_AFTER_OPERATOR$NUMBER)([${OPERATORS[2].toString() + OPERATORS[3].toString()}])($MINUS$NUMBER)")
        private val SECOND_OPERATOR_REGEX =
            Regex("($MINUS$NUMBER)([${OPERATORS[0].toString() + OPERATORS[1].toString()}])($MINUS$NUMBER)")
        private val LEADING_ZEROS_REGEX = Regex("^${MINUS}${DIGITS[0]}+(?!$)")
    }

    private var currentExpression: String = EMPTY_STRING

    private val _expressionStateFlow =
        MutableStateFlow<CalculationResult>(CalculationResult.Input(currentExpression))
    val expressionStateFlow = _expressionStateFlow.asStateFlow()

    private var openBracketsAmount = 0

    private fun canAddOperator(operator: CalculatorButton) =
        currentExpression.last() == BRACKETS[1] || currentExpression.last() in DIGITS || currentExpression.last() == PERCENT || currentExpression.last() == BRACKETS[0] && operator.char == OPERATORS[1]

    private fun canSwitchOperator() =
        currentExpression.last() in OPERATORS && (currentExpression.length < 2 || !(currentExpression.last() == OPERATORS[1] && currentExpression[currentExpression.lastIndex - 1] == BRACKETS[0]))

    private fun canAddComma() =
        currentExpression.isNotEmpty() && currentExpression.last() in DIGITS && currentExpression.lastIndexOf(
            DOT
        ) <= currentExpression.lastIndexOfAny(OPERATORS)

    private fun canAddDigit(number: CalculatorButton) =
        number.char in DIGITS && (currentExpression.isEmpty() || currentExpression.last() != BRACKETS[1] && currentExpression.last() != PERCENT)

    private fun canAddClosingBracket() =
        openBracketsAmount > 0 && currentExpression.isNotEmpty() && (currentExpression.last() in DIGITS || currentExpression.last() == PERCENT || currentExpression.last() == BRACKETS[1])

    private fun canAddOpeningBracket() =
        currentExpression.isEmpty() || currentExpression.isNotEmpty() && (currentExpression.last() in OPERATORS || currentExpression.last() == BRACKETS[0])

    private fun canAddPercentSign() =
        currentExpression.isNotEmpty() && (currentExpression.last() in DIGITS || currentExpression.last() == BRACKETS[1])


    fun addOrChangeOperator(operator: CalculatorButton) {
        if (currentExpression.isEmpty() || operator.char !in OPERATORS) return
        if (canSwitchOperator()) {
            currentExpression = currentExpression.dropLast(1) + operator.symbol
        } else if (canAddOperator(operator)) {
            currentExpression += operator.symbol
        }
        _expressionStateFlow.value = CalculationResult.Input(currentExpression)
    }

    fun addComma() {
        if (canAddComma()) {
            currentExpression += DOT
            _expressionStateFlow.value = CalculationResult.Input(currentExpression)
        }
    }

    fun addDigit(number: CalculatorButton) {
        if (canAddDigit(number)) {
            currentExpression += number.symbol
            _expressionStateFlow.value = CalculationResult.Input(currentExpression)
        }
    }

    fun addBracket() {
        if (canAddOpeningBracket()) {
            currentExpression += BRACKETS[0]
            openBracketsAmount++
        } else if (canAddClosingBracket()) {
            currentExpression += BRACKETS[1]
            openBracketsAmount--
        }
        _expressionStateFlow.value = CalculationResult.Input(currentExpression)
    }

    fun percentage() {
        if (canAddPercentSign()) {
            currentExpression += PERCENT
            _expressionStateFlow.value = CalculationResult.Input(currentExpression)
        }
    }

    fun clear() {
        currentExpression = EMPTY_STRING
        openBracketsAmount = 0
        _expressionStateFlow.value = CalculationResult.Input(currentExpression)
    }

    fun backspace() {
        if (currentExpression.isEmpty()) return
        if (currentExpression.last() == BRACKETS[1]) {
            openBracketsAmount++
        } else if (currentExpression.last() == BRACKETS[0]) {
            openBracketsAmount--
        }
        currentExpression = currentExpression.dropLast(1)
        _expressionStateFlow.value = CalculationResult.Input(currentExpression)
    }

    fun checkAndCalculate() {
        if (currentExpression.isEmpty() || currentExpression.last() in OPERATORS || currentExpression.last() == DOT || openBracketsAmount != 0) return
        when (val result = calculate()) {
            CalculationResult.Failure -> _expressionStateFlow.value = result
            is CalculationResult.Success -> _expressionStateFlow.value =
                CalculationResult.Success(result.output.formatResult())
            else -> Unit
        }
    }

    private fun calculate(): CalculationResult {
        var expressionCopy = "($currentExpression)"
        while (INNER_BRACKET_REGEX.containsMatchIn(expressionCopy)) {
            try {
                expressionCopy =
                    expressionCopy.replace(INNER_BRACKET_REGEX) { evaluateInnerExpression(it.value) }
            } catch (ex: ArithmeticException) {
                Log.e(TAG, ex.stackTraceToString())
                return CalculationResult.Failure
            } catch (ex: NullPointerException) {
                Log.e(TAG, ex.stackTraceToString())
                return CalculationResult.Failure
            }
        }
        return CalculationResult.Success(expressionCopy)
    }

    private fun evaluateInnerExpression(expression: String): String {
        var expressionCopy = expression.substring(1, expression.lastIndex)
        expressionCopy = "0$expressionCopy"
        while (PERCENT_REGEX.containsMatchIn(expressionCopy)) {
            expressionCopy = expressionCopy.replace(PERCENT_REGEX) {
                (it.value.dropLast(1).toDoubleWithInfOrNan() / 100).toString()
            }
        }
        while (FIRST_OPERATOR_REGEX.containsMatchIn(expressionCopy)) {
            val toReplace = FIRST_OPERATOR_REGEX.find(expressionCopy)
            expressionCopy = expressionCopy.replaceFirst(
                toReplace!!.value, evaluateTwoOperandExpression(
                    toReplace.groups[1]!!.value,
                    toReplace.groups[3]!!.value,
                    toReplace.groups[2]!!.value
                )
            )
        }
        while (SECOND_OPERATOR_REGEX.containsMatchIn(expressionCopy)) {
            val toReplace = SECOND_OPERATOR_REGEX.find(expressionCopy)
            expressionCopy = expressionCopy.replaceFirst(
                toReplace!!.value, evaluateTwoOperandExpression(
                    toReplace.groups[1]!!.value,
                    toReplace.groups[3]!!.value,
                    toReplace.groups[2]!!.value
                )
            )
        }
        return expressionCopy
    }

    private fun evaluateTwoOperandExpression(
        firstOperand: String, secondOperand: String, operator: String
    ): String {
        val first = firstOperand.toDoubleWithInfOrNan()
        val second = secondOperand.toDoubleWithInfOrNan()
        return when (operator[0]) {
            OPERATORS[0] -> (first + second).toString()
            OPERATORS[1] -> (first - second).toString()
            OPERATORS[2] -> (first * second).toString()
            OPERATORS[3] -> if (second != 0.0) (first / second).toString()
                            else throw ArithmeticException(ZERO_DIVISION_MESSAGE)
            else -> throw ArithmeticException(OPERATOR_ERROR_MESSAGE)
        }
    }

    private fun String.toDoubleWithInfOrNan(): Double {
        return when (this) {
            POSITIVE_INF -> Double.POSITIVE_INFINITY
            OPERATORS[1].toString() + POSITIVE_INF -> Double.NEGATIVE_INFINITY
            NOT_A_NUMBER -> Double.NaN
            else -> this.toDouble()
        }
    }

    private fun String.formatResult(): String {
        return when (this) {
            POSITIVE_INF, NOT_A_NUMBER, OPERATORS[1].toString() + POSITIVE_INF -> this
            else -> {
                var result = this
                if (result.endsWith(".0")) {
                    result = result.dropLast(2)
                }
                result = result.replace(LEADING_ZEROS_REGEX, "")
                result
            }
        }
    }
}