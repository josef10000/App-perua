package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Announcement
import com.example.ui.theme.AccentRed
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(announcement.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (announcement.isUrgent) AccentRed.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (announcement.isUrgent) Color(0xFFFEF2F2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (announcement.isUrgent) AccentRed else YellowPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (announcement.isUrgent) Icons.Default.PriorityHigh else Icons.Default.Campaign,
                            contentDescription = "Comunicado",
                            tint = if (announcement.isUrgent) Color.White else SlateNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = announcement.driverName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateNavy
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (announcement.isUrgent) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentRed
                    ) {
                        Text(
                            text = "URGENTE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = announcement.message,
                fontSize = 14.sp,
                color = SlateNavy,
                lineHeight = 20.sp
            )
        }
    }
}
