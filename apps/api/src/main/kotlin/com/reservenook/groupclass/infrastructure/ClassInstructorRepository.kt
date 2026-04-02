package com.reservenook.groupclass.infrastructure

import com.reservenook.groupclass.domain.ClassInstructor
import org.springframework.data.jpa.repository.JpaRepository

interface ClassInstructorRepository : JpaRepository<ClassInstructor, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<ClassInstructor>
    fun findByIdAndCompanyId(id: Long, companyId: Long): ClassInstructor?
    fun findAllByCompanyIdAndActiveTrueOrderByDisplayNameAsc(companyId: Long): List<ClassInstructor>
    fun findFirstByCompanySlugAndLinkedUserId(companySlug: String, linkedUserId: Long): ClassInstructor?
}
