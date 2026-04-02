package com.reservenook.groupclass.infrastructure

import com.reservenook.groupclass.domain.ClassType
import org.springframework.data.jpa.repository.JpaRepository

interface ClassTypeRepository : JpaRepository<ClassType, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<ClassType>
    fun findByIdAndCompanyId(id: Long, companyId: Long): ClassType?
    fun findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId: Long): List<ClassType>
}
