package com.suslanium.calculator.model

import android.util.Log

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
        const val FORMAT_ERROR_MESSAGE = "Format error"
        const val ERROR_MESSAGE = "Error"
        private const val OPERATOR_ERROR_MESSAGE = "Unknown operator"
        private const val ZERO_DIVISION_MESSAGE = "Division by zero"
        private const val TAG = "Calculator"

        private val NUMBER = "(?:(?:[${DIGITS.joinToString("")}\\$DOT]|E${OPERATORS[1]}[${DIGITS.joinToString("")}]|E[${DIGITS.joinToString("")}])+|$POSITIVE_INF|$NOT_A_NUMBER)"
        private val MINUS = "[${OPERATORS[1]}]?"
        private val INNER_BRACKET_REGEX = Regex("\\([^\\(\\)]+\\)")
        private val PERCENT_REGEX = Regex("$NUMBER$PERCENT")
        private val MINUS_AFTER_OPERATOR = "(?:(?<=[${OPERATORS.joinToString("\\")}])[${OPERATORS[1]}]|)"
        private val FIRST_OPERATOR_REGEX = Regex("($MINUS_AFTER_OPERATOR$NUMBER)([${OPERATORS[2].toString() + OPERATORS[3].toString()}])($MINUS$NUMBER)")
        private val SECOND_OPERATOR_REGEX = Regex("($MINUS$NUMBER)([${OPERATORS[0].toString() + OPERATORS[1].toString()}])($MINUS$NUMBER)")

        enum class CalculatorButton(val symbol: String) {
            ZERO(DIGITS[0].toString()),
            ONE(DIGITS[1].toString()),
            TWO(DIGITS[2].toString()),
            THREE(DIGITS[3].toString()),
            FOUR(DIGITS[4].toString()),
            FIVE(DIGITS[5].toString()),
            SIX(DIGITS[6].toString()),
            SEVEN(DIGITS[7].toString()),
            EIGHT(DIGITS[8].toString()),
            NINE(DIGITS[9].toString()),
            ADD(OPERATORS[0].toString()),
            SUBTRACT(OPERATORS[1].toString()),
            MULTIPLY(OPERATORS[2].toString()),
            DIVIDE(OPERATORS[3].toString()),
            CALCULATE("="),
            CLEAR("AC"),
            BRACKETS(CalculatorModel.BRACKETS.joinToString("")),
            PERCENT(CalculatorModel.PERCENT.toString()), COMMA(DOT.toString()),
            BACKSPACE("")
        }
    }

    var currentExpression: String = EMPTY_STRING
        private set

    private var openBracketsAmount = 0

    fun addOrChangeOperator(operator: CalculatorButton): String {
        if (currentExpression.isNotEmpty() && operator.symbol[0] in OPERATORS) {
            if (currentExpression.last() in OPERATORS && (currentExpression.length < 2 || !(currentExpression.last() == OPERATORS[1] && currentExpression[currentExpression.lastIndex - 1] == BRACKETS[0]))) {
                currentExpression = currentExpression.dropLast(1) + operator.symbol
            } else if (currentExpression.last() == BRACKETS[1] || currentExpression.last() in DIGITS || currentExpression.last() == PERCENT || currentExpression.last() == BRACKETS[0] && operator.symbol[0] == OPERATORS[1]) {
                currentExpression += operator.symbol
            }
        }
        return currentExpression
    }

    fun addComma(): String {
        if (currentExpression.isNotEmpty()) {
            if (currentExpression.last() in DIGITS && currentExpression.lastIndexOf(DOT) <= currentExpression.lastIndexOfAny(OPERATORS)) {
                currentExpression += DOT
            }
        }
        return currentExpression
    }

    fun addNumber(number: CalculatorButton): String {
        if (number.symbol[0] in DIGITS && (currentExpression.isEmpty() || currentExpression.last() != BRACKETS[1] && currentExpression.last() != PERCENT)) {
            currentExpression += number.symbol
        }
        return currentExpression
    }

    fun addBracket(): String {
        if (currentExpression.isEmpty() || currentExpression.isNotEmpty() && (currentExpression.last() in OPERATORS || currentExpression.last() == BRACKETS[0])) {
            currentExpression += BRACKETS[0]
            openBracketsAmount++
        } else if (openBracketsAmount > 0 && currentExpression.isNotEmpty() && (currentExpression.last() in DIGITS || currentExpression.last() == PERCENT || currentExpression.last() == BRACKETS[1])) {
            currentExpression += BRACKETS[1]
            openBracketsAmount--
        }
        return currentExpression
    }

    fun percentage(): String {
        if (currentExpression.isNotEmpty() && (currentExpression.last() in DIGITS || currentExpression.last() == BRACKETS[1])) {
            currentExpression += PERCENT
        }
        return currentExpression
    }

    fun clear(): String {
        currentExpression = EMPTY_STRING
        openBracketsAmount = 0
        return currentExpression
    }

    fun backspace(): String {
        if (currentExpression.isNotEmpty()) {
            if (currentExpression.last() == BRACKETS[1]) {
                openBracketsAmount++
            } else if (currentExpression.last() == BRACKETS[0]) {
                openBracketsAmount--
            }
            currentExpression = currentExpression.dropLast(1)
        }
        return currentExpression
    }


    fun checkAndCalculate(): String {
        if (currentExpression.isNotEmpty()) {
            if (currentExpression.last() in OPERATORS || currentExpression.last() == DOT || openBracketsAmount != 0) {
                return FORMAT_ERROR_MESSAGE
            }
            return when (val result = calculate()) {
                ERROR_MESSAGE -> ERROR_MESSAGE
                else -> result.toDoubleWithInfOrNan().toStringWithoutTrailingZeros()
            }
        } else {
            return EMPTY_STRING
        }
    }

    private fun calculate(): String {
        var expressionCopy = "($currentExpression)"
        while (INNER_BRACKET_REGEX.containsMatchIn(expressionCopy)) {
            try {
                expressionCopy =
                    expressionCopy.replace(INNER_BRACKET_REGEX) { evaluateInnerExpression(it.value) }
            } catch (ex: Exception) {
                Log.e(TAG, ex.stackTraceToString())
                return ERROR_MESSAGE
            }
        }
        return expressionCopy
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
            OPERATORS[3] -> if (second != 0.0) (first / second).toString() else throw ArithmeticException(ZERO_DIVISION_MESSAGE)
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

    private fun Double.toStringWithoutTrailingZeros(): String {
        return when (this) {
            Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY -> this.toString()
            else -> {
                if(this % 1.0 == 0.0) {
                    val result = this.toString()
                    if(result.endsWith(".0")) {
                        result.dropLast(2)
                    } else {
                        result
                    }
                } else {
                    this.toString()
                }
            }
        }
    }
}