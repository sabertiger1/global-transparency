package com.floatingoverlay

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var imageView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var currentAlpha: Float = 0.5f
    private var currentX: Int = 0
    private var currentY: Int = 0
    private var currentWidth: Int = -1
    private var currentHeight: Int = -1
    private lateinit var prefsManager: PreferencesManager
    
    private val adjustmentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_MOVE_LEFT -> moveLeft()
                ACTION_MOVE_RIGHT -> moveRight()
                ACTION_MOVE_UP -> moveUp()
                ACTION_MOVE_DOWN -> moveDown()
                ACTION_INCREASE_ALPHA -> increaseAlpha()
                ACTION_DECREASE_ALPHA -> decreaseAlpha()
                ACTION_INCREASE_WIDTH -> increaseWidth()
                ACTION_DECREASE_WIDTH -> decreaseWidth()
                ACTION_INCREASE_HEIGHT -> increaseHeight()
                ACTION_DECREASE_HEIGHT -> decreaseHeight()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsManager = PreferencesManager.getInstance(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // 注册广播接收器
        val filter = IntentFilter().apply {
            addAction(ACTION_MOVE_LEFT)
            addAction(ACTION_MOVE_RIGHT)
            addAction(ACTION_MOVE_UP)
            addAction(ACTION_MOVE_DOWN)
            addAction(ACTION_INCREASE_ALPHA)
            addAction(ACTION_DECREASE_ALPHA)
            addAction(ACTION_INCREASE_WIDTH)
            addAction(ACTION_DECREASE_WIDTH)
            addAction(ACTION_INCREASE_HEIGHT)
            addAction(ACTION_DECREASE_HEIGHT)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(adjustmentReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingWindow()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持悬浮窗显示"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("悬浮窗运行中")
            .setContentText("点击返回应用")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(true)
            .build()
    }

    private fun showFloatingWindow() {
        if (floatingView != null) {
            return // 已经显示
        }

        // 获取屏幕尺寸和方向
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // 判断当前是否为横屏（宽度大于高度）
        val isLandscape = screenWidth > screenHeight
        val displayWidth = if (isLandscape) screenWidth else screenHeight
        val displayHeight = if (isLandscape) screenHeight else screenWidth

        // 从SharedPreferences恢复设置
        val savedAlpha = prefsManager.getAlpha()
        val savedPosition = prefsManager.getPosition()
        val savedSize = prefsManager.getSize()
        
        currentAlpha = savedAlpha
        
        // 确定初始尺寸
        // 默认宽度3400（用户指定），如果没有保存的设置则使用默认值
        val defaultWidth = 3400
        val initialWidth = if (savedSize.first > 0) savedSize.first else defaultWidth
        val initialHeight = if (savedSize.second > 0) savedSize.second else (defaultWidth * 0.25).toInt()
        currentWidth = initialWidth
        currentHeight = initialHeight

        // 创建ImageView显示图片
        imageView = ImageView(this).apply {
            setImageResource(R.drawable.ganjiang_aiming)
            scaleType = ImageView.ScaleType.FIT_CENTER
            alpha = currentAlpha
            
            // 如果当前是竖屏，旋转90度以实现横屏显示效果
            if (!isLandscape) {
                rotation = 90f
            }
        }

        // 创建容器View
        floatingView = imageView

        // 设置窗口参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE // 触摸穿透
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START // 使用TOP|START以便精确控制位置
            
            width = currentWidth
            height = currentHeight
            
            // 恢复保存的位置，如果没有则使用屏幕中央
            if (savedPosition.first != Int.MIN_VALUE && savedPosition.second != Int.MIN_VALUE) {
                currentX = savedPosition.first
                currentY = savedPosition.second
            } else {
                currentX = (screenWidth / 2) - (width / 2)
                currentY = (screenHeight / 2) - (height / 2)
            }
            x = currentX
            y = currentY
        }
        
        layoutParams = params

        try {
            windowManager?.addView(floatingView, params)
            // 发送初始值更新
            sendAlphaUpdate()
            sendSizeUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeFloatingWindow() {
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
        imageView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(adjustmentReceiver)
        removeFloatingWindow()
    }
    
    // 调整位置的方法
    private fun moveLeft() {
        layoutParams?.let { params ->
            currentX -= MOVE_STEP
            params.x = currentX
            updateWindowPosition()
            prefsManager.savePosition(currentX, currentY) // 自动保存
        }
    }
    
    private fun moveRight() {
        layoutParams?.let { params ->
            currentX += MOVE_STEP
            params.x = currentX
            updateWindowPosition()
            prefsManager.savePosition(currentX, currentY) // 自动保存
        }
    }
    
    private fun moveUp() {
        layoutParams?.let { params ->
            currentY -= MOVE_STEP
            params.y = currentY
            updateWindowPosition()
            prefsManager.savePosition(currentX, currentY) // 自动保存
        }
    }
    
    private fun moveDown() {
        layoutParams?.let { params ->
            currentY += MOVE_STEP
            params.y = currentY
            updateWindowPosition()
            prefsManager.savePosition(currentX, currentY) // 自动保存
        }
    }
    
    // 调整透明度的方法
    private fun increaseAlpha() {
        currentAlpha = (currentAlpha + ALPHA_STEP).coerceAtMost(1.0f)
        imageView?.alpha = currentAlpha
        prefsManager.saveAlpha(currentAlpha) // 自动保存
        sendAlphaUpdate()
    }
    
    private fun decreaseAlpha() {
        currentAlpha = (currentAlpha - ALPHA_STEP).coerceAtLeast(0.1f)
        imageView?.alpha = currentAlpha
        prefsManager.saveAlpha(currentAlpha) // 自动保存
        sendAlphaUpdate()
    }
    
    // 调整尺寸的方法
    private fun increaseWidth() {
        layoutParams?.let { params ->
            val displayMetrics = resources.displayMetrics
            val maxWidth = displayMetrics.widthPixels
            currentWidth = (currentWidth + SIZE_STEP).coerceAtMost(maxWidth)
            params.width = currentWidth
            updateWindowSize()
            prefsManager.saveSize(currentWidth, currentHeight) // 自动保存
            sendSizeUpdate()
        }
    }
    
    private fun decreaseWidth() {
        layoutParams?.let { params ->
            currentWidth = (currentWidth - SIZE_STEP).coerceAtLeast(200) // 最小宽度200px
            params.width = currentWidth
            updateWindowSize()
            prefsManager.saveSize(currentWidth, currentHeight) // 自动保存
            sendSizeUpdate()
        }
    }
    
    private fun increaseHeight() {
        layoutParams?.let { params ->
            val displayMetrics = resources.displayMetrics
            val maxHeight = displayMetrics.heightPixels
            currentHeight = (currentHeight + SIZE_STEP).coerceAtMost(maxHeight)
            params.height = currentHeight
            updateWindowSize()
            prefsManager.saveSize(currentWidth, currentHeight) // 自动保存
            sendSizeUpdate()
        }
    }
    
    private fun decreaseHeight() {
        layoutParams?.let { params ->
            currentHeight = (currentHeight - SIZE_STEP).coerceAtLeast(100) // 最小高度100px
            params.height = currentHeight
            updateWindowSize()
            prefsManager.saveSize(currentWidth, currentHeight) // 自动保存
            sendSizeUpdate()
        }
    }
    
    private fun updateWindowPosition() {
        layoutParams?.let { params ->
            try {
                windowManager?.updateViewLayout(floatingView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateWindowSize() {
        layoutParams?.let { params ->
            try {
                windowManager?.updateViewLayout(floatingView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun sendAlphaUpdate() {
        val intent = Intent(ACTION_ALPHA_UPDATED).apply {
            putExtra(EXTRA_ALPHA_VALUE, currentAlpha)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun sendSizeUpdate() {
        val intent = Intent(ACTION_SIZE_UPDATED).apply {
            putExtra(EXTRA_WIDTH_VALUE, currentWidth)
            putExtra(EXTRA_HEIGHT_VALUE, currentHeight)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        private const val CHANNEL_ID = "FloatingWindowChannel"
        private const val NOTIFICATION_ID = 1
        
        // 调整步长
        private const val MOVE_STEP = 20 // 像素
        private const val ALPHA_STEP = 0.05f // 透明度步长
        private const val SIZE_STEP = 50 // 尺寸调整步长（像素）
        
        // 广播Action
        const val ACTION_MOVE_LEFT = "com.floatingoverlay.MOVE_LEFT"
        const val ACTION_MOVE_RIGHT = "com.floatingoverlay.MOVE_RIGHT"
        const val ACTION_MOVE_UP = "com.floatingoverlay.MOVE_UP"
        const val ACTION_MOVE_DOWN = "com.floatingoverlay.MOVE_DOWN"
        const val ACTION_INCREASE_ALPHA = "com.floatingoverlay.INCREASE_ALPHA"
        const val ACTION_DECREASE_ALPHA = "com.floatingoverlay.DECREASE_ALPHA"
        const val ACTION_INCREASE_WIDTH = "com.floatingoverlay.INCREASE_WIDTH"
        const val ACTION_DECREASE_WIDTH = "com.floatingoverlay.DECREASE_WIDTH"
        const val ACTION_INCREASE_HEIGHT = "com.floatingoverlay.INCREASE_HEIGHT"
        const val ACTION_DECREASE_HEIGHT = "com.floatingoverlay.DECREASE_HEIGHT"
        const val ACTION_ALPHA_UPDATED = "com.floatingoverlay.ALPHA_UPDATED"
        const val ACTION_SIZE_UPDATED = "com.floatingoverlay.SIZE_UPDATED"
        const val EXTRA_ALPHA_VALUE = "alpha_value"
        const val EXTRA_WIDTH_VALUE = "width_value"
        const val EXTRA_HEIGHT_VALUE = "height_value"
    }
}

