package com.reservenook.companybackoffice.domain

import com.reservenook.registration.domain.Company
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "company_customer_questions")
class CompanyCustomerQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @Column(name = "label", nullable = false)
    var label: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    var questionType: CustomerQuestionType,

    @Column(name = "required", nullable = false)
    var required: Boolean = false,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int,

    @Column(name = "options_text")
    var optionsText: String? = null
)
