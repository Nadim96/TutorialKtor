import com.sun.media.jfxmediaimpl.MediaDisposer
import java.awt.Event
import java.util.concurrent.atomic.AtomicInteger


data class SomeEvent(val text: String)


suspend fun main(){

    val bus = EventBus();



    bus.subscribeToEvent<SomeEvent>()

    bus.send(SomeEvent("msg"))




    bus.listenTo(SomeEvent::text)



    bus.listenTo(SomeEvent::text)

}