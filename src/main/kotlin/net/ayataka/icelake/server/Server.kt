package net.ayataka.icelake.server

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.util.InternalAPI
import net.ayataka.icelake.server.utilities.Database
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("IceLake")
private val GSON = GsonBuilder().setPrettyPrinting().create()
private val HTTPCLIENT = HttpClient()

@InternalAPI
fun Application.module() {
    install(ContentNegotiation) { gson { setPrettyPrinting() } }

    routing {
        get("/") {
            call.respond("IceLake API 0.1")
        }

        post("/api/actions") {
            val json = call.receiveJson()
            val code = json["code"].asString
            val type = json["type"].asString
            val name = json["name"].asString

            // Validate the action
            if (HTTPCLIENT.call("https://pigg.ameba.jp/stat/swf/motion/${code.takeWhile { it != '#' }}.mot").response.status != HttpStatusCode.OK) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            // Name overwrite prevent
            val action = Database.getAction(code)
            if (action != null && action.name.isNotEmpty() && name.isEmpty()) {
                call.respond("OK")
                return@post
            }

            // Register the action
            Database.addAction(code, type, name)

            LOGGER.info("Added action: $code ($name)")

            call.respond("OK")
        }

        get("/api/actions") {
            call.respond(mapOf("actions" to Database.getActions()))
        }

        post("/api/shops") {
            val json = call.receiveJson()
            val code = json["code"].asString
            val name = json["name"].asString

            // Register the action
            Database.addShop(code, name)

            LOGGER.info("Added shop: $code ($name)")

            call.respond("OK")
        }

        get("/api/shops") {
            call.respond(mapOf("shops" to Database.getShops()))
        }

        post("/api/areas") {
            val json = call.receiveJson()
            val code = json["code"].asString
            val name = json["name"].asString

            // Register the action
            Database.addArea(code, name)

            LOGGER.info("Added area: $code ($name)")

            call.respond("OK")
        }

        get("/api/areas") {
            call.respond(mapOf("areas" to Database.getAreas()))
        }
    }
}

@InternalAPI
suspend fun ApplicationCall.receiveJson() = GSON.fromJson(receiveText(), JsonObject::class.java)!!

@InternalAPI
fun main(args: Array<String>) {
    LOGGER.info("IceLake API Server v0.1")
    embeddedServer(Jetty, port = 8080, host = "127.0.0.1", module = Application::module).start()
}