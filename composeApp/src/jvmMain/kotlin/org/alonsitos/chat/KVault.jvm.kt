package org.alonsitos.chat

import java.util.prefs.Preferences

actual fun getKVault(): KVaultWrapper = JVMKVault()

class JVMKVault : KVaultWrapper {
    private val prefs = Preferences.userRoot().node("alonsitos_prefs")

    override fun set(key: String, value: String) {
        prefs.put(key, value)
    }

    override fun get(key: String): String? {
        return prefs.get(key, null)
    }

    override fun clear() {
        prefs.clear()
    }
}
