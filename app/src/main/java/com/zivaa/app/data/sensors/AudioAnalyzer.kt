package com.zivaa.app.data.sensors

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class AudioAnalyzer(private val dataStore: SensorDataStore) {
    
    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    
    @SuppressLint("MissingPermission")
    suspend fun startListening() = withContext(Dispatchers.IO) {
        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext
            }

            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize)

            while (isActive) {
                val readResult = audioRecord.read(buffer, 0, buffer.size)
                if (readResult > 0) {
                    val rms = calculateRMS(buffer, readResult)
                    // HEURISTIC: A very simple amplitude threshold placeholder.
                    // In production, this should be replaced by a TFLite classification model
                    // that classifies the audio buffer as 'cough' or 'snore'.
                    if (rms > 1500) {
                        // Classify as loud event -> record it
                        // (Mock heuristic: randomly distribute between cough/snore for testing)
                        if (Math.random() > 0.5) {
                            dataStore.incrementCoughCount()
                        } else {
                            dataStore.incrementSnoringEvent()
                        }
                        
                        // Sleep to avoid over-counting a single loud noise
                        delay(2000)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord?.stop()
            audioRecord?.release()
        }
    }

    private fun calculateRMS(buffer: ShortArray, length: Int): Double {
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }
        return sqrt(sum / length)
    }
}
