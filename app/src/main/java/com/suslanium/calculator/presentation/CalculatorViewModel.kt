package com.suslanium.calculator.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suslanium.calculator.CalculatorButton
import com.suslanium.calculator.model.CalculationResult
import com.suslanium.calculator.model.CalculatorModel
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {
    private val model = CalculatorModel()

    private val _state: MutableLiveData<CalculatorUiState> =
        MutableLiveData(CalculatorUiState.Input)
    val state: LiveData<CalculatorUiState> = _state

    private val _input: MutableLiveData<String> = MutableLiveData()
    val input: LiveData<String> = _input

    companion object {
        val BUTTON_ROWS = listOf(
            listOf(
                CalculatorButton.CLEAR,
                CalculatorButton.BRACKETS,
                CalculatorButton.PERCENT,
                CalculatorButton.DIVIDE
            ), listOf(
                CalculatorButton.SEVEN,
                CalculatorButton.EIGHT,
                CalculatorButton.NINE,
                CalculatorButton.MULTIPLY
            ), listOf(
                CalculatorButton.FOUR,
                CalculatorButton.FIVE,
                CalculatorButton.SIX,
                CalculatorButton.SUBTRACT
            ), listOf(
                CalculatorButton.ONE,
                CalculatorButton.TWO,
                CalculatorButton.THREE,
                CalculatorButton.ADD
            ), listOf(CalculatorButton.ZERO, CalculatorButton.COMMA, CalculatorButton.CALCULATE)
        )
    }

    init {
        viewModelScope.launch {
            model.expressionStateFlow.collect {result ->
                when(result) {
                    CalculationResult.Failure -> _state.value = CalculatorUiState.Error
                    is CalculationResult.Input -> _input.value = result.expression
                    is CalculationResult.Success -> _state.value = CalculatorUiState.Result(result.output)
                }
            }
        }
    }

    fun action(button: CalculatorButton) {
        if (_state.value !is CalculatorUiState.Input) {
            model.clear()
            _state.value = CalculatorUiState.Input
        }
        when (button) {
            CalculatorButton.ADD, CalculatorButton.SUBTRACT, CalculatorButton.MULTIPLY, CalculatorButton.DIVIDE -> {
                model.addOrChangeOperator(button)
            }
            CalculatorButton.CALCULATE -> {
                model.checkAndCalculate()
            }
            CalculatorButton.CLEAR -> {
                model.clear()
            }
            CalculatorButton.BRACKETS -> {
                model.addBracket()
            }
            CalculatorButton.PERCENT -> {
                model.percentage()
            }
            CalculatorButton.COMMA -> {
                model.addComma()
            }
            CalculatorButton.BACKSPACE -> {
                model.backspace()
            }
            else -> {
                model.addDigit(button)
            }
        }
    }
}