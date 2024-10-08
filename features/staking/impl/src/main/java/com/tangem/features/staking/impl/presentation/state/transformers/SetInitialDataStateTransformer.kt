package com.tangem.features.staking.impl.presentation.state.transformers

import com.tangem.common.extensions.remove
import com.tangem.common.ui.amountScreen.converters.AmountStateConverter
import com.tangem.common.ui.amountScreen.models.AmountState
import com.tangem.core.ui.components.currency.icon.converter.CryptoCurrencyToIconStateConverter
import com.tangem.core.ui.components.list.RoundedListWithDividersItemData
import com.tangem.core.ui.extensions.*
import com.tangem.core.ui.pullToRefresh.PullToRefreshConfig
import com.tangem.core.ui.utils.BigDecimalFormatter
import com.tangem.domain.appcurrency.model.AppCurrency
import com.tangem.domain.core.serialization.SerializedBigDecimal
import com.tangem.domain.staking.model.stakekit.BalanceItem
import com.tangem.domain.staking.model.stakekit.Yield
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.wallets.models.UserWallet
import com.tangem.features.staking.impl.R
import com.tangem.features.staking.impl.presentation.state.*
import com.tangem.features.staking.impl.presentation.state.bottomsheet.InfoType
import com.tangem.features.staking.impl.presentation.state.converters.RewardsValidatorStateConverter
import com.tangem.features.staking.impl.presentation.state.converters.YieldBalancesConverter
import com.tangem.features.staking.impl.presentation.viewmodel.StakingClickIntents
import com.tangem.lib.crypto.BlockchainUtils.isPolkadot
import com.tangem.utils.Provider
import com.tangem.utils.isNullOrZero
import com.tangem.utils.transformer.Transformer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import java.math.BigDecimal

@Suppress("LongParameterList")
internal class SetInitialDataStateTransformer(
    private val clickIntents: StakingClickIntents,
    private val yield: Yield,
    private val isApprovalNeeded: Boolean,
    private val isAnyTokenStaked: Boolean,
    private val cryptoCurrencyStatusProvider: Provider<CryptoCurrencyStatus>,
    private val userWalletProvider: Provider<UserWallet>,
    private val appCurrencyProvider: Provider<AppCurrency>,
    private val balancesToShowProvider: Provider<List<BalanceItem>>,
) : Transformer<StakingUiState> {

    private val iconStateConverter by lazy(::CryptoCurrencyToIconStateConverter)

    private val amountStateConverter by lazy(LazyThreadSafetyMode.NONE) {
        AmountStateConverter(
            clickIntents = clickIntents,
            cryptoCurrencyStatusProvider = cryptoCurrencyStatusProvider,
            appCurrencyProvider = appCurrencyProvider,
            userWalletProvider = userWalletProvider,
            iconStateConverter = iconStateConverter,
        )
    }

    private val rewardsValidatorStateConverter by lazy(LazyThreadSafetyMode.NONE) {
        RewardsValidatorStateConverter(cryptoCurrencyStatusProvider, appCurrencyProvider, yield)
    }

    private val yieldBalancesConverter by lazy(LazyThreadSafetyMode.NONE) {
        YieldBalancesConverter(
            cryptoCurrencyStatusProvider,
            appCurrencyProvider,
            balancesToShowProvider,
            yield,
        )
    }

    override fun transform(prevState: StakingUiState): StakingUiState {
        val cryptoCurrency = cryptoCurrencyStatusProvider().currency
        return prevState.copy(
            title = TextReference.EMPTY,
            cryptoCurrencyName = cryptoCurrency.name,
            cryptoCurrencySymbol = cryptoCurrency.symbol,
            clickIntents = clickIntents,
            currentStep = StakingStep.InitialInfo,
            initialInfoState = createInitialInfoState(),
            amountState = createInitialAmountState(),
            confirmationState = createInitialConfirmationState(),
            rewardsValidatorsState = rewardsValidatorStateConverter.convert(Unit),
            bottomSheetConfig = null,
        )
    }

    private fun createInitialInfoState(): StakingStates.InitialInfoState.Data {
        val yieldBalance = yieldBalancesConverter.convert(Unit)
        return StakingStates.InitialInfoState.Data(
            isPrimaryButtonEnabled = !cryptoCurrencyStatusProvider().value.amount.isNullOrZero(),
            showBanner = !isAnyTokenStaked && yieldBalance == InnerYieldBalanceState.Empty,
            aprRange = getAprRange(yield.validators),
            infoItems = getInfoItems(),
            onInfoClick = clickIntents::onInfoClick,
            yieldBalance = yieldBalance,
            pullToRefreshConfig = PullToRefreshConfig(
                onRefresh = { clickIntents.onRefreshSwipe(it.value) },
                isRefreshing = false,
            ),
        )
    }

    private fun getInfoItems(): PersistentList<RoundedListWithDividersItemData> {
        val cryptoCurrencyStatus = cryptoCurrencyStatusProvider()

        return listOfNotNull(
            createAnnualPercentageRateItem(yield.validators),
            createAvailableItem(cryptoCurrencyStatus),
            createUnbondingPeriodItem(yield.metadata.cooldownPeriod?.days),
            createMinimumRequirementItem(
                cryptoCurrencyStatus = cryptoCurrencyStatus,
                minimumCryptoAmount = yield.args.enter.args[Yield.Args.ArgType.AMOUNT]?.minimum,
            ),
            createRewardClaimingItem(yield.metadata.rewardClaiming),
            createWarmupPeriodItem(yield.metadata.warmupPeriod.days),
            createRewardScheduleItem(yield.metadata.rewardSchedule),
        ).toPersistentList()
    }

    private fun createAnnualPercentageRateItem(validators: List<Yield.Validator>): RoundedListWithDividersItemData {
        return RoundedListWithDividersItemData(
            id = R.string.staking_details_annual_percentage_rate,
            startText = TextReference.Res(R.string.staking_details_annual_percentage_rate),
            endText = getAprRange(validators),
            iconClick = { clickIntents.onInfoClick(InfoType.ANNUAL_PERCENTAGE_RATE) },
            isEndTextHighlighted = true,
        )
    }

    private fun createAvailableItem(cryptoCurrencyStatus: CryptoCurrencyStatus): RoundedListWithDividersItemData {
        return RoundedListWithDividersItemData(
            id = R.string.staking_details_available,
            startText = TextReference.Res(R.string.staking_details_available),
            endText = TextReference.Str(
                value = BigDecimalFormatter.formatCryptoAmount(
                    cryptoAmount = cryptoCurrencyStatus.value.amount,
                    cryptoCurrency = cryptoCurrencyStatus.currency.symbol,
                    decimals = cryptoCurrencyStatus.currency.decimals,
                ),
            ),
            isEndTextHideable = true,
        )
    }

    private fun createUnbondingPeriodItem(cooldownPeriodDays: Int?): RoundedListWithDividersItemData? {
        cooldownPeriodDays ?: return null
        return RoundedListWithDividersItemData(
            id = R.string.staking_details_unbonding_period,
            startText = TextReference.Res(R.string.staking_details_unbonding_period),
            endText = pluralReference(
                id = R.plurals.common_days,
                count = cooldownPeriodDays,
                formatArgs = wrappedList(cooldownPeriodDays),
            ),
            iconClick = { clickIntents.onInfoClick(InfoType.UNBONDING_PERIOD) },
        )
    }

    private fun createMinimumRequirementItem(
        cryptoCurrencyStatus: CryptoCurrencyStatus,
        minimumCryptoAmount: SerializedBigDecimal?,
    ): RoundedListWithDividersItemData? {
        if (minimumCryptoAmount == null) return null
        if (!isPolkadot(cryptoCurrencyStatus.currency.network.id.value)) return null

        val formattedAmount = BigDecimalFormatter.formatCryptoAmount(
            cryptoAmount = minimumCryptoAmount,
            cryptoCurrency = cryptoCurrencyStatus.currency.symbol,
            decimals = cryptoCurrencyStatus.currency.decimals,
        )

        return RoundedListWithDividersItemData(
            id = R.string.staking_details_minimum_requirement,
            startText = TextReference.Res(R.string.staking_details_minimum_requirement),
            endText = TextReference.Str(formattedAmount),
        )
    }

    private fun createRewardClaimingItem(
        rewardClaiming: Yield.Metadata.RewardClaiming,
    ): RoundedListWithDividersItemData? {
        val endTextId = rewardClaimingResources[rewardClaiming] ?: return null

        return RoundedListWithDividersItemData(
            id = R.string.staking_details_reward_claiming,
            startText = TextReference.Res(R.string.staking_details_reward_claiming),
            endText = TextReference.Res(endTextId),
            iconClick = { clickIntents.onInfoClick(InfoType.REWARD_CLAIMING) },
        )
    }

    private fun createWarmupPeriodItem(warmupPeriodDays: Int): RoundedListWithDividersItemData? {
        if (warmupPeriodDays == 0) return null

        return RoundedListWithDividersItemData(
            id = R.string.staking_details_warmup_period,
            startText = TextReference.Res(R.string.staking_details_warmup_period),
            endText = pluralReference(
                id = R.plurals.common_days,
                count = warmupPeriodDays,
                formatArgs = wrappedList(warmupPeriodDays),
            ),
            iconClick = { clickIntents.onInfoClick(InfoType.WARMUP_PERIOD) },
        )
    }

    private fun createRewardScheduleItem(
        rewardSchedule: Yield.Metadata.RewardSchedule,
    ): RoundedListWithDividersItemData? {
        val endTextReference = getRewardScheduleText(rewardSchedule) ?: return null

        return RoundedListWithDividersItemData(
            id = R.string.staking_details_reward_schedule,
            startText = resourceReference(R.string.staking_details_reward_schedule),
            endText = endTextReference,
            iconClick = { clickIntents.onInfoClick(InfoType.REWARD_SCHEDULE) },
        )
    }

    private fun createInitialAmountState(): AmountState {
        return amountStateConverter.convert("")
    }

    private fun createInitialConfirmationState(): StakingStates.ConfirmationState {
        return StakingStates.ConfirmationState.Data(
            isPrimaryButtonEnabled = false,
            innerState = InnerConfirmationStakingState.ASSENT,
            feeState = FeeState.Loading,
            validatorState = ValidatorState.Loading,
            notifications = persistentListOf(),
            footerText = TextReference.EMPTY,
            transactionDoneState = TransactionDoneState.Empty,
            pendingAction = null,
            pendingActions = null,
            isApprovalNeeded = isApprovalNeeded,
            reduceAmountBy = null,
            balanceState = null,
        )
    }

    private fun getAprRange(validators: List<Yield.Validator>): TextReference {
        val aprValues = validators.mapNotNull { it.apr }

        val minApr = aprValues.min()
        val maxApr = aprValues.max()

        val formattedMinApr = BigDecimalFormatter.formatPercent(
            percent = minApr,
            useAbsoluteValue = true,
        ).remove("%")
        val formattedMaxApr = BigDecimalFormatter.formatPercent(
            percent = maxApr,
            useAbsoluteValue = true,
        )

        if (maxApr - minApr < EQUALITY_THRESHOLD) {
            return stringReference("$formattedMinApr%")
        }
        return resourceReference(R.string.common_range, wrappedList(formattedMinApr, formattedMaxApr))
    }

    private fun getRewardScheduleText(rewardSchedule: Yield.Metadata.RewardSchedule): TextReference? {
        return when (rewardSchedule) {
            Yield.Metadata.RewardSchedule.BLOCK -> resourceReference(R.string.staking_reward_schedule_block)
            Yield.Metadata.RewardSchedule.WEEK -> resourceReference(R.string.staking_reward_schedule_week)
            Yield.Metadata.RewardSchedule.HOUR -> resourceReference(R.string.staking_reward_schedule_hour)
            Yield.Metadata.RewardSchedule.DAY -> resourceReference(R.string.staking_reward_schedule_each_day)
            Yield.Metadata.RewardSchedule.MONTH -> resourceReference(R.string.staking_reward_schedule_month)
            Yield.Metadata.RewardSchedule.ERA -> resourceReference(R.string.staking_reward_schedule_era)
            Yield.Metadata.RewardSchedule.EPOCH -> resourceReference(R.string.staking_reward_schedule_epoch)
            Yield.Metadata.RewardSchedule.UNKNOWN -> null
        }
    }

    private companion object {
        val EQUALITY_THRESHOLD = BigDecimal(1E-10)

        val rewardClaimingResources = mapOf(
            Yield.Metadata.RewardClaiming.MANUAL to R.string.staking_reward_claiming_manual,
            Yield.Metadata.RewardClaiming.AUTO to R.string.staking_reward_claiming_auto,
            Yield.Metadata.RewardSchedule.UNKNOWN to null,
        )
    }
}
