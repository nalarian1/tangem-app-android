package com.tangem.data.feedback.converters

import com.tangem.domain.common.TapWorkarounds.isStart2Coin
import com.tangem.domain.common.TapWorkarounds.isVisa
import com.tangem.domain.common.util.getBackupCardsCount
import com.tangem.domain.feedback.models.CardInfo
import com.tangem.domain.models.scan.CardDTO
import com.tangem.domain.models.scan.ScanResponse
import com.tangem.domain.wallets.builder.UserWalletIdBuilder
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.utils.converter.Converter

/**
 * Converter from [ScanResponse] to [CardInfo]
 *
 * @author Andrew Khokhlov on 07/03/2024
 */
internal object CardInfoConverter : Converter<ScanResponse, CardInfo> {

    override fun convert(value: ScanResponse): CardInfo {
        return with(value) {
            CardInfo(
                userWalletId = createUserWalletId(scanResponse = value),
                cardId = card.cardId,
                cardsCount = value.getBackupCardsCount()?.toString() ?: "0",
                firmwareVersion = card.firmwareVersion.stringValue,
                cardBlockchain = walletData?.blockchain,
                signedHashesList = card.wallets.map {
                    CardInfo.SignedHashes(curve = it.curve.curve, total = it.totalSignedHashes?.toString())
                },
                isImported = value.card.wallets.any(CardDTO.Wallet::isImported),
                isStart2Coin = value.card.isStart2Coin,
                isVisa = value.card.isVisa,
            )
        }
    }

    private fun createUserWalletId(scanResponse: ScanResponse): UserWalletId? {
        return UserWalletIdBuilder.scanResponse(scanResponse = scanResponse).build()
    }
}
