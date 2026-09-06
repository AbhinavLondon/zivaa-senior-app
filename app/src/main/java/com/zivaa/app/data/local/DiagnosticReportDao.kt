package com.zivaa.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticReportDao {
    @Query("SELECT * FROM diagnostic_reports WHERE patientId = :patientId ORDER BY effectiveDate DESC")
    fun getAllReports(patientId: String): Flow<List<DiagnosticReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<DiagnosticReportEntity>)
    
    @Query("DELETE FROM diagnostic_reports WHERE patientId = :patientId")
    suspend fun deleteAll(patientId: String)
}
