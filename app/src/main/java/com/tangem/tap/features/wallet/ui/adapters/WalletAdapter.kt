package com.tangem.tap.features.wallet.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.Token
import com.tangem.domain.common.TapWorkarounds.derivationStyle
import com.tangem.tap.common.extensions.getString
import com.tangem.tap.common.extensions.hide
import com.tangem.tap.common.extensions.loadCurrenciesIcon
import com.tangem.tap.common.extensions.show
import com.tangem.tap.features.wallet.redux.Currency
import com.tangem.tap.features.wallet.redux.WalletAction
import com.tangem.tap.features.wallet.redux.WalletData
import com.tangem.tap.features.wallet.ui.BalanceStatus
import com.tangem.tap.store
import com.tangem.wallet.R
import com.tangem.wallet.databinding.ItemCurrencyWalletBinding

class WalletAdapter
    : ListAdapter<WalletData, WalletAdapter.WalletsViewHolder>(DiffUtilCallback) {

    override fun getItemId(position: Int): Long {
        return currentList[position].currencyData.currencySymbol?.hashCode()?.toLong() ?: 0
    }

    fun submitList(
        list: List<WalletData>,
        primaryBlockchain: Blockchain?,
        primaryToken: Token? = null
    ) {
        // We used this method to sort the list of currencies. Sorting is disabled for now.
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletsViewHolder {
        val layout = ItemCurrencyWalletBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WalletsViewHolder(layout)
    }

    override fun onBindViewHolder(holder: WalletsViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    object DiffUtilCallback : DiffUtil.ItemCallback<WalletData>() {
        override fun areContentsTheSame(
            oldItem: WalletData, newItem: WalletData
        ) = oldItem == newItem

        override fun areItemsTheSame(
            oldItem: WalletData, newItem: WalletData
        ) = oldItem == newItem
    }

    class WalletsViewHolder(val binding: ItemCurrencyWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wallet: WalletData) = with(binding) {
            tvCurrency.text = wallet.currencyData.currency
            tvAmount.text = wallet.currencyData.amountFormatted?.takeWhile { !it.isWhitespace() }
            tvCurrencySymbol.text =
                wallet.currencyData.amountFormatted?.takeLastWhile { !it.isWhitespace() }
            tvAmountFiat.text = wallet.currencyData.fiatAmountFormatted
            tvExchangeRate.text = wallet.fiatRateString
            cardWallet.setOnClickListener {
                store.dispatch(WalletAction.MultiWallet.SelectWallet(wallet))
            }
            val blockchain = wallet.currency.blockchain
            val token = (wallet.currency as? Currency.Token)?.token

            val isCustom =
                wallet.currency.isCustomCurrency(store.state.globalState.scanResponse?.card?.derivationStyle)
            tvExchangeRate.show(!isCustom)
            tvCustomCurrency.show(isCustom)

            Picasso.get().loadCurrenciesIcon(
                imageView = ivCurrency,
                textView = tvTokenLetter,
                token = token, blockchain = blockchain,
            )

            when (wallet.currencyData.status) {
                BalanceStatus.VerifiedOnline, BalanceStatus.SameCurrencyTransactionInProgress -> hideWarning(isCustom)
                BalanceStatus.Loading -> {
                    hideWarning(isCustom)
                    if (wallet.currencyData.amountFormatted == null) {
                        tvExchangeRate.text = root.getString(R.string.wallet_balance_loading)
                    }
                }
                BalanceStatus.TransactionInProgress ->
                    showWarning(root.getString(R.string.wallet_balance_tx_in_progress), isCustom)
                BalanceStatus.Unreachable ->
                    showWarning(root.getString(R.string.wallet_balance_blockchain_unreachable), isCustom)

                BalanceStatus.NoAccount ->
                    showWarning(root.getString(R.string.wallet_error_no_account), isCustom)
                else -> {
                }
            }
        }

        private fun showWarning(message: String, isCustom: Boolean = false) {
            toggleWarning(true, isCustom)
            binding.tvStatusErrorMessage.text = message
        }

        private fun hideWarning(isCustom: Boolean = false) {
            toggleWarning(false, isCustom)
        }

        private fun toggleWarning(show: Boolean, isCustom: Boolean = false) {
            if (!show) {
                binding.tvExchangeRate.show(!isCustom)
                binding.tvCustomCurrency.show(isCustom)
            } else {
                binding.tvExchangeRate.hide()
                binding.tvCustomCurrency.hide()
            }
            binding.tvStatusErrorMessage.show(show)
        }
    }
}
