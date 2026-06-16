package com.tripplanner.android.feature.itinerary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripplanner.android.R
import com.tripplanner.android.ui.components.TripCategory

/**
 * Horizontal bar breakdown of budget by category, with bars that grow in on
 * first composition. Replaces the web viewer's Recharts breakdown using the
 * design-system category palette.
 */
@Composable
fun BudgetByCategoryChart(
    breakdown: List<Pair<TripCategory, Long>>,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    if (breakdown.isEmpty()) return
    val max = breakdown.maxOf { it.second }.coerceAtLeast(1)

    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(breakdown) { animate = true }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        breakdown.forEach { (category, amount) ->
            val target = (amount.toFloat() / max).coerceIn(0f, 1f)
            val fraction by animateFloatAsState(
                targetValue = if (animate) target else 0f,
                animationSpec = tween(600),
                label = "bar_${category.name}",
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(category.labelRes), style = MaterialTheme.typography.bodySmall)
                    Text(
                        currency.format(amount),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(category.color),
                    )
                }
            }
        }
    }
}
