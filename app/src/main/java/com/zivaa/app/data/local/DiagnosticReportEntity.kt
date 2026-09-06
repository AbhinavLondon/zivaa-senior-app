package com.zivaa.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostic_reports")
data class DiagnosticReportEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val title: String,
    val performer: String,
    val effectiveDate: String
)
