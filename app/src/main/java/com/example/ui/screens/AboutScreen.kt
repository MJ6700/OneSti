package com.example.ui.screens

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.SessionManager
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  BackHandler {
    onNavigateBack()
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.portal_info_title),
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("about_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back_to_portal),
              tint = Color.White
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = StiBlue
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header Logo and App Title
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(StiBlue),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.School,
          contentDescription = "ONE STI",
          tint = StiYellow,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "ONE STI Student Portal",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = StiBlue
      )

      Text(
        text = "Made by MJ",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(6.dp))

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = StiYellow.copy(alpha = 0.25f)
      ) {
        Text(
          text = "Version 1.0 • Smooth Animation Release",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = StiBlue,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Features Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Engine & UX Enhancements",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = StiBlue
          )

          Spacer(modifier = Modifier.height(12.dp))

          FeatureItem(
            icon = Icons.Default.Animation,
            title = "Navigation Transition Animations",
            description = "Fluid 60fps slide & fade physics across all Compose screens."
          )

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
          )

          FeatureItem(
            icon = Icons.Default.Lock,
            title = "Permanent Session Persistence",
            description = "Never get signed out when switching apps or closing the portal."
          )

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
          )

          FeatureItem(
            icon = Icons.Default.Speed,
            title = "Chromium Pre-Rasterization",
            description = "Tiled offscreen pre-rendering eliminates stutter and blank flashes."
          )

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
          )

          FeatureItem(
            icon = Icons.Default.CloudDone,
            title = "Offline Cache Fallback",
            description = "Access recently visited schedules and grades even when offline."
          )

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
          )

          FeatureItem(
            icon = Icons.Default.Download,
            title = "Integrated Download Manager",
            description = "Direct PDF, syllabus, and assessment downloads with system notifications."
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Session & Cache Maintenance Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Session & Storage Maintenance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = StiBlue
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Your Microsoft 365 and ONE STI login tokens are backed up locally. If you ever need to reset web resources, use the controls below.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = {
                SessionManager.persistSession(context, null)
                CookieManager.getInstance().flush()
                Toast.makeText(context, "Session tokens flushed to secure storage", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("flush_session_button")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = StiBlue)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sync Session", fontSize = 12.sp, color = StiBlue)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                WebStorage.getInstance().deleteAllData()
                Toast.makeText(context, "Temporary web cache cleared", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = StiBlue
              ),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("clear_cache_button")
            ) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Clear Cache", fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "STI College • Dedicated to Excellence\nMade by MJ",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun FeatureItem(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(StiBlue.copy(alpha = 0.1f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = StiBlue,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 15.sp
      )
    }
  }
}
