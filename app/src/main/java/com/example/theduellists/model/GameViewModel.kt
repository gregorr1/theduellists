package com.example.theduellists.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random

const val GREEN = "Green"
const val BLUE = "Blue"
const val RED = "Red"

class GameViewModel : ViewModel() {
    private val _playerHP = MutableLiveData(100)
    val playerHP: LiveData<Int>
        get() = _playerHP

    private val _opponentHP = MutableLiveData(100)
    val opponentHP: LiveData<Int>
        get() = _opponentHP

    private var _attackState = true
    val attackState: Boolean
        get() = _attackState

    private var _turn = 0
    val turn: Int
        get() = _turn

    private val random = Random(System.currentTimeMillis())

    private var _opponentRoll = (1..3).random(random)
    val opponentRoll: Int
        get() = _opponentRoll

    private var _damage = 0
    val damage: Int
        get() = _damage

    private var _selectedOption = MutableLiveData(GREEN)
    val selectedOption: LiveData<String> = _selectedOption

    fun onFightClicked() {
        _turn++
        if(_attackState) {
            playerAttack()
        } else {
            opponentAttack()
        }
    }

    fun setOption(getOption: () -> String) {
        _selectedOption.value = getOption()
    }

    fun changeStatus() {
        _attackState = !_attackState
    }

    fun reinitializeData() {
        _playerHP.value = 100
        _opponentHP.value = 100
        _turn = 0
        _attackState = true
    }

    private fun randomizeRoll() {
        _opponentRoll = (1..3).random(random)
    }

    private fun playerAttack() {
        randomizeRoll()
        when (_selectedOption.value) {
            GREEN -> swordAttack(true)
            BLUE -> arrowAttack(true)
            RED -> fireballAttack(true)
        }
    }

    private fun opponentAttack() {
        randomizeRoll()
        when (_opponentRoll) {
            1 -> swordAttack(false)
            2 -> arrowAttack(false)
            3 -> fireballAttack(false)
        }
    }

    private fun swordAttack(playerAttacker: Boolean) {
        if (playerAttacker) {
            when (_opponentRoll) {
                1 -> _damage = 3
                2 -> _damage = 6
                3 -> _damage = 10
            }
            _opponentHP.value = _opponentHP.value?.minus(damage)
        } else {
            when (_selectedOption.value) {
                GREEN -> _damage = 3
                BLUE -> _damage = 6
                RED -> _damage = 10
            }
            _playerHP.value = _playerHP.value?.minus(damage)
        }
    }

    private fun arrowAttack(playerAttacker: Boolean) {
        if (playerAttacker) {
            when (_opponentRoll) {
                1 -> _damage = 8
                2 -> _damage = 0
                3 -> _damage = 12
            }
            _opponentHP.value = _opponentHP.value?.minus(damage)
        } else {
            when (_selectedOption.value) {
                GREEN -> _damage = 8
                BLUE -> _damage = 0
                RED -> _damage = 12
            }
            _playerHP.value = _playerHP.value?.minus(damage)
        }
    }

    private fun fireballAttack(playerAttacker: Boolean) {
        if (playerAttacker) {
            when (_opponentRoll) {
                1 -> _damage = 10
                2 -> _damage = 14
                3 -> _damage = 10
            }
            if (_opponentRoll == 3) {
                _playerHP.value = _playerHP.value?.minus(damage)
            } else {
                _opponentHP.value = _opponentHP.value?.minus(damage)
            }
        } else {
            when (_selectedOption.value) {
                GREEN -> _damage = 10
                BLUE -> _damage = 14
                RED -> _damage = 10
            }
            if (_selectedOption.value == RED) {
                _opponentHP.value = _opponentHP.value?.minus(damage)
            } else {
                _playerHP.value = _playerHP.value?.minus(damage)
            }
        }
    }
}