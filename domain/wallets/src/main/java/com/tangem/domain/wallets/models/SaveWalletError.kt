package com.tangem.domain.wallets.models

/**
 * @author Andrew Khokhlov on 14/07/2023
 */
sealed interface SaveWalletError {

    object CommonError : SaveWalletError
}