package com.suslanium.calculator

enum class CalculatorButton(val symbol: String, val char: Char) {
    ZERO("0", '0'),
    ONE("1", '1'),
    TWO("2", '2'),
    THREE("3", '3'),
    FOUR("4", '4'),
    FIVE("5", '5'),
    SIX("6", '6'),
    SEVEN("7", '7'),
    EIGHT("8", '8'),
    NINE("9", '9'),
    ADD("+", '+'),
    SUBTRACT("-", '-'),
    MULTIPLY("×", '×'),
    DIVIDE("÷", '÷'),
    CALCULATE("=", Char.MIN_VALUE),
    CLEAR("AC", Char.MIN_VALUE),
    BRACKETS("()", Char.MIN_VALUE),
    PERCENT("%", Char.MIN_VALUE),
    COMMA(",", Char.MIN_VALUE),
    BACKSPACE("", Char.MIN_VALUE)
}