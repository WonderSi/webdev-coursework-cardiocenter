package com.medical.cardio.repository

import com.medical.cardio.entity.GlossaryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GlossaryRepository : JpaRepository<GlossaryEntity, Long> {
    // Получить конкретный справочник по имени
    fun findByName(name: String): GlossaryEntity?

    // Загрузить все справочники сразу вместе с их значениями
    @Query("SELECT g FROM GlossaryEntity g LEFT JOIN FETCH g.values")
    fun findAllWithValues(): List<GlossaryEntity>
}
