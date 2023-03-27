/**
 * Computer Player Strategy...
 *
 *This strategy considers 2 aspects. When to re-roll and which dice to re-roll. Since the computer
 *player cannot see the human player's current dice these decisions are based on targeting to get
 *the maximum value possible from a throw.
 *
 * 1. When to re-roll:
 *      The computer should re-roll if the current dice contains 1s, 2s, or 3s, aiming to get rid of
 * small values. Targeting minimum score is 20.(4*5) which is above the average score of a 5 dice.
 * ((1+2+3+4+5+6)/6)5 = 17.5
 *
 * 2. Which dice to re-roll:
 *      For each die, computer checks its value and re-roll if the value is 1, 2 or 3
 *
 * To optimize the strategy further, the computer should take into account the current game score
 * and the number of re-rolls remaining. If it's close to the target score or needs a high score
 * to catch up with the human player, it should be more aggressive in re-rolling.
 * Conversely, if it has a significant lead, it should be more conservative and keep dice with higher
 * values.
 **/

package com.example.dicerollercw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.random.Random


class NewGame : AppCompatActivity() {
    private lateinit var humanDiceViews: Array<ImageView>
    private lateinit var computerDiceViews: Array<ImageView>
    private lateinit var throwButton: Button
    private lateinit var scoreButton: Button
    private lateinit var humanScoreTextView: TextView
    private lateinit var rollNumber: TextView
    private lateinit var computerScoreTextView: TextView
    private lateinit var humanWinsTextView: TextView
    private lateinit var comWinsTextView: TextView
    private lateinit var selectMode:ToggleButton

    private var humanPlayer = Player()
    private var computerPlayer = Player()

    private var currentHumanScoresList: MutableList<Int> = mutableListOf()
    private var currentComScoresList: MutableList<Int> = mutableListOf()
    private var currentHumanRoll: MutableList<Int> = mutableListOf()
    private var currentComputerRoll: MutableList<Int> = mutableListOf()
    private var humanNotSelectedImageView: MutableList<ImageView> = mutableListOf()
    private var comNotSelectedImageView: MutableList<ImageView> = mutableListOf()
    private var isHardMode: Boolean = false

    private var winningScore = 101
    private var humanRollCount = 0
    private var computerRollCount = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()            //Hiding the action bar
        setContentView(R.layout.activity_game)

        // Retrieve the total number of wins for the computer and human players from shared preferences.
        // The total number of wins are stored as key-value pairs with keys "COMPUTER-SCORE" and "HUMAN-SCORE", respectively.
        val totalWins = getPreferences(Context.MODE_PRIVATE) ?: return
        findViewById<TextView>(R.id.computer_wins).text = totalWins.getInt("COMPUTER-SCORE", 0).toString()
        findViewById<TextView>(R.id.human_wins).text = totalWins.getInt("HUMAN-SCORE", 0).toString()

        winningScore = intent.getIntExtra(MainActivity.WINNING_SCORE, 0)        //getting the user changed target score as a intent
        findViewById<TextView>(R.id.targetScore).text = "TARGET SCORE: " +winningScore.toString()       //displaying the target score

        // Initialize an array of ImageViews representing the human player's dice.
        humanDiceViews = arrayOf(
            findViewById(R.id.human_dice1),
            findViewById(R.id.human_dice2),
            findViewById(R.id.human_dice3),
            findViewById(R.id.human_dice4),
            findViewById(R.id.human_dice5)
        )
        // Initialize an array of ImageViews representing the human player's dice.
        computerDiceViews = arrayOf(
            findViewById(R.id.computer_dice1),
            findViewById(R.id.computer_dice2),
            findViewById(R.id.computer_dice3),
            findViewById(R.id.computer_dice4),
            findViewById(R.id.computer_dice5)
        )

        selectMode = findViewById(R.id.toggleButton)
        selectMode.setOnCheckedChangeListener { _, isChecked ->        //Set a listener on the selectMode switch to detect when it is checked or unchecked.
            val msg = if (isChecked) "You are on hard mode" else "You are on easy mode"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            isHardMode = true           //The isHardMode variable is set to true when the switch is checked.
            selectMode.isEnabled = false  // Disable selectMode switch to prevent the user from toggling it again.
        }

        // Retrieve the views from the layout file and store them in corresponding variables.
        throwButton = findViewById(R.id.throw_button)
        scoreButton = findViewById(R.id.score_button)
        humanScoreTextView = findViewById(R.id.human_score)
        computerScoreTextView = findViewById(R.id.computer_score)
        rollNumber = findViewById(R.id.rollView)
        humanWinsTextView = findViewById(R.id.human_wins)
        comWinsTextView = findViewById(R.id.computer_wins)

        // Set click listeners to detect when they are clicked.
        throwButton.setOnClickListener {
            onThrowButtonClick()
            comOnThrowButtonClick()
        }

        humanDiceViews[0].setOnClickListener { selectDie(humanDiceViews[0]) }
        humanDiceViews[1].setOnClickListener { selectDie(humanDiceViews[1]) }
        humanDiceViews[2].setOnClickListener { selectDie(humanDiceViews[2]) }
        humanDiceViews[3].setOnClickListener { selectDie(humanDiceViews[3]) }
        humanDiceViews[4].setOnClickListener { selectDie(humanDiceViews[4]) }

        scoreButton.setOnClickListener {
            if (humanRollCount>0){      // If the human player has taken at least one roll in their turn, the other functions are called.
                humanScore()            //to prevent the code from crashing
                computerScore()
//                humanPlayer.totalScore = 41
//                computerPlayer.totalScore= 41
                checkForWinner()
            }

        }
    }

    /** Handles the logic for rolling the dice and updating the game state based on the number of
      rolls the human player has taken.**/
    private fun onThrowButtonClick() {
        when (humanRollCount) {
            // When the human player has taken 0 rolls, all dice are rolled and their faces are displayed.
            0 -> {
                rollNumber.text = "Roll"
                humanNotSelectedImageView.addAll(humanDiceViews)
                comNotSelectedImageView.addAll(computerDiceViews)

                currentHumanRoll = rollDice(humanNotSelectedImageView.size)         //called to roll the unselected dice
                updateDiceViews(humanNotSelectedImageView, currentHumanRoll)        //called to display the current roll on the unselected dice

                humanRollCount++
                humanNotSelectedImageView.clear()
            }

            // When the human player has taken 1 or 2 rolls, only the unselected dice are rolled and displayed.
            1 -> {
                rollNumber.text = "Re-roll 1"
                for (i in humanDiceViews.indices) {
                    if (!humanDiceViews[i].isSelected) {
                        humanNotSelectedImageView.add(humanDiceViews[i])
                    }
                }
                currentHumanRoll = rollDice(humanNotSelectedImageView.size)         //called to roll the unselected dice
                updateDiceViews(humanNotSelectedImageView, currentHumanRoll)        //called to display the current roll on the unselected dice.

                for (i in humanDiceViews.indices) {
                    if (humanDiceViews[i].isSelected) {
                        humanDiceViews[i].setBackgroundColor(Color.TRANSPARENT)
                        humanDiceViews[i].isSelected = !humanDiceViews[i].isSelected
                    }
                }
                humanRollCount++
                humanNotSelectedImageView.clear()
            }

            2 -> {
                rollNumber.text = "Re-roll 2"
                for (i in humanDiceViews.indices) {
                    if (!humanDiceViews[i].isSelected) {
                        humanNotSelectedImageView.add(humanDiceViews[i])
                    }
                }
                currentHumanRoll = rollDice(humanNotSelectedImageView.size)            //called to roll the unselected dice
                updateDiceViews(humanNotSelectedImageView, currentHumanRoll)           //called to display the current roll on the unselected dice

                for (i in humanDiceViews.indices) {
                    if (humanDiceViews[i].isSelected) {
                        humanDiceViews[i].setBackgroundColor(Color.TRANSPARENT)
                        humanDiceViews[i].isSelected = !humanDiceViews[i].isSelected
                    }
                }

                humanNotSelectedImageView.clear()           //clears the humanNotSelectedImageView list
                humanScore()  //After the 2nd roll, the humanScore() function is called to score the current turn and update the game state.
            }
        }
    }

    /** updates the images of the specified dice views to reflect the current roll.
        Parameters: a list of ImageView objects representing the dice views to update,
        and a list of integers representing the current roll.**/
    private fun updateDiceViews(DiceViews: MutableList<ImageView>, currentRoll: MutableList<Int>) {
        //Loops through the dice views and sets their images to the corresponding face of the current roll.
        for (i in 0 until DiceViews.size) {
            DiceViews[i].setImageResource(currentRoll[i])
            DiceViews[i].tag = currentRoll[i]       // The tag property of each ImageView is set to the corresponding integer value for future reference.
        }
    }

    /** Simulates a dice roll by generating a random number between 1 and 6 for each unselected die.
        Parameters: the number of unselected dice to roll.
        MutableList of Ints representing the current roll.**/
    private fun rollDice(numberOfNotSelectedDice: Int): MutableList<Int> {
        var currentRoll: MutableList<Int> = mutableListOf()
        for (i in 0 until numberOfNotSelectedDice) {
            val drawableResource = when ((1..6).random()) {
                //convert the random numbers into drawable resources for each die face.
                1 -> R.drawable.dice_1
                2 -> R.drawable.dice_2
                3 -> R.drawable.dice_3
                4 -> R.drawable.dice_4
                5 -> R.drawable.dice_5
                else -> R.drawable.dice_6
            }
            currentRoll.add(i, drawableResource)        //drawable resources are then added to a MutableList of Ints to represent the current roll.
        }
        return currentRoll
    }

    /**
        Scores the current roll of the human player's dice and updates the human player's total score.
    **/
    private fun humanScore(){
        for (imageView in humanDiceViews) {
            //extracting the integer value of each die face from its corresponding tag property
            val parts = resources.getResourceName((imageView.tag as Int)).toString().split("_")
            currentHumanScoresList.add(parts[1].toInt())        //adding these values to a MutableList of Ints representing the current roll's scores
        }
        humanPlayer.addScore(currentHumanScoresList)            //add the scores to the player's total score.
        humanScoreTextView.text = humanPlayer.totalScore.toString()         //Update the humanScoreTextView to display the new total score.
        currentHumanScoresList.clear()
        humanRollCount = 0

    }

    /**
        Scores the current roll of the computer player's dice and updates the computer player's total score.
    **/
    private fun computerScore(){

        //if the computer player has rolled less than 3 times, the function enters a while loop to complete the remaining rolls
        //calls the appropriate roll strategy based on the game mode (hard or easy).
        if (computerRollCount <3){
            Log.d("IfCondition", "inside")
            while (computerRollCount < 3){
                Log.d("WhileCondition", "Inside")
                //Thread.sleep(1000)
                if (isHardMode){
                    comRollStrategy()
                }else comReRollRandom()
                computerRollCount++
            }
        }
        //extracting the integer value of each die face from its corresponding tag property
        for (imageView in computerDiceViews) {
            val parts = resources.getResourceName((imageView.tag as Int)).toString().split("_")
            currentComScoresList.add(parts[1].toInt())      //adding these values to a MutableList of Ints representing the current roll's scores
            Log.d(currentComScoresList.toString(),currentComScoresList.toString())
        }
        computerPlayer.addScore(currentComScoresList)           //add the scores to the player's total score
        computerScoreTextView.text = computerPlayer.totalScore.toString()       //Update the humanScoreTextView to display the new total score.
        currentComScoresList.clear()

        computerRollCount = 0
    }

    /** Selects or deselects a die when it is clicked by the user
        It does so by toggling the isSelected property of the clicked ImageView, and changing its background color accordingly.**/
    private fun selectDie(selectedDie: ImageView) {
        if (humanRollCount != 0) {          //check if the human player has already rolled at least once.
            selectedDie.isSelected = !selectedDie.isSelected

            if (selectedDie.isSelected) {
                selectedDie.setBackgroundColor(Color.rgb(175, 225, 175))
            } else {
                selectedDie.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    /** Checks if either the human or computer player has won the game, and displays a corresponding popup message.
        The winPopup(), tiePopup(), or losePopup() function is called based on the outcome of the game.
        The endTurn() function is called to reset the game state for the next round.
        The storeWins() function is called to save the game results in the SharedPreferences.**/
    private fun checkForWinner() {
        if (humanPlayer.totalScore >= winningScore) {
            if (computerPlayer.totalScore >= winningScore){
                if (humanPlayer.totalScore > computerPlayer.totalScore) {
                    winPopup()
                    endTurn()
                    storeWins(0,1)
                } else if (humanPlayer.totalScore == computerPlayer.totalScore){
                    tiePopup()
                    humanRollCount=2
                    computerRollCount=2
                    rollNumber.text = "Tie-Breaker Roll"

                } else {
                    losePopup()
                    endTurn()
                    storeWins(1,0)
                }
            }else{
                winPopup()
                endTurn()
                storeWins(0,1)
            }
        } else if (computerPlayer.totalScore >= winningScore) {
            losePopup()
            endTurn()
            storeWins(1,0)
        }
    }

    /**
        Displays a popup message to inform the user that they have won the game.
    **/
    private fun winPopup(){
        //inflate the win_popup.xml layout file
        val popupView = layoutInflater.inflate(R.layout.win_popup, null)

        // Create a PopupWindow object
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)         //show the PopupWindow in the center of the screen

        val closeButton = popupView.findViewById<Button>(R.id.cancel_button)

        closeButton.setOnClickListener { // Set a click listener for the close button
            popupWindow.dismiss()              // Dismiss the popup window
        }
    }

    /**
        Displays a popup message to inform the user that they have won the game.
     **/
    private fun losePopup(){
        //inflate the win_popup.xml layout file
        val popupView = layoutInflater.inflate(R.layout.lose_popup, null)

        // Create a PopupWindow object
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)     //show the PopupWindow in the center of the screen

        val closeButton = popupView.findViewById<Button>(R.id.cancel_button)

        closeButton.setOnClickListener { // Set a click listener for the close button
            popupWindow.dismiss()              // Dismiss the popup window
        }
    }

    /**
    Displays a popup message to inform the user that they have won the game.
     **/
    private fun tiePopup(){
        //inflate the win_popup.xml layout file
        val popupView = layoutInflater.inflate(R.layout.tie_popup, null)

        // Create a PopupWindow object
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)     //show the PopupWindow in the center of the screen

        val closeButton = popupView.findViewById<Button>(R.id.cancel_button)
        closeButton.setOnClickListener { // Set a click listener for the close button
            popupWindow.dismiss()              // Dismiss the popup window
        }
    }

    /**
        Disables the "Throw" and "Score" buttons at the end of a turn, preventing the user from using them until the next turn.
     **/
    private fun endTurn() {
        throwButton.isEnabled = false
        scoreButton.isEnabled = false
    }

    /**
        Saves the the number of wins for the human player and computer player in the SharedPreferences.
    **/
    private fun storeWins(computerPoint:Int,humanPoint:Int){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return     //retrieving the SharedPreferences object using the getPreferences() method
        with (sharedPref.edit()) {
            val currentComputerPoint = sharedPref.getInt("COMPUTER-SCORE", 0)       //Retrieve current number of wins
            val currentHumanPoint = sharedPref.getInt("HUMAN-SCORE", 0)
            putInt("COMPUTER-SCORE",currentComputerPoint+computerPoint)     //Add new number of wins and save
            putInt("HUMAN-SCORE",currentHumanPoint+humanPoint)
            apply()
        }
    }

    /** Simulates the computer player's roll of the dice when the "Throw" button is clicked.
     **/
    private fun comOnThrowButtonClick(){
        when (computerRollCount){
            0-> {
                currentComputerRoll = rollDice(comNotSelectedImageView.size)
                updateDiceViews(comNotSelectedImageView, currentComputerRoll)

                computerRollCount++
                comNotSelectedImageView.clear()
            }
            1 -> {
                computerRollCount++
                if (isHardMode){
                    comRollStrategy()
                }else comReRollRandom()
            }
            2 ->{
                if (isHardMode){
                    comRollStrategy()
                }else comReRollRandom()
                computerScore()
                checkForWinner()
            }
        }
    }

    /**
        Determines which dice the computer player should reroll based on the current roll of the computer player.
    **/
    private fun comRollStrategy () {
        Log.d("Function","Inside")
        var comReRoll = false
        //loops through the computer player's dice views to determine the values of each die in the current roll.
        for (imageView in computerDiceViews) {
            val parts = resources.getResourceName((imageView.tag as Int)).toString().split("_")
            currentComScoresList.add(parts[1].toInt())
        }
        Log.d("currentComScoresList",currentComScoresList.toString())

        //If the roll contains at least one 1, 2, or 3, comReRoll is set to true.
        if (1 in currentComScoresList || 2 in currentComScoresList || 3 in currentComScoresList){
            comReRoll = true
        }
        Log.d("bool", comReRoll.toString())

        //loops through the values in the current roll to find the indices of the dice that should be rerolled
        if(comReRoll){
            for(i in currentComScoresList.indices){
                if (currentComScoresList[i] == 1 || currentComScoresList[i] == 2 || currentComScoresList[i] == 3){
                    comNotSelectedImageView.add(computerDiceViews[i])
                }
            }
            currentComputerRoll = rollDice(comNotSelectedImageView.size)
            updateDiceViews(comNotSelectedImageView, currentComputerRoll)

            comNotSelectedImageView.clear()
        }
        currentComScoresList.clear()

    }

    /**
        Determines whether the computer player should reroll its dice randomly.
    **/
    private fun comReRollRandom(){
        val comReRoll = Random.nextBoolean()
        if (comReRoll) {
            comRandomStrategy()
        }
    }

    /**
        Generates a random selection status for each of the computer player's dice views.
    **/
    private fun comRandomStrategy(){
        for (i in computerDiceViews.indices){
            computerDiceViews[i].isSelected = Random.nextBoolean()
        }

        for (i in computerDiceViews.indices) {
            if (!computerDiceViews[i].isSelected) {
                comNotSelectedImageView.add(computerDiceViews[i])
            }
        }
        currentComputerRoll = rollDice(comNotSelectedImageView.size)
        updateDiceViews(comNotSelectedImageView, currentComputerRoll)
    }
}


