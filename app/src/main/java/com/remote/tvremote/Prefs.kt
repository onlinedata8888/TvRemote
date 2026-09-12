package com.remote.tvremote

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE)

    var lastIp: String
        get() = sp.getString("last_ip", "") ?: ""
        set(value) = sp.edit().putString("last_ip", value).apply()
}
