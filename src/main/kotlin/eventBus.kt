import io.ktor.http.cio.websocket.*
import io.ktor.network.util.ioCoroutineDispatcher
import io.ktor.util.*
import javafx.application.Application.launch
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty1
import kotlin.reflect.full.valueParameters


//events


data class SomeEvent(val text: String)
data class somethingelse(val text: String)
data class anotherelse(val text: String)



class EventBus {

    public val channel = ConflatedBroadcastChannel<Any>()

    suspend fun send(event: Any){
               channel.send(event)
        channel.offer(event)
                println("iets")
        println(channel.value)

    }

    fun subscribe(): ReceiveChannel<Any> =
        channel.openSubscription()


    inline fun <reified T> subscribeToEvent() =
        subscribe().filter { it is T }.map { it as T }




}


