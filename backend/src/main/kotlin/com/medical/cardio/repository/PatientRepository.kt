package com.medical.cardio.repository

import com.medical.cardio.dto.*
import com.medical.cardio.entity.PatientEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PatientRepository : JpaRepository<PatientEntity, Long>, JpaSpecificationExecutor<PatientEntity> {

    override fun findAll(spec: Specification<PatientEntity>?, pageable: Pageable): Page<PatientEntity>

    override fun findAll(): List<PatientEntity>

    @Query("SELECT DISTINCT p FROM PatientEntity p LEFT JOIN FETCH p.diagnoses")
    fun findAllWithDiagnoses(): List<PatientEntity>

    @Query("SELECT COUNT(p) FROM PatientEntity p")
    fun countAll(): Long

    @Query("SELECT AVG(p.age) FROM PatientEntity p")
    fun averageAge(): Double?

    @Query("SELECT p.gender.value AS label, COUNT(p) AS count FROM PatientEntity p GROUP BY p.gender.id, p.gender.value")
    fun countByGender(): List<GenderProjection>

    @Query(value = """
        SELECT gv.value            AS label,
               COUNT(p.id)         AS total,
               COUNT(diag.patient_id) AS diagnosed
        FROM patients p
        JOIN glossary_values gv ON p.gender_id = gv.id
        LEFT JOIN (SELECT DISTINCT patient_id FROM diagnoses) diag ON p.id = diag.patient_id
        GROUP BY gv.id, gv.value
    """, nativeQuery = true)
    fun countDiagnosedByGender(): List<GenderDiagnosisRaw>

    @Query(value = """
        SELECT CASE WHEN p.age < 30  THEN '<30'
                    WHEN p.age < 40  THEN '30-39'
                    WHEN p.age < 50  THEN '40-49'
                    WHEN p.age < 60  THEN '50-59'
                    WHEN p.age < 70  THEN '60-69'
                    ELSE '70+' END                  AS age_group,
               gv.value                             AS gender,
               diag.patient_id IS NOT NULL          AS is_diagnosed,
               COUNT(p.id)                          AS count
        FROM patients p
        JOIN glossary_values gv ON p.gender_id = gv.id
        LEFT JOIN (SELECT DISTINCT patient_id FROM diagnoses) diag ON p.id = diag.patient_id
        GROUP BY CASE WHEN p.age < 30  THEN '<30'
                      WHEN p.age < 40  THEN '30-39'
                      WHEN p.age < 50  THEN '40-49'
                      WHEN p.age < 60  THEN '50-59'
                      WHEN p.age < 70  THEN '60-69'
                      ELSE '70+' END,
                 gv.id, gv.value,
                 diag.patient_id IS NOT NULL
    """, nativeQuery = true)
    fun countByAgeGroupAndGender(): List<AgeGenderRaw>

    @Query(value = """
        SELECT CASE WHEN p.weight / POWER(p.height / 100.0, 2) < 18.5 THEN 'Недостаточный'
                    WHEN p.weight / POWER(p.height / 100.0, 2) < 25.0 THEN 'Нормальный'
                    WHEN p.weight / POWER(p.height / 100.0, 2) < 30.0 THEN 'Избыточный'
                    ELSE 'Ожирение' END         AS category,
               diag.patient_id IS NOT NULL      AS is_diagnosed,
               COUNT(p.id)                      AS count
        FROM patients p
        LEFT JOIN (SELECT DISTINCT patient_id FROM diagnoses) diag ON p.id = diag.patient_id
        WHERE p.height IS NOT NULL AND p.weight IS NOT NULL AND p.height > 0
        GROUP BY CASE WHEN p.weight / POWER(p.height / 100.0, 2) < 18.5 THEN 'Недостаточный'
                      WHEN p.weight / POWER(p.height / 100.0, 2) < 25.0 THEN 'Нормальный'
                      WHEN p.weight / POWER(p.height / 100.0, 2) < 30.0 THEN 'Избыточный'
                      ELSE 'Ожирение' END,
                 diag.patient_id IS NOT NULL
    """, nativeQuery = true)
    fun countByBmiCategory(): List<CategoryDiagnosisRaw>

    @Query(value = """
        SELECT gv.value                  AS category,
               diag.patient_id IS NOT NULL AS is_diagnosed,
               COUNT(p.id)               AS count
        FROM patients p
        JOIN glossary_values gv ON p.alcohol_id = gv.id
        LEFT JOIN (SELECT DISTINCT patient_id FROM diagnoses) diag ON p.id = diag.patient_id
        WHERE p.alcohol_id IS NOT NULL
        GROUP BY gv.id, gv.value, diag.patient_id IS NOT NULL
    """, nativeQuery = true)
    fun countByAlcohol(): List<CategoryDiagnosisRaw>

    @Query(value = """
        SELECT gv.value              AS prof_name,
               COUNT(p.id)           AS total,
               COUNT(diag.patient_id) AS diagnosed_count
        FROM patients p
        JOIN glossary_values gv ON p.profession_id = gv.id
        LEFT JOIN (SELECT DISTINCT patient_id FROM diagnoses) diag ON p.id = diag.patient_id
        WHERE p.profession_id IS NOT NULL
        GROUP BY gv.id, gv.value
        HAVING COUNT(p.id) >= :minSample
        ORDER BY COUNT(diag.patient_id) DESC
    """, nativeQuery = true)
    fun countDiagnosedByProfession(@Param("minSample") minSample: Int): List<ProfessionRaw>

    @Query(value = """
        SELECT gv.value               AS name,
               COUNT(diag.patient_id) AS diagnosed_count
        FROM patients p
        JOIN glossary_values gv ON p.region_id = gv.id
        LEFT JOIN (SELECT DISTINCT patient_id FROM diagnoses) diag ON p.id = diag.patient_id
        WHERE p.region_id IS NOT NULL
        GROUP BY gv.id, gv.value
        ORDER BY gv.value
    """, nativeQuery = true)
    fun countDiagnosedByRegion(): List<DistrictRaw>
}
