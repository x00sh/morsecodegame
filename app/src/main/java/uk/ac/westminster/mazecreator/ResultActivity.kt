package uk.ac.westminster.mazecreator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    private lateinit var resultTextView: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Initialize views
        resultTextView = findViewById(R.id.resultTextView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            // Navigate back to the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }

    }
}
