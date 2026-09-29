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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudentDataManager
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiDarkBlue
import com.example.ui.theme.StiYellow

@Composable
fun DashboardScreen(
  onNavigateToGrades: () -> Unit,
  onNavigateToSchedule: () -> Unit,
  onNavigateToLedger: () -> Unit,
  onNavigateToPortal: (String?) -> Unit,
  onNavigateToProfile: () -> Unit
) {
  val context = LocalContext.current
  val profile = remember { StudentDataManager.getProfile(context) }
  val schedule = remember { StudentDataManager.getDefaultSchedule().take(2) }
  val announcements = remember { StudentDataManager.getAnnouncements() }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Sleek solid Black Top Status Bar
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsTopHeight(WindowInsets.statusBars)
          .background(Color.Black)
      )

      // Black Top App Bar with Branding and User Greeting
      Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(StiYellow),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = "STI Logo",
                tint = StiDarkBlue,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "ONE STI",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp
              )
              Text(
                text = profile.name,
                color = StiYellow,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // 144Hz indicator badge
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF1E293B),
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Speed,
                  contentDescription = null,
                  tint = StiYellow,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "144Hz",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            IconButton(
              onClick = onNavigateToProfile,
              modifier = Modifier.testTag("dashboard_profile_button")
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White
              )
            }
          }
        }
      }

      // Main Content Scroll
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("dashboard_content_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        // 1. Virtual Student ID Card
        item {
          VirtualStudentIdCard(
            profile = profile,
            onClick = onNavigateToProfile
          )
        }

        // 2. Quick Actions Grid
        item {
          Text(
            text = "Quick Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            QuickServiceCard(
              title = "Grades",
              subtitle = "1.18 GWA",
              icon = Icons.Default.Assessment,
              color = Color(0xFF10B981),
              modifier = Modifier.weight(1f),
              onClick = onNavigateToGrades
            )
            QuickServiceCard(
              title = "Schedule",
              subtitle = "7 Subjects",
              icon = Icons.Default.CalendarMonth,
              color = Color(0xFF3B82F6),
              modifier = Modifier.weight(1f),
              onClick = onNavigateToSchedule
            )
            QuickServiceCard(
              title = "Ledger",
              subtitle = "₱10,500 due",
              icon = Icons.Default.AccountBalance,
              color = Color(0xFFF59E0B),
              modifier = Modifier.weight(1f),
              onClick = onNavigateToLedger
            )
          }
        }

        // 3. Portals Quick Launch Bar
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = StiBlue,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Live Campus Portals",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                  )
                }
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.CloudDone,
                      contentDescription = null,
                      tint = Color(0xFF10B981),
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Auto-Login Active",
                      fontSize = 10.sp,
                      color = Color(0xFF10B981),
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                PortalLaunchChip(
                  label = "One STI",
                  modifier = Modifier.weight(1f),
                  onClick = { onNavigateToPortal("https://one.sti.edu") }
                )
                PortalLaunchChip(
                  label = "STI ELMS",
                  modifier = Modifier.weight(1f),
                  onClick = { onNavigateToPortal("https://elms.sti.edu") }
                )
                PortalLaunchChip(
                  label = "Microsoft 365",
                  modifier = Modifier.weight(1f),
                  onClick = { onNavigateToPortal("https://portal.office.com") }
                )
              }
            }
          }
        }

        // 4. Today's Class Schedule Preview
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Today's Schedule",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "View All",
              color = StiBlue,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable { onNavigateToSchedule() }
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            schedule.forEach { item ->
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StiBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = item.subjectCode.take(2),
                        color = StiBlue,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.subjectCode + " • " + item.subjectTitle,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "${item.startTime} - ${item.endTime} | ${item.room}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = "Upcoming",
                      color = Color(0xFF10B981),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // 5. Campus Announcements
        item {
          Text(
            text = "Announcements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            announcements.forEach { ann ->
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = StiYellow.copy(alpha = 0.25f)
                    ) {
                      Text(
                        text = ann.category,
                        color = Color(0xFF926300),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                    Text(
                      text = ann.date,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = ann.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = ann.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun VirtualStudentIdCard(
  profile: com.example.data.StudentProfile,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("virtual_student_id_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              StiDarkBlue,
              StiBlue,
              Color(0xFF072C4F)
            )
          )
        )
        .padding(18.dp)
    ) {
      Column {
        // Card Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(StiYellow),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "STI",
                fontWeight = FontWeight.Black,
                color = StiDarkBlue,
                fontSize = 12.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "STI COLLEGE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
              )
              Text(
                text = "OFFICIAL STUDENT IDENTIFICATION",
                color = StiYellow,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF10B981)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "VALID",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Student Details
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Photo placeholder
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(56.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = profile.name.uppercase(),
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 16.sp
            )
            Text(
              text = profile.program,
              color = StiYellow,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "SN: ${profile.studentNumber} • ${profile.yearLevel}",
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulated Barcode Line
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "||||| ||| ||||||| || |||||| |||| ||||| |||",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
          )
          Text(
            text = profile.studentNumber,
            color = StiYellow,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun QuickServiceCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.size(36.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun PortalLaunchChip(
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = StiBlue,
        maxLines = 1
      )
    }
  }
}
