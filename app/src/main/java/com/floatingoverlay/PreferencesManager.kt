package com.floatingoverlay

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "FloatingOverlayPrefs"
        private const val KEY_POSITION_X = "position_x"
        private const val KEY_POSITION_Y = "position_y"
        private const val KEY_ALPHA = "alpha"
        private const val KEY_WIDTH = "width"
        private const val KEY_HEIGHT = "height"
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // 保存位置
    fun savePosition(x: Int, y: Int) {
        prefs.edit().apply {
            putInt(KEY_POSITION_X, x)
            putInt(KEY_POSITION_Y, y)
            apply()
        }
    }
    
    // 获取位置
    fun getPosition(): Pair<Int, Int> {
        val x = prefs.getInt(KEY_POSITION_X, Int.MIN_VALUE)
        val y = prefs.getInt(KEY_POSITION_Y, Int.MIN_VALUE)
        return Pair(x, y)
    }
    
    // 保存透明度
    fun saveAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_ALPHA, alpha).apply()
    }
    
    // 获取透明度
    fun getAlpha(): Float {
        return prefs.getFloat(KEY_ALPHA, 0.5f)
    }
    
    // 保存尺寸
    fun saveSize(width: Int, height: Int) {
        prefs.edit().apply {
            putInt(KEY_WIDTH, width)
            putInt(KEY_HEIGHT, height)
            apply()
        }
    }
    
    // 获取尺寸
    fun getSize(): Pair<Int, Int> {
        val width = prefs.getInt(KEY_WIDTH, -1)
        val height = prefs.getInt(KEY_HEIGHT, -1)
        return Pair(width, height)
    }
    
    // 清除所有设置
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}




