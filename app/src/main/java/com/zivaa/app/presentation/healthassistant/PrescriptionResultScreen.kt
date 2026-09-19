package com.zivaa.app.presentation.healthassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.remote.AuthManager
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabasePatientDocument
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PrescriptionResultScreen(
    onNavigateBack: () -> Unit,
    onSetReminders: () -> Unit,
    onAskQuestion: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val bgColors = ZivaaTheme.colors
    val isDarkTheme = isSystemInDarkTheme()
    
    var document by remember { mutableStateOf<SupabasePatientDocument?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val patientId = authManager.getUserId() ?: "default_patient"
                val response = RetrofitClient.apiService.getPatientDocuments(
                    patientIdQuery = "eq.$patientId",
                    documentTypeQuery = "eq.Prescription"
                )
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    document = response.body()!!.first()
                } else {
                    error = "Could not load prescription data."
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, bgColors.ink.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColors.bgElev)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Back",
                    tint = bgColors.textStrong
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "PRESCRIPTION",
                style = ZivaaTheme.typography.meta.copy(letterSpacing = 0.1.sp),
                color = bgColors.textMeta
            )
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = bgColors.sage)
            }
        } else if (error != null || document == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${error ?: "No prescriptions found"}", color = bgColors.textMeta)
            }
        } else {
            val data = document!!.extracted_data
            val doctorName = data?.get("doctor_name")?.takeIf { !it.isJsonNull }?.asString?.uppercase() ?: "YOUR DOCTOR"
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 130.dp)
            ) {
                item {
                    Text(
                        text = "Let's read the doctor's hand.",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 36.sp,
                            lineHeight = 40.sp,
                            color = bgColors.textStrong
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Read indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${doctorName} • LATEST",
                            style = ZivaaTheme.typography.meta,
                            color = bgColors.textMeta
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(bgColors.leaf.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = bgColors.leaf,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "READ",
                                    style = ZivaaTheme.typography.meta.copy(fontSize = 9.sp),
                                    color = bgColors.leaf
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Medications ---
                val meds = data?.getAsJsonArray("medications")
                if (meds != null && meds.size() > 0) {
                    item {
                        Text(
                            text = "${meds.size()} medicines • here's the routine.",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = bgColors.textStrong)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(meds.size()) { idx ->
                        val m = meds.get(idx).asJsonObject
                        MedicineRoutineCard(
                            name = m.get("name")?.asString ?: "",
                            strength = m.get("strength")?.takeIf{ !it.isJsonNull }?.asString,
                            schedule = m.get("schedule")?.takeIf{ !it.isJsonNull }?.asString ?: "",
                            notes = m.get("notes")?.takeIf{ !it.isJsonNull }?.asString
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // --- Exercises ---
                val exercises = data?.getAsJsonArray("exercises")
                if (exercises != null && exercises.size() > 0) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${exercises.size()} physical exercises prescribed.",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = bgColors.textStrong)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(exercises.size()) { idx ->
                        val ex = exercises.get(idx).asJsonObject
                        val name = ex.get("name")?.asString ?: ""
                        val details = ex.get("sets_reps_duration")?.takeIf{ !it.isJsonNull }?.asString
                        MedicineRoutineCard(
                            name = name,
                            strength = "Exercise",
                            schedule = details ?: "Daily",
                            notes = null
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // --- Labs ---
                val labs = data?.getAsJsonArray("lab_orders")
                if (labs != null && labs.size() > 0) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Lab tests ordered:",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = bgColors.textStrong)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(labs.size()) { idx ->
                        val lb = labs.get(idx).asJsonObject
                        RoutineInfoNote(text = "${lb.get("test_name")?.asString} (${lb.get("timeframe")?.asString ?: "Soon"})")
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                // --- Lifestyle ---
                val lifestyle = data?.getAsJsonArray("lifestyle_diet")
                if (lifestyle != null && lifestyle.size() > 0) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Lifestyle & Diet:",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = bgColors.textStrong)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(lifestyle.size()) { idx ->
                        RoutineInfoNote(text = lifestyle.get(idx).asString)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // --- Follow Up ---
                val followUp = data?.get("follow_up")?.takeIf { !it.isJsonNull }?.asString
                if (!followUp.isNullOrBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Follow up:",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = bgColors.textStrong)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RoutineInfoNote(text = followUp)
                    }
                }
                
                // Document View Button
                if (document!!.file_url != null) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { /* MOCK: Open URL */ }
                                .border(1.dp, bgColors.ink.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VIEW ORIGINAL PRESCRIPTION",
                                style = ZivaaTheme.typography.meta.copy(fontSize = 13.sp),
                                color = bgColors.textStrong
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineRoutineCard(
    name: String,
    strength: String?,
    schedule: String,
    notes: String?
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cardBg = ZivaaTheme.colors.bgElev
    val borderColor = if (isDarkTheme) Color(0xFF2E2E2E) else Color(0xFFE8E8E8)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = ZivaaTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            color = ZivaaTheme.colors.textStrong
                        )
                    )
                    if (strength != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strength,
                            style = ZivaaTheme.typography.meta,
                            color = ZivaaTheme.colors.textMeta,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(ZivaaTheme.colors.bg, RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = schedule,
                        style = ZivaaTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = ZivaaTheme.colors.textStrong
                        )
                    )
                }
            }
            
            if (notes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = notes,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        color = ZivaaTheme.colors.textBody,
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RoutineInfoNote(text: String) {
    val isDarkTheme = isSystemInDarkTheme()
    val noteBg = if (isDarkTheme) Color(0xFF2A1215) else ZivaaTheme.colors.eveningTint
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(noteBg)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = ZivaaTheme.typography.bodyMedium.copy(
                color = if (isDarkTheme) Color(0xFFE9E5DD) else ZivaaTheme.colors.textStrong,
                lineHeight = 22.sp
            )
        )
    }
}
