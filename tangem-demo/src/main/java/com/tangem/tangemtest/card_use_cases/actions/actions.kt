package com.tangem.tangemtest.card_use_cases.actions

import androidx.annotation.StringRes
import com.tangem.tangemtest.R

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
sealed class Action(@StringRes val resId: Int) {
    class Scan: Action(R.string.action_card_scan)
    class Sign: Action(R.string.action_card_sign)
}