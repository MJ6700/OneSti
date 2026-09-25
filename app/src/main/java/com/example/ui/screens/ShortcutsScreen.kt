package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraduationCap
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiYellow

data class PortalShortcut(
  val title: String,
  val subtitle: String,
  val url: String,
  val icon: ImageVector,
  val badge: String? = null
)

private val STI_SHORTCUTS = listOf(
  PortalShortcut(
    title = "ONE STI Student Portal",
    subtitle = "Grades, schedules, curriculum, and enrollment status",
    url = "https://one.sti.edu",
    icon = Icons.Default.School,
    badge = "Primary"
  ),
  PortalShortcut(
    title = "STI eLMS (e-Learning)",
    subtitle = "Courses, assignments, video lectures, and quizzes",
    url = "https://elms.sti.edu",
    icon = Icons.Default.MenuBook,
    badge = "Online Class"
  ),
  PortalShortcut(
    title = "Student Ledger & Accounts",
    subtitle = "Tuition breakdown, installment dues, and payment records",
    url = "https://one.sti.edu",
    icon = Icons.Default.AccountBalance
  ),
  PortalShortcut(
    title = "Office 365 & Student Email",
    subtitle = "Access official student Outlook mailbox and Teams",
    url = "https://outlook.office.com",
    icon = Icons.Default.Email,
    badge = "MS365"
  ),
  PortalShortcut(
    title = "Academic Evaluation & Grades",
    subtitle = "Midterm and final grade submissions per trimester",
    url = "https://one.sti.edu",
    icon = Icons.Default.GraduationCap
  ),
  PortalShortcut(
    title = "STI Official Website",
    subtitle = "Campus news, academic calendars, and admissions",
    url = "https://www.sti.edu",
    icon = Icons.Default.Language
  )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsScreen(
  onNavigateBack: () -> Unit,
  onOpenUrl: (String) -> Unit
) {
  BackHandler {
    onNavigateBack()
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.portal_shortcuts_title),
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("shortcuts_back_button")
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("shortcuts_list"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = StiYellow.copy(alpha = 0.18f)
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
                .background(StiYellow),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = StiBlue,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "STI Student Hub",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = StiBlue
              )
              Text(
                text = "Tap any service to jump directly within your active portal session.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      items(STI_SHORTCUTS) { shortcut ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onOpenUrl(shortcut.url)
            }
            .testTag("shortcut_item_${shortcut.title.replace(" ", "_").lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StiBlue.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = shortcut.icon,
                contentDescription = null,
                tint = StiBlue,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = shortcut.title,
                  fontWeight = FontWeight.SemiBold,
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurface
                )
                if (shortcut.badge != null) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StiYellow
                  ) {
                    Text(
                      text = shortcut.badge,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = StiBlue,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = shortcut.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "ONE STI • Made by MJ",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth(),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
  }
}
