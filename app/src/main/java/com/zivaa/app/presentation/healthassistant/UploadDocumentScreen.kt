package com.zivaa.app.presentation.healthassistant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentScreen(
    title: String,
    subtitle: String,
    label: String,
    viewModel: UploadDocumentViewModel? = null,
    onNavigateBack: () -> Unit,
    onUploadOptionSelected: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val bgColors = ZivaaTheme.colors
    val context = LocalContext.current

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    
    // Explicit Consent State
    var showConsentDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val uploadState by viewModel?.uploadState?.collectAsState(initial = UploadState.Idle) ?: remember { mutableStateOf(UploadState.Idle) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            selectedUri = tempPhotoUri
            viewModel?.uploadDocument(context, tempPhotoUri!!) {
                onUploadOptionSelected()
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            viewModel?.uploadDocument(context, uri) {
                onUploadOptionSelected()
            }
        }
    }

    LaunchedEffect(uploadState) {
        // Removed auto-navigation so user can read the success page and click continue
    }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text(text = "Privacy & Security Notice", style = ZivaaTheme.typography.cardTitle) },
            text = { Text(text = "Zivaa uses Cloud AI to process your medical reports. By continuing, you consent to your data being processed securely in the cloud to extract insights.") },
            confirmButton = {
                TextButton(onClick = {
                    showConsentDialog = false
                    pendingAction?.invoke()
                    pendingAction = null
                }) {
                    Text("I Agree")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DateRequired block removed because missing dates are handled asynchronously via NudgeAlerts.

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
                text = label.toEyebrowTitleCase(),
                style = ZivaaTheme.typography.eyebrow,
                color = Color(0xFF111111)
            )
        }

        if (uploadState is UploadState.Success) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = bgColors.leaf,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Upload Received!",
                    style = ZivaaTheme.typography.cardTitle.copy(fontSize = 28.sp),
                    color = bgColors.textStrong
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Great decision! By uploading your lab report, Zivaa can build a complete picture of your health, track your biomarker trends over time, and automatically notify you of any concerning changes.",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = bgColors.textBody,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "We are looking into it.",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = bgColors.textStrong,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDarkTheme) Color(0xFF1E3A32) else bgColors.sage)
                        .clickable {
                            onUploadOptionSelected()
                            viewModel?.resetState()
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue to Health Wallet",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = if (isDarkTheme) Color(0xFFE9E5DD) else bgColors.sageInk
                        )
                    )
                }

                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
            }
        } else if (selectedUri != null) {
            // Preview State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected Document",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                )
                
                if (uploadState is UploadState.Uploading || uploadState is UploadState.Idle) {
                    // Let's assume Idle means it's about to upload, or just started
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Extracting FHIR data securely...",
                                style = ZivaaTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
                
                
                if (uploadState is UploadState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Upload Failed",
                                style = ZivaaTheme.typography.cardTitle,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (uploadState as UploadState.Error).message,
                                style = ZivaaTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { viewModel?.resetState(); selectedUri = null }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        } else {
            // Upload Options State
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = title,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        color = bgColors.textStrong
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = subtitle,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = bgColors.textBody
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Primary Upload Button
                val primaryBg = if (isDarkTheme) Color(0xFF1E3A32) else bgColors.sage
                val primaryText = if (isDarkTheme) Color(0xFFE9E5DD) else bgColors.sageInk
                val primaryIconBg = if (isDarkTheme) Color(0xFFE9E5DD).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.15f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(primaryBg)
                        .clickable {
                            pendingAction = {
                                val file = File(context.cacheDir, "camera_images/temp_photo_${System.currentTimeMillis()}.jpg")
                                file.parentFile?.mkdirs()
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                tempPhotoUri = uri
                                cameraLauncher.launch(uri)
                            }
                            showConsentDialog = true
                        }
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(primaryIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = primaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Take a photo",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    color = primaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Easiest — hold the paper flat, I'll do the rest.",
                                style = ZivaaTheme.typography.bodySmall.copy(
                                    fontSize = 14.sp,
                                    color = primaryText.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SecondaryUploadButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Description,
                        iconBg = bgColors.clay,
                        title = "Choose a file",
                        subtitle = "PDF or photo from the phone",
                        onClick = {
                            pendingAction = {
                                filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                            }
                            showConsentDialog = true
                        }
                    )

                    SecondaryUploadButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.History,
                        iconBg = bgColors.leaf,
                        title = "From WhatsApp",
                        subtitle = "The one Meera forwarded",
                        onClick = {
                            pendingAction = {
                                filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                            }
                            showConsentDialog = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Wrinkled paper, tilted photos, doctor's handwriting — all fine.",
                    style = ZivaaTheme.typography.bodySmall.copy(
                        color = bgColors.textMeta,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
            }
        }
    }
}

@Composable
fun SecondaryUploadButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val borderColor = if (isSystemInDarkTheme()) {
        Color.White.copy(alpha = 0.1f)
    } else {
        ZivaaTheme.colors.ink.copy(alpha = 0.1f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = ZivaaTheme.colors.textStrong,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = ZivaaTheme.typography.bodySmall.copy(
                    color = ZivaaTheme.colors.textBody,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
