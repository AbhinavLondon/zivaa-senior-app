package com.zivaa.app.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorDobBottomSheet(
    initialDate: String, // format "dd-mm-yyyy" or empty
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Parse initial date
    var initialDay = 15
    var initialMonth = 6
    var initialYear = 1960
    
    if (initialDate.isNotBlank() && initialDate.length == 10) {
        try {
            val parts = initialDate.split("-")
            initialDay = parts[0].toInt()
            initialMonth = parts[1].toInt()
            initialYear = parts[2].toInt()
        } catch (e: Exception) {
            // Ignore parse errors, use defaults
        }
    }

    val days = (1..31).toList()
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val currentYear = LocalDate.now().year
    val years = (currentYear - 100..currentYear - 18).toList().reversed()

    val dayState = rememberLazyListState(initialFirstVisibleItemIndex = days.indexOf(initialDay).coerceAtLeast(0))
    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = (initialMonth - 1).coerceIn(0, 11))
    val yearState = rememberLazyListState(initialFirstVisibleItemIndex = years.indexOf(initialYear).coerceAtLeast(0))

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ZivaaTheme.colors.bg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ZivaaTheme.colors.inkMute.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "When were you born?",
                style = ZivaaTheme.typography.titleLarge,
                color = ZivaaTheme.colors.ink,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // The Picker row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Highlight bar in the center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(ZivaaTheme.colors.sage.copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium)
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Day Picker
                    ZivaaWheelPicker(
                        items = days,
                        state = dayState,
                        modifier = Modifier.weight(1f),
                        itemToString = { it.toString().padStart(2, '0') }
                    )

                    // Month Picker
                    ZivaaWheelPicker(
                        items = months,
                        state = monthState,
                        modifier = Modifier.weight(1.5f),
                        itemToString = { it }
                    )

                    // Year Picker
                    ZivaaWheelPicker(
                        items = years,
                        state = yearState,
                        modifier = Modifier.weight(1.5f),
                        itemToString = { it.toString() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            ZivaaButton(
                text = "Confirm Date",
                onClick = {
                    val selectedDay = days.getOrNull(dayState.firstVisibleItemIndex) ?: days.first()
                    val selectedMonthIdx = monthState.firstVisibleItemIndex
                    val selectedYear = years.getOrNull(yearState.firstVisibleItemIndex) ?: years.first()
                    
                    val formattedMonth = (selectedMonthIdx + 1).toString().padStart(2, '0')
                    val formattedDay = selectedDay.toString().padStart(2, '0')
                    val finalDate = "$formattedDay-$formattedMonth-$selectedYear"
                    
                    onDateSelected(finalDate)
                    onDismissRequest()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun <T> ZivaaWheelPicker(
    items: List<T>,
    state: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
    itemToString: (T) -> String
) {
    // Add empty items for padding (2 top, 2 bottom) so the first actual item can be centered
    val paddedItems = remember(items) {
        val list = mutableListOf<String?>()
        list.add(null)
        list.add(null)
        list.addAll(items.map { itemToString(it) })
        list.add(null)
        list.add(null)
        list
    }

    val itemHeight = 56.dp

    LazyColumn(
        state = state,
        modifier = modifier.height(itemHeight * 5),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    ) {
        items(paddedItems.size) { index ->
            // In LazyColumn, when state.firstVisibleItemIndex is 0, the items visible are 0, 1, 2, 3, 4.
            // Center item is index 2. So center is always firstVisibleItemIndex + 2.
            // Wait, we also need to account for scroll offset if not perfectly snapped, but with snap behavior, it's mostly fine.
            // To make it dynamic during scroll, we can derive it from state.firstVisibleItemIndex and state.firstVisibleItemScrollOffset
            val firstVisible = state.firstVisibleItemIndex
            val offset = state.firstVisibleItemScrollOffset
            
            // If scrolled more than half, the active item switches
            val activeIndex = if (offset > 56 /* roughly item height in px, actual depends on density, let's keep it simple */) {
                firstVisible + 2 // Wait, this logic is tricky without LayoutInfo. Let's stick to firstVisibleItemIndex + 2 for style.
            } else {
                firstVisible + 2
            }
            
            val isCenter = index == firstVisible + 2
            val text = paddedItems[index] ?: ""
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = ZivaaTheme.typography.titleLarge.copy(
                            fontSize = if (isCenter) 28.sp else 22.sp,
                            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isCenter) ZivaaTheme.colors.ink else ZivaaTheme.colors.inkMute,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(if (isCenter) 1f else 0.5f)
                    )
                }
            }
        }
    }
}
