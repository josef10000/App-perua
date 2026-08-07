package com.example.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.PixCard
import com.example.ui.theme.SlateNavy

@Composable
fun ParentPaymentScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val paymentInfo by viewModel.paymentInfo.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PAGAMENTO MENSAL DA PERUA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SlateNavy
        )
        Text(
            text = "Copie a chave Pix abaixo e efetue o pagamento no app do seu banco.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        PixCard(paymentInfo = paymentInfo)

        Spacer(modifier = Modifier.height(24.dp))
    }
}
