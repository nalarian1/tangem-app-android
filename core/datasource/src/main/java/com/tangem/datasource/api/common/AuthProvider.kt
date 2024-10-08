package com.tangem.datasource.api.common

/**
 * Provides auth for tangemTech API
 */
interface AuthProvider {

    /**
     * Returns authToken for tangem tech api
     */
    fun getCardPublicKey(): String

    fun getCardId(): String
}
