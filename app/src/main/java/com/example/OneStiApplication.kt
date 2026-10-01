package com.example

import android.app.Application
import android.content.Context
import android.system.Os
import java.io.File

/**
 * Custom Application class for One STI.
 * Configures persistent session cookies on launch and optimizes runtime graphics.
 */
class OneStiApplication : Application() {

  companion object {
    init {
      configureGraphicsEnvironment()
    }

    private fun configureGraphicsEnvironment() {
      try {
        val hasDrmRenderNode = try {
          File("/dev/dri/renderD128").exists() || File("/dev/dri").exists()
        } catch (_: Throwable) {
          false
        }
        if (!hasDrmRenderNode) {
          Os.setenv("LIBGL_ALWAYS_SOFTWARE", "true", true)
          Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
        }
        Os.setenv("MESA_NO_ERROR", "1", true)
        Os.setenv("MESA_DEBUG", "0", true)
        Os.setenv("MESA_LOG_LEVEL", "fatal", true)
        Os.setenv("EGL_LOG_LEVEL", "fatal", true)
        Os.setenv("LIBGL_DRI3_DISABLE", "1", true)
      } catch (_: Throwable) {
        // Graceful fallback if Os.setenv is restricted
      }
    }
  }

  override fun attachBaseContext(base: Context?) {
    configureGraphicsEnvironment()
    super.attachBaseContext(base)
  }

  override fun onCreate() {
    super.onCreate()
    configureGraphicsEnvironment()
    // Initialize session cookies and restore persistent login state early
    SessionManager.initCookieManager(this)
    // Setup notification channels for grade alerts
    com.example.notifications.GradeNotificationManager.createNotificationChannel(this)
  }
}
