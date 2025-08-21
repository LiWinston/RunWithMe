package com.example

import landr.loginapp.LoginRequest
import landr.loginapp.LoginResponse
import landr.registerapp.RegisterRequest
import landr.registerapp.RegisterResponse
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import com.example.database.DatabaseFactory
import io.ktor.server.plugins.calllogging.*
fun main() {
    io.ktor.server.netty.EngineMain.main(emptyArray())
}

fun Application.module() {

    install(CallLogging)

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        get("/") {
            call.respond(mapOf("message" to "Hello Ktor!"))
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                println("Received login request: $request")

                val valid = DatabaseFactory.validateUser(request.username, request.password)
                if (valid) call.respond(LoginResponse("success"))
                else call.respond(LoginResponse("failed"))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(LoginResponse("error"))
            }
        }

        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                println("Received register request: $request")

                val insert = DatabaseFactory.insertUser(request.first_name, request.last_name, request.user_name, request.gender, request.age, request.password)
                if (insert) call.respond(RegisterResponse("success"))
                else call.respond(RegisterResponse("failed"))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(RegisterResponse("error"))
            }
        }
    }
}