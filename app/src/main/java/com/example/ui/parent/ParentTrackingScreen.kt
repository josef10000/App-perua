package com.example.ui.parent

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.LiveMapView
import com.example.ui.theme.OnPrimaryContainerDark
import com.example.ui.theme.PrimaryContainerLavender
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryMint
import com.example.ui.theme.SecondaryMintContainer
import com.example.ui.theme.SurfaceVariantLight

@Composable
fun ParentTrackingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val routeState by viewModel.routeState.collectAsState()
    val studentStops by viewModel.studentStops.collectAsState()
    val bindingState by viewModel.parentBindingState.collectAsState()

    var codeInput by remember { mutableStateOf(bindingState.boundInviteCode) }

    val nextStop = studentStops.firstOrNull { it.isCurrentTarget } ?: studentStops.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Student Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Vinculado",
                                tint = SecondaryMintContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PERUA VINCULADA: ${bindingState.boundInviteCode}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryMintContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${bindingState.studentName} • ${bindingState.studentGrade}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = bindingState.boundDriverName,
                            fontSize = 12.sp,
                            color = PrimaryContainerLavender
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerLavender),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "Perua",
                            tint = OnPrimaryContainerDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Onboarding / Code Binding Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VINCULAR NOVA PERUA ESCOLAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Digite o código fornecido pelo motorista no WhatsApp para sincronizar o rastreamento.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { codeInput = it.uppercase() },
                            placeholder = { Text("Ex: PERUA-TIO-CARLOS") },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val success = viewModel.bindParentToVanCode(codeInput)
                                if (success) {
                                    Toast.makeText(context, "Perua vinculada com sucesso!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Código inválido. Tente: PERUA-TIO-CARLOS", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = "Vincular", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VINCULAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Interactive Map View
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOCALIZAÇÃO DA PERUA AO VIVO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (routeState.isActive) SecondaryMintContainer else Color.LightGray.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (routeState.isActive) "AO VIVO" else "OFFLINE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (routeState.isActive) SecondaryMint else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LiveMapView(
                    routeState = routeState,
                    studentStops = studentStops,
                    modifier = Modifier.height(300.dp)
                )
            }
        }

        // ETA & Next Stop Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "ETA",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (routeState.isActive) "ESTIMATIVA DE CHEGADA (ETA)" else "STATUS DO TRANSPORTE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = if (routeState.isActive) "Aproximadamente 8 a 12 minutos" else "Motorista ainda não iniciou a rota",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainerDark
                            )
                        }
                    }

                    if (nextStop != null && routeState.isActive) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryContainerLavender.copy(alpha = 0.4f))
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PinDrop,
                                contentDescription = "Parada",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Próxima parada:",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${nextStop.studentName} • ${nextStop.address}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnPrimaryContainerDark
                                )
                            }
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
