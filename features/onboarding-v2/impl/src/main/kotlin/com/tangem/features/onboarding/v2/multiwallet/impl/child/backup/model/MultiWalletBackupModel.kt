package com.tangem.features.onboarding.v2.multiwallet.impl.child.backup.model

import androidx.compose.runtime.Stable
import com.tangem.common.CompletionResult
import com.tangem.common.core.TangemSdkError
import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.core.analytics.api.AnalyticsExceptionHandler
import com.tangem.core.analytics.models.AnalyticsParam
import com.tangem.core.analytics.models.ExceptionAnalyticsEvent
import com.tangem.core.analytics.models.event.OnboardingAnalyticsEvent
import com.tangem.core.decompose.di.ModelScoped
import com.tangem.core.decompose.model.Model
import com.tangem.core.decompose.model.ParamsContainer
import com.tangem.core.decompose.ui.UiMessageSender
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.toWrappedList
import com.tangem.core.ui.message.dialog.Dialogs
import com.tangem.domain.card.repository.CardRepository
import com.tangem.domain.card.repository.CardSdkConfigRepository
import com.tangem.domain.feedback.SendFeedbackEmailUseCase
import com.tangem.domain.feedback.models.FeedbackEmailType
import com.tangem.domain.models.scan.ProductType
import com.tangem.features.onboarding.v2.common.analytics.OnboardingEvent
import com.tangem.features.onboarding.v2.impl.R
import com.tangem.features.onboarding.v2.multiwallet.impl.child.MultiWalletChildParams
import com.tangem.features.onboarding.v2.multiwallet.impl.child.backup.MultiWalletBackupComponent
import com.tangem.features.onboarding.v2.multiwallet.impl.child.backup.ui.backupCardAttestationFailedDialog
import com.tangem.features.onboarding.v2.multiwallet.impl.child.backup.ui.onlyOneBackupDeviceDialog
import com.tangem.features.onboarding.v2.multiwallet.impl.child.backup.ui.resetBackupCardDialog
import com.tangem.features.onboarding.v2.multiwallet.impl.child.backup.ui.state.MultiWalletBackupUM
import com.tangem.sdk.api.BackupServiceHolder
import com.tangem.sdk.api.TangemSdkManager
import com.tangem.sdk.extensions.localizedDescriptionRes
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@ModelScoped
@Suppress("LongParameterList")
class MultiWalletBackupModel @Inject constructor(
    paramsContainer: ParamsContainer,
    override val dispatchers: CoroutineDispatcherProvider,
    private val backupServiceHolder: BackupServiceHolder,
    private val cardSdkConfigRepository: CardSdkConfigRepository,
    private val tangemSdkManager: TangemSdkManager,
    private val analyticsEventHandler: AnalyticsEventHandler,
    private val analyticsExceptionHandler: AnalyticsExceptionHandler,
    private val uiMessageSender: UiMessageSender,
    private val sendFeedbackEmailUseCase: SendFeedbackEmailUseCase,
    private val cardRepository: CardRepository,
) : Model() {

    @Suppress("UnusedPrivateMember")
    private val params = paramsContainer.require<MultiWalletChildParams>()
    private val scanResponse
        get() = params.multiWalletState.value.currentScanResponse
    private val backupService
        get() = backupServiceHolder.backupService.get()!!

    private val state = MutableStateFlow(BackupState())

    private val _uiState = MutableStateFlow(
        when (scanResponse.productType) {
            ProductType.Wallet,
            ProductType.Wallet2,
            ProductType.Ring,
            -> getInitState()
            else -> error("Type: ${scanResponse.productType.name} is not supported!")
        },
    )

    val uiState: StateFlow<MultiWalletBackupUM> = _uiState
    val eventFlow = MutableSharedFlow<MultiWalletBackupComponent.Event>()

    init {
        // for wallet 1 this event is sent in Wallet1ChooseOptionModel
        if (scanResponse.productType == ProductType.Wallet2 || scanResponse.productType == ProductType.Ring) {
            analyticsEventHandler.send(OnboardingEvent.Backup.ScreenOpened)
        }

        analyticsEventHandler.send(OnboardingEvent.Backup.Started)
    }

    private fun getInitState(): MultiWalletBackupUM {
        return MultiWalletBackupUM(
            title = resourceReference(R.string.onboarding_title_no_backup_cards),
            bodyText = resourceReference(R.string.onboarding_subtitle_no_backup_cards),
            finalizeButtonEnabled = false,
            addBackupButtonEnabled = true,
            addBackupButtonLoading = false,
            onAddBackupClick = ::startBackupWallet,
            onFinalizeButtonClick = ::onFinalizeClick,
        )
    }

    private fun startBackupWallet() {
        if (state.value.numberOfBackupCards == 0 && scanResponse.primaryCard != null) {
            backupService.discardSavedBackup()
        }

        val primaryCard = scanResponse.primaryCard
        if (primaryCard != null) {
            backupService.setPrimaryCard(primaryCard)
        }

        addBackupCardWithService()
    }

    private fun setNumberOfBackupCards(number: Int) {
        // set state for adding backup cards and disable button if there is more than 2 backup cards
        _uiState.update { st ->
            when (number) {
                0 -> {
                    st.copy(
                        title = resourceReference(R.string.onboarding_title_no_backup_cards),
                        bodyText = resourceReference(R.string.onboarding_subtitle_no_backup_cards),
                        addBackupButtonEnabled = true,
                    )
                }
                1 -> {
                    modelScope.launch {
                        eventFlow.emit(MultiWalletBackupComponent.Event.OneDeviceAdded)
                    }
                    params.multiWalletState.update { it.copy(isThreeCards = false) }
                    st.copy(
                        title = resourceReference(R.string.onboarding_title_one_backup_card),
                        bodyText = resourceReference(R.string.onboarding_subtitle_one_backup_card),
                        addBackupButtonEnabled = true,
                        finalizeButtonEnabled = true,
                    )
                }
                2 -> {
                    modelScope.launch {
                        eventFlow.emit(MultiWalletBackupComponent.Event.TwoDeviceAdded)
                    }
                    params.multiWalletState.update { it.copy(isThreeCards = true) }
                    st.copy(
                        title = resourceReference(R.string.onboarding_title_two_backup_cards),
                        bodyText = resourceReference(R.string.onboarding_subtitle_two_backup_cards),
                        addBackupButtonEnabled = false,
                        finalizeButtonEnabled = true,
                    )
                }
                else -> st.copy(addBackupButtonEnabled = false)
            }
        }
    }

    private fun onFinalizeClick(fromDialog: Boolean = false) {
        when (state.value.numberOfBackupCards) {
            1 -> {
                params.multiWalletState.update { it.copy(isThreeCards = false) }

                if (fromDialog.not()) {
                    showOnlyOneBackupWarningDialog()
                    return
                }
            }
            2 -> {
                params.multiWalletState.update { it.copy(isThreeCards = true) }
            }
        }

        modelScope.launch { eventFlow.emit(MultiWalletBackupComponent.Event.Done) }

        analyticsEventHandler.send(OnboardingEvent.Backup.Finished(cardsCount = state.value.numberOfBackupCards + 1))
    }

    private fun showOnlyOneBackupWarningDialog() {
        _uiState.update {
            it.copy(
                dialog = onlyOneBackupDeviceDialog(
                    onDismiss = { _uiState.update { it.copy(dialog = null) } },
                    onConfirm = { onFinalizeClick(fromDialog = true) },
                ),
            )
        }
    }

    private fun addBackupCardWithService() {
        _uiState.update { st ->
            st.copy(addBackupButtonLoading = true)
        }

        backupService.addBackupCard { result ->
            backupService.skipCompatibilityChecks = false
            cardSdkConfigRepository.sdk.config.filter.cardIdFilter = null

            _uiState.update { st ->
                st.copy(addBackupButtonLoading = false)
            }

            when (result) {
                is CompletionResult.Success -> {
                    state.update {
                        it.copy(
                            numberOfBackupCards = it.numberOfBackupCards + 1,
                        )
                    }

                    val backupCardInfo = MultiWalletChildParams.Backup.BackupCardInfo(
                        cardId = result.data.cardId,
                        cardPublicKey = result.data.cardPublicKey,
                        manufacturer = result.data.manufacturer,
                        firmwareVersion = result.data.firmwareVersion,
                    )
                    params.backups.update {
                        it.copy(
                            card2 = if (state.value.numberOfBackupCards == 1) backupCardInfo else it.card2,
                            card3 = if (state.value.numberOfBackupCards == 2) backupCardInfo else it.card3,
                        )
                    }

                    setNumberOfBackupCards(state.value.numberOfBackupCards)
                }
                is CompletionResult.Failure -> {
                    when (val error = result.error) {
                        is TangemSdkError.CardVerificationFailed -> showCardVerificationFailedDialog(error)
                        is TangemSdkError.BackupFailedNotEmptyWallets -> {
                            _uiState.update { st ->
                                st.copy(
                                    dialog = resetBackupCardDialog(
                                        onReset = { resetBackupCard(cardId = error.cardId) },
                                        onDismiss = {
                                            _uiState.update { it.copy(dialog = null) }
                                        },
                                        onDismissClick = {
                                            analyticsEventHandler.send(OnboardingEvent.Backup.ResetCancelEvent)
                                        },
                                    ),
                                )
                            }
                        }
                        is TangemSdkError.IssuerSignatureLoadingFailed -> {
                            _uiState.update { st ->
                                st.copy(
                                    dialog = backupCardAttestationFailedDialog(
                                        onDismiss = { _uiState.update { it.copy(dialog = null) } },
                                    ),
                                )
                            }
                        }
                        else -> analyticsExceptionHandler.sendException(ExceptionAnalyticsEvent(result.error))
                    }
                }
            }
        }
    }

    private fun showCardVerificationFailedDialog(error: TangemSdkError.CardVerificationFailed) {
        analyticsEventHandler.send(
            event = OnboardingAnalyticsEvent.Onboarding.OfflineAttestationFailed(AnalyticsParam.ScreensSources.Backup),
        )

        val resource = error.localizedDescriptionRes()
        val resId = resource.resId ?: com.tangem.core.ui.R.string.common_unknown_error
        val resArgs = resource.args.map { it.value }

        uiMessageSender.send(
            message = Dialogs.cardVerificationFailed(
                errorDescription = resourceReference(id = resId, resArgs.toWrappedList()),
                onRequestSupport = {
                    modelScope.launch {
                        sendFeedbackEmailUseCase(type = FeedbackEmailType.CardAttestationFailed)
                    }
                },
            ),
        )
    }

    private fun resetBackupCard(cardId: String) {
        analyticsEventHandler.send(OnboardingEvent.Backup.ResetPerformEvent)

        modelScope.launch {
            tangemSdkManager.resetToFactorySettings(
                cardId = cardId,
                allowsRequestAccessCodeFromRepository = false,
            )

            cardRepository.finishCardActivation(cardId)
        }
    }
}
