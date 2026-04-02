package com.reservenook.groupclass.infrastructure

import com.reservenook.groupclass.domain.ClassSession
import java.time.Instant
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import jakarta.persistence.LockModeType

interface ClassSessionRepository : JpaRepository<ClassSession, Long> {
    fun findAllByCompanyIdOrderByStartsAtAsc(companyId: Long): List<ClassSession>
    fun findByIdAndCompanyId(id: Long, companyId: Long): ClassSession?
    fun findAllByCompanyIdAndStatusOrderByStartsAtAsc(companyId: Long, status: com.reservenook.groupclass.domain.ClassSessionStatus): List<ClassSession>
    @EntityGraph(attributePaths = ["classType", "instructor"])
    fun findAllByCompanyIdAndClassTypeIdAndStatusAndStartsAtGreaterThanEqualOrderByStartsAtAsc(
        companyId: Long,
        classTypeId: Long,
        status: com.reservenook.groupclass.domain.ClassSessionStatus,
        startsAt: Instant
    ): List<ClassSession>
    fun findAllByInstructorIdAndStartsAtBetweenOrderByStartsAtAsc(instructorId: Long, startsAt: Instant, endsAt: Instant): List<ClassSession>
    fun existsByInstructorIdAndIdNotAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
        instructorId: Long,
        id: Long,
        status: com.reservenook.groupclass.domain.ClassSessionStatus,
        endsAt: Instant,
        startsAt: Instant
    ): Boolean
    fun existsByInstructorIdAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
        instructorId: Long,
        status: com.reservenook.groupclass.domain.ClassSessionStatus,
        endsAt: Instant,
        startsAt: Instant
    ): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): ClassSession?
}
