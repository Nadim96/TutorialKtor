import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.features.ContentNegotiation
import io.ktor.http.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.jackson.jackson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.io.ByteReadChannel


val client = HttpClient(CIO) {
    engine {
        maxConnectionsCount = 1000 // Maximum number of socket connections.
        endpoint.apply {
            maxConnectionsPerRoute = 100 // Maximum number of requests for a specific endpoint route.
            pipelineMaxSize = 20 // Max number of opened endpoints.
            keepAliveTime = 5000 // Max number of milliseconds to keep each connection alive.
            connectTimeout = 5000 // Number of milliseconds to wait trying to connect to the server.
            connectRetryAttempts = 5 // Maximum number of attempts for retrying a connection.
        }
    }
    install(WebSockets)
}


suspend fun main() {
    client.ws( host = "localhost", port = 8080, path = "/chat") { // this: DefaultClientWebSocketSession

        for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
            println(message.readText())
        }

    }
}
