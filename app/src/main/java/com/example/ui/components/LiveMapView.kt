package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RouteState
import com.example.data.models.StudentStop
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary
import java.util.Locale

@Composable
fun LiveMapView(
    routeState: RouteState,
    studentStops: List<StudentStop>,
    modifier: Modifier = Modifier,
    onRecenter: () -> Unit = {}
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Pulse animation for active route marker
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
    ) {
        // Map Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background Map Land Texture
            drawRect(color = Color(0xFFE2E8F0))

            // Green Parks / School Campus area
            drawRoundRect(
                color = Color(0xFFDCFCE7),
                topLeft = Offset(width * 0.55f, height * 0.58f),
                size = androidx.compose.ui.geometry.Size(width * 0.4f, height * 0.35f),
                cornerRadius = CornerRadius(16.dp.toPx())
            )

            // Street Grid Layout
            val gridPaint = Stroke(
                width = 16.dp.toPx() * zoomScale,
                pathEffect = PathEffect.cornerPathEffect(12f)
            )
            val streetColor = Color(0xFFFFFFFF)

            // Main Avenues
            val path = Path().apply {
                // Ave Paulista diagonal
                moveTo(0f, height * 0.3f)
                lineTo(width, height * 0.7f)

                // Ave Augusta cross street
                moveTo(width * 0.35f, 0f)
                lineTo(width * 0.65f, height)

                // Secondary streets
                moveTo(0f, height * 0.6f)
                lineTo(width * 0.8f, height * 0.9f)

                moveTo(width * 0.2f, height * 0.15f)
                lineTo(width * 0.9f, height * 0.15f)
            }
            drawPath(path = path, color = streetColor, style = gridPaint)

            // Street centerline accents
            val dashPath = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
            val centerLinePaint = Stroke(width = 2.dp.toPx(), pathEffect = dashPath)
            drawPath(path = path, color = Color(0xFFCBD5E1), style = centerLinePaint)

            // Map Coordinates Projection Reference
            // Map boundaries centered around São Paulo Jardins route (-23.56 to -23.575, -46.66 to -46.64)
            val minLat = -23.575
            val maxLat = -23.560
            val minLng = -46.658
            val maxLng = -46.640

            fun projectToCanvas(lat: Double, lng: Double): Offset {
                val normX = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.1, 0.9)
                val normY = (1.0 - (lat - minLat) / (maxLat - minLat)).coerceIn(0.1, 0.9)
                return Offset(
                    x = (normX * width * zoomScale).toFloat() + (width * (1 - zoomScale) / 2),
                    y = (normY * height * zoomScale).toFloat() + (height * (1 - zoomScale) / 2)
                )
            }

            // Draw Student Stop Pins
            studentStops.forEach { stop ->
                val pos = projectToCanvas(stop.latitude, stop.longitude)
                val isSchool = stop.studentName.contains("Escola", ignoreCase = true) || stop.studentName.contains("Colégio", ignoreCase = true)

                val stopColor = when {
                    isSchool -> Color(0xFF9333EA) // Purple for School
                    stop.isDone -> AccentGreen
                    stop.isCurrentTarget -> YellowPrimary
                    else -> SlateNavy
                }

                // Stop circle shadow & base
                drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = 14.dp.toPx(), center = pos + Offset(0f, 4f))
                drawCircle(color = Color.White, radius = 12.dp.toPx(), center = pos)
                drawCircle(color = stopColor, radius = 9.dp.toPx(), center = pos)

                if (stop.isCurrentTarget) {
                    drawCircle(color = YellowPrimary.copy(alpha = 0.4f), radius = 18.dp.toPx(), center = pos, style = Stroke(width = 3.dp.toPx()))
                }
            }

            // Draw Active Van Position
            val vanLat = routeState.currentLocation.latitude
            val vanLng = routeState.currentLocation.longitude
            val vanPos = projectToCanvas(vanLat, vanLng)

            if (routeState.isActive) {
                // Pulse Ring
                drawCircle(
                    color = YellowPrimary.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = vanPos
                )
            }

            // Van Base Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 18.dp.toPx(),
                center = vanPos + Offset(0f, 5f)
            )

            // Outer Circle Ring
            drawCircle(
                color = Color.White,
                radius = 18.dp.toPx(),
                center = vanPos
            )

            // Van Body Color
            drawCircle(
                color = if (routeState.isActive) YellowPrimary else Color(0xFF64748B),
                radius = 14.dp.toPx(),
                center = vanPos
            )

            // Heading direction arrow on map
            rotate(degrees = routeState.headingDegrees, pivot = vanPos) {
                val arrowPath = Path().apply {
                    moveTo(vanPos.x, vanPos.y - 10.dp.toPx())
                    lineTo(vanPos.x - 6.dp.toPx(), vanPos.y + 6.dp.toPx())
                    lineTo(vanPos.x, vanPos.y + 2.dp.toPx())
                    lineTo(vanPos.x + 6.dp.toPx(), vanPos.y + 6.dp.toPx())
                    close()
                }
                drawPath(arrowPath, color = SlateNavy)
            }
        }

        // Overlay Status Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
            shape = RoundedCornerShape(30.dp),
            color = if (routeState.isActive) SlateNavy else Color(0xFF475569),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (routeState.isActive) AccentGreen else AccentRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (routeState.isActive) "ROTA ATIVA • EM RASTREAMENTO" else "PERUA PARADA • FORA DE SERVIÇO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Telemetry & Control Floating Bar (Bottom)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Speed & Heading Telemetry Pill
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavy.copy(alpha = 0.92f)),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Direção",
                        tint = YellowPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(routeState.headingDegrees)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f km/h", routeState.currentSpeedKmH),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "Lat: %.4f", routeState.currentLocation.latitude),
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Map Controls (Zoom / Recenter)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    IconButton(
                        onClick = { if (zoomScale < 1.8f) zoomScale += 0.2f },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = SlateNavy)
                    }
                    IconButton(
                        onClick = { if (zoomScale > 0.6f) zoomScale -= 0.2f },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = SlateNavy)
                    }
                    IconButton(
                        onClick = {
                            zoomScale = 1.0f
                            onRecenter()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Recentralizar", tint = AccentBlue)
                    }
                }
            }
        }
    }
}
