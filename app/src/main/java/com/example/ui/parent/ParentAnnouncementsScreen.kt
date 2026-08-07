package com.example.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AnnouncementCard
import com.example.ui.theme.SlateNavy

@Composable
fun ParentAnnouncementsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val announcements by viewModel.announcements.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MURAL DE AVISOS DO MOTORISTA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SlateNavy
            )
            Text(
                text = "Comunicados diretos de Tio Carlos sobre horários e novidades da rota.",
                fontSize = 12.sp,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }

        items(announcements) { announcement ->
            AnnouncementCard(announcement = announcement)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
