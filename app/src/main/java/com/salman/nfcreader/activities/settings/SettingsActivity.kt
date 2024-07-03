package com.salman.nfcreader.activities.settings

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.salman.nfcreader.R
import com.salman.nfcreader.model.Beep
import com.salman.nfcreader.model.Button
import com.salman.nfcreader.model.getBeepsList
import com.salman.nfcreader.persistance.SharedPref
import com.salman.nfcreader.util.DarkModeManager
import com.salman.nfcreader.util.SharedKeys
import com.salman.nfcreader.util.afterTextChange

class SettingsActivity : AppCompatActivity() {



    private val darkModeManager by lazy { DarkModeManager(this) }
    private val sharedPref by lazy { SharedPref(this) }
    private val lytDarkMode by lazy { findViewById<LinearLayout>(R.id.lytDarkMode) }
    private val switchDarkMode by lazy { findViewById<SwitchMaterial>(R.id.switchDarkMode) }
    private val imageBack by lazy { findViewById<View>(R.id.imageBack) }
    private val spinnerErrorBeep by lazy { findViewById<Spinner>(R.id.spinnerErrorBeep) }
    private val seekBarVolume by lazy { findViewById<SeekBar>(R.id.seekBarVolume) }

    private val switchBtn1Visibility by lazy { findViewById<SwitchMaterial>(R.id.switchBtn1Visibility) }
    private val etBtn1Label by lazy { findViewById<EditText>(R.id.etBtn1Label) }
    private val etBtn1Link by lazy { findViewById<EditText>(R.id.etBtn1Link) }


    private val switchBtn2Visibility by lazy { findViewById<SwitchMaterial>(R.id.switchBtn2Visibility) }
    private val etBtn2Label by lazy { findViewById<EditText>(R.id.etBtn2Label) }
    private val etBtn2Link by lazy { findViewById<EditText>(R.id.etBtn2Link) }


    private val beepAdapter by lazy {
        Log.d("TAG", "beepAdapter created")
        ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item, getBeepsList())

    }

    private var btn1Details=Button(false,"Button 1","")
    private var btn2Details=Button(false,"Button 2","")
    private var lastPlayed=-1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        imageBack.setOnClickListener {  finish() }
        switchDarkMode.isChecked=darkModeManager.isDarkModeEnabled()
        spinnerErrorBeep.adapter=beepAdapter

        spinnerErrorBeep.setSelection(getBeepsList().indexOfFirst { it.fileName==sharedPref.getString(SharedKeys.SHARED_PREF_SOUND,"beep-01.mp3") })

        sharedPref.getString(SharedKeys.SHARED_PREF_BUTTON_1,"")?.let {
            if(it.isNotBlank())
            {
                btn1Details=Gson().fromJson(it,Button::class.java)
            }
        }
        sharedPref.getString(SharedKeys.SHARED_PREF_BUTTON_2,"")?.let {
            if(it.isNotBlank())
            {
                btn2Details=Gson().fromJson(it,Button::class.java)
            }
        }


        spinnerErrorBeep.onItemSelectedListener=object: AdapterView.OnItemSelectedListener
        {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(lastPlayed!=-1 && lastPlayed!=p2)
                {
                    getBeepsList().getOrElse(p2) { Beep("beep-01.mp3", "Beep 1") }.fileName.let {
                        playMedia(it)
                        sharedPref.saveString(SharedKeys.SHARED_PREF_SOUND,it)
                    }

                }
                lastPlayed=p2
               }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d("TAG", "onNothingSelected: ")
            }

        }


        seekBarVolume.progress = sharedPref.getInt(SharedKeys.SHARED_PREF_VOLUME,50)
        lytDarkMode.setOnClickListener { switchDarkMode.isChecked=!switchDarkMode.isChecked }

        seekBarVolume.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                   sharedPref.saveInt(SharedKeys.SHARED_PREF_VOLUME,p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
        switchDarkMode.setOnCheckedChangeListener { _, checked ->
            darkModeManager.enableDarkMode(checked)
        }





        etBtn1Label.setText(btn1Details.label)
        etBtn1Link.setText(btn1Details.link)
        switchBtn1Visibility.isChecked=btn1Details.visible


        etBtn2Label.setText(btn2Details.label)
        etBtn2Link.setText(btn2Details.link)
        switchBtn2Visibility.isChecked=btn2Details.visible


        etBtn1Label.afterTextChange {
            btn1Details.label=it
            saveBtnDetails(SharedKeys.SHARED_PREF_BUTTON_1,btn1Details)
        }
        etBtn1Link.afterTextChange {
            btn1Details.link=it
            saveBtnDetails(SharedKeys.SHARED_PREF_BUTTON_1,btn1Details)
        }
        switchBtn1Visibility.setOnCheckedChangeListener { compoundButton, b ->
            btn1Details.visible=b
            saveBtnDetails(SharedKeys.SHARED_PREF_BUTTON_1,btn1Details)
        }


        etBtn2Label.afterTextChange {
            btn2Details.label=it
            saveBtnDetails(SharedKeys.SHARED_PREF_BUTTON_2,btn2Details)
        }
        etBtn2Link.afterTextChange {
            btn2Details.link=it
            saveBtnDetails(SharedKeys.SHARED_PREF_BUTTON_2,btn2Details)
        }
        switchBtn2Visibility.setOnCheckedChangeListener { compoundButton, b ->
            btn2Details.visible=b
            saveBtnDetails(SharedKeys.SHARED_PREF_BUTTON_2,btn2Details)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TAG", "onDestroy: ")
    }

    private fun saveBtnDetails(key: String, btn: Button) {
        sharedPref.saveString(key,Gson().toJson(btn))
    }

    var mediaPlayer:MediaPlayer?=null
    private fun playMedia(fileName: String) {

        kotlin.runCatching {
            if(mediaPlayer?.isPlaying==true)
                mediaPlayer?.stop()

            mediaPlayer = MediaPlayer()

            val assetFileDescriptor = assets.openFd("beeps/${fileName}")
            mediaPlayer?.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
            val volume=sharedPref.getInt(SharedKeys.SHARED_PREF_VOLUME,0)/100f

            mediaPlayer?.setVolume(volume,volume)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }.onFailure {
            Log.e("TAG", "playMedia: ERR"+it.message )
        }

    }
}