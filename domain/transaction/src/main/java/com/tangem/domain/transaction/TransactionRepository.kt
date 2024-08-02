package com.tangem.domain.transaction

import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.common.transaction.TransactionSendResult
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.transaction.models.TransactionType
import com.tangem.domain.wallets.models.UserWalletId
import java.math.BigInteger

interface TransactionRepository {

    @Suppress("LongParameterList")
    suspend fun createTransaction(
        amount: Amount,
        fee: Fee,
        memo: String?,
        destination: String,
        userWalletId: UserWalletId,
        network: Network,
        txExtras: TransactionExtras?,
        hash: String?,
    ): TransactionData.Uncompiled?

    @Suppress("LongParameterList")
    suspend fun validateTransaction(
        amount: Amount,
        fee: Fee?,
        memo: String?,
        destination: String,
        userWalletId: UserWalletId,
        network: Network,
        isSwap: Boolean = false,
        txExtras: TransactionExtras?,
        hash: String? = null,
    ): Result<Unit>

    suspend fun sendTransaction(
        txData: TransactionData,
        signer: CommonSigner,
        userWalletId: UserWalletId,
        network: Network,
    ): com.tangem.blockchain.extensions.Result<TransactionSendResult>

    fun createTransactionDataExtras(
        data: String,
        network: Network,
        transactionType: TransactionType,
        nonce: BigInteger?,
        gasLimit: BigInteger?,
    ): TransactionExtras
}
