package com.medical.cardio.repository

import com.medical.cardio.dto.GenderProjection
import com.medical.cardio.entity.PatientEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface PatientRepository : JpaRepository<PatientEntity, Long>, JpaSpecificationExecutor<PatientEntity> {
    // Таблица пациентов с фильтрами и пагинацией на /admin/data
    override fun findAll(spec: Specification<PatientEntity>?, pageable: Pageable): Page<PatientEntity>

    // Все пациенты для переобучения ML
    override fun findAll(): List<PatientEntity>

    // Все пациенты вместе с их диагнозами одним запросом
    @Query("SELECT DISTINCT p FROM PatientEntity p LEFT JOIN FETCH p.diagnoses")
    fun findAllWithDiagnoses(): List<PatientEntity>

    @Query("SELECT COUNT(p) FROM PatientEntity p")
    fun countAll(): Long

    @Query("SELECT p.gender.value AS label, COUNT(p) AS count FROM PatientEntity p GROUP BY p.gender.id, p.gender.value")
    fun countByGender(): List<GenderProjection>

    @Query("SELECT AVG(p.age) FROM PatientEntity p")
    fun averageAge(): Double?
}
