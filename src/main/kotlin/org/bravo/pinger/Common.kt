package org.bravo.pinger

import org.icmp4j.IcmpPingRequest
import org.icmp4j.IcmpPingUtil

object Common {

    private fun createRequest(ip: String): IcmpPingRequest =
        IcmpPingUtil.createIcmpPingRequest().apply {
            timeout = 500
            ttl = 255
            host = ip
            packetSize = 32
        }

    fun pingTo(ip: String): Boolean {
        val request = createRequest(ip)

        return executePing(request)
    }

    private fun executePing(request: IcmpPingRequest): Boolean {
        repeat(3) {
            val response = IcmpPingUtil.executePingRequest(request)

            if (response.successFlag) {
                return true
            }
        }

        return false
    }
}