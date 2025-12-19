package com.floatingoverlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    private lateinit var tvAlphaValue: TextView
    private lateinit var tvSizeValue: TextView
    
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                FloatingWindowService.ACTION_ALPHA_UPDATED -> {
                    val alpha = intent.getFloatExtra(FloatingWindowService.EXTRA_ALPHA_VALUE, 0.5f)
                    updateAlphaDisplay(alpha)
                }
                FloatingWindowService.ACTION_SIZE_UPDATED -> {
                    val width = intent.getIntExtra(FloatingWindowService.EXTRA_WIDTH_VALUE, 0)
                    val height = intent.getIntExtra(FloatingWindowService.EXTRA_HEIGHT_VALUE, 0)
                    updateSizeDisplay(width, height)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton: MaterialButton = findViewById(R.id.btn_start)
        val stopButton: MaterialButton = findViewById(R.id.btn_stop)
        tvAlphaValue = findViewById(R.id.tv_alpha_value)
        tvSizeValue = findViewById(R.id.tv_size_value)

        // 位置调整按钮
        val btnMoveLeft: MaterialButton = findViewById(R.id.btn_move_left)
        val btnMoveRight: MaterialButton = findViewById(R.id.btn_move_right)
        val btnMoveUp: MaterialButton = findViewById(R.id.btn_move_up)
        val btnMoveDown: MaterialButton = findViewById(R.id.btn_move_down)
        
        // 透明度调整按钮
        val btnIncreaseAlpha: MaterialButton = findViewById(R.id.btn_increase_alpha)
        val btnDecreaseAlpha: MaterialButton = findViewById(R.id.btn_decrease_alpha)
        
        // 尺寸调整按钮
        val btnIncreaseWidth: MaterialButton = findViewById(R.id.btn_increase_width)
        val btnDecreaseWidth: MaterialButton = findViewById(R.id.btn_decrease_width)
        val btnIncreaseHeight: MaterialButton = findViewById(R.id.btn_increase_height)
        val btnDecreaseHeight: MaterialButton = findViewById(R.id.btn_decrease_height)

        startButton.setOnClickListener {
            if (checkOverlayPermission()) {
                startFloatingService()
            } else {
                requestOverlayPermission()
            }
        }

        stopButton.setOnClickListener {
            stopFloatingService()
        }
        
        // 位置调整
        btnMoveLeft.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_MOVE_LEFT) }
        btnMoveRight.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_MOVE_RIGHT) }
        btnMoveUp.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_MOVE_UP) }
        btnMoveDown.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_MOVE_DOWN) }
        
        // 透明度调整
        btnIncreaseAlpha.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_INCREASE_ALPHA) }
        btnDecreaseAlpha.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_DECREASE_ALPHA) }
        
        // 尺寸调整
        btnIncreaseWidth.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_INCREASE_WIDTH) }
        btnDecreaseWidth.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_DECREASE_WIDTH) }
        btnIncreaseHeight.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_INCREASE_HEIGHT) }
        btnDecreaseHeight.setOnClickListener { sendAdjustmentAction(FloatingWindowService.ACTION_DECREASE_HEIGHT) }
        
        // 注册广播接收器
        val filter = IntentFilter().apply {
            addAction(FloatingWindowService.ACTION_ALPHA_UPDATED)
            addAction(FloatingWindowService.ACTION_SIZE_UPDATED)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, filter)
        
        // 初始化显示
        updateAlphaDisplay(0.5f)
        updateSizeDisplay(0, 0)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
    }
    
    private fun sendAdjustmentAction(action: String) {
        val intent = Intent(action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun updateAlphaDisplay(alpha: Float) {
        val percentage = (alpha * 100).toInt()
        tvAlphaValue.text = "当前透明度: ${percentage}%"
    }
    
    private fun updateSizeDisplay(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            tvSizeValue.text = "当前尺寸: ${width} × ${height} px"
        } else {
            tvSizeValue.text = "当前尺寸: -"
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                startFloatingService()
            }
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        stopService(intent)
    }
}

