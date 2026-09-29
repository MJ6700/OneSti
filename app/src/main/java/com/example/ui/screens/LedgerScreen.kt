package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiDarkBlue
import com.example.ui.theme.StiYellow

@Composable
fun LedgerScreen(
  onNavigateBack: () -> Unit,
  onOpenLivePortal: () -> Unit
) {
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
              modifier = Modifier.testTag("ledger_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Student Ledger",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          }

          IconButton(
            onClick = onOpenLivePortal,
            modifier = Modifier.testTag("ledger_portal_button")
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
          .testTag("ledger_content_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Balance Overview Hero Card
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
                .padding(20.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "REMAINING BALANCE",
                    color = StiYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  )
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981)
                  ) {
                    Text(
                      text = "Good Standing",
                      color = Color.White,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "₱10,500.00",
                  color = Color.White,
                  fontWeight = FontWeight.Black,
                  fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(
                      text = "Total Assessment",
                      color = Color.White.copy(alpha = 0.8f),
                      fontSize = 11.sp
                    )
                    Text(
                      text = "₱34,500.00",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "Total Paid",
                      color = Color.White.copy(alpha = 0.8f),
                      fontSize = 11.sp
                    )
                    Text(
                      text = "₱24,000.00",
                      color = StiYellow,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }
                }
              }
            }
          }
        }

        // Installment Schedule
        item {
          Text(
            text = "Installment Due Dates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              InstallmentRow("Downpayment (Enrollment)", "₱5,000.00", "Paid on Aug 15", isPaid = true)
              HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
              InstallmentRow("Prelim Period", "₱9,500.00", "Paid on Sep 10", isPaid = true)
              HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
              InstallmentRow("Midterm Period", "₱9,500.00", "Paid on Oct 01", isPaid = true)
              HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
              InstallmentRow("Pre-Final Period", "₱5,250.00", "Due Oct 15, 2026", isPaid = false, isDueSoon = true)
              HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
              InstallmentRow("Final Period", "₱5,250.00", "Due Nov 20, 2026", isPaid = false)
            }
          }
        }

        // Official Payment Channels
        item {
          Text(
            text = "Accredited Payment Channels",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              PaymentMethodRow("GCash / Maya", "Pay via Bills Payment > School > STI College")
              PaymentMethodRow("BDO / UnionBank", "Online bills payment using 11-digit Student Number")
              PaymentMethodRow("Dragonpay Online", "Available on One STI Portal payment gateway")
              PaymentMethodRow("Campus Cashier", "Cash, Debit/Credit Card, or Manager's Check")
            }
          }
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
              .testTag("open_live_portal_ledger")
          ) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = StiYellow)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Official Live Ledger & Receipt", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun InstallmentRow(
  label: String,
  amount: String,
  statusText: String,
  isPaid: Boolean,
  isDueSoon: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = label,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
      )
      Text(
        text = statusText,
        style = MaterialTheme.typography.bodySmall,
        color = if (isPaid) Color(0xFF10B981) else if (isDueSoon) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = amount,
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.titleSmall
      )
      Spacer(modifier = Modifier.width(8.dp))
      if (isPaid) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Paid",
          tint = Color(0xFF10B981),
          modifier = Modifier.size(18.dp)
        )
      } else {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = "Pending",
          tint = if (isDueSoon) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
fun PaymentMethodRow(title: String, desc: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = StiBlue.copy(alpha = 0.1f),
      modifier = Modifier.size(40.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Default.CreditCard,
          contentDescription = null,
          tint = StiBlue,
          modifier = Modifier.size(20.dp)
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
      Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
