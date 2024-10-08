package com.tangem.datasource.config

/**
 * Created by Anton Zhilenkov on 12/11/2020.
 */
interface Loader<T> {
    suspend fun load(onComplete: (T) -> Unit)
}
