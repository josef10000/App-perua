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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
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
import com.example.ui.theme.OnPrimaryContainerDark
import com.example.ui.theme.PrimaryContainerLavender
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryMint
import com.example.ui.theme.SlateDark

@Composable
fun FirebaseArchitectureScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val securityRulesScript = """
// ===== 1. FIREBASE FIRESTORE SECURITY RULES (firestore.rules) =====
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    function isDriver() {
      return isAuthenticated() && request.auth.token.role == 'driver';
    }

    // Users Collection
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if request.auth.uid == userId;
    }

    // Van Invites & Vinculation Code (PERUA-TIO-CARLOS)
    match /van_invites/{code} {
      allow read: if isAuthenticated();
      allow write: if isDriver();
    }

    // Announcements Collection (Mural de Avisos)
    match /announcements/{announcementId} {
      allow read: if isAuthenticated();
      allow create, delete: if isDriver();
    }

    // Payment Info
    match /payments_info/{driverId} {
      allow read: if isAuthenticated();
      allow write: if isDriver() && request.auth.uid == driverId;
    }
  }
}

// ===== 2. FIREBASE REALTIME DATABASE SECURITY RULES (database.rules.json) =====
// Utilizado para streaming de GPS com baixa latência (<1s)
{
  "rules": {
    "live_location": {
      "${'$'}route_id": {
        ".read": "auth != null",
        ".write": "auth != null && root.child('users').child(auth.uid).child('role').val() === 'driver'",
        ".indexOn": ["timestamp"]
      }
    }
  }
}

    """.trimIndent()

    val kotlinRepositoryScript = """
// ===== FIREBASE TRANSPORT REPOSITORY (KOTLIN ANDROID) =====
package com.example.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseTransportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val realtimeDb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    // 1. Envia coordenadas GPS em Tempo Real para o Firebase Realtime DB
    fun updateLiveLocation(routeId: String, lat: Double, lng: Double, heading: Float, speed: Float) {
        val locationMap = mapOf(
            "latitude" to lat,
            "longitude" to lng,
            "heading" to heading,
            "speed" to speed,
            "timestamp" to System.currentTimeMillis()
        )
        realtimeDb.getReference("live_location").child(routeId).setValue(locationMap)
    }

    // 2. Escuta a localização ao vivo da Perua no App dos Pais (Flow)
    fun observeLiveLocation(routeId: String): Flow<LocationUpdate?> = callbackFlow {
        val ref = realtimeDb.getReference("live_location").child(routeId)
        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val lat = snapshot.child("latitude").getValue(Double::class.java) ?: return
                val lng = snapshot.child("longitude").getValue(Double::class.java) ?: return
                val heading = snapshot.child("heading").getValue(Float::class.java) ?: 0f
                val speed = snapshot.child("speed").getValue(Float::class.java) ?: 0f
                
                trySend(LocationUpdate(lat, lng, heading, speed))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    // 3. Posta Comunicado no Firestore
    fun postAnnouncement(driverId: String, driverName: String, message: String, isUrgent: Boolean) {
        val ann = mapOf(
            "driverId" to driverId,
            "driverName" to driverName,
            "message" to message,
            "isUrgent" to isUrgent,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("announcements").add(ann)
    }
}

data class LocationUpdate(val latitude: Double, val longitude: Double, val heading: Float, val speed: Float)
    """.trimIndent()

    val fcmMessagingScript = """
// ===== FIREBASE CLOUD MESSAGING SERVICE (MyFirebaseMessagingService.kt) =====
package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Rota Escolar"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "A perua está próxima!"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "school_van_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas da Perua Escolar",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        // Envia o novo token FCM ao Firestore do perfil do usuário
        println("FCM Device Token: ${'$'}token")
    }

}
    """.trimIndent()

    val currentText = when (selectedTab) {
        0 -> securityRulesScript
        1 -> kotlinRepositoryScript
        else -> fcmMessagingScript
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Firebase",
                        tint = Color(0xFFFFCA28),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ARQUITETURA 100% FIREBASE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Firestore, Realtime Database GPS, Rules & FCM Push",
                            fontSize = 11.sp,
                            color = PrimaryContainerLavender
                        )
                    }
                }
            }
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PrimaryPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryPurple,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("1. Security Rules", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("2. Kotlin Repository", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("3. FCM Messaging", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Copy Code Action Button
        Button(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Codigo Firebase", currentText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Código copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("COPIAR CÓDIGO FIREBASE", fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Code Viewer Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SlateDark)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Text(
                    text = currentText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFFFE082),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

