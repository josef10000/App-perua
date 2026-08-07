package com.example.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.LiveMapView
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary
import java.util.Locale

@Composable
fun DriverRouteScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val routeState by viewModel.routeState.collectAsState()
    val studentStops by viewModel.studentStops.collectAsState()

    val buttonBgColor by animateColorAsState(
        targetValue = if (routeState.isActive) AccentRed else AccentGreen,
        label = "StartStopBtnColor"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Big intuitive Start/Stop Route Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "PAINEL DE CONTROLE DA ROTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = YellowPrimary
                            )
                            Text(
                                text = viewModel.driverProfile.vanIdentifier,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (routeState.isActive) AccentGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (routeState.isActive) "GPS TRANSMITINDO" else "GPS AGUARDANDO",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (routeState.isActive) AccentGreen else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Giant Start/Stop Action Button
                    Button(
                        onClick = {
                            if (routeState.isActive) {
                                viewModel.stopRoute()
                            } else {
                                viewModel.startRoute()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor)
                    ) {
                        Icon(
                            imageVector = if (routeState.isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (routeState.isActive) "Finalizar Rota" else "Iniciar Rota",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (routeState.isActive) "FINALIZAR ROTA" else "INICIAR ROTA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Live Map Preview
        item {
            Column {
                Text(
                    text = "VISUALIZAÇÃO NO MAPA (TEMPO REAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LiveMapView(
                    routeState = routeState,
                    studentStops = studentStops,
                    modifier = Modifier.height(260.dp)
                )
            }
        }

        // Live Telemetry Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = "Velocidade",
                            tint = YellowPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Velocidade", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = String.format(Locale.getDefault(), "%.0f km/h", routeState.currentSpeedKmH),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = SlateNavy
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Posição",
                            tint = AccentGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Coordenadas", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = String.format(Locale.getDefault(), "%.3f, %.3f", routeState.currentLocation.latitude, routeState.currentLocation.longitude),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SlateNavy
                            )
                        }
                    }
                }
            }
        }

        // Student Stops Checklist
        item {
            Text(
                text = "PONTOS DE EMBARQUE / DESEMBARQUE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SlateNavy,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(studentStops) { stop ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (stop.isCurrentTarget) Color(0xFFFEF3C7) else Color.White
                ),
                border = if (stop.isCurrentTarget) androidx.compose.foundation.BorderStroke(1.5.dp, YellowPrimary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        stop.isDone -> AccentGreen
                                        stop.isCurrentTarget -> YellowPrimary
                                        else -> Color(0xFFE2E8F0)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (stop.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Status",
                                tint = if (stop.isDone || stop.isCurrentTarget) Color.White else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stop.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateNavy
                            )
                            Text(
                                text = stop.address,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (stop.isCurrentTarget) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = YellowPrimary
                        ) {
                            Text(
                                text = "PRÓXIMA PARADA",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateNavy
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
