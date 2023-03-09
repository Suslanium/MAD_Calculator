package com.suslanium.calculator.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    private val _state: MutableLiveData<CalculatorUiState> =
        MutableLiveData(CalculatorUiState.Input)
    val state: LiveData<CalculatorUiState> = _state
    private val _input: MutableLiveData<String> = MutableLiveData("")
    val input: LiveData<String> = _input

    fun action(button: CalculatorButton) {
        if (_state.value !is CalculatorUiState.Input) {
            _input.value = ""
            _state.value = CalculatorUiState.Input
        }
        when (button) {
            CalculatorButton.ADD -> {}
            CalculatorButton.SUBTRACT -> {}
            CalculatorButton.MULTIPLY -> {}
            CalculatorButton.DIVIDE -> {}
            CalculatorButton.CALCULATE -> {
                //Temporary testing stuff
                _state.value = CalculatorUiState.Result(_input.value?:"")
            }
            CalculatorButton.CLEAR -> {
                //Temporary testing stuff
                _state.value = CalculatorUiState.Error
            }
            CalculatorButton.BRACKETS -> {}
            CalculatorButton.PERCENT -> {}
            CalculatorButton.COMMA -> {}
            CalculatorButton.BACKSPACE -> {}
            else -> {
                //Temporary testing stuff
                _input.value = _input.value + button.symbol
            }
        }
    }
}