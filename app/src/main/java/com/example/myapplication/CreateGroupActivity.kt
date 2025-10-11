package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        // TODO: 调用数据库API创建Group
        // val groupId = createGroupInDatabase(groupName)
        
        // Mock: 模拟创建成功
        val mockGroupId = "group_${System.currentTimeMillis()}"
        
        Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show()
        
        // 返回结果给GroupFragment
        val resultIntent = Intent()
        resultIntent.putExtra("group_id", mockGroupId)
        resultIntent.putExtra("group_name", groupName)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

