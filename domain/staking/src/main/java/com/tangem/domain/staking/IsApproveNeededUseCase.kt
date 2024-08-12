package com.tangem.domain.staking

import arrow.core.Either
import com.tangem.domain.staking.model.stakekit.StakingError
import com.tangem.domain.staking.repositories.StakingErrorResolver
import com.tangem.domain.staking.repositories.StakingRepository
import com.tangem.domain.tokens.model.CryptoCurrency

class IsApproveNeededUseCase(
    private val stakingRepository: StakingRepository,
    private val stakingErrorResolver: StakingErrorResolver,
) {
    operator fun invoke(cryptoCurrency: CryptoCurrency): Either<StakingError, Boolean> {
        return Either
            .catch { stakingRepository.isApproveNeeded(cryptoCurrency) }
            .mapLeft { stakingErrorResolver.resolve(it) }
    }
}
