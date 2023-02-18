package com.suslanium.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suslanium.calculator.ui.CalculatorButton
import com.suslanium.calculator.ui.theme.CalculatorTheme

class CalculatorActivity : ComponentActivity() {

    companion object {
        private val BUTTON_ROWS = listOf(
            listOf(
                CalculatorButton.CLEAR,
                CalculatorButton.BRACKETS,
                CalculatorButton.PERCENT,
                CalculatorButton.DIVIDE
            ),
            listOf(
                CalculatorButton.SEVEN,
                CalculatorButton.EIGHT,
                CalculatorButton.NINE,
                CalculatorButton.MULTIPLY
            ),
            listOf(
                CalculatorButton.FOUR,
                CalculatorButton.FIVE,
                CalculatorButton.SIX,
                CalculatorButton.SUBTRACT
            ),
            listOf(
                CalculatorButton.ONE,
                CalculatorButton.TWO,
                CalculatorButton.THREE,
                CalculatorButton.ADD
            ),
            listOf(CalculatorButton.ZERO, CalculatorButton.COMMA, CalculatorButton.CALCULATE)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }

    @Composable
    fun CalculatorScreen() {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
        ) {
            val scale = if (maxWidth < maxHeight) maxWidth.value / 412 else maxHeight.value / 412
            if (700 * scale < maxHeight.value) {
                Text(
                    "Calculator",
                    Modifier
                        .align(Alignment.TopStart)
                        .padding((16 * scale).dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = (MaterialTheme.typography.headlineMedium.fontSize.value * scale).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy((16 * scale).dp),
                modifier = Modifier.padding(bottom = (16 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var value by remember { mutableStateOf("2345,003") }
                InputField(scale = scale, value = value)
                Backspace(scale = scale, enabled = false)
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (16 * scale).dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy((16 * scale).dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(weight = 1f, fill = false)
                ) {
                    NumberPad(scale = scale)
                }
            }
        }
    }

    @Composable
    fun InputField(scale: Float, value: String) {
        BasicTextField(
            value = value,
            onValueChange = {},
            textStyle = MaterialTheme.typography.displayLarge.merge(
                TextStyle(
                    fontSize = (57 * scale).sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start
                )
            ),
            modifier = Modifier.padding(
                horizontal = (16 * scale).dp, vertical = (8 * scale).dp
            ),
            singleLine = true,
            readOnly = true
        )
    }

    @Composable
    fun Backspace(scale: Float, enabled: Boolean, onClick: () -> Unit = {}) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = (16 * scale).dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .width((48 * scale).dp)
                    .height((48 * scale).dp),
                enabled = enabled,
                colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.backspace),
                    contentDescription = ""
                )
            }
        }
    }

    @Composable
    fun NumberPad(scale: Float) {
        for(buttonRow in BUTTON_ROWS) {
            ButtonRow(scale = scale, buttons = buttonRow)
        }
    }

    @Composable
    fun ButtonRow(scale: Float, buttons: List<CalculatorButton>) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = (16 * scale).dp),
            horizontalArrangement = Arrangement.spacedBy(
                (16 * scale).dp, alignment = Alignment.CenterHorizontally
            )
        ) {
            if(buttons.size==4) {
                SecondaryButton(button = buttons[0], scale = scale)
                SecondaryButton(button = buttons[1], scale = scale)
                SecondaryButton(button = buttons[2], scale = scale)
                TertiaryButton(button = buttons[3], scale = scale)
            } else {
                WideSecondaryButton(button = buttons[0], scale = scale)
                SecondaryButton(button = buttons[1], scale = scale)
                TertiaryButton(button = buttons[2], scale = scale)
            }
        }
    }

    @Composable
    fun SecondaryButton(button: CalculatorButton, scale: Float, onClick: () -> Unit = {}) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape((28 * scale).dp),
            modifier = Modifier.size(
                (83 * scale).dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            contentPadding = PaddingValues(all = (6.5 * scale).dp)
        ) {
            Text(
                text = button.symbol,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = (MaterialTheme.typography.headlineLarge.fontSize.value * scale).sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun WideSecondaryButton(button: CalculatorButton, scale: Float, onClick: () -> Unit = {}) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape((28 * scale).dp),
            modifier = Modifier
                .height((83 * scale).dp)
                .width((182 * scale).dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            contentPadding = PaddingValues(all = (6.5 * scale).dp)
        ) {
            Text(
                text = button.symbol,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = (MaterialTheme.typography.headlineLarge.fontSize.value * scale).sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun TertiaryButton(button: CalculatorButton, scale: Float, onClick: () -> Unit = {}) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape((28 * scale).dp),
            modifier = Modifier.size(
                (83 * scale).dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentPadding = PaddingValues(all = (6.5 * scale).dp)
        ) {
            Text(
                text = button.symbol,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = (MaterialTheme.typography.headlineLarge.fontSize.value * scale).sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun CalculatorPreview() {
        CalculatorTheme {
            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
            ) {
                CalculatorScreen()
            }
        }
    }
}