package org.bravo.pinger

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.bravo.pinger.Common.pingTo

object FlowAsyncPinger : IPinger {

    override suspend fun ping(ips: List<String>): Map<String, Boolean> {
        val answers = hashMapOf<String, Boolean>()

        val asyncJobs = mutableListOf<Job>()

        ips.asFlow().collect { ip ->
            GlobalScope.launch {
                answers[ip] = pingTo(ip)
            }.also { job ->
                asyncJobs.add(job)
            }
        }

        asyncJobs.joinAll()

        return answers
    }
}
