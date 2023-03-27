package com.suslanium.calculator.model

sealed interface CalculationResult {
    data class Success(val output: String) : CalculationResult

    object Failure : CalculationResult

    object Nothing : CalculationResult
}
