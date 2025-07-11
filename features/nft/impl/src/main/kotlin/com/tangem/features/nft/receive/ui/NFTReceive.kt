package com.tangem.features.nft.receive.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tangem.common.ui.bottomsheet.receive.TokenReceiveBottomSheet
import com.tangem.common.ui.bottomsheet.receive.TokenReceiveBottomSheetConfig
import com.tangem.core.ui.components.appbar.TangemTopAppBar
import com.tangem.core.ui.components.appbar.models.TopAppBarButtonUM
import com.tangem.core.ui.components.bottomsheets.TangemBottomSheetConfig
import com.tangem.core.ui.components.fields.SearchBar
import com.tangem.core.ui.components.fields.TangemSearchBarDefaults
import com.tangem.core.ui.extensions.resolveReference
import com.tangem.core.ui.extensions.stringResourceSafe
import com.tangem.core.ui.res.TangemTheme
import com.tangem.features.nft.impl.R
import com.tangem.features.nft.receive.entity.NFTReceiveUM

@Composable
internal fun NFTReceive(state: NFTReceiveUM, modifier: Modifier = Modifier) {
    BackHandler(onBack = state.onBackClick)

    Scaffold(
        modifier = modifier,
        containerColor = TangemTheme.colors.background.secondary,
        topBar = {
            TangemTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                startButton = TopAppBarButtonUM(
                    iconRes = R.drawable.ic_close_24,
                    onIconClicked = state.onBackClick,
                ),
                title = stringResourceSafe(id = R.string.nft_receive_title),
                subtitle = state.appBarSubtitle.resolveReference(),
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TangemTheme.dimens.spacing16),
                    state = state.search,
                    colors = TangemSearchBarDefaults.secondaryTextFieldColors,
                )

                when (val networks = state.networks) {
                    is NFTReceiveUM.Networks.Content -> NFTReceiveNetworksContent(networks)
                    is NFTReceiveUM.Networks.Empty -> NFTReceiveNetworksEmpty()
                }
            }
        },
    )

    ShowBottomSheet(state.bottomSheetConfig)
}

@Composable
private fun ShowBottomSheet(bottomSheetConfig: TangemBottomSheetConfig?) {
    if (bottomSheetConfig != null) {
        when (bottomSheetConfig.content) {
            is TokenReceiveBottomSheetConfig -> {
                TokenReceiveBottomSheet(config = bottomSheetConfig)
            }
        }
    }
}
