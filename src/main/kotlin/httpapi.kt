import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.*

const val ENDPOINT = "/my-test-endpoint"


//Snippets containing text
data class PostSnippet(val snippet: Text) {
    data class Text(val text: String)
}

data class Snippet(val user: String, val text: String)

val snippets = Collections.synchronizedList(mutableListOf(
    Snippet(user = "ddd", text = "ddd")
))
//


// JWT Authentication
open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

//user data storage
class User(val name: String, val password: String)


val users = Collections.synchronizedMap(
    listOf(User("test", "test"))
        .associateBy { it.name }
        .toMutableMap()
)
class LoginRegister(val user: String, val password: String)


fun main() {
    embeddedServer(Netty, 8080) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }
        install(CORS) {
            method(HttpMethod.Options)
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Delete)
            method(HttpMethod.Patch)
            header(HttpHeaders.Authorization)
            allowCredentials = true
            anyHost()
        }
        val simpleJwt = SimpleJWT("my-super-secret-for-jwt")
        install(Authentication) {
            jwt {
                verifier(simpleJwt.verifier)
                validate {
                    UserIdPrincipal(it.payload.getClaim("name").asString())
                }
            }
        }


        routing {
            route("/snippets") {
                get {
                    call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))

                }
                authenticate {
                    post {
                        val post = call.receive<PostSnippet>()
                        val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                        snippets += Snippet(principal.name, post.snippet.text)
                        call.respond(mapOf("OK" to true))
                    }
                }
            }
            route("/login-register"){
                post{
                    val post = call.receive<LoginRegister>()
                    val user = users.getOrPut(post.user) { User(post.user, post.password) }
                    if (user.password != post.password) error("Invalid credentials")
                    call.respond(mapOf("token" to simpleJwt.sign(user.name)))
                }
            }
        }

    }.start(wait = true)
}