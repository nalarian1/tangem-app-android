package com.tangem.datasource.config

import com.tangem.datasource.config.models.Config
import com.tangem.datasource.config.models.ConfigModel

/**
 * Created by Anton Zhilenkov on 12/11/2020.
 */
interface ConfigManager {

    val config: Config

    suspend fun load(configLoader: Loader<ConfigModel>, onComplete: ((config: Config) -> Unit)? = null)

    fun turnOff(name: String)

    fun resetToDefault(name: String)

    companion object {
        const val IS_CREATING_TWIN_CARDS_ALLOWED = "isCreatingTwinCardsAllowed"
        const val IS_TOP_UP_ENABLED = "isTopUpEnabled"
    }
}
