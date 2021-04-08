package org.bravo.pinger

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.bravo.pinger.Common.pingTo

object FlowAsyncPinger : IPinger {
    val dispatcher = newFixedThreadPoolContext(256, "flow-dispatcher")

    override suspend fun ping(ips: List<String>): Map<String, Boolean> {
        val answers = hashMapOf<String, Boolean>()

        val asyncJobs = mutableListOf<Job>()

        ips.forEach { ip ->
            GlobalScope.launch(dispatcher) {
                answers[ip] = pingTo(ip)
            }.also { job ->
                asyncJobs.add(job)
            }
        }

        asyncJobs.joinAll()

        return answers
    }
}
