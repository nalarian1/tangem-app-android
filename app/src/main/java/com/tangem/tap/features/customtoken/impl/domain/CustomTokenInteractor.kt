package com.tangem.tap.features.customtoken.impl.domain

import com.tangem.blockchain.common.Blockchain
import com.tangem.common.core.TangemError
import com.tangem.domain.features.addCustomToken.CustomCurrency
import com.tangem.tap.features.customtoken.impl.domain.models.FoundToken

/**
 * Custom token interactor
 *
 * @author Andrew Khokhlov on 25/04/2023
 */
interface CustomTokenInteractor {

    /** Find token by [address] and [blockchain] */
    suspend fun findToken(address: String, blockchain: Blockchain): FoundToken

    /** Save token [customCurrency] */
    @Throws(TangemError::class)
    suspend fun saveToken(customCurrency: CustomCurrency): Result<Unit>
}
