package com.example.todo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Monochrome toggle chip. Pass [Modifier.weight(1f)] from the call site
 * when used inside a Row — this keeps the composable free of RowScope coupling.
 */
@Composable
fun SegmentButton(
    text: String,
    isSelected: Boolean,
    fgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val textColor = when {
        !isSelected -> fgColor
        fgColor == Color.White -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, if (isSelected) fgColor else borderColor, RectangleShape)
            .background(if (isSelected) fgColor else Color.Transparent)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
        )
    }
}