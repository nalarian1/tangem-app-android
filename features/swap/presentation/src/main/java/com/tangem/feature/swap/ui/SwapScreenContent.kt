package com.tangem.feature.swap.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.tangem.common.Strings.STARS
import com.tangem.core.ui.components.*
import com.tangem.core.ui.components.notifications.Notification
import com.tangem.core.ui.components.notifications.NotificationConfig
import com.tangem.core.ui.extensions.getActiveIconResByCoinId
import com.tangem.core.ui.extensions.resolveReference
import com.tangem.core.ui.extensions.stringReference
import com.tangem.core.ui.res.TangemTheme
import com.tangem.feature.swap.domain.models.ui.FeeType
import com.tangem.feature.swap.models.*
import com.tangem.feature.swap.models.states.FeeItemState
import com.tangem.feature.swap.models.states.ProviderState
import com.tangem.feature.swap.presentation.R

@Suppress("LongMethod")
@Composable
internal fun SwapScreenContent(state: SwapStateHolder, modifier: Modifier = Modifier) {
    val keyboard by keyboardAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = TangemTheme.colors.background.secondary),
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = TangemTheme.dimens.spacing16,
                        end = TangemTheme.dimens.spacing16,
                        top = TangemTheme.dimens.spacing16,
                        bottom = TangemTheme.dimens.spacing32,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TangemTheme.dimens.spacing16),
            ) {
                MainInfo(state)

                ProviderItemBlock(
                    state = state.providerState,
                    modifier = Modifier
                        .clickable(
                            enabled = state.providerState.onProviderClick != null,
                            onClick = { state.providerState.onProviderClick?.invoke(state.providerState.id) },
                        ),
                )

                FeeItemBlock(state = state.fee)

                if (state.warnings.isNotEmpty()) SwapWarnings(warnings = state.warnings)

                MainButton(state = state, onPermissionWarningClick = state.onShowPermissionBottomSheet)
            }
        }

        AnimatedVisibility(
            visible = keyboard is Keyboard.Opened,
            modifier = Modifier
                .imePadding()
                .align(Alignment.BottomCenter),
        ) {
            Text(
                text = stringResource(id = R.string.send_max_amount_label),
                style = TangemTheme.typography.button,
                color = TangemTheme.colors.text.primary1,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TangemTheme.colors.button.secondary)
                    .clickable { state.onMaxAmountSelected?.invoke() }
                    .padding(
                        horizontal = TangemTheme.dimens.spacing14,
                        vertical = TangemTheme.dimens.spacing16,
                    ),
                textAlign = TextAlign.Start,
            )
        }

        if (state.alert != null) {
            val message = if (state.alert.type == GenericWarningType.NETWORK) {
                stringResource(id = R.string.disclaimer_error_loading)
            } else {
                state.alert.message ?: stringResource(id = R.string.swapping_generic_error)
            }
            SimpleOkDialog(
                message = message,
                onDismissDialog = state.alert.onClick,
            )
        }
    }
}

@Composable
private fun MainInfo(state: SwapStateHolder) {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val (topCard, bottomCard, button) = createRefs()
        val priceImpactWarning = state.warnings.filterIsInstance<SwapWarning.HighPriceImpact>().firstOrNull()
        TransactionCardData(
            priceImpactWarning = priceImpactWarning,
            swapCardState = state.sendCardData,
            modifier = Modifier.constrainAs(topCard) {
                top.linkTo(parent.top)
            },
            onSelectTokenClick = state.onSelectTokenClick,
        )
        val marginCard = TangemTheme.dimens.spacing16
        TransactionCardData(
            priceImpactWarning = priceImpactWarning,
            swapCardState = state.receiveCardData,
            modifier = Modifier.constrainAs(bottomCard) {
                top.linkTo(topCard.bottom, margin = marginCard)
            },
            onSelectTokenClick = state.onSelectTokenClick,
        )
        val marginButton = TangemTheme.dimens.spacing32
        SwapButton(
            state,
            modifier = Modifier.constrainAs(button) {
                bottom.linkTo(topCard.bottom, margin = -marginButton)
                start.linkTo(topCard.start)
                end.linkTo(topCard.end)
            },
        )
    }
}

@Composable
private fun TransactionCardData(
    priceImpactWarning: SwapWarning.HighPriceImpact?,
    swapCardState: SwapCardState,
    onSelectTokenClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    when (swapCardState) {
        is SwapCardState.Empty -> {
            TransactionCardEmpty(
                type = swapCardState.type,
                amountEquivalent = swapCardState.amountEquivalent,
                textFieldValue = swapCardState.amountTextFieldValue,
                onChangeTokenClick = if (swapCardState.canSelectAnotherToken) onSelectTokenClick else null,
                modifier = modifier,
            )
        }
        is SwapCardState.SwapCardData -> {
            TransactionCard(
                type = swapCardState.type,
                balance = if (swapCardState.isBalanceHidden) {
                    STARS
                } else {
                    swapCardState.balance
                },
                textFieldValue = swapCardState.amountTextFieldValue,
                amountEquivalent = swapCardState.amountEquivalent,
                tokenIconUrl = swapCardState.tokenIconUrl ?: "",
                tokenCurrency = swapCardState.tokenCurrency,
                priceImpact = priceImpactWarning,
                networkIconRes = if (swapCardState.isNotNativeToken) swapCardState.networkIconRes else null,
                iconPlaceholder = swapCardState.coinId?.let {
                    getActiveIconResByCoinId(it)
                },
                onChangeTokenClick = if (swapCardState.canSelectAnotherToken) onSelectTokenClick else null,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwapButton(state: SwapStateHolder, modifier: Modifier = Modifier) {
    Card(
        elevation = TangemTheme.dimens.elevation3,
        shape = CircleShape,
        backgroundColor = TangemTheme.colors.background.plain,
        contentColor = TangemTheme.colors.text.primary1,
        modifier = modifier.size(TangemTheme.dimens.size48),
        onClick = state.onChangeCardsClicked,
        enabled = state.changeCardsButtonState == ChangeCardsButtonState.ENABLED,
    ) {
        when (state.changeCardsButtonState) {
            ChangeCardsButtonState.UPDATE_IN_PROGRESS -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(TangemTheme.dimens.size16)
                        .padding(TangemTheme.dimens.spacing14),
                    color = TangemTheme.colors.icon.primary1,
                    strokeWidth = TangemTheme.dimens.size2,
                )
            }
            ChangeCardsButtonState.ENABLED -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_exchange_vertical_24),
                    contentDescription = null,
                    tint = TangemTheme.colors.text.primary1,
                    modifier = Modifier.padding(TangemTheme.dimens.spacing12),
                )
            }
            ChangeCardsButtonState.DISABLED -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_exchange_vertical_24),
                    contentDescription = null,
                    tint = TangemTheme.colors.text.disabled,
                    modifier = Modifier.padding(TangemTheme.dimens.spacing12),
                )
            }


        }
    }
}

@Suppress("LongMethod")
@Composable
private fun SwapWarnings(warnings: List<SwapWarning>) {
    Column(
        modifier = Modifier
            .background(color = TangemTheme.colors.background.secondary)
            .fillMaxWidth(),
    ) {
        warnings.forEach { warning ->
            when (warning) {
                is SwapWarning.HighPriceImpact -> {
                    Notification(
                        config = warning.notificationConfig,
                        iconTint = TangemTheme.colors.icon.warning,
                    )
                }
                is SwapWarning.PermissionNeeded -> {
                    Notification(
                        config = warning.notificationConfig,
                    )
                }
                is SwapWarning.GenericWarning -> {
                    val message = warning.message?.let {
                        if (warning.shouldWrapMessage) {
                            String.format(stringResource(id = R.string.swapping_error_wrapper), it)
                        } else {
                            it
                        }
                    } ?: stringResource(id = R.string.swapping_generic_error)
                    RefreshableWaringCard(
                        title = stringResource(id = R.string.common_warning),
                        description = message,
                        onClick = warning.onClick,
                    )
                }
                is SwapWarning.NoAvailableTokensToSwap -> {
                    Notification(
                        config = warning.notificationConfig,
                    )
                }
                is SwapWarning.TooSmallAmountWarning -> {
                    Notification(
                        config = warning.notificationConfig,
                        iconTint = TangemTheme.colors.icon.warning,
                    )
                }
                is SwapWarning.UnableToCoverFeeWarning -> {
                    Notification(
                        config = warning.notificationConfig,
                    )
                }
                is SwapWarning.GeneralWarning -> {
                    Notification(
                        config = warning.notificationConfig,
                    )
                }
                is SwapWarning.TransactionInProgressWarning -> {
                    CardWithIcon(
                        title = warning.title.resolveReference(),
                        description = warning.description.resolveReference(),
                        icon = {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(TangemTheme.dimens.size16),
                                color = TangemTheme.colors.icon.primary1,
                                strokeWidth = TangemTheme.dimens.size2,
                            )
                        },
                    )
                }
                else -> {}
            }
            SpacerH8()
        }
    }
}

@Composable
private fun MainButton(state: SwapStateHolder, onPermissionWarningClick: () -> Unit) {
    // order is important
    when {
        state.warnings.any { it is SwapWarning.InsufficientFunds } -> {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.swapping_insufficient_funds),
                enabled = false,
                showProgress = state.swapButton.loading,
                onClick = state.swapButton.onClick,
            )
        }
        state.warnings.any { it is SwapWarning.PermissionNeeded } -> {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.swapping_give_permission),
                enabled = true,
                showProgress = state.swapButton.loading,
                onClick = onPermissionWarningClick,
            )
        }
        else -> {
            PrimaryButtonIconEnd(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.common_swap),
                iconResId = R.drawable.ic_tangem_24,
                enabled = state.swapButton.enabled,
                showProgress = state.swapButton.loading,
                onClick = state.swapButton.onClick,
            )
        }
    }
}

// region preview

private val sendCard = SwapCardState.SwapCardData(
    type = TransactionCardType.SendCard({}) {},
    amountTextFieldValue = TextFieldValue(),
    amountEquivalent = "1 000 000",
    tokenIconUrl = "",
    tokenCurrency = "DAI",
    isNotNativeToken = true,
    canSelectAnotherToken = false,
    balance = "123",
    coinId = "",
    token = null,
    networkIconRes = R.drawable.img_polygon_22,
    isBalanceHidden = false,
)

private val receiveCard = SwapCardState.SwapCardData(
    type = TransactionCardType.ReceiveCard(),
    amountTextFieldValue = TextFieldValue(),
    amountEquivalent = "1 000 000",
    tokenIconUrl = "",
    tokenCurrency = "DAI",
    isNotNativeToken = true,
    canSelectAnotherToken = true,
    balance = "33333",
    coinId = "",
    token = null,
    networkIconRes = R.drawable.img_polygon_22,
    isBalanceHidden = false,
)

private val state = SwapStateHolder(
    sendCardData = sendCard,
    receiveCardData = receiveCard,
    fee = FeeItemState.Content(
        feeType = FeeType.NORMAL,
        title = stringReference("Fee"),
        amountCrypto = "100",
        symbolCrypto = "1000",
        amountFiatFormatted = "(100)",
        isClickable = true,
        onClick = {},
    ),
    warnings = listOf(
        SwapWarning.PermissionNeeded(
            notificationConfig = NotificationConfig(
                title = stringReference("Give Premission"),
                subtitle = stringReference("To continue swapping you need to give permission to Tangem"),
                iconResId = R.drawable.ic_locked_24,
            ),
        ),
        SwapWarning.NoAvailableTokensToSwap(
            notificationConfig = NotificationConfig(
                title = stringReference("No tokens"),
                subtitle = stringReference("Swap tokens not available"),
                iconResId = R.drawable.img_attention_20,
            ),
        ),
    ),
    networkCurrency = "MATIC",
    swapButton = SwapButton(enabled = true, loading = false, onClick = {}),
    onRefresh = {},
    onBackClicked = {},
    onChangeCardsClicked = {},
    permissionState = SwapPermissionState.InProgress,
    blockchainId = "POLYGON",
    providerState = ProviderState.Loading(),
)

@Preview
@Composable
private fun SwapScreenContentPreview() {
    TangemTheme(isDark = false) {
        SwapScreenContent(state = state, modifier = Modifier)
    }
}

// endregion preview
