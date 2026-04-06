package com.medical.cardio.repository

import com.medical.cardio.entity.GlossaryValueEntity
import org.springframework.data.jpa.repository.JpaRepository

interface GlossaryValueRepository : JpaRepository<GlossaryValueEntity, Long> {
    fun findAllByGlossaryId(glossaryId: Long): List<GlossaryValueEntity>
}
