package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthManager
import com.example.data.models.UserRole
import com.example.ui.auth.AuthScreen
import com.example.ui.driver.DriverAnnouncementsScreen
import com.example.ui.driver.DriverPixSettingsScreen
import com.example.ui.driver.DriverRouteScreen
import com.example.ui.parent.ParentAnnouncementsScreen
import com.example.ui.parent.ParentPaymentScreen
import com.example.ui.parent.ParentTrackingScreen
import com.example.ui.script.FirebaseArchitectureScreen
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val routeIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val authManager = remember { FirebaseAuthManager.getInstance() }
    val currentUser by authManager.currentUserState.collectAsState()
    val userProfile by authManager.userProfileState.collectAsState()
    val isAuthReady by authManager.isAuthReady.collectAsState()
    var isAuthenticated by remember { mutableStateOf(false) }

    // Atualiza isAuthenticated quando o perfil do usuário muda
    if (userProfile != null) {
        isAuthenticated = true
    }
    if (currentUser == null && userProfile == null && isAuthReady) {
        isAuthenticated = false
    }

    // Splash de carregamento enquanto o Firebase inicializa
    if (!isAuthReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateNavy),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "Logo",
                    tint = YellowPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rota Escolar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.CircularProgressIndicator(color = YellowPrimary)
            }
        }
        return
    }

    // Se não autenticado, exibir a tela de Login/Cadastro
    if (!isAuthenticated) {
        AuthScreen(onAuthSuccess = { isAuthenticated = true })
        return
    }

    val activeRole = userProfile?.role ?: UserRole.DRIVER
    var selectedDriverTab by remember { mutableIntStateOf(0) }
    var selectedParentTab by remember { mutableIntStateOf(0) }

    val driverNavItems = listOf(
        NavItem("Rota GPS", Icons.Default.DirectionsBus, 0),
        NavItem("Comunicados", Icons.Default.Campaign, 1),
        NavItem("Pix", Icons.Default.QrCode, 2)
    )

    val parentNavItems = listOf(
        NavItem("Mapa Vivo", Icons.Default.Map, 0),
        NavItem("Mural", Icons.Default.Notifications, 1),
        NavItem("Pagamento", Icons.Default.Receipt, 2)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(YellowPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = "Logo",
                                    tint = SlateNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Rota Escolar",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (activeRole == UserRole.DRIVER) "Perfil: Motorista" else "Perfil: Responsável",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { FirebaseAuthManager.getInstance().logout() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sair",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateNavy)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateNavy,
                tonalElevation = 8.dp
            ) {
                val navItems = if (activeRole == UserRole.DRIVER) driverNavItems else parentNavItems
                val currentTab = if (activeRole == UserRole.DRIVER) selectedDriverTab else selectedParentTab

                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentTab == item.routeIndex,
                        onClick = {
                            if (currentRole == UserRole.DRIVER) {
                                selectedDriverTab = item.routeIndex
                            } else {
                                selectedParentTab = item.routeIndex
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (currentTab == item.routeIndex) SlateNavy else Color.LightGray
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == item.routeIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentTab == item.routeIndex) YellowPrimary else Color.LightGray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = YellowPrimary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = Pair(activeRole, if (activeRole == UserRole.DRIVER) selectedDriverTab else selectedParentTab),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { (role, tab) ->
                when {
                    role == UserRole.DRIVER && tab == 0 -> DriverRouteScreen(viewModel = viewModel)
                    role == UserRole.DRIVER && tab == 1 -> DriverAnnouncementsScreen(viewModel = viewModel)
                    role == UserRole.DRIVER && tab == 2 -> DriverPixSettingsScreen(viewModel = viewModel)

                    role == UserRole.PARENT && tab == 0 -> ParentTrackingScreen(viewModel = viewModel)
                    role == UserRole.PARENT && tab == 1 -> ParentAnnouncementsScreen(viewModel = viewModel)
                    role == UserRole.PARENT && tab == 2 -> ParentPaymentScreen(viewModel = viewModel)
                }
            }
        }
    }
}
