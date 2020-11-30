package com.tangem.tap.domain

import com.tangem.commands.Card
import java.util.*

object TapWorkarounds {

    var isStart2Coin: Boolean = false
        private set

    fun updateCard(card: Card) {
        isStart2Coin = card.cardData?.issuerName?.toLowerCase(Locale.US) == START_2_COIN_ISSUER
    }

    const val START_2_COIN_ISSUER = "start2coin"
}