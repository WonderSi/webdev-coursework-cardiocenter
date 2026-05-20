package com.medical.cardio.repository

import com.medical.cardio.dto.DiagnosisCountRaw
import com.medical.cardio.entity.DiagnosisEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DiagnosisRepository : JpaRepository<DiagnosisEntity, Long> {

    fun findAllByPatientId(patientId: Long): List<DiagnosisEntity>

    @Query("SELECT d.diagnosis.value AS name, COUNT(d) AS count FROM DiagnosisEntity d GROUP BY d.diagnosis.id, d.diagnosis.value ORDER BY count DESC")
    fun countByDiagnosis(): List<DiagnosisCountRaw>
}
