package com.salman.nfcreader

import android.app.Application
import com.salman.nfcreader.util.DarkModeManager

class app : Application() {
    override fun onCreate() {
        super.onCreate()

        DarkModeManager(this).let {
            it.enableDarkMode(it.isDarkModeEnabled())
        }
    }
}