package com.tangem.tap.domain

import android.content.Context
import androidx.annotation.StringRes
import com.tangem.Message
import com.tangem.TangemSdk
import com.tangem.blockchain.common.Blockchain
import com.tangem.common.CardFilter
import com.tangem.common.CompletionResult
import com.tangem.common.SuccessResponse
import com.tangem.common.card.FirmwareVersion
import com.tangem.common.core.CardIdDisplayFormat
import com.tangem.common.core.CardSessionRunnable
import com.tangem.common.core.Config
import com.tangem.common.core.TangemSdkError
import com.tangem.common.extensions.ByteArrayKey
import com.tangem.common.hdWallet.DerivationPath
import com.tangem.common.map
import com.tangem.domain.common.CardDTO
import com.tangem.domain.common.ScanResponse
import com.tangem.operations.CommandResponse
import com.tangem.operations.ScanTask
import com.tangem.operations.derivation.DerivationTaskResponse
import com.tangem.operations.derivation.DeriveMultipleWalletPublicKeysTask
import com.tangem.operations.pins.CheckUserCodesCommand
import com.tangem.operations.pins.CheckUserCodesResponse
import com.tangem.operations.pins.SetUserCodeCommand
import com.tangem.tap.common.analytics.Analytics
import com.tangem.tap.common.analytics.events.Basic
import com.tangem.tap.domain.tasks.CreateWalletAndRescanTask
import com.tangem.tap.domain.tasks.product.CreateProductWalletTask
import com.tangem.tap.domain.tasks.product.CreateProductWalletTaskResponse
import com.tangem.tap.domain.tasks.product.ResetToFactorySettingsTask
import com.tangem.tap.domain.tasks.product.ScanProductTask
import com.tangem.tap.domain.tokens.UserTokensRepository
import com.tangem.wallet.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TangemSdkManager(private val tangemSdk: TangemSdk, private val context: Context) {

    suspend fun scanProduct(
        userTokensRepository: UserTokensRepository,
        additionalBlockchainsToDerive: Collection<Blockchain>? = null,
        messageRes: Int? = null,
    ): CompletionResult<ScanResponse> {
        val message = Message(context.getString(messageRes ?: R.string.initial_message_scan_header))
        return runTaskAsyncReturnOnMain(
            runnable = ScanProductTask(null, userTokensRepository, additionalBlockchainsToDerive),
            cardId = null, initialMessage = message,
        ).also { sendScanResultsToAnalytics(it) }
    }

    suspend fun createProductWallet(
        scanResponse: ScanResponse,
    ): CompletionResult<CreateProductWalletTaskResponse> {
        return runTaskAsync(
            CreateProductWalletTask(scanResponse.productType),
            scanResponse.card.cardId,
            Message(context.getString(R.string.initial_message_create_wallet_body)),
        )
    }

    private fun sendScanResultsToAnalytics(
        result: CompletionResult<ScanResponse>,
    ) {
        if (result is CompletionResult.Failure) {
            (result.error as? TangemSdkError)?.let { error ->
                Analytics.send(Basic.ScanError(error))
            }
        }
    }

    suspend fun createWallet(cardId: String?): CompletionResult<CardDTO> {
        return runTaskAsyncReturnOnMain(
            CreateWalletAndRescanTask(),
            cardId,
            initialMessage = Message(context.getString(R.string.initial_message_create_wallet_body)),
        )
            .map { CardDTO(it) }
    }

    suspend fun derivePublicKeys(
        cardId: String,
        derivations: Map<ByteArrayKey, List<DerivationPath>>,
    ): CompletionResult<DerivationTaskResponse> {
        return runTaskAsyncReturnOnMain(DeriveMultipleWalletPublicKeysTask(derivations), cardId)
    }

    suspend fun resetToFactorySettings(cardId: String): CompletionResult<CardDTO> {
        return runTaskAsyncReturnOnMain(
            runnable = ResetToFactorySettingsTask(),
            cardId = cardId,
            initialMessage = Message(context.getString(R.string.card_settings_reset_card_to_factory)),
        )
            .map { CardDTO(it) }
    }

    suspend fun setPasscode(cardId: String?): CompletionResult<SuccessResponse> {
        return runTaskAsyncReturnOnMain(
            SetUserCodeCommand.changePasscode(null),
            cardId,
            initialMessage = Message(context.getString(R.string.initial_message_change_passcode_body)),
        )
    }

    suspend fun setAccessCode(cardId: String?): CompletionResult<SuccessResponse> {
        return runTaskAsyncReturnOnMain(
            SetUserCodeCommand.changeAccessCode(null),
            cardId,
            initialMessage = Message(context.getString(R.string.initial_message_change_access_code_body)),
        )
    }

    suspend fun setLongTap(cardId: String?): CompletionResult<SuccessResponse> {
        return runTaskAsyncReturnOnMain(
            SetUserCodeCommand.resetUserCodes(),
            cardId,
            initialMessage = Message(context.getString(R.string.initial_message_tap_header)),
        )
    }

    suspend fun checkUserCodes(cardId: String?): CompletionResult<CheckUserCodesResponse> {
        return runTaskAsyncReturnOnMain(
            CheckUserCodesCommand(),
            cardId,
            initialMessage = Message(context.getString(R.string.initial_message_tap_header)),
        )
    }

    suspend fun scanCard(): CompletionResult<CardDTO> {
        return runTaskAsyncReturnOnMain(
            runnable = ScanTask(),
            initialMessage = Message(context.getString(R.string.initial_message_tap_header)),
        )
            .map { CardDTO(it) }
    }

    suspend fun <T : CommandResponse> runTaskAsync(
        runnable: CardSessionRunnable<T>,
        cardId: String? = null,
        initialMessage: Message? = null,
        accessCode: String? = null,
    ): CompletionResult<T> =
        withContext(Dispatchers.Main) {
            suspendCoroutine { continuation ->
                tangemSdk.startSessionWithRunnable(runnable, cardId, initialMessage, accessCode) { result ->
                    if (continuation.context.isActive) continuation.resume(result)
                }
            }
        }

    private suspend fun <T : CommandResponse> runTaskAsyncReturnOnMain(
        runnable: CardSessionRunnable<T>,
        cardId: String? = null,
        initialMessage: Message? = null,
    ): CompletionResult<T> {
        val result = runTaskAsync(runnable, cardId, initialMessage)
        return withContext(Dispatchers.Main) { result }
    }

    fun changeDisplayedCardIdNumbersCount(scanResponse: ScanResponse?) {
        tangemSdk.config.cardIdDisplayFormat = when {
            scanResponse == null -> CardIdDisplayFormat.Full
            scanResponse.isTangemTwins() -> CardIdDisplayFormat.LastLuhn(4)
            scanResponse.isSaltPay() -> CardIdDisplayFormat.None
            else -> CardIdDisplayFormat.Full
        }
    }

    fun getString(@StringRes stringResId: Int, vararg formatArgs: Any?): String {
        return context.getString(stringResId, formatArgs)
    }

    companion object {
        val config = Config(
            linkedTerminal = true,
            allowUntrustedCards = true,
            filter = CardFilter(
                allowedCardTypes = FirmwareVersion.FirmwareType.values().toList(),
            ),
        )
    }
}
