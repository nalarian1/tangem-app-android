package com.tangem.domain.wallets.models

data class Artwork(val artworkId: String) {

    companion object {
        const val DEFAULT_IMG_URL = "https://app.tangem.com/cards/card_default.png"
        const val SERGIO_CARD_URL = "https://app.tangem.com/cards/card_tg059.png"
        const val MARTA_CARD_URL = "https://app.tangem.com/cards/card_tg083.png"
        const val TWIN_CARD_1_URL = "https://app.tangem.com/cards/card_tg085.png"
        const val TWIN_CARD_2_URL = "https://app.tangem.com/cards/card_tg086.png"
        const val SERGIO_CARD_ID = "BC01"
        const val MARTA_CARD_ID = "BC02"
    }
}
