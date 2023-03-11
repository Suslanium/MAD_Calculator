package com.suslanium.calculator.presentation

sealed interface CalculatorUiState {
    object Input : CalculatorUiState

    object Error: CalculatorUiState

    data class Result(val result: String) : CalculatorUiState
}
