package org.bravo.pinger

import kotlin.system.measureTimeMillis

suspend fun main() {
    val testData = generateIps()

//    warmUp(testData)

    println("size of ips ${testData.size}")

    repeat(100) {
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
}

private fun warmUp(ips: List<String>) {
    ips.forEach { ip ->
        Common.pingTo(ip)
    }
}

private fun generateIps(): List<String> {
    val list = mutableListOf<String>()

    (1..1).map { a ->
        (1..1).map { b ->
            (0..3).map { c ->
                (0..255).map { d ->
                    list.add("$a.$b.$c.$d")
                }
            }
        }
    }

    return list
}
