package com.suslanium.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.suslanium.calculator.presentation.CalculatorUiState
import com.suslanium.calculator.presentation.CalculatorViewModel
import com.suslanium.calculator.ui.theme.*
import com.suslanium.calculator.model.CalculatorModel.Companion.CalculatorButton
import com.suslanium.calculator.presentation.CalculatorViewModel.Companion.BUTTON_ROWS

class CalculatorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: CalculatorViewModel by viewModels()
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun CalculatorScreen(viewModel: CalculatorViewModel) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp
        val state by viewModel.state.observeAsState(initial = CalculatorUiState.Input)
        val input by viewModel.input.observeAsState(initial = "")
        Column(modifier = Modifier.fillMaxSize()) {
            if (screenHeight > 700) {
                Spacer(modifier = Modifier.weight(TitleRowSpacerWeight))
                TitleText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(TitleRowWeight)
                )
            }
            Spacer(modifier = Modifier.weight(ResultRowSpacerWeight))
            AnimatedContent(targetState = state, transitionSpec = {
                (slideInVertically { width -> width } + fadeIn() with slideOutVertically { width -> -width } + fadeOut()).using(
                    SizeTransform(clip = true)
                )
            }) {
                InputField(
                    value = when (it) {
                        CalculatorUiState.Error -> stringResource(id = R.string.error)
                        CalculatorUiState.Input -> input
                        is CalculatorUiState.Result -> it.result
                    }, color = when (it) {
                        is CalculatorUiState.Error -> MaterialTheme.colorScheme.error
                        CalculatorUiState.Input -> MaterialTheme.colorScheme.onBackground
                        is CalculatorUiState.Result -> MaterialTheme.colorScheme.primary
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .weight(ResultRowWeight)
                )
            }
            Spacer(modifier = Modifier.weight(ActionsVerticalSpacerWeight))
            Actions(
                viewModelAction = viewModel::action,
                enabledBackSpace = input.isNotEmpty() && state is CalculatorUiState.Input,
                modifier = Modifier
                    .weight(ActionsWeight)
                    .verticalScroll(
                        rememberScrollState()
                    )
            )
        }
    }

    @Composable
    fun TitleText(modifier: Modifier) {
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(TitleTextWeight)
            )
        }
    }

    @Composable
    fun InputField(value: String, color: Color, modifier: Modifier) {
        val scrollState = rememberScrollState(0)
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
            BasicTextField(
                value = value,
                onValueChange = {},
                textStyle = MaterialTheme.typography.displayLarge.merge(
                    TextStyle(
                        fontSize = MaterialTheme.typography.displayLarge.fontSize,
                        color = color,
                        textAlign = TextAlign.Start
                    )
                ),
                singleLine = true,
                readOnly = true,
                modifier = Modifier.weight(DividerWeight).horizontalScroll(scrollState)
            )
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
        }
        //TODO: Do something about overscroll when there is no overflow
        LaunchedEffect(scrollState.maxValue) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    @Composable
    fun Actions(viewModelAction: (CalculatorButton) -> Unit, enabledBackSpace: Boolean, modifier: Modifier) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly, modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(BackSpaceSpacerWeight))
                Backspace(
                    enabled = enabledBackSpace,
                    onClick = { viewModelAction(CalculatorButton.BACKSPACE) },
                    modifier = Modifier
                        .weight(BackSpaceRowWeight)
                        .aspectRatio(PadButtonWeight)
                )
                Spacer(modifier = Modifier.weight(PadSpacerWeight))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(PadSpacerWeight))
                Divider(
                    modifier = Modifier.weight(DividerWeight),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.weight(PadSpacerWeight))
            }
            NumberPad(
                viewModelAction = viewModelAction
            )
        }
    }

    @Composable
    fun Backspace(enabled: Boolean, onClick: () -> Unit = {}, modifier: Modifier) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.backspace), contentDescription = ""
            )
        }
    }


    @Composable
    fun NumberPad(viewModelAction: (CalculatorButton) -> Unit) {
        for (buttonRow in BUTTON_ROWS) {
            ButtonsRow(
                buttons = buttonRow,
                viewModelAction = viewModelAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            )
        }
    }

    @Composable
    fun ButtonsRow(
        buttons: List<CalculatorButton>,
        viewModelAction: (CalculatorButton) -> Unit,
        modifier: Modifier
    ) {
        Row(
            modifier = modifier
        ) {
            if (buttons.size == 4) {
                buttons.forEachIndexed { index, button ->
                    Spacer(modifier = Modifier.weight(PadSpacerWeight))
                    PadButton(
                        button = button,
                        onClick = { viewModelAction(button) },
                        isTertiary = index == 3,
                        modifier = Modifier
                            .weight(PadButtonWeight)
                            .aspectRatio(PadButtonWeight)
                    )
                }
                Spacer(modifier = Modifier.weight(PadSpacerWeight))
            } else {
                buttons.forEachIndexed { index, button ->
                    Spacer(modifier = Modifier.weight(PadSpacerWeight))
                    PadButton(
                        button = button,
                        onClick = { viewModelAction(button) },
                        isTertiary = index == 2,
                        modifier = Modifier
                            .weight(if (index == 0) WidePadButtonWeight else PadButtonWeight)
                            .aspectRatio(if (index == 0) WidePadButtonWeight else PadButtonWeight)
                    )
                }
                Spacer(modifier = Modifier.weight(PadSpacerWeight))
            }
        }
    }

    @Composable
    fun PadButton(
        button: CalculatorButton,
        isTertiary: Boolean = false,
        onClick: () -> Unit = {},
        modifier: Modifier,
    ) {
        Button(
            onClick = onClick,
            shape = PadButtonShape,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isTertiary) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = if (!isTertiary) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentPadding = PaddingValues(all = PadButtonContentPadding)
        ) {
            Text(
                text = button.symbol,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}