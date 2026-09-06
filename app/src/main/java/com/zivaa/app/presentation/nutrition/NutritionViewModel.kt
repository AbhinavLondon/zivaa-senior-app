package com.zivaa.app.presentation.nutrition

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.ZivaaBackendClient
import com.zivaa.app.data.remote.FoodItemResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.AnalyzeMealTextRequest
import com.zivaa.app.data.remote.LogMealRequest
import com.zivaa.app.presentation.nutrition.components.FoodItem
import java.time.LocalDate
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream

sealed class NutritionAnalysisState {
    object Idle : NutritionAnalysisState()
    object Analyzing : NutritionAnalysisState()
    data class Success(val mealName: String?, val foods: List<FoodItemResult>, val analysis: String?) : NutritionAnalysisState()
    data class Error(val message: String) : NutritionAnalysisState()
}

data class NutritionGoalsState(
    val targetCalories: Int = 1800,
    val targetProteinG: Int = 90,
    val targetCarbsG: Int = 225,
    val targetFatG: Int = 60
)

class NutritionViewModel : ViewModel() {

    private val _analysisState = MutableStateFlow<NutritionAnalysisState>(NutritionAnalysisState.Idle)
    val analysisState: StateFlow<NutritionAnalysisState> = _analysisState

    private val _goalsState = MutableStateFlow(NutritionGoalsState())
    val goalsState: StateFlow<NutritionGoalsState> = _goalsState

    private val _mealsState = MutableStateFlow<Map<String, List<FoodItem>>>(emptyMap())
    val mealsState: StateFlow<Map<String, List<FoodItem>>> = _mealsState

    init {
        fetchNutritionGoals()
    }

    fun fetchMealsForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                val response = ZivaaBackendClient.apiService.getMeals(patientId, date.toString())
                if (response.isSuccessful) {
                    val meals = response.body() ?: emptyList()
                    val grouped = meals.groupBy { it.meal_type }.mapValues { entry ->
                        entry.value.map { dto ->
                            FoodItem(
                                id = dto.id,
                                name = dto.food_name,
                                calories = dto.calories,
                                initials = dto.food_name.take(2).uppercase(),
                                protein = dto.protein_g,
                                carbs = dto.carbs_g,
                                fat = dto.fat_g
                            )
                        }
                    }
                    _mealsState.value = grouped
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchNutritionGoals() {
        viewModelScope.launch {
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                val response = RetrofitClient.apiService.getPlanSetup(patientIdQuery = "eq.$patientId")
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val latest = response.body()!!.first()
                    
                    val cals = latest.targetCaloriesUser ?: latest.targetCaloriesSystem ?: 1800
                    val protein = latest.proteinGUser ?: latest.proteinGSystem ?: 90
                    val carbs = latest.carbsGUser ?: latest.carbsGSystem ?: 225
                    val fat = latest.fatGUser ?: latest.fatGSystem ?: 60

                    _goalsState.value = NutritionGoalsState(
                        targetCalories = cals,
                        targetProteinG = protein,
                        targetCarbsG = carbs,
                        targetFatG = fat
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun analyzeMealPhoto(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _analysisState.value = NutritionAnalysisState.Analyzing
            
            try {
                val compressedBytes = try {
                    compressImage(context, uri, maxDimension = 1280, quality = 80)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } ?: run {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }

                if (compressedBytes == null || compressedBytes.isEmpty()) {
                    _analysisState.value = NutritionAnalysisState.Error("Failed to read image file.")
                    return@launch
                }

                val requestFile = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", "meal_photo.jpg", requestFile)

                val response = ZivaaBackendClient.apiService.analyzeMealPhoto(filePart)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.error == null) {
                        _analysisState.value = NutritionAnalysisState.Success(body.meal_name, body.foods, body.analysis)
                    } else {
                        _analysisState.value = NutritionAnalysisState.Error(body?.error ?: "Unknown error")
                    }
                } else {
                    _analysisState.value = NutritionAnalysisState.Error("Server error: ${response.code()}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _analysisState.value = NutritionAnalysisState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun compressImage(context: Context, uri: Uri, maxDimension: Int, quality: Int): ByteArray? {
        val contentResolver = context.contentResolver

        // 1. Decode bounds
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOptions)
        } ?: return null

        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight
        if (origW <= 0 || origH <= 0) return null

        // 2. Compute sample size
        var sampleSize = 1
        val maxSide = maxOf(origW, origH)
        while (maxSide / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }

        // 3. Decode sampled bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val sampledBitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: return null

        // 4. Check EXIF orientation
        val rotationDegrees = try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            0f
        }

        val matrix = Matrix()
        if (rotationDegrees != 0f) {
            matrix.postRotate(rotationDegrees)
        }

        // 5. Scale if needed
        val curMax = maxOf(sampledBitmap.width, sampledBitmap.height)
        if (curMax > maxDimension) {
            val scale = maxDimension.toFloat() / curMax
            matrix.postScale(scale, scale)
        }

        val finalBitmap = if (!matrix.isIdentity) {
            Bitmap.createBitmap(sampledBitmap, 0, 0, sampledBitmap.width, sampledBitmap.height, matrix, true).also {
                if (it != sampledBitmap) sampledBitmap.recycle()
            }
        } else {
            sampledBitmap
        }

        // 6. Compress to JPEG
        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        finalBitmap.recycle()
        return outputStream.toByteArray()
    }

    fun analyzeMealText(text: String) {
        viewModelScope.launch {
            _analysisState.value = NutritionAnalysisState.Analyzing
            try {
                val request = AnalyzeMealTextRequest(text = text)
                val response = ZivaaBackendClient.apiService.analyzeMealText(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.error == null) {
                        _analysisState.value = NutritionAnalysisState.Success(body.meal_name, body.foods, body.analysis)
                    } else {
                        _analysisState.value = NutritionAnalysisState.Error(body?.error ?: "Unknown error")
                    }
                } else {
                    _analysisState.value = NutritionAnalysisState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _analysisState.value = NutritionAnalysisState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun analyzeBarcode(barcode: String) {
        viewModelScope.launch {
            _analysisState.value = NutritionAnalysisState.Analyzing
            try {
                val request = com.zivaa.app.data.remote.AnalyzeBarcodeRequest(barcode = barcode)
                val response = ZivaaBackendClient.apiService.analyzeBarcode(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.error == null) {
                        _analysisState.value = NutritionAnalysisState.Success(body.meal_name, body.foods, body.analysis)
                    } else {
                        _analysisState.value = NutritionAnalysisState.Error(body?.error ?: "Unknown error")
                    }
                } else {
                    _analysisState.value = NutritionAnalysisState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _analysisState.value = NutritionAnalysisState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun saveMealToDatabase(mealName: String, mealQuantity: Float, mealType: String, loggingMethod: String, analysisText: String?, foods: List<FoodItemResult>, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                val request = LogMealRequest(
                    patient_id = patientId,
                    meal_type = mealType,
                    meal_name = mealName,
                    meal_quantity = mealQuantity.toDouble(),
                    logging_method = loggingMethod,
                    ai_analysis = analysisText,
                    foods = foods,
                    date = date.toString()
                )
                val response = ZivaaBackendClient.apiService.logMeal(request)
                if (response.isSuccessful) {
                    println("Meal logged successfully: ${response.body()?.message}")
                    // Refetch meals for the selected date after a successful log
                    fetchMealsForDate(date)
                } else {
                    val err = response.errorBody()?.string()
                    println("Failed to log meal. Code: ${response.code()}, Error: $err")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMeal(mealId: String, currentDate: LocalDate) {
        viewModelScope.launch {
            try {
                val response = ZivaaBackendClient.apiService.deleteMeal(mealId)
                if (response.isSuccessful) {
                    fetchMealsForDate(currentDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _analysisState.value = NutritionAnalysisState.Idle
    }
}

class NutritionViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
