package com.example.dicerollercw

import android.util.Log

class Player {
    var totalScore: Int = 0

    /**
    Takes a list of integers representing the values of rolled dice, sums them up,
    and adds the resulting score to the total score of the player object that it's called on.
    parameters: A mutable list of integers representing the values of rolled dice.
     */
    fun addScore (diceValues : MutableList<Int>)
    {
        var totalDieScore = 0
        totalDieScore = diceValues.sum()
        this.totalScore += totalDieScore
    }

}