package com.tangem.tap.domain.tokens

import com.squareup.moshi.JsonAdapter
import com.tangem.Log
import com.tangem.common.card.Card
import com.tangem.network.api.tangemTech.UserTokensResponse
import com.tangem.network.common.MoshiConverter
import com.tangem.tap.common.FileReader
import com.tangem.tap.features.wallet.models.Currency
import com.tangem.tap.features.wallet.models.toCurrencies

class UserTokensStorageService(
    private val oldUserTokensRepository: OldUserTokensRepository,
    private val fileReader: FileReader,
) {
    private val moshi = MoshiConverter.defaultMoshi()
    private val userTokensAdapter: JsonAdapter<UserTokensResponse> =
        moshi.adapter(UserTokensResponse::class.java)

    fun getUserTokens(userId: String): List<Currency>? {
        return try {
            val json = fileReader.readFile(getFileNameForUserTokens(userId))
            userTokensAdapter.fromJson(json)?.tokens?.map { Currency.fromTokenResponse(it) }
        } catch (exception: Exception) {
            Log.error { exception.stackTraceToString() }
            null
        }
    }

    @Deprecated("")
    suspend fun getUserTokens(card: Card): List<Currency> {
        val blockchainNetworks =
            oldUserTokensRepository.loadSavedCurrencies(card.cardId, card.settings.isHDWalletAllowed)
        return blockchainNetworks.flatMap { it.toCurrencies() }
    }

    fun saveUserTokens(userId: String, tokens: List<Currency>) {
        val tokensResponse = tokens.map { it.toTokenResponse() }
        val data = UserTokensResponse(tokens = tokensResponse)
        val json = userTokensAdapter.toJson(data)
        fileReader.rewriteFile(json, getFileNameForUserTokens(userId))
    }

    companion object {
        private const val FILE_NAME_PREFIX_USER_TOKENS = "user_tokens"
        private fun getFileNameForUserTokens(userId: String): String = "${FILE_NAME_PREFIX_USER_TOKENS}_$userId"
    }
}