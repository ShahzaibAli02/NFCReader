package com.salman.nfcreader
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.text.SimpleDateFormat
import java.util.Date


class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()


        findViewById<View>(R.id.registerButton).setOnClickListener {

            val firstName = findViewById<TextInputEditText>(R.id.firstNameEditText).text.toString()
            val lastName =findViewById<TextInputEditText>(R.id.lastNameEditText).text.toString()
            val email = findViewById<TextInputEditText>(R.id.emailIdeditText).text.toString()
            val password = findViewById<TextInputEditText>(R.id.ConfirmPasswordInp).text.toString()
            if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RegisterUser(email, password, firstName, lastName)
        }
    }


    private fun RegisterUser(email: String, password: String, firstName: String, lastName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    Toast.makeText(
                        this@Register,
                        "Register Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                    auth.signOut()
                } else
                {
                    // User creation failed
                    val errorMessage = task.exception?.message
                    Toast.makeText(
                        this@Register,
                        "Failed to Register user: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }



}