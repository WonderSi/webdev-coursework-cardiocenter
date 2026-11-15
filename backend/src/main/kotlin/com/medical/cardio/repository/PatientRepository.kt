package com.medical.cardio.repository

import com.medical.cardio.entity.PatientEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface PatientRepository : JpaRepository<PatientEntity, Long>, JpaSpecificationExecutor<PatientEntity> {

    override fun findAll(spec: Specification<PatientEntity>?, pageable: Pageable): Page<PatientEntity>

    override fun findAll(): List<PatientEntity>

    @Query("SELECT DISTINCT p FROM PatientEntity p LEFT JOIN FETCH p.diagnoses")
    fun findAllWithDiagnoses(): List<PatientEntity>

}
