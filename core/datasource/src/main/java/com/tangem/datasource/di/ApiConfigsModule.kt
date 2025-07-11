package com.tangem.datasource.di

import com.tangem.datasource.api.common.AuthProvider
import com.tangem.datasource.api.common.config.*
import com.tangem.datasource.local.config.environment.EnvironmentConfigStorage
import com.tangem.lib.auth.ExpressAuthProvider
import com.tangem.lib.auth.StakeKitAuthProvider
import com.tangem.utils.version.AppVersionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal object ApiConfigsModule {

    @Provides
    @IntoSet
    fun provideExpressConfig(
        environmentConfigStorage: EnvironmentConfigStorage,
        expressAuthProvider: ExpressAuthProvider,
        appVersionProvider: AppVersionProvider,
    ): ApiConfig {
        return Express(environmentConfigStorage, expressAuthProvider, appVersionProvider)
    }

    @Provides
    @IntoSet
    fun provideStakeKitConfig(stakeKitAuthProvider: StakeKitAuthProvider): ApiConfig {
        return StakeKit(stakeKitAuthProvider)
    }

    @Provides
    @IntoSet
    fun provideTangemTechConfig(appVersionProvider: AppVersionProvider, authProvider: AuthProvider): ApiConfig =
        TangemTech(appVersionProvider, authProvider)

    @Provides
    @IntoSet
    fun provideTangemVisaConfig(appVersionProvider: AppVersionProvider): ApiConfig = TangemVisa(appVersionProvider)

    @Provides
    @IntoSet
    fun provideBlockAidConfig(environmentConfigStorage: EnvironmentConfigStorage): ApiConfig {
        return BlockAid(environmentConfigStorage)
    }

    @Provides
    @IntoSet
    fun provideAttestationConfig(): ApiConfig = Attestation()
}
