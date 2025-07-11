package com.tangem.features.staking.impl.presentation.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.tangem.core.ui.components.inputrow.InputRowImageSelector
import com.tangem.core.ui.decorations.roundedShapeItemDecoration
import com.tangem.core.ui.extensions.*
import com.tangem.core.ui.format.bigdecimal.format
import com.tangem.core.ui.format.bigdecimal.percent
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.res.TangemThemePreview
import com.tangem.domain.staking.model.stakekit.Yield
import com.tangem.features.staking.impl.R
import com.tangem.features.staking.impl.presentation.model.StakingClickIntents
import com.tangem.features.staking.impl.presentation.state.StakingStates
import com.tangem.features.staking.impl.presentation.state.previewdata.ValidatorStatePreviewData
import com.tangem.features.staking.impl.presentation.state.stub.StakingClickIntentsStub
import com.tangem.utils.extensions.orZero

/**
 * Staking screen with validators
 */
@Composable
internal fun StakingValidatorListContent(
    state: StakingStates.ValidatorState,
    clickIntents: StakingClickIntents,
    modifier: Modifier = Modifier,
) {
    val bottomBarHeight = with(LocalDensity.current) { WindowInsets.systemBars.getBottom(this).toDp() }

    LazyColumn(
        contentPadding = PaddingValues(bottom = bottomBarHeight),
        modifier = modifier
            .background(TangemTheme.colors.background.secondary)
            .padding(horizontal = TangemTheme.dimens.spacing16),
    ) {
        if (state is StakingStates.ValidatorState.Data) {
            val validators = state.availableValidators
            items(
                count = validators.size,
                key = { validators[it].address },
                contentType = { validators[it]::class.java },
            ) { index ->
                val item = validators[index]

                InputRowImageSelector(
                    subtitle = stringReference(item.name),
                    caption = item.getAprTextNeutral(),
                    imageUrl = item.image.orEmpty(),
                    isSelected = item == state.chosenValidator,
                    onSelect = { clickIntents.onValidatorSelect(item) },
                    modifier = Modifier
                        .roundedShapeItemDecoration(
                            currentIndex = index,
                            lastIndex = validators.lastIndex,
                            radius = TangemTheme.dimens.radius12,
                            addDefaultPadding =
                            false,
                        )
                        .background(TangemTheme.colors.background.action),
                    subtitleExtraContent = {
                        ValidatorLabel(item.isStrategicPartner)
                    },
                    selectorContent = { checked, _, _ ->
                        AnimatedVisibility(
                            modifier = Modifier,
                            visible = checked,
                        ) {
                            Icon(
                                painter = rememberVectorPainter(
                                    image = ImageVector.vectorResource(id = R.drawable.ic_check_24),
                                ),
                                tint = TangemTheme.colors.icon.accent,
                                contentDescription = null,
                            )
                        }
                    },
                    onImageError = { ValidatorImagePlaceholder() },
                )
            }
        }
    }
}

/**
 * For FCA fixes remove coloring for now
 */
@Suppress("UnusedPrivateMember")
@Composable
private fun Yield.Validator.getAprTextColored() = combinedReference(
    resourceReference(R.string.staking_details_annual_percentage_rate),
    annotatedReference {
        appendSpace()
        appendColored(
            text = apr.orZero().format { percent() },
            color = TangemTheme.colors.text.accent,
        )
    },
)

@Composable
private fun Yield.Validator.getAprTextNeutral() = combinedReference(
    resourceReference(R.string.staking_details_annual_percentage_rate),
    stringReference(" " + apr.orZero().format { percent() }),
)

@Composable
private fun RowScope.ValidatorLabel(isStrategicPartner: Boolean) {
    if (isStrategicPartner) {
        Text(
            text = stringResourceSafe(R.string.staking_validators_label),
            style = TangemTheme.typography.caption1,
            color = TangemTheme.colors.icon.constant,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(horizontal = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(TangemTheme.colors.text.accent)
                .padding(horizontal = 8.dp),
        )
    }
}

// region Preview
@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StakingValidatorListContent_Preview(
    @PreviewParameter(StakingValidatorListContentPreviewProvider::class)
    data: StakingStates.ValidatorState,
) {
    TangemThemePreview {
        StakingValidatorListContent(
            state = data,
            clickIntents = StakingClickIntentsStub,
        )
    }
}

private class StakingValidatorListContentPreviewProvider : PreviewParameterProvider<StakingStates.ValidatorState> {
    override val values: Sequence<StakingStates.ValidatorState>
        get() = sequenceOf(ValidatorStatePreviewData.validatorState)
}
// endregion
