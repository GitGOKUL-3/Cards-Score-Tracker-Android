package com.cardscoretracker.pro.model

data class Player(
    val id: Int = 0,
    val name: String = "",
    var totalScore: Int = 0,
    var chances: Int = 0,
    var isLoser: Boolean = false
)
