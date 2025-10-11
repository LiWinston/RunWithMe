package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * QR Code Activity
 * 功能：
 * 1. MODE_SHOW: 显示group的二维码供他人扫描加入
 * 2. MODE_SCAN: 扫描他人的group二维码并申请加入
 */
class QRCodeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_GROUP_ID = "group_id"
        const val EXTRA_GROUP_NAME = "group_name"
        
        const val MODE_SHOW = "show"
        const val MODE_SCAN = "scan"
        
        private const val CAMERA_PERMISSION_REQUEST = 100
    }

    private lateinit var mode: String
    private var groupId: String? = null
    private var groupName: String? = null

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivQRCode: ImageView
    private lateinit var layoutQrDisplay: LinearLayout
    private lateinit var barcodeScanner: DecoratedBarcodeView
    private lateinit var btnAction: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_SHOW
        groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        groupName = intent.getStringExtra(EXTRA_GROUP_NAME)

        initViews()
        setupUI()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_qr_title)
        tvDescription = findViewById(R.id.tv_qr_description)
        ivQRCode = findViewById(R.id.iv_qrcode)
        layoutQrDisplay = findViewById(R.id.layout_qr_display)
        barcodeScanner = findViewById(R.id.barcode_scanner)
        btnAction = findViewById(R.id.btn_qr_action)
        btnBack = findViewById(R.id.btn_qr_back)
    }

    private fun setupUI() {
        when (mode) {
            MODE_SHOW -> setupShowMode()
            MODE_SCAN -> setupScanMode()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupShowMode() {
        tvTitle.text = "Invite to Group"
        tvDescription.text = "Share this QR code with others to invite them to join your group"
        btnAction.text = "Share QR Code"

        // 显示二维码布局，隐藏扫描器
        layoutQrDisplay.visibility = View.VISIBLE
        barcodeScanner.visibility = View.GONE

        // 生成二维码
        groupId?.let { id ->
            val qrData = "RWM_GROUP:$id"
            val qrBitmap = generateQRCode(qrData, 512, 512)
            ivQRCode.setImageBitmap(qrBitmap)
        }

        btnAction.setOnClickListener {
            // TODO: 分享二维码
            Toast.makeText(this, "Share QR Code (TODO)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupScanMode() {
        tvTitle.text = "Scan QR Code"
        tvDescription.text = "Point your camera at the QR code"
        btnAction.visibility = View.GONE // 隐藏按钮，直接显示相机

        // 隐藏二维码布局，显示扫描器
        layoutQrDisplay.visibility = View.GONE
        barcodeScanner.visibility = View.VISIBLE

        // 检查权限并启动相机
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun startCamera() {
        barcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    // 扫描成功，暂停扫描
                    barcodeScanner.pause()
                    handleScannedData(it.text)
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {
                // 可以在这里处理可能的扫描点
            }
        })
        
        barcodeScanner.resume()
    }

    private fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
        try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height)
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onResume() {
        super.onResume()
        if (mode == MODE_SCAN) {
            barcodeScanner.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mode == MODE_SCAN) {
            barcodeScanner.pause()
        }
    }

    private fun handleScannedData(data: String) {
        // 解析二维码数据
        if (data.startsWith("RWM_GROUP:")) {
            val scannedGroupId = data.removePrefix("RWM_GROUP:")
            showJoinGroupDialog(scannedGroupId)
        } else {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showJoinGroupDialog(scannedGroupId: String) {
        AlertDialog.Builder(this)
            .setTitle("Join Group")
            .setMessage("Do you want to request to join this group?")
            .setPositiveButton("Yes") { dialog, _ ->
                requestJoinGroup(scannedGroupId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun requestJoinGroup(scannedGroupId: String) {
        // TODO: 调用数据库API申请加入group
        // sendJoinRequest(scannedGroupId)
        
        Toast.makeText(this, "Join request sent! (TODO: API call)", Toast.LENGTH_SHORT).show()
        
        // 返回结果
        val resultIntent = Intent()
        resultIntent.putExtra("join_requested_group_id", scannedGroupId)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mode == MODE_SCAN) {
                    startCamera()
                }
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

