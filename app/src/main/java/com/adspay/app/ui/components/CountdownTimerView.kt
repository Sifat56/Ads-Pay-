package com.adspay.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.ui.theme.*

@Composable
fun CountdownTimerView(
    remainingSeconds: Int,
    totalSeconds: Int = 10,
    modifier: Modifier = Modifier
) {
    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer")

    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(PurpleSubtle),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            color = if (remainingSeconds <= 3) RedError else PurplePrimary,
            trackColor = PurpleLighter,
            strokeCap = StrokeCap.Round
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${remainingSeconds}s",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (remainingSeconds <= 3) RedError else PurpleDark
            )
            Text(
                text = "WAIT",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}
