package com.example.chamber_api_2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class RegistrationActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.editTextEmail)
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber)
        passwordEditText = findViewById(R.id.editTextPassword)

        val registerButton: Button = findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email = emailEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val password = passwordEditText.text.toString()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Step 1: Send registration request
                val token = sendRegistrationRequest(email, phoneNumber, password)

                // Step 2: Save the token
                saveToken(token)

                // Step 3: Send token with GET request
                val verificationCode = sendTokenVerificationRequest(token)

                // Step 4: Go to the verification page
                navigateToVerificationPage(verificationCode)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegistrationActivity, "Registration failed. Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendRegistrationRequest(email: String, phoneNumber: String, password: String): String {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("phoneNumber", phoneNumber)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("http://95.70.151.149:6898/auth/signup")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseString = response.body?.string()
            return processRegistrationResponse(responseString)
        } else {
            throw Exception("Registration request failed. Code: ${response.code}")
        }
    }

    private fun processRegistrationResponse(responseString: String?): String {
        val jsonObject = JSONObject(responseString)
        return jsonObject.optString("token", "")
    }

    private fun sendTokenVerificationRequest(token: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://95.70.151.149:6898/auth/signup")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseString = response.body?.string()
            return processVerificationResponse(responseString)
        } else {
            throw Exception("Verification request failed. Code: ${response.code}")
        }
    }

    private fun processVerificationResponse(responseString: String?): String {
        val jsonObject = JSONObject(responseString)
        return jsonObject.optString("verificationCode", "")
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.apply()
    }

    private fun navigateToVerificationPage(verificationCode: String) {
        val intent = Intent(this, MainActivity2::class.java)
        intent.putExtra("verificationCode", verificationCode)
        startActivity(intent)
    }
}
