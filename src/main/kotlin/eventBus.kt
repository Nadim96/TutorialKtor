import io.ktor.network.util.ioCoroutineDispatcher
import javafx.application.Application.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty1
import kotlin.reflect.full.valueParameters

class EventBus {

    private val channel = BroadcastChannel<Any>(1)

    suspend fun send(event: Any, context: CoroutineContext = Dispatchers.IO) {
        channel.onSend(broadcast("hey"))
            channel.send(event)

        println("inside eventbus")
        println(event)
        println(channel.openSubscription())
        println(channel.onSend)
    }

    fun subscribe(): ReceiveChannel<Any> =
        channel.openSubscription()


    inline fun <reified T> subscribeToEvent() =
        subscribe().filter { it is T }.map { it as T }

    fun listenTo(signal: KProperty1<SomeEvent, String>) {
        channel.openSubscription()

        println(channel.openSubscription())
        println("kek")
    }

}


