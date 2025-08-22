package com.example.myapplication.landr.registerapp

import android.content.Intent
import android.widget.RadioGroup
import android.widget.RadioButton
import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.loginapp.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.landr.registerapp.models.RegisterRequest
import com.example.myapplication.landr.registerapp.models.RegisterResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegisterActivity : AppCompatActivity() {

    private lateinit var firstnameEt: EditText
    private lateinit var lastnameEt: EditText
    private lateinit var usernameEt: EditText
    private lateinit var genderRg: RadioGroup
    private lateinit var ageEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firstnameEt = findViewById(R.id.firstnameEt)
        lastnameEt = findViewById(R.id.lastnameEt)
        usernameEt = findViewById(R.id.usernameEt)
        genderRg = findViewById<RadioGroup>(R.id.genderRg)
        ageEt = findViewById(R.id.ageEt)
        passwordEt = findViewById(R.id.passwordEt)
        registerBtn = findViewById(R.id.registerBtn)

        registerBtn.setOnClickListener {
            val first_name = firstnameEt.text.toString()
            val last_name = lastnameEt.text.toString()
            val user_name = usernameEt.text.toString()
            val selectedGenderId = genderRg.checkedRadioButtonId
            var gender = ""
            if (selectedGenderId != -1) {
                val selectedGenderRb = findViewById<RadioButton>(selectedGenderId)
                gender = selectedGenderRb.text.toString()
                Log.d("Register", "Selected gender: $gender")
            } else {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            }
            val age = ageEt.text.toString().toInt()
            val password = passwordEt.text.toString()


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response: RegisterResponse = RetrofitClient.api.register(
                        RegisterRequest(
                            first_name,
                            last_name,
                            user_name,
                            gender,
                            age,
                            password
                        )
                    )
                    withContext(Dispatchers.Main) {
                        if (response.status == "success") {
                            Toast.makeText(this@RegisterActivity, "Register success", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)

                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Register error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Network error", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}