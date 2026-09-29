package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

/**
 * Manages notification channels and triggers local push notifications
 * whenever a new grade is posted to the student's profile.
 */
object GradeNotificationManager {

  const val CHANNEL_ID = "sti_grade_alerts"
  const val EXTRA_TARGET_URL = "extra_target_portal_url"
  const val GRADES_URL = "https://one.sti.edu/Student/Grades"

  /**
   * Creates the notification channel for Grade Updates (Android 8.0+).
   */
  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = context.getString(R.string.channel_grades_name)
      val descriptionText = context.getString(R.string.channel_grades_desc)
      val importance = NotificationManager.IMPORTANCE_HIGH

      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        enableLights(true)
        lightColor = Color.parseColor("#0B5592") // STI Blue
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 250, 150, 250)
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
      }

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  /**
   * Dispatches a local push notification informing the student of a newly posted grade.
   */
  fun notifyNewGrade(
    context: Context,
    courseCode: String,
    courseDescription: String,
    grade: String,
    term: String
  ) {
    try {
      createNotificationChannel(context)

      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(EXTRA_TARGET_URL, GRADES_URL)
      }

      val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }

      // Unique request code based on subject code and term
      val requestCode = (courseCode + term).hashCode()
      val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, pendingIntentFlags)

      val title = context.getString(R.string.new_grade_title_format, courseCode)
      val shortBody = context.getString(R.string.new_grade_body_format, courseDescription, grade, term)
      val expandedText = "New grade evaluation posted for your student profile:\n\n" +
          "• Subject: $courseCode — $courseDescription\n" +
          "• Grade / Rating: $grade\n" +
          "• Period: $term\n\n" +
          "Tap to view your complete official grade breakdown on One STI."

      val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(shortBody)
        .setStyle(
          NotificationCompat.BigTextStyle()
            .bigText(expandedText)
            .setBigContentTitle(title)
            .setSummaryText(term)
        )
        .setColor(Color.parseColor("#0B5592")) // STI Brand Blue
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_EVENT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

      val notificationManager = NotificationManagerCompat.from(context)
      try {
        notificationManager.notify(requestCode, notificationBuilder.build())
      } catch (_: SecurityException) {
        // Handled if runtime permission was not granted
      }
    } catch (_: Throwable) {}
  }

  /**
   * Helper method to trigger a test grade alert notification.
   */
  fun sendTestNotification(context: Context) {
    notifyNewGrade(
      context = context,
      courseCode = "IT301",
      courseDescription = "Mobile Application Development",
      grade = "1.00",
      term = "Final Grade Posted"
    )
  }
}
