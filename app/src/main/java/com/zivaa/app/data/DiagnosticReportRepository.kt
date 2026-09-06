package com.zivaa.app.data

import com.zivaa.app.data.local.DiagnosticReportDao
import com.zivaa.app.data.local.DiagnosticReportEntity
import com.zivaa.app.data.remote.SupabaseApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiagnosticReportRepository(
    private val apiService: SupabaseApiService,
    private val dao: DiagnosticReportDao
) {
    fun getReports(patientId: String): Flow<List<DiagnosticReportEntity>> {
        return dao.getAllReports(patientId)
    }

    suspend fun syncReports(patientId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDiagnosticReports("eq.$patientId")
                if (response.isSuccessful) {
                    val reports = response.body() ?: emptyList()
                    val entities = reports.map { report ->
                        val codeText = report.resource?.code?.text ?: ""
                        val performer = report.resource?.performer?.firstOrNull()?.display ?: ""
                        val title = codeText.ifEmpty { "Unknown Report" }
                        
                        val effectiveDate = report.resource?.effectiveDateTime ?: ""
                        
                        DiagnosticReportEntity(
                            id = report.id,
                            patientId = patientId,
                            title = title,
                            performer = performer,
                            effectiveDate = effectiveDate
                        )
                    }
                    dao.deleteAll(patientId)
                    dao.insertAll(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
