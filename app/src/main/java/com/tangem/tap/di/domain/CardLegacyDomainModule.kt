package com.tangem.tap.di.domain

import com.tangem.domain.card.*
import com.tangem.domain.wallets.legacy.UserWalletsListManager
import com.tangem.domain.wallets.usecase.GenerateWalletNameUseCase
import com.tangem.tap.domain.scanCard.DefaultScanCardProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CardLegacyDomainModule {

    @Provides
    @Singleton
    fun provideScanCardProcessor(): ScanCardProcessor = DefaultScanCardProcessor()

    @Provides
    @Singleton
    fun providesWalletNameGenerateUseCase(userWalletsListManager: UserWalletsListManager): GenerateWalletNameUseCase {
        return GenerateWalletNameUseCase(userWalletsListManager)
    }
}
