package com.example.ui.script

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary

@Composable
fun FirebaseArchitectureScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val tabs = listOf("Firestore", "Realtime GPS", "Security Rules", "Push FCM")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YellowPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = SlateNavy
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Arquitetura 100% Firebase",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Firestore, Realtime DB, Auth & Cloud Messaging",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateNavy,
            contentColor = YellowPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = YellowPrimary
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .padding(bottom = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) YellowPrimary else Color.LightGray
                        )
                    }
                )
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> FirestoreSchemaView(context)
                1 -> RealtimeGpsView(context)
                2 -> SecurityRulesView(context)
                3 -> PushFcmView(context)
            }
        }
    }
}

@Composable
private fun FirestoreSchemaView(context: Context) {
    val firestoreJson = """
        // Estutura de Coleções no Cloud Firestore

        // 1. Coleção: drivers
        drivers/{driverId} = {
            "name": "Carlos Eduardo (Tio Carlos)",
            "email": "carlos.van@escola.com",
            "vanIdentifier": "Perua Amarela #102 - Mercedes Sprinter",
            "pixKey": "12345678900",
            "activeRouteId": "route_active_102",
            "createdAt": FieldValue.serverTimestamp()
        }

        // 2. Coleção: students
        students/{studentId} = {
            "name": "Lucas Silva",
            "parentId": "parent_501",
            "driverId": "driver_102",
            "schoolName": "Escola Colégio Futuro",
            "address": "Rua das Flores, 123",
            "pickupTime": "06:45",
            "status": "BOARDED" // NOT_PICKED, BOARDED, DROPPED_OFF
        }

        // 3. Coleção: announcements
        announcements/{announcementId} = {
            "driverId": "driver_102",
            "driverName": "Tio Carlos",
            "message": "Trânsito intenso na Av. Principal. Atraso estimado de 10 minutos.",
            "timestamp": FieldValue.serverTimestamp()
        }
    """.trimIndent()

    CodeViewerCard(
        title = "Modelagem de Coleções no Cloud Firestore",
        icon = Icons.Default.Storage,
        codeContent = firestoreJson,
        context = context
    )
}

@Composable
private fun RealtimeGpsView(context: Context) {
    val realtimeJson = """
        // Nó do Realtime Database para Transmissão de GPS em Tempo Real
        // Path: /active_routes/{driverId}/location

        {
          "active_routes": {
            "driver_102": {
              "latitude": -23.550520,
              "longitude": -46.633308,
              "speedKmh": 42.5,
              "bearing": 180.0,
              "lastUpdated": 1723023600000,
              "status": "IN_PROGRESS"
            }
          }
        }
    """.trimIndent()

    CodeViewerCard(
        title = "Streaming de Posição GPS via Realtime Database",
        icon = Icons.Default.LocalShipping,
        codeContent = realtimeJson,
        context = context
    )
}

@Composable
private fun SecurityRulesView(context: Context) {
    val rulesContent = """
        // Regras de Segurança do Firebase (firestore.rules)
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {

            // Apenas motoristas autenticados podem publicar comunicados
            match /announcements/{announcementId} {
              allow read: if request.auth != null;
              allow write: if request.auth != null && request.auth.token.role == 'DRIVER';
            }

            // Pais só acessam os dados de seus próprios filhos
            match /students/{studentId} {
              allow read, write: if request.auth != null && 
                (resource.data.parentId == request.auth.uid || request.auth.token.role == 'DRIVER');
            }
          }
        }
    """.trimIndent()

    CodeViewerCard(
        title = "Regras de Segurança (Firestore & Realtime)",
        icon = Icons.Default.Security,
        codeContent = rulesContent,
        context = context
    )
}

@Composable
private fun PushFcmView(context: Context) {
    val fcmContent = """
        // Configuração do Firebase Cloud Messaging (FCM)

        // Inscrição em Tópicos por Rota Escolar
        FirebaseMessaging.getInstance().subscribeToTopic("van_driver_102")

        // Payload de Notificação de Aproximação
        {
          "to": "/topics/van_driver_102",
          "notification": {
            "title": "🚌 Perua Chegando!",
            "body": "Tio Carlos está a 500m da sua residência."
          },
          "data": {
            "routeId": "route_active_102",
            "type": "PROXIMITY_ALERT"
          }
        }
    """.trimIndent()

    CodeViewerCard(
        title = "Alertas e Notificações Push com Firebase FCM",
        icon = Icons.Default.Notifications,
        codeContent = fcmContent,
        context = context
    )
}

@Composable
private fun CodeViewerCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    codeContent: String,
    context: Context
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = YellowPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Firebase Schema", codeContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = SlateNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar", color = SlateNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = codeContent,
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
