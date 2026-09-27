package com.example.tamilfixer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var fixer: TamilTextFixer
    private lateinit var inputText: EditText
    private lateinit var outputText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fixer = TamilTextFixer(this)

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)

        val fixButton = findViewById<Button>(R.id.fixButton)
        val copyButton = findViewById<Button>(R.id.copyButton)
        val shareButton = findViewById<Button>(R.id.shareButton)

        fixButton.setOnClickListener {
            outputText.setText(fixer.fix(inputText.text.toString()))
        }

        copyButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Fixed Tamil text", outputText.text.toString()))
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }

        shareButton.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, outputText.text.toString())
            }
            startActivity(Intent.createChooser(sendIntent, "Share fixed text"))
        }

        handleIncomingShare(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShare(intent)
    }

    private fun handleIncomingShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                inputText.setText(sharedText)
                outputText.setText(fixer.fix(sharedText))
            }
        }
    }
}
