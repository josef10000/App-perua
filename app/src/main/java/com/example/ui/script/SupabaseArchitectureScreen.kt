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
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.YellowPrimary

@Composable
fun SupabaseArchitectureScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val sqlScript = """
-- 1. EXTENSÕES & TABELAS BASE SUPABASE
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de Usuários (Motoristas e Pais)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('driver', 'parent')),
    name TEXT NOT NULL,
    van_identifier TEXT DEFAULT 'Perua #102',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabela de Rotas do Motorista
CREATE TABLE routes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES users(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE
);

-- Tabela de Logs de Localização GPS em Tempo Real
CREATE TABLE location_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id UUID REFERENCES routes(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    heading REAL DEFAULT 0,
    speed REAL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabela do Mural de Comunicados
CREATE TABLE announcements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_urgent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabela de Configurações de Pagamento Pix
CREATE TABLE payments_info (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    pix_key TEXT NOT NULL,
    pix_key_type TEXT NOT NULL,
    monthly_fee NUMERIC(10,2) NOT NULL DEFAULT 380.00,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. ROTAS E RLS (ROW LEVEL SECURITY)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE routes ENABLE ROW LEVEL SECURITY;
ALTER TABLE location_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments_info ENABLE ROW LEVEL SECURITY;

-- Políticas de Leitura Pública/Autenticada para o MVP
CREATE POLICY "Leitura autenticada para todos os usuarios" ON users FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Leitura de rotas ativas" ON routes FOR SELECT USING (true);
CREATE POLICY "Leitura de localizacao em tempo real" ON location_logs FOR SELECT USING (true);
CREATE POLICY "Leitura de comunicados" ON announcements FOR SELECT USING (true);
CREATE POLICY "Leitura de informacoes Pix" ON payments_info FOR SELECT USING (true);

-- Escrita restrita ao motorista
CREATE POLICY "Motorista atualiza propria rota" ON routes FOR ALL USING (auth.uid() = driver_id);
CREATE POLICY "Motorista insere logs de GPS" ON location_logs FOR INSERT WITH CHECK (true);
CREATE POLICY "Motorista posta comunicados" ON announcements FOR INSERT WITH CHECK (auth.uid() = driver_id);

-- 3. HABILITAR SUPABASE REALTIME REPLICATION NA TABELA LOCATION_LOGS
ALTER PUBLICATION supabase_realtime ADD TABLE location_logs;
    """.trimIndent()

    val hookScript = """
// src/hooks/useBackgroundLocation.ts
import { useEffect, useState } from 'react';
import * as Location from 'expo-location';
import * as TaskManager from 'expo-task-manager';
import { supabase } from '../lib/supabase';

const LOCATION_TASK_NAME = 'BACKGROUND_VAN_LOCATION_TASK';

TaskManager.defineTask(LOCATION_TASK_NAME, async ({ data, error }) => {
  if (error) {
    console.error('Erro na task de localização em segundo plano:', error);
    return;
  }
  if (data) {
    const { locations } = data as { locations: Location.LocationObject[] };
    const location = locations[0];
    if (location) {
      const { latitude, longitude, heading, speed } = location.coords;

      // Envia coordenadas diretamente ao Supabase Realtime
      await supabase.from('location_logs').insert([
        {
          latitude,
          longitude,
          heading: heading || 0,
          speed: (speed || 0) * 3.6, // m/s para km/h
          updated_at: new Date().toISOString(),
        },
      ]);
    }
  }
});

export const useBackgroundLocation = (routeId: string | null, isActive: boolean) => {
  const [permissionGranted, setPermissionGranted] = useState(false);

  useEffect(() => {
    (async () => {
      const { status: fgStatus } = await Location.requestForegroundPermissionsAsync();
      const { status: bgStatus } = await Location.requestBackgroundPermissionsAsync();

      if (fgStatus === 'granted' && bgStatus === 'granted') {
        setPermissionGranted(true);
      }
    })();
  }, []);

  useEffect(() => {
    if (isActive && permissionGranted && routeId) {
      Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
        accuracy: Location.Accuracy.High,
        timeInterval: 5000, // 5 segundos
        distanceInterval: 5, // 5 metros
        showsBackgroundLocationIndicator: true,
        foregroundService: {
          notificationTitle: 'Perua Escolar em Rota',
          notificationBody: 'Transmitindo localização para os pais...',
        },
      });
    } else {
      Location.hasStartedLocationUpdatesAsync(LOCATION_TASK_NAME).then((started) => {
        if (started) {
          Location.stopLocationUpdatesAsync(LOCATION_TASK_NAME);
        }
      });
    }
  }, [isActive, permissionGranted, routeId]);

  return { permissionGranted };
};
    """.trimIndent()

    val mapScript = """
// src/screens/ParentMapScreen.tsx
import React, { useEffect, useState } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import MapView, { Marker, PROVIDER_DEFAULT } from 'react-native-maps';
import { supabase } from '../lib/supabase';

interface VanLocation {
  latitude: number;
  longitude: number;
  heading: number;
  speed: number;
}

export const ParentMapScreen = () => {
  const [vanLocation, setVanLocation] = useState<VanLocation | null>({
    latitude: -23.5617,
    longitude: -46.6560,
    heading: 45,
    speed: 32,
  });

  useEffect(() => {
    // Inscreve no canal Supabase Realtime para escutar inserts na location_logs
    const channel = supabase
      .channel('realtime_van_tracker')
      .on(
        'postgres_changes',
        { event: 'INSERT', schema: 'public', table: 'location_logs' },
        (payload) => {
          const newLoc = payload.new;
          setVanLocation({
            latitude: newLoc.latitude,
            longitude: newLoc.longitude,
            heading: newLoc.heading,
            speed: newLoc.speed,
          });
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, []);

  return (
    <View style={styles.container}>
      {vanLocation && (
        <MapView
          style={styles.map}
          provider={PROVIDER_DEFAULT}
          initialRegion={{
            latitude: vanLocation.latitude,
            longitude: vanLocation.longitude,
            latitudeDelta: 0.01,
            longitudeDelta: 0.01,
          }}
        >
          <Marker
            coordinate={{
              latitude: vanLocation.latitude,
              longitude: vanLocation.longitude,
            }}
            title="Perua do Tio Carlos"
            description={"Velocidade: " + Math.round(vanLocation.speed) + " km/h"}
            rotation={vanLocation.heading}
            flat={true}
          />
        </MapView>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  map: { width: '100%', height: '100%' },
});
    """.trimIndent()

    val currentText = when (selectedTab) {
        0 -> sqlScript
        1 -> hookScript
        else -> mapScript
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
            colors = CardDefaults.cardColors(containerColor = SlateNavy)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Script",
                        tint = YellowPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ARQUITETURA SUPABASE & EXPO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Código SQL RLS, Realtime Replication e Hooks React Native",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = SlateNavy,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SlateNavy,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("1. SQL Supabase", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("2. Hook GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("3. Tela Mapa", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Copy Code Action Button
        Button(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Codigo Arquitetura", currentText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Código copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SlateNavy)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = YellowPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("COPIAR ESTE CÓDIGO", fontWeight = FontWeight.Bold, color = Color.White)
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
                    color = Color(0xFF38BDF8),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
