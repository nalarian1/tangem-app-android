package com.tangem.tap.features.walletSelector.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tangem.core.ui.components.BasicDialog
import com.tangem.core.ui.components.DialogButton
import com.tangem.core.ui.res.TangemTheme
import com.tangem.tap.features.walletSelector.ui.model.DialogModel
import com.tangem.wallet.R

@Composable
internal fun BiometricsLockoutDialogContent(
    dialog: DialogModel.BiometricsLockoutDialog,
) {
    BasicDialog(
        title = stringResource(id = R.string.biometric_lockout_warning_title),
        message = stringResource(
            id = if (dialog.isPermanent) {
                R.string.biometric_lockout_permanent_warning_description
            } else {
                R.string.biometric_lockout_warning_description
            },
        ),
        onDismissDialog = dialog.onDismiss,
        confirmButton = DialogButton(
            title = stringResource(id = R.string.common_ok),
            onClick = dialog.onDismiss,
        ),
    )
}

// region Preview
@Composable
private fun BiometricsLockoutDialogSample(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        BiometricsLockoutDialogContent(
            dialog = DialogModel.BiometricsLockoutDialog(
                isPermanent = false,
                onDismiss = {},
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BiometricsLockoutDialogPreview_Light() {
    TangemTheme {
        BiometricsLockoutDialogSample()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BiometricsLockoutDialogPreview_Dark() {
    TangemTheme(isDark = true) {
        BiometricsLockoutDialogSample()
    }
}

@Composable
private fun BiometricsLockoutDialog_Permanent_Sample(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        BiometricsLockoutDialogContent(
            dialog = DialogModel.BiometricsLockoutDialog(
                isPermanent = true,
                onDismiss = {},
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BiometricsLockoutDialog_Permanent_Preview_Light() {
    TangemTheme {
        BiometricsLockoutDialog_Permanent_Sample()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BiometricsLockoutDialog_Permanent_Preview_Dark() {
    TangemTheme(isDark = true) {
        BiometricsLockoutDialog_Permanent_Sample()
    }
}
// endregion Preview