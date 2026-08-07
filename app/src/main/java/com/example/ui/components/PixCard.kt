package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PaymentInfo
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun PixCard(
    paymentInfo: PaymentInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCFBF1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "pix",
                            color = Color(0xFF0F766E),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pagamento via Pix",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SlateNavy
                        )
                        Text(
                            text = "Mensalidade do Transporte Escolar",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = paymentInfo.pixKeyType,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fee Display Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VALOR DA MENSALIDADE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "R$ %.2f", paymentInfo.monthlyFee),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = SlateNavy
                        )
                    }

                    // QR Code Graphic Mockup
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(42.dp)) {
                            val w = size.width
                            val h = size.height
                            drawRect(Color.Black, topLeft = Offset(0f, 0f), size = Size(w * 0.35f, h * 0.35f))
                            drawRect(Color.Black, topLeft = Offset(w * 0.65f, 0f), size = Size(w * 0.35f, h * 0.35f))
                            drawRect(Color.Black, topLeft = Offset(0f, h * 0.65f), size = Size(w * 0.35f, h * 0.35f))
                            drawRect(Color.DarkGray, topLeft = Offset(w * 0.4f, h * 0.4f), size = Size(w * 0.25f, h * 0.25f))
                            drawRect(Color.Black, topLeft = Offset(w * 0.7f, h * 0.7f), size = Size(w * 0.3f, h * 0.3f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pix Key Container
            Column {
                Text(
                    text = "CHAVE PIX DO MOTORISTA (${paymentInfo.driverName}):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = paymentInfo.pixKey,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = SlateNavy,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copy Action Button
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Chave Pix", paymentInfo.pixKey)
                    clipboard.setPrimaryClip(clip)

                    isCopied = true
                    Toast.makeText(context, "Chave Pix copiada com sucesso!", Toast.LENGTH_SHORT).show()

                    scope.launch {
                        delay(2500)
                        isCopied = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCopied) AccentGreen else SlateNavy
                )
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                    contentDescription = "Copiar Chave Pix",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCopied) "CHAVE COPIADA!" else "COPIAR CHAVE PIX",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
