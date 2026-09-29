package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DetailedGradeItem
import com.example.data.StudentDataManager
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiDarkBlue
import com.example.ui.theme.StiYellow

@Composable
fun GradesScreen(
  onNavigateBack: () -> Unit,
  onOpenLivePortal: () -> Unit
) {
  val context = LocalContext.current
  val gradesList = remember { StudentDataManager.getDefaultGrades() }
  var selectedTerm by remember { mutableStateOf("1st Term AY 2026-2027") }
  val terms = listOf("1st Term AY 2026-2027", "2nd Term AY 2025-2026", "1st Term AY 2025-2026")

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Solid Black Top Bar
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsTopHeight(WindowInsets.statusBars)
          .background(Color.Black)
      )

      Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.testTag("grades_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Grades & Evaluations",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          }

          IconButton(
            onClick = onOpenLivePortal,
            modifier = Modifier.testTag("grades_sync_portal_button")
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = "Live Portal",
              tint = StiYellow
            )
          }
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("grades_content_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // GWA Summary Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.linearGradient(
                    colors = listOf(StiDarkBlue, StiBlue)
                  )
                )
                .padding(18.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Star,
                      contentDescription = null,
                      tint = StiYellow,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "ACADEMIC STANDING",
                      color = StiYellow,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Dean's Lister Candidate",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Total Enrolled Units: 20.0 • Passed: 20.0",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                  )
                }

                Surface(
                  shape = CircleShape,
                  color = Color.White.copy(alpha = 0.15f),
                  modifier = Modifier.size(68.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                        text = "1.18",
                        color = StiYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                      )
                      Text(
                        text = "GWA",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Term Filter Chips
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            terms.forEach { term ->
              val isSelected = selectedTerm == term
              FilterChip(
                selected = isSelected,
                onClick = { selectedTerm = term },
                label = {
                  Text(
                    text = term.replace("AY 2026-2027", "").trim(),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StiBlue,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }

        // Subject Grades List
        items(gradesList) { grade ->
          GradeSubjectCard(grade = grade)
        }

        // Live Portal Deep Link
        item {
          Button(
            onClick = onOpenLivePortal,
            colors = ButtonDefaults.buttonColors(containerColor = StiDarkBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("open_live_portal_grades")
          ) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = StiYellow)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Official One STI Live Grades", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun GradeSubjectCard(grade: DetailedGradeItem) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = grade.subjectCode,
            color = StiBlue,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
          )
          Text(
            text = grade.subjectTitle,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF10B981).copy(alpha = 0.12f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = grade.remarks,
              color = Color(0xFF10B981),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Term Grades Breakdown Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
          .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        GradeMetric(label = "Prelim", value = grade.prelim)
        GradeMetric(label = "Midterm", value = grade.midterm)
        GradeMetric(label = "Pre-Final", value = grade.prefinal)
        GradeMetric(label = "Final", value = grade.finalGrade, isHighlight = true)
        GradeMetric(label = "Units", value = "${grade.units.toInt()}.0")
      }
    }
  }
}

@Composable
fun GradeMetric(
  label: String,
  value: String,
  isHighlight: Boolean = false
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Bold,
      color = if (isHighlight) StiBlue else MaterialTheme.colorScheme.onSurface
    )
  }
}
