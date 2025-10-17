package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.group.CreateGroupBody
import com.example.myapplication.group.GroupApi
import com.example.myapplication.group.Result
import com.example.myapplication.landr.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 创建Group的Activity
 * 功能：
 * 1. 输入Group名称
 * 2. 创建Group（最多6人，最少2人）
 * 3. 创建成功后返回GroupFragment
 */
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var etGroupName: EditText
    private lateinit var btnCreateGroup: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etGroupName = findViewById(R.id.et_group_name)
        btnCreateGroup = findViewById(R.id.btn_create_group)
        btnCancel = findViewById(R.id.btn_cancel)
    }

    private fun setupListeners() {
        btnCreateGroup.setOnClickListener {
            createGroup()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createGroup() {
        val groupName = etGroupName.text.toString().trim()

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.create(GroupApi::class.java)
        val body = CreateGroupBody(name = groupName, memberLimit = 6)
        api.create(body).enqueue(object: Callback<Result<com.example.myapplication.group.GroupInfo>> {
            override fun onResponse(
                call: Call<Result<com.example.myapplication.group.GroupInfo>>,
                response: Response<Result<com.example.myapplication.group.GroupInfo>>
            ) {
                val res = response.body()
                if (response.isSuccessful && res != null && res.code == 0 && res.data != null) {
                    Toast.makeText(this@CreateGroupActivity, "Group created successfully!", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("group_id", res.data.id.toString())
                    resultIntent.putExtra("group_name", res.data.name)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@CreateGroupActivity, res?.message ?: "Create failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: Call<Result<com.example.myapplication.group.GroupInfo>>,
                t: Throwable
            ) {
                Toast.makeText(this@CreateGroupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

