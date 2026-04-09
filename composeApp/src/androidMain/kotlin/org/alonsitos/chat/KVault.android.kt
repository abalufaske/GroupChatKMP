package org.alonsitos.chat

import com.liftric.kvault.KVault
import android.content.Context

actual fun getKVault(): KVaultWrapper = AndroidKVault(getAndroidContext())

private var androidContext: Context? = null
fun setAndroidContext(context: Context) {
    androidContext = context
}
fun getAndroidContext(): Context = androidContext ?: throw Exception("Context not set")

class AndroidKVault(context: Context) : KVaultWrapper {
    private val kvault = KVault(context, "alonsitos_prefs")

    override fun set(key: String, value: String) {
        kvault.set(key, value)
    }

    override fun get(key: String): String? {
        return kvault.string(key)
    }

    override fun clear() {
        kvault.clear()
    }
}
