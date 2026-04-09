package org.alonsitos.chat

expect fun getKVault(): KVaultWrapper

interface KVaultWrapper {
    fun set(key: String, value: String)
    fun get(key: String): String?
    fun clear()
}
