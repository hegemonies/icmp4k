package org.bravo.pinger

import kotlin.system.measureTimeMillis

suspend fun main() {
    val testData = listOf("google.com", "10.25.96.33", "90.189.213.176", "3.3.3.3")

    measureTimeMillis {
        println(
            FlowAsyncPinger.ping(testData)
        )
    }.also { elapsedTime ->
        println("flow async pings took $elapsedTime ms")
    }

    measureTimeMillis {
        println(
            ActorPinger.ping(testData)
        )
    }.also { elapsedTime ->
        println("actor pings took $elapsedTime ms")
    }
}
