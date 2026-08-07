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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.PixCard
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary
import java.util.Locale

@Composable
fun DriverPixSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paymentInfo by viewModel.paymentInfo.collectAsState()

    var pixKeyInput by remember(paymentInfo) { mutableStateOf(paymentInfo.pixKey) }
    var selectedPixType by remember(paymentInfo) { mutableStateOf(paymentInfo.pixKeyType) }
    var feeInput by remember(paymentInfo) { mutableStateOf(String.format(Locale.US, "%.2f", paymentInfo.monthlyFee)) }

    val pixKeyTypes = listOf("E-mail", "CPF", "Telefone", "Chave Aleatória")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CONFIGURAR COBRANÇA PIX",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateNavy
                )
                Text(
                    text = "Essas informações serão exibidas na tela dos pais para pagamento da mensalidade.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TIPO DE CHAVE PIX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pixKeyTypes.take(2).forEach { type ->
                        FilterChip(
                            selected = selectedPixType == type,
                            onClick = { selectedPixType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SlateNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pixKeyTypes.drop(2).forEach { type ->
                        FilterChip(
                            selected = selectedPixType == type,
                            onClick = { selectedPixType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SlateNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CHAVE PIX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = pixKeyInput,
                    onValueChange = { pixKeyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Digite sua chave Pix") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlateNavy,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "VALOR DA MENSALIDADE (R$)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = feeInput,
                    onValueChange = { feeInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: 380.00") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlateNavy,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val feeDouble = feeInput.replace(",", ".").toDoubleOrNull() ?: 380.00
                        viewModel.updatePaymentInfo(pixKeyInput, selectedPixType, feeDouble)
                        Toast.makeText(context, "Dados de pagamento salvos com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateNavy)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Salvar",
                        tint = YellowPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Live Preview of Pix Card
        Text(
            text = "PRÉ-VISUALIZAÇÃO (COMO OS PAIS VERÃO):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SlateNavy,
            modifier = Modifier.padding(top = 8.dp)
        )

        PixCard(paymentInfo = paymentInfo)

        Spacer(modifier = Modifier.height(24.dp))
    }
}
