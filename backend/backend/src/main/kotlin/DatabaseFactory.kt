package com.example.database

import io.ktor.network.sockets.*
import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {
    private const val url = "jdbc:mysql://192.168.0.4:3306/myapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    private const val user = "appuser"
    private const val password = "MyP@ssw0rd123"

    fun getConnection(): Connection {
        Class.forName("com.mysql.cj.jdbc.Driver")
        return DriverManager.getConnection(url, user, password)
    }

    fun validateUser(username: String, password: String): Boolean {
        val sql = "SELECT * FROM users WHERE user_name=? AND password=?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, username)
                stmt.setString(2, password)
                stmt.executeQuery().use { rs ->
                    return rs.next()  // if user exist, return true
                }
            }
        }
    }

    fun insertUser(first_name: String, last_name: String, user_name: String, gender: String, age: Int, password: String): Boolean {
        val sql = "INSERT INTO users (first_name, last_name, user_name, gender, age, password) VALUES (?,?,?,?,?,?)"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, first_name)
                stmt.setString(2, last_name)
                stmt.setString(3, user_name)
                stmt.setString(4, gender)
                stmt.setInt(5, age)
                stmt.setString(6, password)
                val rows = stmt.executeUpdate()
                return rows > 0
            }
        }
    }
}