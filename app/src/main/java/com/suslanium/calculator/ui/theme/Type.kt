package com.suslanium.calculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.W500,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp
    ),
    displayLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.W500,
        fontSize = 57.sp
    )
)