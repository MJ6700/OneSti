package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.notifications.GradeNotificationManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents an individual subject grade record posted to a student profile.
 */
data class GradeRecord(
  val courseCode: String,
  val courseDescription: String,
  val grade: String,
  val term: String,
  val timestamp: Long = System.currentTimeMillis()
)

/**
 * Tracks posted grades in local cache, detects newly added or updated grades,
 * and triggers push notifications.
 */
object GradeTracker {

  private const val PREFS_NAME = "one_sti_grades_cache"
  private const val KEY_RECORD_PREFIX = "grade_rec_"
  private const val KEY_HAS_INITIAL_SNAPSHOT = "has_initial_grade_snapshot"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  /**
   * Generates a stable unique storage key for a grade record based on course code and term.
   */
  fun makeKey(courseCode: String, term: String): String {
    val cleanCode = courseCode.trim().uppercase()
    val cleanTerm = term.trim().lowercase()
    return "$KEY_RECORD_PREFIX${cleanCode}_$cleanTerm"
  }

  /**
   * Parses JSON string received from the WebView JavaScript scraper,
   * detects any newly posted or updated grades, and fires push notifications.
   *
   * Expected JSON schema:
   * [
   *   {
   *     "courseCode": "IT101",
   *     "courseDescription": "Introduction to Computing",
   *     "grade": "1.25",
   *     "term": "Midterm"
   *   }
   * ]
   */
  fun processJsonGrades(context: Context, jsonString: String): List<GradeRecord> {
    if (jsonString.isBlank()) return emptyList()

    val parsedList = mutableListOf<GradeRecord>()
    try {
      val jsonArray = JSONArray(jsonString)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.optJSONObject(i) ?: continue
        val courseCode = obj.optString("courseCode", "").trim()
        val courseDescription = obj.optString("courseDescription", "").trim()
        val grade = obj.optString("grade", "").trim()
        val term = obj.optString("term", "Semester").trim()

        if (courseCode.isNotEmpty() && grade.isNotEmpty()) {
          parsedList.add(
            GradeRecord(
              courseCode = courseCode,
              courseDescription = courseDescription.ifEmpty { courseCode },
              grade = grade,
              term = term
            )
          )
        }
      }
    } catch (_: Exception) {
      return emptyList()
    }

    return checkAndNotifyNewGrades(context, parsedList)
  }

  /**
   * Compares incoming grade records with previously cached records.
   * If a grade is newly posted (or changed from incomplete/blank to a grade),
   * a local push notification is sent immediately.
   */
  fun checkAndNotifyNewGrades(context: Context, incomingGrades: List<GradeRecord>): List<GradeRecord> {
    if (incomingGrades.isEmpty()) return emptyList()

    val prefs = getPrefs(context)
    val editor = prefs.edit()
    val newlyPosted = mutableListOf<GradeRecord>()
    val hasSnapshot = prefs.getBoolean(KEY_HAS_INITIAL_SNAPSHOT, false)

    for (record in incomingGrades) {
      val key = makeKey(record.courseCode, record.term)
      val cachedGrade = prefs.getString(key, null)

      // If we already had an initial snapshot and this grade is brand new or updated
      if (hasSnapshot) {
        if (cachedGrade == null || (cachedGrade != record.grade && isValidGrade(record.grade))) {
          newlyPosted.add(record)
          // Fire local push notification
          GradeNotificationManager.notifyNewGrade(
            context = context,
            courseCode = record.courseCode,
            courseDescription = record.courseDescription,
            grade = record.grade,
            term = record.term
          )
        }
      }

      // Save/update grade in cache
      editor.putString(key, record.grade)
    }

    // Mark that we have established the initial baseline so subsequent entries trigger alerts
    editor.putBoolean(KEY_HAS_INITIAL_SNAPSHOT, true)
    editor.apply()

    return newlyPosted
  }

  /**
   * Distinguishes valid posted grades from empty or placeholder strings.
   */
  private fun isValidGrade(grade: String): Boolean {
    val clean = grade.trim().uppercase()
    if (clean.isEmpty() || clean == "-" || clean == "--" || clean == "N/A" || clean == "PENDING") {
      return false
    }
    return true
  }

  /**
   * Resets grade snapshot cache (for testing or account switching).
   */
  fun clearGradeCache(context: Context) {
    getPrefs(context).edit().clear().apply()
  }
}
