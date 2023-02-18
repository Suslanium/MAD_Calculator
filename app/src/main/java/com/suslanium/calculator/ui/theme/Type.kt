package com.suslanium.calculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.suslanium.calculator.R

val GoogleSans = FontFamily(
    Font(R.font.googlesans_regular, FontWeight.Normal),
    Font(R.font.googlesans_bold, FontWeight.Bold),
    Font(R.font.googlesans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.googlesans_medium, FontWeight.Medium),
    Font(R.font.googlesans_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.googlesans_mediumitalic, FontWeight.Medium, FontStyle.Italic),
)

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