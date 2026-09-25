package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.SessionManager
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickShortcutsScreen(
  onNavigateBack: () -> Unit,
  onOpenPortalUrl: (String) -> Unit
) {
  val context = LocalContext.current

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = "Portal Quick Access",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = StiBlue
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("shortcuts_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = StiBlue
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "STI Student Services",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = StiBlue
        )
        Text(
          text = "Direct links to essential modules inside ONE STI",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
      }

      item {
        ShortcutItemCard(
          title = "ONE STI Home Dashboard",
          subtitle = "Main portal announcements, news & campus feeds",
          icon = Icons.Default.School,
          onClick = { onOpenPortalUrl("https://one.sti.edu/") }
        )
      }

      item {
        ShortcutItemCard(
          title = "eLMS - STI eLearning",
          subtitle = "Course modules, online quizzes, and assignments",
          icon = Icons.Default.BookmarkBorder,
          onClick = { onOpenPortalUrl("https://elms.sti.edu/") }
        )
      }

      item {
        ShortcutItemCard(
          title = "Academic Calendar & Schedules",
          subtitle = "Term dates, examination weeks, and school activities",
          icon = Icons.Default.CalendarMonth,
          onClick = { onOpenPortalUrl("https://one.sti.edu/calendar") }
        )
      }

      item {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Persistent Session Status",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = StiBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = StiBlue.copy(alpha = 0.08f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(StiBlue),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StiYellow,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "Permanent Login Active",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = StiBlue
              )
              Text(
                text = "Cookies & SSO credentials are securely backed up. Exiting or killing the app will never sign you out.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPortalScreen(
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = "About ONE STI",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = StiBlue
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("about_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = StiBlue
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        Spacer(modifier = Modifier.height(12.dp))
        // STI Shield Graphic Icon
        Box(
          modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(StiBlue),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "STI",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = StiYellow
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "ONE STI",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = StiBlue
        )
        Text(
          text = "Made by MJ",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = StiYellow.copy(alpha = 0.2f)
        ) {
          Text(
            text = "Version 1.0 • Android Native",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = StiBlue,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = StiBlue)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Key Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StiBlue
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            FeatureBullet(title = "Permanent Login", desc = "Stay logged into your student portal even after app restarts.")
            FeatureBullet(title = "Hardware Acceleration", desc = "Optimized 60/120fps scrolling and offscreen pre-rasterization.")
            FeatureBullet(title = "Offline Intelligence", desc = "Caches portal data automatically with instant reconnect detection.")
            FeatureBullet(title = "File Attachment & Camera", desc = "Seamless file uploads for assignments, projects, and documents.")
          }
        }
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = StiBlue)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "APK Download & Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StiBlue
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "This application connects directly to the official STI College portal at one.sti.edu. Developed for STIers with ❤️ by MJ.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun ShortcutItemCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(StiBlue.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = StiBlue,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = null,
        tint = StiBlue.copy(alpha = 0.6f),
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun FeatureBullet(title: String, desc: String) {
  Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
    Text(text = "•", color = StiBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
    Column {
      Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
