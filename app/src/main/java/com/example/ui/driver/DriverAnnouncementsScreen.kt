package com.example.ui.driver

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AnnouncementCard
import com.example.ui.theme.AccentRed
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary

@Composable
fun DriverAnnouncementsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val announcements by viewModel.announcements.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Compose Announcement Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ENVIAR COMUNICADO AOS PAIS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = {
                            Text(
                                "Ex: Atraso de 15 minutos por conta do trânsito na Paulista, ou aviso sobre feriado...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SlateNavy,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Marcar como Urgente",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isUrgent) AccentRed else SlateNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isUrgent,
                                onCheckedChange = { isUrgent = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AccentRed
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (messageText.isBlank()) {
                                    Toast.makeText(context, "Digite uma mensagem!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.postAnnouncement(messageText, isUrgent)
                                messageText = ""
                                isUrgent = false
                                Toast.makeText(context, "Comunicado enviado aos pais!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlateNavy)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                modifier = Modifier.padding(end = 6.dp),
                                tint = YellowPrimary
                            )
                            Text("ENVIAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "HISTÓRICO DE COMUNICADOS ENVIADOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SlateNavy,
                modifier = Modifier.padding(top = 8.dp)
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
