package com.medical.cardio.service.upload

import com.medical.cardio.entity.GlossaryEntity
import com.medical.cardio.entity.GlossaryValueEntity

class GlossaryResolver(allGlossaries: List<GlossaryEntity>) {

    private val map: Map<String, Map<Int, GlossaryValueEntity>> =
        allGlossaries.associate { g -> g.name to g.values.associateBy { it.code } }

    fun resolve(glossaryName: String, code: Int): GlossaryValueEntity? =
        map[glossaryName]?.get(code)

    fun isValidCode(glossaryName: String, code: Int): Boolean =
        map[glossaryName]?.containsKey(code) == true
}
