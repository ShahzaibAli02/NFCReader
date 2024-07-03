package com.salman.nfcreader


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.salman.nfcreader.activities.settings.SettingsActivity
import com.salman.nfcreader.persistance.SharedPref
import com.salman.nfcreader.util.SharedKeys
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    companion object {
        var tagScanned: String = ""
    }


    private val sharedPref by lazy { SharedPref(this) }
    private var intentFiltersArray: Array<IntentFilter>? = null
    private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null


    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MyRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    var BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets/"
    private val REQUEST_SMS_PERMISSION = 1
    private var dialog: AlertDialog? = null
    val masterSheetID = "1vfo921YwXP7FzhLL-U8WZBG1xM6XT2sFptp2W6aMbuM"
    val sheetName = "Sheet1"
    val key = "AIzaSyCTKsgpHxrKxkBApc8NX0NaqwzlK_A_hPI"
    var sheetResponse: SheetResponse? = null
    var tagIdInput: EditText? = null
    private lateinit var mediaPlayer: MediaPlayer
    private val imageMenu by lazy { findViewById<ImageView>(R.id.imageMenu) }
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val lytSettings by lazy { findViewById<LinearLayout>(R.id.lytSettings) }
    private val navLayout by lazy { findViewById<NavigationView>(R.id.navLayout) }

    private val btn1 by lazy { findViewById<Button>(R.id.btn1) }
    private val btn2 by lazy { findViewById<Button>(R.id.btn2) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.activity_main)


        navLayout.bringToFront()
        imageMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        lytSettings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
          Intent(this,SettingsActivity::class.java).apply {
              startActivity(this)
          }
        }
        recyclerView = findViewById(R.id.datalist)
        adapter = MyRecyclerViewAdapter(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        auth = FirebaseAuth.getInstance()

        requestSmsPermission()
        val retrieveButton = findViewById<Button>(R.id.RData)

        val sendButton = findViewById<Button>(R.id.sendSmsButton)
        tagIdInput = findViewById<EditText>(R.id.tagIdInput)
        tagIdInput!!.setText(tagScanned)
        if (isNetworkAvailable()) {

            val currentUser = auth.currentUser
            val uid = currentUser?.uid
            if (uid != null) {

                showLoaderPopup()
                fetchDataWithRetrofit(masterSheetID, true)
                //  checkDataLoaded()
            }
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            retrieveButton.text = "Load data from Local Storage"
        }

        retrieveButton.setOnClickListener { // Load data from local storage when offline
            sheetResponse = loadDataFromLocalStorage()
            sheetResponse?.let { adapter.setData(it.values!!) } //                shimmerLayout.startShimmer()


        }

        sendButton.setOnClickListener {
            sendMsg()
        }


        findViewById<View>(R.id.lytLogout).setOnClickListener {
            auth = FirebaseAuth.getInstance() // Log out the user and navigate to the login screen
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        initNFC()

    }


    private fun setupButton(button:Button,key: String) {

        sharedPref.getString(key,"")?.let {
            if(it.isNotBlank())
            {
                val btn=Gson().fromJson(it,com.salman.nfcreader.model.Button::class.java)
                button.isVisible=btn.visible
                button.text = btn.label
                button.setOnClickListener {
                    openApp(btn.link)
                }
            }
        }

    }

    private fun openApp(packageName: String) {
        val packageManager = packageManager

        // Intent to launch the target app
        val intent = packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            // Start the activity
            startActivity(intent)
        } else {
            Toast.makeText(this, "App not installed ${packageName}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendMsg() {
        val tagId = tagIdInput!!.text.toString()
        if (tagId.isEmpty())
            return

        val item = sheetResponse?.values?.filter { it.getOrNull(0)?.trim().equals(tagId, true) }
        item?.let {
            if (it.isEmpty()) {
                playBeep()
                Toast.makeText(this, "Tag not found in the data ", Toast.LENGTH_SHORT).show()
            } else {
                tagScanned = ""
                sendSMS(it)
            }

        } ?: kotlin.run {
            playBeep()
            Toast.makeText(this, "Tag not found in the data ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playBeep() {
        Handler().postDelayed({

            kotlin.runCatching {
                if (mediaPlayer.isPlaying)
                    mediaPlayer.stop()
                setupMediaPlayer()
                val volume=sharedPref.getInt(SharedKeys.SHARED_PREF_VOLUME,50)/100f
                mediaPlayer.setVolume(volume,volume)
                mediaPlayer.prepare()
                mediaPlayer.start()

            }


        }, 1000)

    }

    private fun initNFC() {
        try {
            pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            try {
                ndef.addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
            intentFiltersArray = arrayOf(ndef)
        } catch (ex: Exception) {
            Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setupButton(btn1,SharedKeys.SHARED_PREF_BUTTON_1)
        setupButton(btn2,SharedKeys.SHARED_PREF_BUTTON_2)
        setupMediaPlayer()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
        if (auth.currentUser == null) {
            finish()
            startActivity(Intent(this, Login::class.java))
            showToast("Please login to continue")
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer()
        val assetFileDescriptor = assets.openFd("beeps/${sharedPref.getString(SharedKeys.SHARED_PREF_SOUND,"beep-01.mp3")}")
        mediaPlayer.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )
    }


    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {
                try {
                    val inNdefMessage = this[0] as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    var ndefRecord_0 = inNdefRecords[0]
                    var inMessage = String(ndefRecord_0.payload)
                    tagScanned = inMessage.drop(3)
                    tagIdInput!!.setText(tagScanned)
                    sendMsg()
                } catch (ex: Exception) {
                    showToast("ERROR READING TAG " + ex.message)
                }
            }


        }

    }

    override fun onPause() {
        if (this.isFinishing) {
            nfcAdapter?.disableForegroundDispatch(this)
        }
        super.onPause()
    }


    private fun messagePop() { // Show the Toast message with a 3-second delay
        Toast.makeText(this, "Sending SMS messages...", Toast.LENGTH_SHORT).show()
        showMessagePopup() // Dismiss the dialog after 3 seconds
        val dialogDismissHandler = Handler()
        dialogDismissHandler.postDelayed({
            dialog?.dismiss()
        }, 5000)
    }

    // Function to show the loader popup
    private fun showLoaderPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_loader_popup, null)
        dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        // Show the dialog
        dialog?.show()
    }

    // message sent popup
    private fun showMessagePopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_message_sent, null)
        dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        // Show the dialog
        dialog?.show()
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


    private fun saveDataToLocalStorage(data: SheetResponse) {
        val gson = Gson() // You need to add Gson library to your project
        val dataJson = gson.toJson(data)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(auth.currentUser!!.email, dataJson)
        editor.apply()
    }

    private fun loadDataFromLocalStorage(): SheetResponse? {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val dataJson = sharedPreferences.getString(auth.currentUser!!.email, null)

        if (dataJson != null) {
            val gson = Gson()
            val dataArray = gson.fromJson(dataJson, SheetResponse::class.java)
            return dataArray
        } else {
            return null
        }
    }


    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SMS_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission granted, send SMS
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

            } else { // Permission denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun sendSMS(messages: List<List<String>>) {
        val smsManager = SmsManager.getDefault()
        for (message in messages) {
            val phone = message.getOrElse(1, { "" })
            var note = message.getOrElse(2, { "" })
//
//           //TODO REMOVE
//            if(true)
//            {
//                note="Hey, I;ve just scanned the tag, today is %DAY% the %DATE%, time is %TIME%"
//            }
            if(note.isNotBlank())
            {
                note=formatMsg(note)
            }
            Log.d("MainActivity", "sendSMS: ph:" + phone + " sms=" + note)
            smsManager.sendTextMessage(phone, null, note, null, null)
        }
        messagePop()
    }


    fun getDayOfMonthSuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    fun formatMsg(template: String): String {
        val currentDate = SimpleDateFormat("dd", Locale.getDefault()).format(Date()).toInt()
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

        val dayOfMonthSuffix = getDayOfMonthSuffix(currentDate)

        return template
            .replace("%DAY%", currentDay)
            .replace("%DATE%","${currentDate}$dayOfMonthSuffix")
            .replace("%TIME%", currentTime)
    }


    private fun fetchDataWithRetrofit(sheetID: String, isMaster: Boolean) {

        val retrofit = Retrofit.Builder().baseUrl(BASE_URL) // Base URL of your API
            .addConverterFactory(GsonConverterFactory.create()) // Gson converter for JSON parsing
            .build()

        // Create an instance of the ApiService interface
        val apiService = retrofit.create(ApiInterface::class.java)

        // Make the network request using Retrofit and pass the URL as a query parameter
        val call = apiService.Getdata(sheetId = sheetID, sheet_name = sheetName, key = key)

        call.enqueue(object : Callback<SheetResponse> {
            override fun onResponse(
                call: Call<SheetResponse>,
                response: Response<SheetResponse>
            ) {

               if(lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED) && dialog?.isShowing==true)
                  dialog?.dismiss()


                if (response.isSuccessful) {

                    if (isMaster) {
                        val temp = response.body()
                        temp?.values?.let {

                            val list = it.firstOrNull { it ->
                                it.first().trim().equals(getEmail(), true)
                            }
                            list?.let {
                                fetchDataWithRetrofit(getSheetId(it[1]), false)
                            }
                        }
                    } else {
                        sheetResponse = response.body()
                        if (sheetResponse != null) {
                            adapter.setData(sheetResponse!!.values!!) // Save the data to local storage
                            saveDataToLocalStorage(sheetResponse!!)
                            if (tagScanned.isNotEmpty()) {
                                sendMsg()
                            }
                        }
                    }

                } else {
                    // Handle non-successful response (e.g., display an error message)
                    Log.e("Response Error", "HTTP Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SheetResponse>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(this@MainActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }
    //pstags@outlook.com
    fun getEmail():String
    {
//        //TODO REMOVE
//        if (true)
//            return "pstags@outlook.com"
        return auth.currentUser!!.email!!
    }

    private fun getSheetId(sheetUrl: String): String {

        val pattern = Regex("/d/([a-zA-Z0-9-_]+)")

        val matchResult = pattern.find(sheetUrl)
        val sheetId = matchResult?.groups?.get(1)?.value
        Log.d("TAG", "getSheetId: " + sheetId)
        return sheetId ?: "";
    }
}
