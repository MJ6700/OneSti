package com.example

import android.app.Application
import android.system.Os

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
        Os.setenv("MESA_LOG_LEVEL", "none", true)
        Os.setenv("EGL_LOG_LEVEL", "fatal", true)
        Os.setenv("MESA_DEBUG", "0", true)
      } catch (_: Throwable) {
        // Graceful fallback if Os.setenv is restricted
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    configureGraphicsEnvironment()
    // Initialize session cookies and restore persistent login state early
    SessionManager.initCookieManager(this)
  }
}
