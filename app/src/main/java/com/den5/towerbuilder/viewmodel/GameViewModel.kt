package com.den5.towerbuilder.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.funny.compose.physics.PhysicsConfig
import com.funny.compose.physics.PhysicsLayoutState
import com.funny.compose.physics.PhysicsParentData
import com.funny.compose.physics.PhysicsShape
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class GameViewModel : ViewModel() {
    var score by mutableStateOf(0)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var boundSize by mutableStateOf(20)
        private set

    private val _blocks = MutableStateFlow<List<PhysicsParentData>>(emptyList())
    val blocks: StateFlow<List<PhysicsParentData>> = _blocks

    fun addBlock() {
        val newBlock = PhysicsParentData(
            physicsConfig = PhysicsConfig(shape = if (Random.nextBoolean()) PhysicsShape.RECTANGLE else PhysicsShape.CIRCLE),
            initialX = 0f,
            initialY = 1f
        )
        _blocks.value = _blocks.value + newBlock
        score++
        checkGameOver()
    }

    private fun checkGameOver() {
        isGameOver = _blocks.value.any { it.body?.position?.y ?: 0f > 1000f } // Example height check
    }

    fun resetGame() {
        score = 0
        isGameOver = false
        _blocks.value = emptyList()
    }

    fun giveRandomImpulse(physicsState: PhysicsLayoutState) {
        physicsState.giveRandomImpulse()
    }

    fun setRandomGravity(physicsState: PhysicsLayoutState) {
        physicsState.setGravity(
            Random.nextDouble(-5.0, 5.0).toFloat(),
            Random.nextDouble(-9.8, 9.8).toFloat()
        )
    }

    fun setRandomBoundSize() {
        boundSize = Random.nextInt(5, 20)
    }
}