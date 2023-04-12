package com.suslanium.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.suslanium.calculator.presentation.CalculatorUiState
import com.suslanium.calculator.presentation.CalculatorViewModel
import com.suslanium.calculator.presentation.CalculatorViewModel.Companion.BUTTON_ROWS
import com.suslanium.calculator.ui.theme.*

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
    private fun CalculatorScreen(viewModel: CalculatorViewModel) {
        val state by viewModel.state.observeAsState(initial = CalculatorUiState.Input)
        val input by viewModel.input.observeAsState(initial = "")
        Column(modifier = Modifier.fillMaxSize()) {
            if (LocalConfiguration.current.screenHeightDp > AppTitleMinRequiredHeight) {
                Spacer(modifier = Modifier.weight(TitleRowSpacerWeight))
                TitleText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(TitleRowWeight)
                )
            }
            Spacer(modifier = Modifier.weight(ResultRowSpacerWeight))
            AnimatedContent(targetState = state, transitionSpec = inputFieldTransitionSpec()) {
                InputField(
                    value = when (it) {
                        CalculatorUiState.Error -> stringResource(id = R.string.error)
                        CalculatorUiState.Input -> input
                        is CalculatorUiState.Result -> it.result
                    },
                    color = when (it) {
                        CalculatorUiState.Error -> MaterialTheme.colorScheme.error
                        CalculatorUiState.Input -> MaterialTheme.colorScheme.onBackground
                        is CalculatorUiState.Result -> MaterialTheme.colorScheme.primary
                    },
                    isInput = it == CalculatorUiState.Input,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(ResultRowWeight)
                )
            }
            Spacer(modifier = Modifier.weight(ActionsVerticalSpacerWeight))
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .weight(ActionsWeight)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(BackSpaceSpacerWeight))
                    Backspace(
                        enabled = input.isNotEmpty() && state is CalculatorUiState.Input,
                        onClick = { viewModel.action(CalculatorButton.BACKSPACE) },
                        modifier = Modifier
                            .weight(BackSpaceRowWeight)
                            .aspectRatio(PadButtonWeight)
                    )
                    Spacer(modifier = Modifier.weight(PadSpacerWeight))
                }
                Actions(viewModel::action)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalAnimationApi::class)
    private fun inputFieldTransitionSpec(): AnimatedContentScope<CalculatorUiState>.() -> ContentTransform =
        {
            (slideInVertically { width -> width } + fadeIn() with slideOutVertically { width -> -width } + fadeOut()).using(
                SizeTransform(clip = true)
            )
        }

    @Composable
    private fun TitleText(modifier: Modifier) {
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun InputField(value: String, color: Color, isInput: Boolean, modifier: Modifier) {
        val scrollState = rememberScrollState(0)
        val interactionSource = remember { MutableInteractionSource() }
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val haptic = LocalHapticFeedback.current
        val context = LocalContext.current
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                color = color,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier
                    .weight(DividerWeight)
                    .horizontalScroll(scrollState)
                    .combinedClickable(interactionSource = interactionSource,
                        indication = null,
                        onClick = {},
                        onLongClick = {
                            clipboardManager.setText(AnnotatedString(value))
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast
                                .makeText(context, getString(R.string.copied), Toast.LENGTH_SHORT)
                                .show()
                        })
            )
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
        }

        LaunchedEffect(scrollState.maxValue) {
            if (isInput) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    @Composable
    private fun Actions(viewModelAction: (CalculatorButton) -> Unit) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.weight(DividerWeight)
            )
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
        }
        NumberPad(
            viewModelAction = viewModelAction
        )
    }

    @Composable
    private fun Backspace(enabled: Boolean, onClick: () -> Unit = {}, modifier: Modifier) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = modifier
        ) {
            Icon(
                painter = painterResource(id = R.drawable.backspace), contentDescription = ""
            )
        }
    }


    @Composable
    private fun NumberPad(viewModelAction: (CalculatorButton) -> Unit) {
        for (buttonRow in BUTTON_ROWS) {
            ButtonsRow(
                buttons = buttonRow,
                viewModelAction = viewModelAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MinVerticalRowPadding)
            )
        }
    }

    @Composable
    private fun ButtonsRow(
        buttons: List<CalculatorButton>,
        viewModelAction: (CalculatorButton) -> Unit,
        modifier: Modifier
    ) {
        Row(
            modifier = modifier
        ) {
            buttons.forEachIndexed { index, button ->
                Spacer(modifier = Modifier.weight(PadSpacerWeight))
                PadButton(
                    button = button,
                    onClick = { viewModelAction(button) },
                    isTertiary = index == buttons.size - 1,
                    modifier = Modifier
                        .weight(if(button == CalculatorButton.ZERO) WidePadButtonWeight else PadButtonWeight)
                        .aspectRatio(if (button == CalculatorButton.ZERO) WidePadButtonWeight else PadButtonWeight)
                )
            }
            Spacer(modifier = Modifier.weight(PadSpacerWeight))
        }
    }

    @Composable
    private fun PadButton(
        button: CalculatorButton,
        isTertiary: Boolean = false,
        onClick: () -> Unit = {},
        modifier: Modifier,
    ) {
        Button(
            onClick = onClick,
            shape = PadButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTertiary) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isTertiary) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            ),
            contentPadding = PaddingValues(all = PadButtonContentPadding),
            modifier = modifier,
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