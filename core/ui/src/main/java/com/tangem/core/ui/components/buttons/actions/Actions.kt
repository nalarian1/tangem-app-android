package com.tangem.core.ui.components.buttons.actions

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.tangem.core.ui.R
import com.tangem.core.ui.components.SpacerW8
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.extensions.resolveReference
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.res.TangemThemePreview

/**
 * Rounded action button
 *
 * @param config   component config
 * @param modifier modifier
 * @param color    background color
 *
 * @see <a href = "https://www.figma.com/file/14ISV23YB1yVW1uNVwqrKv/Android?type=design&node-id=290-306&mode=design&t=
 * cSLQtIbR2J1h5M9U-4">Show in Figma</a>
 */
@Composable
fun RoundedActionButton(
    config: ActionButtonConfig,
    modifier: Modifier = Modifier,
    color: Color = TangemTheme.colors.button.secondary,
) {
    Button(
        config = config,
        shape = RoundedCornerShape(size = TangemTheme.dimens.radius24),
        modifier = modifier,
        color = color,
    )
}

/**
 * Action button
 *
 * @param config   component config
 * @param modifier modifier
 * @param color    background color
 *
 * @see <a href = "https://www.figma.com/file/14ISV23YB1yVW1uNVwqrKv/Android?type=design&node-id=290-306&mode=design&t=
 * cSLQtIbR2J1h5M9U-4">Show in Figma</a>
 */
@Composable
fun ActionButton(
    config: ActionButtonConfig,
    modifier: Modifier = Modifier,
    color: Color = TangemTheme.colors.button.secondary,
) {
    Button(
        config = config,
        shape = RoundedCornerShape(size = TangemTheme.dimens.radius12),
        modifier = modifier,
        color = color,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Button(
    config: ActionButtonConfig,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    color: Color = TangemTheme.colors.button.secondary,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (config.enabled) color else TangemTheme.colors.button.disabled,
        label = "Update background color",
    )
    val context = LocalContext.current
    Row(
        modifier = modifier
            .heightIn(min = TangemTheme.dimens.size36)
            .clip(shape)
            .background(color = backgroundColor)
            .combinedClickable(
                enabled = config.enabled,
                onClick = config.onClick,
                onLongClick = {
                    val toastReference = config.onLongClick?.invoke()
                    toastReference?.let {
                        Toast
                            .makeText(context, toastReference.resolveReference(context.resources), Toast.LENGTH_SHORT)
                            .show()
                    }
                },
            )
            .padding(start = TangemTheme.dimens.spacing16, end = TangemTheme.dimens.spacing24)
            .padding(vertical = TangemTheme.dimens.spacing8),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconTint by animateColorAsState(
            targetValue = when {
                !config.enabled -> TangemTheme.colors.icon.informative
                config.dimContent -> TangemTheme.colors.icon.informative
                else -> TangemTheme.colors.icon.primary1
            },
            label = "Update tint color",
        )

        Icon(
            painter = painterResource(id = config.iconResId),
            contentDescription = null,
            modifier = Modifier.size(size = TangemTheme.dimens.size20),
            tint = iconTint,
        )

        SpacerW8()

        val textColor by animateColorAsState(
            targetValue = when {
                !config.enabled -> TangemTheme.colors.text.disabled
                config.dimContent -> TangemTheme.colors.text.tertiary
                else -> TangemTheme.colors.text.primary1
            },
            label = "Update text color",
        )

        Text(
            text = config.text.resolveReference(),
            color = textColor,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = TangemTheme.typography.button,
        )
    }
}

@Preview(group = "RoundedActionButton", showBackground = true)
@Preview(group = "RoundedActionButton", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_RoundedActionButton(@PreviewParameter(ActionStateProvider::class) state: ActionButtonConfig) {
    TangemThemePreview {
        RoundedActionButton(state)
    }
}

@Preview(group = "ActionButton", showBackground = true)
@Preview(group = "ActionButton", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_ActionButton(@PreviewParameter(ActionStateProvider::class) state: ActionButtonConfig) {
    TangemThemePreview {
        ActionButton(state)
    }
}

private class ActionStateProvider : CollectionPreviewParameterProvider<ActionButtonConfig>(
    collection = listOf(
        ActionButtonConfig(
            text = TextReference.Str(value = "Enabled"),
            iconResId = R.drawable.ic_arrow_up_24,
            enabled = true,
            onClick = {},
        ),
        ActionButtonConfig(
            text = TextReference.Str(value = "Dimmed"),
            iconResId = R.drawable.ic_arrow_up_24,
            enabled = true,
            dimContent = true,
            onClick = {},
        ),
        ActionButtonConfig(
            text = TextReference.Str(value = "Disabled"),
            iconResId = R.drawable.ic_arrow_down_24,
            enabled = false,
            onClick = {},
        ),
    ),
)
