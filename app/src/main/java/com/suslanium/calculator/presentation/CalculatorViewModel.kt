package com.suslanium.calculator.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suslanium.calculator.model.CalculatorModel.Companion.CalculatorButton
import com.suslanium.calculator.model.CalculatorModel

class CalculatorViewModel : ViewModel() {
    private val model = CalculatorModel()

    private val _state: MutableLiveData<CalculatorUiState> =
        MutableLiveData(CalculatorUiState.Input)
    val state: LiveData<CalculatorUiState> = _state
    private val _input: MutableLiveData<String> = MutableLiveData(model.currentExpression)
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

    fun action(button: CalculatorButton) {
        if (_state.value !is CalculatorUiState.Input) {
            _input.value = model.clear()
            _state.value = CalculatorUiState.Input
        }
        when (button) {
            CalculatorButton.ADD, CalculatorButton.SUBTRACT, CalculatorButton.MULTIPLY, CalculatorButton.DIVIDE -> {
                _input.value = model.addOrChangeOperator(button)
            }
            CalculatorButton.CALCULATE -> {
                when (val result = model.checkAndCalculate()) {
                    CalculatorModel.ERROR_MESSAGE -> {
                        _state.value = CalculatorUiState.Error
                    }
                    CalculatorModel.EMPTY_STRING, CalculatorModel.FORMAT_ERROR_MESSAGE -> {}
                    else -> {
                        _state.value = CalculatorUiState.Result(result)
                    }
                }
            }
            CalculatorButton.CLEAR -> {
                _input.value = model.clear()
            }
            CalculatorButton.BRACKETS -> {
                _input.value = model.addBracket()
            }
            CalculatorButton.PERCENT -> {
                _input.value = model.percentage()
            }
            CalculatorButton.COMMA -> {
                _input.value = model.addComma()
            }
            CalculatorButton.BACKSPACE -> {
                _input.value = model.backspace()
            }
            else -> {
                _input.value = model.addNumber(button)
            }
        }
    }
}