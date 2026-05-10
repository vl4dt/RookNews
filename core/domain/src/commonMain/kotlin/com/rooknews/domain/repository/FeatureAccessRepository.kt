package com.rooknews.domain.repository

interface FeatureAccessRepository {
    val isProUser: Boolean
    suspend fun checkSubscriptionStatus()
    suspend fun purchasePro(): Boolean
    suspend fun restorePurchases(): Boolean
}
