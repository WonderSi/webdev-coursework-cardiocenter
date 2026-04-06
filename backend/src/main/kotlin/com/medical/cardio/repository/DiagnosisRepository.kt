package com.medical.cardio.repository

import com.medical.cardio.entity.DiagnosisEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiagnosisRepository : JpaRepository<DiagnosisEntity, Long> {
    // Все диагнозы для конкретного пациента
    fun findAllByPatientId(patientId: Long): List<DiagnosisEntity>
}
