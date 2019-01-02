import com.fasterxml.jackson.databind.SerializationFeature
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.websocket.*
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.sendBlocking
import java.io.File
import java.io.FileReader
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

val outsideText: MutableList<String> = mutableListOf("1")
val connections: MutableList<String> = mutableListOf("2")

class ChatClient(val session: DefaultWebSocketSession) {
    companion object { var lastId = AtomicInteger(0) }
    val id = lastId.getAndIncrement()
    val name = "user$id"
}



fun main(){
    embeddedServer(Netty, 8080) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }
        install(io.ktor.websocket.WebSockets) {
            pingPeriod = Duration.ofMinutes(1)
        }
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }




        routing {

            val clients = Collections.synchronizedSet(LinkedHashSet<ChatClient>())

            get("/websocket") {

                    val user = User("user name", "user@example.com")
                    call.respond(FreeMarkerContent("index.ftl", mapOf("user" to user), "e"))


//                connections.add(clients.toString())
//                call.respondText(outsideText.toString() + connections.toString() + connections.size)
//

//                var ws = new WebSocket("ws://localhost:8080/websocket");
//
//                ws.onopen = function() { console.log('connection established') }
//                ws.onmessage = (msg) => {console.log("server message: ", msg.data)}
            }


            webSocket("/websocket") { // this: DefaultWebSocketSession
                val client = ChatClient(this)
                clients += client


                try {
                    //new client connection


                    send(Frame.Text("hi ${client}"))



                    while (true) {
                        val frame = incoming.receive()

                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                outsideText.add(text)
                                // Iterate over all the connections and send data such as user name
                                val textToSend = "${client.name} said: $text from $client with id ${client.id} and session ${client.session}"


                                val sess= client.session
//
//
//                                println(sess.send(Frame.Text(textToSend)).toString())
//                                println(sess)
//                                println(sess.toString())

                                sess.send(Frame.Text("Message received!"))


                                for (other in clients.toList()) {
                                    other.session.outgoing.send(Frame.Text(textToSend))
                                }
                            }
                        }
                    }
                } finally {
                    clients -= client
                }
            }
        }
    }.start(wait = true)
}
