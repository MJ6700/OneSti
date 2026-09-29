package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class StudentProfile(
  val name: String = "Juan Dela Cruz",
  val studentNumber: String = "02000284912",
  val program: String = "BS Information Technology",
  val yearLevel: String = "3rd Year • 1st Term",
  val campus: String = "STI College",
  val email: String = "delacruz.284912@sti.edu.ph",
  val status: String = "Officially Enrolled"
)

data class ScheduleItem(
  val subjectCode: String,
  val subjectTitle: String,
  val units: Int,
  val day: String, // "Monday", "Tuesday", etc.
  val startTime: String,
  val endTime: String,
  val room: String,
  val instructor: String,
  val section: String
)

data class DetailedGradeItem(
  val subjectCode: String,
  val subjectTitle: String,
  val units: Double,
  val prelim: String,
  val midterm: String,
  val prefinal: String,
  val finalGrade: String,
  val remarks: String
)

data class LedgerInfo(
  val totalAssessment: Double = 34500.00,
  val totalPaid: Double = 24000.00,
  val balance: Double = 10500.00,
  val nextDueDate: String = "October 15, 2026",
  val nextDueAmount: Double = 5250.00,
  val status: String = "Current / Good Standing"
)

data class AnnouncementItem(
  val id: String,
  val title: String,
  val summary: String,
  val date: String,
  val category: String
)

object StudentDataManager {
  private const val PREFS_NAME = "one_sti_student_data_prefs"
  private const val KEY_PROFILE = "cached_student_profile"

  fun getProfile(context: Context): StudentProfile {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_PROFILE, null) ?: return StudentProfile()
    return try {
      val obj = JSONObject(raw)
      StudentProfile(
        name = obj.optString("name", "Juan Dela Cruz"),
        studentNumber = obj.optString("studentNumber", "02000284912"),
        program = obj.optString("program", "BS Information Technology"),
        yearLevel = obj.optString("yearLevel", "3rd Year • 1st Term"),
        campus = obj.optString("campus", "STI College"),
        email = obj.optString("email", "delacruz.284912@sti.edu.ph"),
        status = obj.optString("status", "Officially Enrolled")
      )
    } catch (_: Exception) {
      StudentProfile()
    }
  }

  fun saveProfile(context: Context, profile: StudentProfile) {
    try {
      val obj = JSONObject().apply {
        put("name", profile.name)
        put("studentNumber", profile.studentNumber)
        put("program", profile.program)
        put("yearLevel", profile.yearLevel)
        put("campus", profile.campus)
        put("email", profile.email)
        put("status", profile.status)
      }
      context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_PROFILE, obj.toString()).apply()
    } catch (_: Exception) {}
  }

  fun getDefaultSchedule(): List<ScheduleItem> = listOf(
    ScheduleItem("IT301", "Mobile Application Development", 3, "Monday", "08:00 AM", "11:00 AM", "Lab 402", "Prof. R. Santos", "BSIT-301A"),
    ScheduleItem("IT305", "Information Assurance & Security", 3, "Monday", "01:00 PM", "04:00 PM", "Room 305", "Engr. M. Garcia", "BSIT-301A"),
    ScheduleItem("IT308", "Web Systems & Technologies", 3, "Tuesday", "09:00 AM", "12:00 PM", "Lab 405", "Prof. D. Reyes", "BSIT-301A"),
    ScheduleItem("CS204", "Database Management Systems", 3, "Wednesday", "10:00 AM", "01:00 PM", "Lab 401", "Prof. J. Cruz", "BSIT-301A"),
    ScheduleItem("GE108", "Ethics & Social Responsibility", 3, "Thursday", "08:00 AM", "10:00 AM", "Room 208", "Prof. L. Mendoza", "BSIT-301A"),
    ScheduleItem("IT312", "Integrative Programming & Technologies", 3, "Friday", "01:00 PM", "04:00 PM", "Lab 403", "Prof. A. Ramos", "BSIT-301A"),
    ScheduleItem("PE104", "Team Sports & Fitness", 2, "Saturday", "08:00 AM", "10:00 AM", "Gymnasium", "Coach V. Tan", "BSIT-301A")
  )

  fun getDefaultGrades(): List<DetailedGradeItem> = listOf(
    DetailedGradeItem("IT301", "Mobile Application Development", 3.0, "1.25", "1.00", "1.25", "1.00", "Passed"),
    DetailedGradeItem("IT305", "Information Assurance & Security", 3.0, "1.50", "1.25", "1.50", "1.25", "Passed"),
    DetailedGradeItem("IT308", "Web Systems & Technologies", 3.0, "1.00", "1.25", "1.00", "1.00", "Passed"),
    DetailedGradeItem("CS204", "Database Management Systems", 3.0, "1.25", "1.50", "1.25", "1.25", "Passed"),
    DetailedGradeItem("GE108", "Ethics & Social Responsibility", 3.0, "1.50", "1.75", "1.50", "1.50", "Passed"),
    DetailedGradeItem("IT312", "Integrative Programming & Tech", 3.0, "1.25", "1.00", "1.25", "1.00", "Passed"),
    DetailedGradeItem("PE104", "Team Sports & Fitness", 2.0, "1.00", "1.00", "1.00", "1.00", "Passed")
  )

  fun getAnnouncements(): List<AnnouncementItem> = listOf(
    AnnouncementItem("1", "Midterm Examination Schedule Released", "The midterm examinations for 1st Term AY 2026-2027 will commence on October 19. Please settle your accounts to obtain test permits.", "Yesterday", "Academics"),
    AnnouncementItem("2", "STI Talent Search & Tagisan ng Talino 2026", "Campus-level eliminations are now open for registration at the Student Affairs Office.", "3 days ago", "Campus Life"),
    AnnouncementItem("3", "ELMS Maintenance Advisory", "STI e-Learning Management System will undergo scheduled upgrades on Saturday at 11:00 PM to improve 144Hz mobile performance.", "5 days ago", "Advisory")
  )
}
