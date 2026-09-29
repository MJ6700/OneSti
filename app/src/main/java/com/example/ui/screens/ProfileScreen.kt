package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudentDataManager
import com.example.notifications.GradeNotificationManager
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiDarkBlue
import com.example.ui.theme.StiYellow

@Composable
fun ProfileScreen(
  onNavigateBack: () -> Unit,
  onNavigateToAbout: () -> Unit
) {
  val context = LocalContext.current
  val profile = remember { StudentDataManager.getProfile(context) }
  var notificationsEnabled by remember { mutableStateOf(true) }

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
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("profile_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Student Profile & Accounts",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("profile_content_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Profile Info Header Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(StiBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = StiBlue,
                  modifier = Modifier.size(44.dp)
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = profile.name,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
              )
              Text(
                text = profile.program,
                color = StiBlue,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Student No. ${profile.studentNumber} • ${profile.campus}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(14.dp))

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = profile.status,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }

        // Account Persistence & Security Card
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Account & Session Security",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(12.dp))

              SettingsStatusItem(
                icon = Icons.Default.Lock,
                title = "Universal Keep-Signed-In",
                subtitle = "Active • All accounts never log out automatically",
                statusColor = Color(0xFF10B981)
              )

              HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

              SettingsStatusItem(
                icon = Icons.Default.Speed,
                title = "144Hz Ultra-Smooth Mode",
                subtitle = "Enabled • Hardware GPU pre-rasterization active",
                statusColor = StiYellow
              )

              HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

              SettingsStatusItem(
                icon = Icons.Default.School,
                title = "Institutional SSO",
                subtitle = "Microsoft 365, Canvas & One STI sync verified",
                statusColor = StiBlue
              )
            }
          }
        }

        // Grade Notifications Card
        item {
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
                    text = "Grade Release Push Notifications",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                  )
                  Text(
                    text = "Receive instant alerts whenever a professor posts a new grade",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Switch(
                  checked = notificationsEnabled,
                  onCheckedChange = { notificationsEnabled = it },
                  colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StiBlue)
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = {
                  GradeNotificationManager.sendTestNotification(context)
                  Toast.makeText(context, "Test grade push notification sent!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StiBlue.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = StiBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Test Grade Alert", color = StiBlue, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // About & System Info
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToAbout() }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = StiBlue,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "About One STI • Made by MJ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                  )
                  Text(
                    text = "Version 1.2 • High Performance Edition",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = StiYellow
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingsStatusItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  statusColor: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = statusColor.copy(alpha = 0.12f),
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
