package com.reservenook.platformadmin.api

data class AbusePreventionPolicyResponse(
    val loginPairLimit: Int,
    val loginClientLimit: Int,
    val loginEmailLimit: Int,
    val publicWritePairLimit: Int,
    val publicWriteClientLimit: Int,
    val publicWriteEmailLimit: Int,
    val publicReadClientLimit: Int,
    val updatedAt: String
)

data class UpdateAbusePreventionPolicyRequest(
    val loginPairLimit: Int,
    val loginClientLimit: Int,
    val loginEmailLimit: Int,
    val publicWritePairLimit: Int,
    val publicWriteClientLimit: Int,
    val publicWriteEmailLimit: Int,
    val publicReadClientLimit: Int
)

data class UpdateAbusePreventionPolicyResponse(
    val message: String,
    val policy: AbusePreventionPolicyResponse
)
