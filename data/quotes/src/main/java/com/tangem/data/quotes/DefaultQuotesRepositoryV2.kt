package com.tangem.data.quotes

import com.tangem.data.quotes.store.QuotesStoreV2
import com.tangem.domain.quotes.QuotesRepositoryV2
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.Quote
import javax.inject.Inject

/**
 * Default implementation of [QuotesRepositoryV2]
 *
 * @property quotesStore quotes store
 *
 * @author Andrew Khokhlov on 11/04/2025
 */
internal class DefaultQuotesRepositoryV2 @Inject constructor(
    private val quotesStore: QuotesStoreV2,
) : QuotesRepositoryV2 {

    override suspend fun getMultiQuoteSyncOrNull(currenciesIds: Set<CryptoCurrency.RawID>): Set<Quote>? {
        return quotesStore.getAllSyncOrNull()?.mapTo(hashSetOf()) {
            it.takeIf { it.rawCurrencyId in currenciesIds } ?: Quote.Empty(it.rawCurrencyId)
        }
    }
}
