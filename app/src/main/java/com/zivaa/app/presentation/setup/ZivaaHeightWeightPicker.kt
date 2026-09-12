package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun UnitTogglePill(
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ZivaaTheme.colors.bgElev,
        border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
    ) {
        Row(
            modifier = modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Surface(
                    onClick = { onSelect(option) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ZivaaTheme.colors.sage else Color.Transparent
                ) {
                    Text(
                        text = option,
                        style = ZivaaTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else ZivaaTheme.colors.inkMute,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorHeightBottomSheet(
    initialHeightInches: Int?,
    onDismissRequest: () -> Unit,
    onHeightSelected: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var heightUnit by remember { mutableStateOf("ft/in") }

    val currentInches = initialHeightInches ?: 67
    val initialFeet = (currentInches / 12).coerceIn(3, 7)
    val initialInchesPart = (currentInches % 12).coerceIn(0, 11)
    val initialCm = (currentInches * 2.54).roundToInt().coerceIn(120, 220)

    val feetList = remember { (3..7).toList() }
    val inchesList = remember { (0..11).toList() }
    val cmList = remember { (120..220).toList() }

    val feetState = rememberLazyListState(initialFirstVisibleItemIndex = feetList.indexOf(initialFeet).coerceAtLeast(0))
    val inchesState = rememberLazyListState(initialFirstVisibleItemIndex = inchesList.indexOf(initialInchesPart).coerceAtLeast(0))
    val cmState = rememberLazyListState(initialFirstVisibleItemIndex = cmList.indexOf(initialCm).coerceAtLeast(0))

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "How tall are you?",
                    style = ZivaaTheme.typography.titleLarge,
                    color = ZivaaTheme.colors.ink
                )
                UnitTogglePill(
                    options = listOf("ft/in", "cm"),
                    selectedOption = heightUnit,
                    onSelect = { newUnit ->
                        if (newUnit == "cm" && heightUnit == "ft/in") {
                            val ft = feetList.getOrNull(feetState.firstVisibleItemIndex) ?: 5
                            val inc = inchesList.getOrNull(inchesState.firstVisibleItemIndex) ?: 7
                            val cm = ((ft * 12 + inc) * 2.54).roundToInt().coerceIn(120, 220)
                            coroutineScope.launch { cmState.scrollToItem(cmList.indexOf(cm).coerceAtLeast(0)) }
                        } else if (newUnit == "ft/in" && heightUnit == "cm") {
                            val cm = cmList.getOrNull(cmState.firstVisibleItemIndex) ?: 170
                            val totInc = (cm / 2.54).roundToInt()
                            val ft = (totInc / 12).coerceIn(3, 7)
                            val inc = (totInc % 12).coerceIn(0, 11)
                            coroutineScope.launch {
                                feetState.scrollToItem(feetList.indexOf(ft).coerceAtLeast(0))
                                inchesState.scrollToItem(inchesList.indexOf(inc).coerceAtLeast(0))
                            }
                        }
                        heightUnit = newUnit
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(ZivaaTheme.colors.sage.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp))
                )

                if (heightUnit == "ft/in") {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZivaaWheelPicker(
                            items = feetList,
                            state = feetState,
                            modifier = Modifier.weight(1f),
                            itemToString = { "$it ft" }
                        )

                        ZivaaWheelPicker(
                            items = inchesList,
                            state = inchesState,
                            modifier = Modifier.weight(1f),
                            itemToString = { "$it in" }
                        )
                    }
                } else {
                    ZivaaWheelPicker(
                        items = cmList,
                        state = cmState,
                        modifier = Modifier.fillMaxWidth(0.5f),
                        itemToString = { "$it cm" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ZivaaButton(
                text = "Confirm Height",
                onClick = {
                    val finalInches = if (heightUnit == "ft/in") {
                        val ft = feetList.getOrNull(feetState.firstVisibleItemIndex) ?: 5
                        val inc = inchesList.getOrNull(inchesState.firstVisibleItemIndex) ?: 7
                        (ft * 12) + inc
                    } else {
                        val cm = cmList.getOrNull(cmState.firstVisibleItemIndex) ?: 170
                        (cm / 2.54).roundToInt()
                    }
                    onHeightSelected(finalInches)
                    onDismissRequest()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorWeightBottomSheet(
    initialWeightKg: Int?,
    title: String = "What is your weight?",
    onDismissRequest: () -> Unit,
    onWeightSelected: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var weightUnit by remember { mutableStateOf("kg") }

    val currentKg = initialWeightKg ?: 70
    val currentLbs = (currentKg * 2.20462).roundToInt().coerceIn(75, 400)

    val kgList = remember { (35..180).toList() }
    val lbsList = remember { (75..400).toList() }

    val kgState = rememberLazyListState(initialFirstVisibleItemIndex = kgList.indexOf(currentKg.coerceIn(35, 180)).coerceAtLeast(0))
    val lbsState = rememberLazyListState(initialFirstVisibleItemIndex = lbsList.indexOf(currentLbs.coerceIn(75, 400)).coerceAtLeast(0))

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.titleLarge,
                    color = ZivaaTheme.colors.ink
                )
                UnitTogglePill(
                    options = listOf("kg", "lbs"),
                    selectedOption = weightUnit,
                    onSelect = { newUnit ->
                        if (newUnit == "lbs" && weightUnit == "kg") {
                            val kg = kgList.getOrNull(kgState.firstVisibleItemIndex) ?: 70
                            val lbs = (kg * 2.20462).roundToInt().coerceIn(75, 400)
                            coroutineScope.launch { lbsState.scrollToItem(lbsList.indexOf(lbs).coerceAtLeast(0)) }
                        } else if (newUnit == "kg" && weightUnit == "lbs") {
                            val lbs = lbsList.getOrNull(lbsState.firstVisibleItemIndex) ?: 154
                            val kg = (lbs * 0.45359237).roundToInt().coerceIn(35, 180)
                            coroutineScope.launch { kgState.scrollToItem(kgList.indexOf(kg).coerceAtLeast(0)) }
                        }
                        weightUnit = newUnit
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(ZivaaTheme.colors.sage.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp))
                )

                if (weightUnit == "kg") {
                    ZivaaWheelPicker(
                        items = kgList,
                        state = kgState,
                        modifier = Modifier.fillMaxWidth(0.5f),
                        itemToString = { "$it kg" }
                    )
                } else {
                    ZivaaWheelPicker(
                        items = lbsList,
                        state = lbsState,
                        modifier = Modifier.fillMaxWidth(0.5f),
                        itemToString = { "$it lbs" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ZivaaButton(
                text = "Confirm Weight",
                onClick = {
                    val finalKg = if (weightUnit == "kg") {
                        kgList.getOrNull(kgState.firstVisibleItemIndex) ?: 70
                    } else {
                        val lbs = lbsList.getOrNull(lbsState.firstVisibleItemIndex) ?: 154
                        (lbs * 0.45359237).roundToInt()
                    }
                    onWeightSelected(finalKg)
                    onDismissRequest()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
