package com.tangem.tap.features.onboarding.products.wallet.saltPay

import com.tangem.common.core.TangemSdkError
import com.tangem.tap.common.extensions.dispatchDialogShow
import com.tangem.tap.common.redux.AppDialog
import com.tangem.tap.features.onboarding.products.wallet.saltPay.dialog.SaltPayDialog
import com.tangem.tap.features.onboarding.products.wallet.saltPay.message.SaltPayRegistrationError
import com.tangem.tap.store

/**
 * Created by Anton Zhilenkov on 22.10.2022.
 */
class SaltPayExceptionHandler {
    companion object {
        fun handle(throwable: Throwable) {
            when (throwable) {
                is SaltPayRegistrationError -> {
                    val dialog = when (throwable) {
                        is SaltPayRegistrationError.NoGas -> SaltPayDialog.Activation.NoGas
                        else -> SaltPayDialog.Activation.OnError(throwable)
                    }
                    store.dispatchDialogShow(dialog)
                }

                is TangemSdkError -> {
                    when (throwable) {
                        is TangemSdkError.NetworkError -> {
                            val message = (throwable).customMessage
                            store.dispatchDialogShow(AppDialog.SimpleOkErrorDialog(message))
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }
                else -> {
                    val message = throwable.message ?: "SaltPay unknown error"
                    store.dispatchDialogShow(AppDialog.SimpleOkErrorDialog(message))
                }
            }
        }
    }
}
