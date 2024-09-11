package com.den5.towerbuilder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onStartGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onStartGame, modifier = Modifier.padding(8.dp)) {
            Text("Начать игру")
        }
        Button(onClick = onOpenSettings, modifier = Modifier.padding(8.dp)) {
            Text("Настройки")
        }
        Button(onClick = onExit, modifier = Modifier.padding(8.dp)) {
            Text("Выход")
        }
    }
}