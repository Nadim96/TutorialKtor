import com.sun.media.jfxmediaimpl.MediaDisposer
import kotlinx.coroutines.*
import java.awt.Event


suspend fun main(){

    val bus = EventBus();

    bus.send(SomeEvent("hey"))



}