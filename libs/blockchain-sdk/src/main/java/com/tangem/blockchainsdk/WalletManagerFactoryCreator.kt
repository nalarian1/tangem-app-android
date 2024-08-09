package com.tangem.blockchainsdk

import com.tangem.blockchain.common.AccountCreator
import com.tangem.blockchain.common.BlockchainFeatureToggles
import com.tangem.blockchain.common.BlockchainSdkConfig
import com.tangem.blockchain.common.WalletManagerFactory
import com.tangem.blockchain.common.datastorage.BlockchainDataStorage
import com.tangem.blockchain.common.logging.BlockchainSDKLogger
import com.tangem.blockchainsdk.featuretoggles.BlockchainSDKFeatureToggles
import timber.log.Timber
import javax.inject.Inject

/**
 * Creator of [WalletManagerFactory]
 *
 * @property accountCreator        account creator
 * @property blockchainDataStorage blockchain data storage
 * @property blockchainSDKLogger   blockchain SDK logger
 *
 * @author Andrew Khokhlov on 16/04/2024
 */
internal class WalletManagerFactoryCreator @Inject constructor(
    private val accountCreator: AccountCreator,
    private val blockchainDataStorage: BlockchainDataStorage,
    private val blockchainSDKLogger: BlockchainSDKLogger,
    private val blockchainSDKFeatureToggles: BlockchainSDKFeatureToggles,
) {

    fun create(config: BlockchainSdkConfig, blockchainProviderTypes: BlockchainProviderTypes): WalletManagerFactory {
        Timber.d("Create WalletManagerFactory")

        return WalletManagerFactory(
            config = config,
            blockchainProviderTypes = blockchainProviderTypes,
            accountCreator = accountCreator,
            featureToggles = BlockchainFeatureToggles(
                isCardanoTokenSupport = blockchainSDKFeatureToggles.isCardanoTokensSupportEnabled,
            ),
            blockchainDataStorage = blockchainDataStorage,
            loggers = listOf(blockchainSDKLogger),
        )
    }
}