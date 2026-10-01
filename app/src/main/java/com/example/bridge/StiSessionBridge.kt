package com.example.bridge

import android.content.Context
import android.content.SharedPreferences
import android.webkit.JavascriptInterface
import com.example.SessionManager
import org.json.JSONObject

/**
 * JavaScript interface that bridges Web Storage (localStorage & sessionStorage)
 * with native Android persistent SharedPreferences.
 *
 * Guarantees that authentication tokens (OAuth, MSAL, ADAL, ASP.NET session tokens)
 * for ALL accounts are mirrored to native disk storage and restored instantly,
 * preventing any account from ever logging out by itself.
 */
class StiSessionBridge(private val context: Context) {

  companion object {
    const val BRIDGE_NAME = "StiSessionBridge"
    private const val PREFS_NAME = "one_sti_native_token_vault"
    private const val PREFIX_STORAGE = "web_vault_"
  }

  private fun getPrefs(): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  /**
   * Called by JavaScript to back up all tokens and web storage items for the current origin.
   */
  @JavascriptInterface
  fun saveWebTokens(origin: String, storageJson: String) {
    if (origin.isBlank() || storageJson.isBlank()) return
    try {
      val prefs = getPrefs()
      val key = PREFIX_STORAGE + origin.trim().lowercase()
      prefs.edit().putString(key, storageJson).apply()
    } catch (_: Throwable) {}
  }

  /**
   * Called by JavaScript on page load to retrieve previously persisted web storage tokens.
   */
  @JavascriptInterface
  fun getWebTokens(origin: String): String {
    if (origin.isBlank()) return "{}"
    return try {
      val prefs = getPrefs()
      val key = PREFIX_STORAGE + origin.trim().lowercase()
      prefs.getString(key, "{}") ?: "{}"
    } catch (_: Throwable) {
      "{}"
    }
  }

  /**
   * Called by JavaScript periodically as an anti-inactivity heartbeat.
   * Flushes and permanently vaults current session cookies to native storage.
   */
  @JavascriptInterface
  fun heartbeat(origin: String) {
    try {
      SessionManager.persistSession(context, origin)
    } catch (_: Throwable) {}
  }
}
