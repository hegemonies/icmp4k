package org.bravo.pinger

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.bravo.pinger.Common.pingTo

sealed class ActorPingerMessage
class PingTo(val ip: String) : ActorPingerMessage()
class SetLimit(val limit: Int) : ActorPingerMessage()
class GetResult(val result: CompletableDeferred<Map<String, Boolean>>) : ActorPingerMessage()

private fun CoroutineScope.pingerActor() = actor<ActorPingerMessage> {
    var counter = 0
    var limit = 0
    val finishResult = Channel<Map<String, Boolean>>()
    val results = Channel<Pair<String, Boolean>>()

    suspend fun fromChannelToMap(channel: Channel<Pair<String, Boolean>>): Map<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()

        repeat(counter) {
            channel.receiveAsFlow().collect { (ip, result) ->
                map[ip] = result
            }
        }

        return map
    }

    consumeEach { message ->
        when (message) {
            is PingTo ->
                if (counter < limit) {
                    counter++

                    GlobalScope.launch {
                        results.send(message.ip to pingTo(message.ip))

                        if (limit != 0 && counter == limit) {
                            finishResult.send(fromChannelToMap(results))
                        }
                    }
                }

            is SetLimit ->
                limit = message.limit

            is GetResult ->
                message.result.complete(finishResult.receive())
        }
    }
}

object ActorPinger : IPinger {

    override suspend fun ping(ips: List<String>): Map<String, Boolean> {
        val actor = GlobalScope.pingerActor()

        actor.send(SetLimit(ips.size))

        ips.asFlow().collect { ip ->
            actor.send(PingTo(ip))
        }

        val result = CompletableDeferred<Map<String, Boolean>>()
        actor.send(GetResult(result))

        return result.await()
    }
}
