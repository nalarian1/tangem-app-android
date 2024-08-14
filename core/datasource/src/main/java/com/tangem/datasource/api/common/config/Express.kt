package com.tangem.datasource.api.common.config

import com.tangem.datasource.BuildConfig
import com.tangem.datasource.utils.RequestHeader
import com.tangem.lib.auth.ExpressAuthProvider
import com.tangem.utils.Provider
import com.tangem.utils.version.AppVersionProvider

/** Express [ApiConfig] */
internal class Express(
    private val expressAuthProvider: ExpressAuthProvider,
    private val appVersionProvider: AppVersionProvider,
) : ApiConfig() {

    override val defaultEnvironment: ApiEnvironment = getInitialEnvironment()

    override val environmentConfigs: List<ApiEnvironmentConfig> = listOf(
        createDevEnvironment(),
        createStageEnvironment(),
        createProdEnvironment(),
    )

    private fun createDevEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.DEV,
        baseUrl = "https://express.tangem.org/v1/",
        headers = createHeaders(),
    )

    private fun createStageEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.STAGE,
        baseUrl = "https://express-stage.tangem.com/v1/",
        headers = createHeaders(),
    )

    private fun createProdEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.PROD,
        baseUrl = "https://express.tangem.com/v1/",
        headers = createHeaders(),
    )

    private fun createHeaders() = buildMap {
        put(key = "api-key", value = Provider(expressAuthProvider::getApiKey))
        put(key = "user-id", value = Provider(expressAuthProvider::getUserId))
        put(key = "session-id", value = Provider(expressAuthProvider::getSessionId))
        putAll(from = RequestHeader.AppVersionPlatformHeaders(appVersionProvider).values)
    }

    private companion object {

        fun getInitialEnvironment(): ApiEnvironment {
            return when (BuildConfig.BUILD_TYPE) {
                DEBUG_BUILD_TYPE -> ApiEnvironment.DEV
                INTERNAL_BUILD_TYPE,
                MOCKED_BUILD_TYPE,
                -> ApiEnvironment.STAGE
                EXTERNAL_BUILD_TYPE,
                RELEASE_BUILD_TYPE,
                -> ApiEnvironment.PROD
                else -> error("Unknown build type [${BuildConfig.BUILD_TYPE}]")
            }
        }
    }
}
