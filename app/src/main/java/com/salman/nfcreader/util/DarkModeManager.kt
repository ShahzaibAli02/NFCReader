package com.salman.nfcreader.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.salman.nfcreader.persistance.SharedPref


class DarkModeManager constructor(val context:Context):ContextWrapper(context)
{

    private val sharedPref by lazy { SharedPref(context) }


    fun isDarkModeEnabled():Boolean {
        return sharedPref.getBoolean(SharedKeys.SHARED_PREF_DARK_MODE,false)
    }

     fun isSystemNightModeOn():Boolean
    {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun enableDarkMode(darkMode: Boolean=true) {
        if(darkMode)
            enableDarkMode()
        else
            disableDarkMode()
    }
    private fun enableDarkMode() {

        sharedPref.saveBoolean(SharedKeys.SHARED_PREF_DARK_MODE,true)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

   private fun disableDarkMode(){
        sharedPref.saveBoolean(SharedKeys.SHARED_PREF_DARK_MODE,false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }


}