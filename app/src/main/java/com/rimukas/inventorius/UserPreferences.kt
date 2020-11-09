package com.rimukas.inventorius

import android.app.Activity
import android.content.Context

class UserPreferences {
 fun getPrefs(actv: Activity): HashMap<String, String>{
    var prefsMap: HashMap<String, String> = HashMap()
    val sharedPref = actv.getSharedPreferences("prefs", 0)

    prefsMap["ip"] = sharedPref.getString("ip", "")!!
    prefsMap["user"] = sharedPref.getString("user", "")!!
    prefsMap["password"] = sharedPref.getString("password", "")!!

    return prefsMap
}

}