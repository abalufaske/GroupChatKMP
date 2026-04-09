package org.alonsitos.chat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform