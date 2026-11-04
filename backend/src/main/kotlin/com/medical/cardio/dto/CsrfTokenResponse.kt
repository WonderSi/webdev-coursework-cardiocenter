package com.medical.cardio.dto

data class CsrfTokenResponse(
    val headerName: String,
    val token: String
)
