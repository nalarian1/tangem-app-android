package com.tangem.tangemtest.ucase.domain.paramsManager.triggers.afterAction

import com.tangem.tangemtest.ucase.domain.paramsManager.IncomingParameter
import com.tangem.tasks.TaskEvent

/**
 * Created by Anton Zhilenkov on 13.03.2020.
 *
 * The After Action Modification class family is intended for modifying parameters (if necessary)
 * after calling CardManager.anyAction.
 * Returns a list of parameters that have been modified
 */
interface AfterActionModification {
    fun modify(taskEvent: TaskEvent<*>, paramsList: List<IncomingParameter>): List<IncomingParameter>
}