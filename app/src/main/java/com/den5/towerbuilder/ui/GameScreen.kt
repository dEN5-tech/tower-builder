package com.den5.towerbuilder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.den5.towerbuilder.viewmodel.GameViewModel
import com.funny.compose.physics.PhysicsLayout
import com.funny.compose.physics.PhysicsLayoutState
import com.funny.compose.physics.PhysicsConfig

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onGameOver: (Int) -> Unit
) {
    val physicsState = remember { PhysicsLayoutState() }
    val blocks by gameViewModel.blocks.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Score: ${gameViewModel.score}",
            modifier = Modifier.padding(16.dp)
        )

        PhysicsLayout(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            physicsLayoutState = physicsState,
            boundSize = gameViewModel.boundSize.toFloat()
        ) {
            LazyColumn {
                items(blocks) { block ->
                    RandomColorBox(
                        modifier = Modifier
                            .size(50.dp)
                            .physics(block.physicsConfig, initialY = 300f),
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                gameViewModel.addBlock()
                if (gameViewModel.isGameOver) {
                    onGameOver(gameViewModel.score)
                }
            }) {
                Text("Add Block")
            }
            Button(onClick = { gameViewModel.giveRandomImpulse(physicsState) }) {
                Text("Random Impulse")
            }
            Button(onClick = { gameViewModel.setRandomGravity(physicsState) }) {
                Text("Random Gravity")
            }
            Button(onClick = { gameViewModel.setRandomBoundSize() }) {
                Text("Random Bound Size")
            }
        }
    }
}