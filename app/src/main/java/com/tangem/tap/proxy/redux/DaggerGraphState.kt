package com.tangem.tap.proxy.redux

import com.tangem.datasource.asset.AssetReader
import com.tangem.datasource.connection.NetworkConnectionManager
import com.tangem.features.tester.api.TesterRouter
import org.rekotlin.StateType

data class DaggerGraphState(
    val assetReader: AssetReader? = null,
    val testerRouter: TesterRouter? = null,
    val networkConnectionManager: NetworkConnectionManager? = null,
) : StateType {

    inline fun <reified T> get(getDependency: DaggerGraphState.() -> T?): T {
        return requireNotNull(getDependency()) {
            "${T::class.simpleName} isn't initialized "
        }
    }
}