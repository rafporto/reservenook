package com.reservenook.companybackoffice.application

import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.companybackoffice.domain.CustomerQuestionType
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.UserStatus
import java.time.LocalTime

object CompanyConfigurationSupport {

    fun parseDay(value: String): BusinessDay =
        runCatching { BusinessDay.valueOf(value.trim().uppercase()) }
            .getOrElse { throw IllegalArgumentException("Business day is not supported.") }

    fun parseTime(value: String, message: String): LocalTime =
        runCatching { LocalTime.parse(value.trim()) }
            .getOrElse { throw IllegalArgumentException(message) }

    fun parseRole(value: String): CompanyRole {
        val normalized = value.trim().uppercase()
        return when (normalized) {
            CompanyRole.COMPANY_ADMIN.name -> CompanyRole.COMPANY_ADMIN
            CompanyRole.STAFF.name -> CompanyRole.STAFF
            else -> throw IllegalArgumentException("Staff role is not supported.")
        }
    }

    fun parseUserStatus(value: String): UserStatus {
        val normalized = value.trim().uppercase()
        return when (normalized) {
            UserStatus.ACTIVE.name -> UserStatus.ACTIVE
            UserStatus.INACTIVE.name -> UserStatus.INACTIVE
            else -> throw IllegalArgumentException("Staff status is not supported.")
        }
    }

    fun parseQuestionType(value: String): CustomerQuestionType =
        runCatching { CustomerQuestionType.valueOf(value.trim().uppercase()) }
            .getOrElse { throw IllegalArgumentException("Question type is not supported.") }
}
