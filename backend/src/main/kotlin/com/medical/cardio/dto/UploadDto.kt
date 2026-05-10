package com.medical.cardio.dto

data class UploadReport(
    val totalRows: Int,
    val savedRows: Int,
    val skippedRows: Int,
    val errors: List<RowError>
)

data class RowError(
    val row: Int,
    val reason: String
)
