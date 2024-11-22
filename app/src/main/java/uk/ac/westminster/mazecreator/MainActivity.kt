package uk.ac.westminster.mazecreator

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Random
import android.content.Intent
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*

class MainActivity : AppCompatActivity() {

    private lateinit var answerField: EditText
    private lateinit var wordDisplay: TextView
    private lateinit var submitButton: Button
    private lateinit var helpButton: Button
    private lateinit var dotButton: Button
    private lateinit var dashButton: Button
    private lateinit var backButton: Button
    private lateinit var spaceButton: Button
    private lateinit var slashButton: Button
    private lateinit var leftButton: Button
    private lateinit var rightButton: Button
    private lateinit var scoreDisplay: TextView
    private lateinit var gameSwitch: Button
    private var game: String = "Encoder"
    private val random = Random()
    private var score = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var popupWindow: PopupWindow

    // List of words for the game
    private val wordList = listOf(
        "about", "all", "an", "and", "are", "as", "at",
        "be", "beats", "bistro", "bombs", "boxes", "break", "brick", "but", "by",
        "cactus", "car", "cat", "crazy", "create",
        "do", "destroy",
        "flick", "for", "from",
        "get", "go",
        "halls", "have", "he", "her", "his",
        "if", "in", "it",
        "jazz", "jump", "jungle", "jacket", "jar",
        "kangaroo", "kite", "king",
        "lazy", "leaks", "love",
        "me", "my", "mean", "make",
        "not", "never", "needs", "no",
        "of", "on", "one", "or", "out",
        "queen", "quilt", "quasar",
        "say", "she", "shell", "slick", "so", "steak", "sting", "strobe",
        "that", "the", "their", "there", "they", "this", "to", "trick",
        "up", "under", "umbrella",
        "vase", "vector", "violin", "vivid",
        "we", "what", "where", "when", "which", "who", "why", "will", "with", "would",
        "xylophone", "xenon",
        "you", "yes",
        "zebra", "zigzag", "zoo"
    )

    private var currentWordIndex: Int = -1

    private val morseCodeMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
        'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
        'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--.."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        answerField = findViewById(R.id.numRowsEditText) // Initialize views
        wordDisplay = findViewById(R.id.resultTextView)
        submitButton = findViewById(R.id.submitButton)
        helpButton = findViewById(R.id.helpButton)
        dotButton = findViewById(R.id.dotButton)
        dashButton = findViewById(R.id.dashButton)
        backButton = findViewById(R.id.backButton)
        spaceButton = findViewById(R.id.spaceButton)
        slashButton = findViewById(R.id.slashButton)
        leftButton = findViewById(R.id.cursorLeft)
        rightButton = findViewById(R.id.cursorRight)
        scoreDisplay = findViewById(R.id.scoreView)
        gameSwitch = findViewById(R.id.switchButton)
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        answerField.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        answerField.requestFocus()

        submitButton.setOnClickListener {
            newCheckAnswer()
        }
        dotButton.setOnClickListener {
            appendCharacter('.')
        }

        dashButton.setOnClickListener {
            appendCharacter('-')
        }

        backButton.setOnClickListener {
            handleBackspace()
        }

        spaceButton.setOnClickListener {
            appendCharacter(' ')
        }

        slashButton.setOnClickListener {
            appendCharacter('/')
        }

        leftButton.setOnClickListener {
            moveCursorLeft()
        }

        rightButton.setOnClickListener {
            moveCursorRight()
        }

        helpButton.setOnClickListener {
            score = 0 // Reset the player's score for looking at the answers
            updateScoreDisplay()
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent) // Start the activity with instructions and chart
        }

        gameSwitch.setOnClickListener {
            if(score == 0){
                switchGame()
            } else {
                // Ask if user wants to switch
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = inflater.inflate(R.layout.popup_help, null)
                val confirmQuestion = popupView.findViewById<TextView>(R.id.gameMode)
                val confirmButton = popupView.findViewById<Button>(R.id.acceptButton)
                val declineButton = popupView.findViewById<Button>(R.id.backButton)

                if (game == "Encoder"){
                    confirmQuestion.text = "Are you sure you want switch to Decoder?\nYour score will be reset."
                } else {
                    confirmQuestion.text = "Are you sure you want switch to Encoder?\nYour score will be reset."
                }
                declineButton.setOnClickListener {
                    popupWindow.dismiss()
                }

                confirmButton.setOnClickListener {
                    switchGame()
                    popupWindow.dismiss()
                }
                popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
                popupWindow.showAtLocation(answerField, Gravity.CENTER, 0, 0)
            }
        }
        score = 0
        updateScoreDisplay() // Initialize score display
        newWord() // Initialize the game by selecting the first random word
    }

    private fun newWord(){
        saveHighScore()
        currentWordIndex = random.nextInt(wordList.size)
        val word = wordList[currentWordIndex]
        if (game == "Encoder"){
            wordDisplay.text = "Word: $word"
        } else {
            val codedWord = convertToMorseCode(word)
            wordDisplay.text = "Code: $codedWord"
        }
    }

    private fun newCheckAnswer(){
        val userAnswer = answerField.text.toString().trim()
        val currentWord = wordList[currentWordIndex]
        if(game == "Encoder"){
            val correctMorse = convertToMorseCode(currentWord)
            println("userAnswer: " + userAnswer)
            println("correctMorse: " + correctMorse)
            val isCorrect = compareMorseCode(userAnswer, correctMorse) // Check if the user's Morse code matches the correct Morse code
            if (isCorrect) {
                score++
                Toast.makeText(this, "Correct! Move on to the next word.", Toast.LENGTH_SHORT).show() // Display a message for correct answer
                newWord() // Select a new random word for the next round
                saveHighScore()
                updateScoreDisplay()
                answerField.text.clear() // Clear the EditText for the next round
            } else {
                // Display a message for incorrect answer
                saveHighScore()
                score = 0
                updateScoreDisplay()
                Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            println("userAnswer: " + userAnswer)
            println("correctWord: " + currentWord)
            val isCorrect = compareWords(userAnswer, currentWord)
            if (isCorrect) {
                score++
                Toast.makeText(this, "Correct! Move on to the next word.", Toast.LENGTH_SHORT).show() // Display a message for correct answer
                newWord() // Select a new random word for the next round
                saveHighScore()
                updateScoreDisplay()
                answerField.text.clear() // Clear the EditText for the next round
            } else {
                // Display a message for incorrect answer
                saveHighScore()
                score = 0
                updateScoreDisplay()
                Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compareMorseCode(userMorseCode: String, correctMorseCode: String): Boolean {
        return userMorseCode == correctMorseCode
    }

    private fun convertToMorseCode(word: String): String {
        val upperCaseWord = word.uppercase() // Convert the input word to uppercase
        val morseCodeBuilder = StringBuilder() // Convert each character to Morse code and join them
        for (char in upperCaseWord) {
            val morseCode = morseCodeMap[char]
            if (morseCode != null) {
                morseCodeBuilder.append(morseCode).append(" ") // Append Morse code for the character
            } else {
                // Handle cases where the character is not in the Morse code map (e.g., space)
                morseCodeBuilder.append(" ") // Append space for unrecognized characters
            }
        }
        return morseCodeBuilder.toString().trim()
    }


    private fun appendCharacter(char: Char) {
        val cursorPosition = answerField.selectionStart // Get the current cursor position

        val currentText = answerField.text.toString() // Get the current text from the EditText

        // Insert the character at the cursor position
        val newText = currentText.substring(0, cursorPosition) + char +
                currentText.substring(cursorPosition)

        answerField.setText(newText) // Set the updated text to the EditText

        answerField.setSelection(cursorPosition + 1) // Move the cursor to the right
    }

    private fun handleBackspace() {
        val cursorPosition = answerField.selectionStart // Get the current cursor position

        val currentText = answerField.text.toString() // Get the current text from the EditText

        // Check if there's a character before the cursor
        if (cursorPosition > 0) {
            // Remove the character before the cursor
            val newText = currentText.substring(0, cursorPosition - 1) +
                    currentText.substring(cursorPosition)

            answerField.setText(newText) // Set the updated text to the EditText

            answerField.setSelection(cursorPosition - 1) // Move the cursor to the left
        }
    }

    private fun moveCursorLeft() {
        val selectionStart = answerField.selectionStart
        if (selectionStart > 0) {
            answerField.setSelection(selectionStart - 1)
        }
    }

    private fun moveCursorRight() {
        val selectionStart = answerField.selectionStart
        val textLength = answerField.text.length
        if (selectionStart < textLength) {
            answerField.setSelection(selectionStart + 1)
        }
    }

    private fun updateScoreDisplay() {
        val high = getHighScore()
        scoreDisplay.text = "$game: Score: $score, High Score: $high"
    }

    companion object {
        private const val HIGH_SCORE_KEY_ENCODER = "encoderHighScore"
        private const val HIGH_SCORE_KEY_DECODER = "decoderHighScore"
    }
    private fun saveHighScore() { // Retrieve the existing high score based on the game mode
        val currentHighScore = getHighScore()
        if (score > currentHighScore) { // Save the high score only if the current score is greater
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            when (game) {
                "Encoder" -> editor.putInt(HIGH_SCORE_KEY_ENCODER, score)
                "Decoder" -> editor.putInt(HIGH_SCORE_KEY_DECODER, score)
                else -> throw IllegalArgumentException("Invalid game mode: $game")
            }
            editor.apply()
        }
    }

    private fun getHighScore(): Int {
        // Retrieve the high score from SharedPreferences based on the game mode
        return when (game) {
            "Encoder" -> sharedPreferences.getInt(HIGH_SCORE_KEY_ENCODER, 0)
            "Decoder" -> sharedPreferences.getInt(HIGH_SCORE_KEY_DECODER, 0)
            else -> throw IllegalArgumentException("Invalid game mode: $game")
        }
    }

    private fun compareWords(userWord: String, correctWord: String): Boolean {
        return userWord == correctWord
    }

    private fun switchGame(){
        if(game == "Encoder"){
            game = "Decoder"
            newWord()
            answerField.hint = "Enter code in English"
            score = 0
            updateScoreDisplay()
            answerField.text.clear()
        } else {
            game = "Encoder"
            newWord()
            answerField.hint = "Enter word in Morse code"
            score = 0
            updateScoreDisplay()
            answerField.text.clear()
        }
    }
}