import com.fasterxml.jackson.databind.SerializationFeature
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.*
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
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import jdk.nashorn.internal.parser.*
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.sendBlocking
import org.json.simple.*
import java.io.File
import java.io.FileReader
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import org.json.simple.JSONObject



val outsideText: MutableList<String> = mutableListOf("1")
val connections: MutableList<String> = mutableListOf("2")

class NotificationClient(val session: DefaultWebSocketSession) {
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



        val clients = Collections.synchronizedSet(LinkedHashSet<NotificationClient>())


        routing {


            get("/websocket") {


                call.respond(FreeMarkerContent("index.ftl", mapOf("client" to clients), "e"))


                connections.add(clients.toString())

            }


            post("/websocket/test"){

                val topicName = call.parameters.getAll("topicName").toString();



                for(client in clients){

                    println(client)

                }

            }


            webSocket("/websocket") { // this: DefaultWebSocketSession
                val client = NotificationClient(this)

                println(client)


                clients += client



                val bus = EventBus();


                try {
                    //new client connection


                    send(Frame.Text("hi ${client}"))


                    while (true) {
                        val frame = incoming.receive()
                            when (frame) {
                                is Frame.Text -> {

                                    when(frame.readText() == "SUBSCRIBE"){

                                        true -> {

                                            bus.subscribeToEvent<SomeEvent>()
                                            bus.subscribeToEvent<somethingelse>()
                                            bus.subscribeToEvent<anotherelse>()


                                            bus.send(somethingelse("hey"))
                                        }
                                        false -> println("not true")
                                    }

                                    when(frame.readText() == "PUBLISH"){

                                        true -> println("true")
                                        false -> println("not true")
                                    }






                                    val message: String




                                    val text = frame.readText()
                                    outsideText.add(text)
                                    // Iterate over all the connections and send data such as user name
                                    val textToSend =
                                        "${client.name} said: $text from $client with id ${client.id} and session ${client.session}"


                                    val sess = client.session
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


//            webSocket("/websocket/test") {
//                println(clients.toString())
//                println("yo")
//
//
//                val bus = EventBus();
//
//
//
//                    println("hiiiiiii")
//
//                println("hiiii")
//
//
//
//            }



        }
    }.start(wait = true)
}



