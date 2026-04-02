package com.reservenook.registration.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "companies")
class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    var businessType: BusinessType,

    @Column(name = "slug", nullable = false, unique = true)
    var slug: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CompanyStatus,

    @Column(name = "default_language", nullable = false)
    var defaultLanguage: String,

    @Column(name = "default_locale", nullable = false)
    var defaultLocale: String,

    @Column(name = "business_description")
    var businessDescription: String? = null,

    @Column(name = "contact_email")
    var contactEmail: String? = null,

    @Column(name = "contact_phone")
    var contactPhone: String? = null,

    @Column(name = "address_line1")
    var addressLine1: String? = null,

    @Column(name = "address_line2")
    var addressLine2: String? = null,

    @Column(name = "city")
    var city: String? = null,

    @Column(name = "postal_code")
    var postalCode: String? = null,

    @Column(name = "country_code")
    var countryCode: String? = null,

    @Column(name = "brand_display_name")
    var brandDisplayName: String? = null,

    @Column(name = "brand_logo_url")
    var brandLogoUrl: String? = null,

    @Column(name = "brand_accent_color")
    var brandAccentColor: String? = null,

    @Column(name = "support_email")
    var supportEmail: String? = null,

    @Column(name = "support_phone")
    var supportPhone: String? = null,

    @Column(name = "notification_destination_email")
    var notificationDestinationEmail: String? = null,

    @Column(name = "notify_on_new_booking", nullable = false)
    var notifyOnNewBooking: Boolean = true,

    @Column(name = "notify_on_cancellation", nullable = false)
    var notifyOnCancellation: Boolean = true,

    @Column(name = "notify_daily_summary", nullable = false)
    var notifyDailySummary: Boolean = false,

    @Column(name = "notify_on_booking_confirmed", nullable = false)
    var notifyOnBookingConfirmed: Boolean = true,

    @Column(name = "notify_on_booking_completed", nullable = false)
    var notifyOnBookingCompleted: Boolean = false,

    @Column(name = "notify_on_booking_no_show", nullable = false)
    var notifyOnBookingNoShow: Boolean = false,

    @Column(name = "widget_cta_label")
    var widgetCtaLabel: String? = null,

    @Column(name = "widget_enabled", nullable = false)
    var widgetEnabled: Boolean = false,

    @Column(name = "widget_allowed_domains")
    var widgetAllowedDomains: String? = null,

    @Column(name = "widget_theme_variant", nullable = false)
    var widgetThemeVariant: String = "minimal",

    @Column(name = "last_activity_at", nullable = false)
    var lastActivityAt: Instant = Instant.now(),

    @Column(name = "inactive_at")
    var inactiveAt: Instant? = null,

    @Column(name = "deletion_scheduled_at")
    var deletionScheduledAt: Instant? = null,

    @Column(name = "legal_hold_until")
    var legalHoldUntil: Instant? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
