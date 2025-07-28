package klubi.plussipoisid.justputitin.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun NumberPickerRow(range: IntRange, selected: Int?, onSelected: (Int) -> Unit) {
    val itemSize = 56.dp
    val selectedItemSize = 60.dp
    val contentPadding = (itemSize / 2)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to selected on first show and whenever selected changes
    LaunchedEffect(selected) {
        if (selected != null) {
            val idx = range.indexOf(selected)
            if (idx >= 0) {
                coroutineScope.launch {
                    listState.animateScrollToItem(idx)
                }
            }
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = contentPadding),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(range.toList()) { value ->
            val isSelected = selected != null && value == selected
            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xffee632c) else Color.LightGray,
                animationSpec = tween(durationMillis = 300), label = ""
            )
            val animatedElevation by animateDpAsState(
                targetValue = if (isSelected) 12.dp else 2.dp,
                animationSpec = tween(durationMillis = 300), label = ""
            )
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = animatedColor),
                elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                modifier = Modifier
                    .size(if (isSelected) selectedItemSize else itemSize)
                    .shadow(if (isSelected) 16.dp else 2.dp, CircleShape)
                    .clickable { onSelected(value) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = value.toString(),
                        style = if (isSelected) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                        color = Color(0xff022f33)
                    )
                }
            }
        }
    }
} 