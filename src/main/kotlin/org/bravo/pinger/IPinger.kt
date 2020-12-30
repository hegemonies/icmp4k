package org.bravo.pinger

interface IPinger {

    suspend fun ping(ips: List<String>): Map<String, Boolean>
}
