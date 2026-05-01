package com.akmeczo.votersystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object UiTokens {
    val screenHorizontalPadding = 24.dp
    val screenVerticalPadding = 24.dp
    val listHorizontalPadding = 20.dp
    val listVerticalPadding = 16.dp
    val sectionGap = 20.dp
    val componentGap = 16.dp
    val heroGap = 56.dp
    val titleToFormGap = 72.dp
    val largeGap = 60.dp
    val detailGap = 24.dp
    val smallGap = 8.dp
    val cardBottomPadding = 20.dp
    val cardInnerGap = 8.dp
    val sectionLabelGap = 12.dp
    val resultsHorizontalPadding = 12.dp
    val roundedSize = 999.dp
    val inputWidth = 220.dp
    val buttonWidth = 164.dp
    val fieldHeight = 48.dp
    val detailCardWidth = 240.dp
}

private val appBackground = Color(0xFFF4ECEE)
private val appSurface = Color(0xFFF1EAEC)
private val appCardWhite = Color(0xFFFFFFFF)
private val appButton = Color(0xFFFBF7F8)
private val appBorder = Color(0xFFD8CACE)
private val appText = Color(0xFF2F2527)
private val appMutedText = Color(0xFF6F6165)

fun Modifier.appBackground(): Modifier = background(appBackground)

@Composable
fun AppTitleText() {
    Text(
        text = "Szavazz rám!",
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            color = appText,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun ScreenTitleText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = appText,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun RoundedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = UiTokens.buttonWidth
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .height(UiTokens.fieldHeight),
        shape = RoundedCornerShape(UiTokens.roundedSize),
        colors = ButtonDefaults.buttonColors(
            containerColor = appButton,
            contentColor = appText
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun RoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.width(UiTokens.inputWidth),
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(UiTokens.roundedSize),
        colors = pillFieldColors()
    )
}

@Composable
fun RoundedPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.width(UiTokens.inputWidth),
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(UiTokens.roundedSize),
        colors = pillFieldColors()
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = appSurface,
    cornerRadius: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = UiTokens.cardBottomPadding),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(UiTokens.sectionGap),
            content = content
        )
    }
}

val AppCardWhite: Color
    get() = appCardWhite

@Composable
fun BottomActionButtons(
    leftText: String,
    rightText: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RoundedActionButton(text = leftText, onClick = onLeftClick)
        RoundedActionButton(text = rightText, onClick = onRightClick)
    }
}

@Composable
fun CardTitleText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = appText,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun SectionLabelText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = appText
        )
    )
}

@Composable
fun MetaText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 13.sp,
            color = appMutedText
        )
    )
}

@Composable
fun BodyText(text: String, centered: Boolean = false) {
    Text(
        text = text,
        modifier = if (centered) Modifier.fillMaxWidth() else Modifier,
        style = TextStyle(
            fontSize = 14.sp,
            color = appText,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )
    )
}

@Composable
fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = UiTokens.sectionLabelGap),
        color = appBorder
    )
}

@Composable
fun VerticalSectionDivider() {
    Spacer(
        modifier = Modifier
            .height(120.dp)
            .width(1.dp)
            .background(appBorder)
    )
}

@Composable
private fun pillFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = appButton,
    unfocusedContainerColor = appButton,
    focusedBorderColor = appBorder,
    unfocusedBorderColor = appBorder,
    focusedTextColor = appText,
    unfocusedTextColor = appText,
    focusedPlaceholderColor = appMutedText,
    unfocusedPlaceholderColor = appMutedText
)

@Composable
fun ErrorPopup(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appSurface,
        titleContentColor = appText,
        textContentColor = appText,
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = appText
                )
            )
        },
        text = {
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = appText
                )
            )
        },
        confirmButton = {
            RoundedActionButton(
                text = "OK",
                onClick = onDismiss,
                width = 96.dp
            )
        }
    )
}
