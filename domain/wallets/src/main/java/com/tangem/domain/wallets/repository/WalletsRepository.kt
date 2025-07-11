package com.tangem.domain.wallets.repository

import com.tangem.domain.wallets.models.SeedPhraseNotificationsStatus
import com.tangem.domain.wallets.models.UserWalletId
import kotlinx.coroutines.flow.Flow

interface WalletsRepository {

    suspend fun shouldSaveUserWalletsSync(): Boolean

    fun shouldSaveUserWallets(): Flow<Boolean>

    suspend fun saveShouldSaveUserWallets(item: Boolean)

    suspend fun isWalletWithRing(userWalletId: UserWalletId): Boolean

    suspend fun setHasWalletsWithRing(userWalletId: UserWalletId)

    fun seedPhraseNotificationStatus(userWalletId: UserWalletId): Flow<SeedPhraseNotificationsStatus>

    suspend fun notifiedSeedPhraseNotification(userWalletId: UserWalletId)

    suspend fun confirmSeedPhraseNotification(userWalletId: UserWalletId)

    suspend fun declineSeedPhraseNotification(userWalletId: UserWalletId)

    suspend fun rejectSeedPhraseSecondNotification(userWalletId: UserWalletId)

    suspend fun acceptSeedPhraseSecondNotification(userWalletId: UserWalletId)

    suspend fun markWallet2WasCreated(userWalletId: UserWalletId)

    fun nftEnabledStatus(userWalletId: UserWalletId): Flow<Boolean>

    fun nftEnabledStatuses(): Flow<Map<UserWalletId, Boolean>>

    suspend fun enableNFT(userWalletId: UserWalletId)

    suspend fun disableNFT(userWalletId: UserWalletId)

    @Throws
    suspend fun isNotificationsEnabled(userWalletId: UserWalletId, force: Boolean): Boolean

    @Throws
    suspend fun setNotificationsEnabled(userWalletId: UserWalletId, isEnabled: Boolean)
}
