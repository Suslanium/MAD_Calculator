package com.suslanium.calculator.model

sealed interface CalculationResult {
    data class Success(val output: String) : CalculationResult

    object Failure : CalculationResult

    data class Input(val expression: String) : CalculationResult
}
