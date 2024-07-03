package com.salman.nfcreader


import android.accounts.NetworkErrorException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var emailID: TextInputEditText? = null
    private var emailLayout: TextInputLayout? = null
    private var pwdlayout: TextInputLayout? = null
    private var passwordinp: TextInputEditText? = null
    private var email: String? = null
    private var password: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // In Activity's onCreate() for instance
        // In Activity's onCreate() for instance
        val w = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_login)
        // Make the activity layout extend into the status bar


        auth = FirebaseAuth.getInstance()
        emailID = findViewById(R.id.emailIdeditText)
        emailLayout = findViewById(R.id.emailid)
        pwdlayout = findViewById(R.id.Password)
        passwordinp = findViewById(R.id.PasswordInp)
        findViewById<View>(R.id.loginButton).setOnClickListener {
            email = emailID?.text.toString()
            password = passwordinp?.text.toString()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(
                    this@Login,
                    "Please enter both email and password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(
                            this@Login,
                            "login successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                        // Update UI or perform other actions upon successful login
                    } else {

                        Toast.makeText(
                            this@Login,
                            if(isNetworkRelatedException(task.exception)) "Failed to connect to internet" else task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
        findViewById<View>(R.id.newUserButton).setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun isNetworkRelatedException(exception: Exception?): Boolean {
        return exception is java.net.SocketException ||
                exception is java.net.UnknownHostException ||
                exception is FirebaseNetworkException
    }

}