package com.example.mmm_tp1_calculatrice

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val vm: CalculatorViewModel by viewModels()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val display = findViewById<TextView>(R.id.display)

        vm.display.observe(this) { text ->
            display.text = text
        }

        // Chiffres 0..9
        val digitIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        for (id in digitIds) {
            findViewById<Button>(id).setOnClickListener { b ->
                val label = (b as Button).text.first()
                vm.onDigit(label)
            }
        }

        // Opérateurs
        findViewById<Button>(R.id.btnPlus).setOnClickListener { vm.onOperator('+') }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { vm.onOperator('-') }
        findViewById<Button>(R.id.btnMul).setOnClickListener { vm.onOperator('*') }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { vm.onOperator('/') }
        findViewById<Button>(R.id.btnMod).setOnClickListener { vm.onOperator('%') }

        // Spéciaux
        findViewById<Button>(R.id.btnEquals).setOnClickListener { vm.onEquals() }
        findViewById<Button>(R.id.btnNegate).setOnClickListener { vm.onNegate() }
        findViewById<Button>(R.id.btnDel).setOnClickListener { vm.onDelete() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { vm.onReset() }
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            Toast.makeText(this, "Bouton . non implémenté pour ce TP", Toast.LENGTH_SHORT).show()
            vm.onDot()
        }
        findViewById<Button>(R.id.btnCopy).setOnClickListener {
            val text = vm.textToCopy()
            if (text.isNotBlank()) {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("result", text))
                Toast.makeText(this, "Copié dans le presse-papier", Toast.LENGTH_SHORT).show()
            }
        }
    }
}