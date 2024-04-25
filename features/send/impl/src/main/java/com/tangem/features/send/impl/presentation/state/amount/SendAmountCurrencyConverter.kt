package com.tangem.features.send.impl.presentation.state.amount

import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.features.send.impl.presentation.state.SendUiState
import com.tangem.features.send.impl.presentation.state.StateRouter
import com.tangem.utils.Provider
import com.tangem.utils.converter.Converter
import com.tangem.utils.isNullOrZero

internal class SendAmountCurrencyConverter(
    private val stateRouterProvider: Provider<StateRouter>,
    private val currentStateProvider: Provider<SendUiState>,
    private val cryptoCurrencyStatusProvider: Provider<CryptoCurrencyStatus>,
) : Converter<Boolean, SendUiState> {
    override fun convert(value: Boolean): SendUiState {
        val state = currentStateProvider()
        val isEditState = stateRouterProvider().isEditState
        val amountState = state.getAmountState(isEditState) ?: return state
        val amountTextField = amountState.amountTextField
        val cryptoCurrencyStatus = cryptoCurrencyStatusProvider()

        val isValidFiatRate = cryptoCurrencyStatus.value.fiatRate.isNullOrZero()
        return if (amountTextField.isFiatValue == value && !isValidFiatRate) {
            state
        } else {
            return state.copyWrapped(
                isEditState = isEditState,
                amountState = amountState.copy(
                    amountTextField = amountTextField.copy(
                        isFiatValue = value,
                        isValuePasted = true,
                    ),
                ),
            )
        }
    }
}
