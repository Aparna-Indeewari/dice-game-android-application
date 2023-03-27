//https://drive.google.com/file/d/18PtgA7H4HXEvQ2p3pNYGGSKsDM2OFwBJ/view?usp=share_link

package com.example.dicerollercw

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    companion object {
        const val WINNING_SCORE = "com.example.application.example.WINNING_SCORE"
    }

    private lateinit var newGameBtn : Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Retrieve the views from the layout file and store them in corresponding variables.
        newGameBtn = findViewById(R.id.newGameButton)
        var aboutBtn: Button = findViewById(R.id.aboutButton)

        // Set click listeners to detect when they are clicked.
        newGameBtn.setOnClickListener {
            setWinningScorePopup()
        }

        aboutBtn.setOnClickListener {
            showAboutPopup()
        }
    }


    /**
        Creates a popup window that allows the user to enter a new winning score for the game.
    **/
    private fun setWinningScorePopup(){
        val scorePopupView = layoutInflater.inflate(R.layout.change_score, null)

        // Create a PopupWindow object
        val scorePopupWindow = PopupWindow(
            scorePopupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        scorePopupWindow.showAtLocation(newGameBtn, Gravity.CENTER, 0, 0)

            // Get references to the views in the popup window
            val closeButton = scorePopupView.findViewById<Button>(R.id.cancel_button)
            val startGameButton = scorePopupView.findViewById<Button>(R.id.start_game)

            closeButton.setOnClickListener { // Set a click listener for the close button
                scorePopupWindow.dismiss()              // Dismiss the popup window
            }

            startGameButton.setOnClickListener {
                val scoreText = Integer.parseInt(scorePopupView.findViewById<TextInputEditText>(R.id.current_game_score).text.toString())
                val intent = Intent(this, NewGame::class.java)
                intent.putExtra(WINNING_SCORE, scoreText)
                startActivity(intent)
            }
    }

    /**
        Creates a popup window that allows the user to see about information
     **/
    private fun showAboutPopup() {
        // Inflate the popup_layout.xml file
        val popupView = layoutInflater.inflate(R.layout.about, null)

        // Create a PopupWindow object
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Get references to the views in the popup window
        val closeButton = popupView.findViewById<Button>(R.id.cancel_about)

        // Set a click listener for the close button
        closeButton.setOnClickListener {
            // Dismiss the popup window
            popupWindow.dismiss()
        }

        // Show the popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }
}