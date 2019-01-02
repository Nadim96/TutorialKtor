import com.sun.media.jfxmediaimpl.MediaDisposer
import kotlinx.coroutines.*
import java.awt.Event
import java.util.concurrent.atomic.AtomicInteger


data class SomeEvent(val text: String)
data class somethingelse(val text: String)

suspend fun main(){

    val bus = EventBus();


    bus.subscribeToEvent<SomeEvent>()


    bus.send(SomeEvent("hey"))
    bus.send(somethingelse("hi"))
    bus.send(SomeEvent("hiya"))








}