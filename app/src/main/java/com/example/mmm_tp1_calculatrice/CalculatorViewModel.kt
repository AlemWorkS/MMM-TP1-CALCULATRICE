package com.example.mmm_tp1_calculatrice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorViewModel(private val state: SavedStateHandle) : ViewModel() {

    val display = MutableLiveData(state["display"] ?: "")

    private var left = state.get<String>("left") ?: ""
    private var right = state.get<String>("right") ?: ""
    private var op = state.get<String>("op") ?: ""

    private fun save() {
        state["display"] = display.value
        state["left"] = left
        state["right"] = right
        state["op"] = op
    }

    private fun rebuildDisplay() {
        val text = buildString {
            append(left)
            if (op.isNotEmpty()) {
                append(" $op ") // Espaces pour lisibilité
                append(right)
            }
        }
        display.value = text
        save()
    }

    fun onDigit(c: Char) {
        if (!c.isDigit() && c != '.') return

        if (op.isEmpty()) {
            left += c
        } else {
            right += c
        }
        rebuildDisplay()
    }

    fun onOperator(newOp: Char) {
        if (newOp !in charArrayOf('+','-','*','/','%')) return
        if (left.isEmpty() && display.value?.isNotEmpty() == true) {
            left = display.value!!
        }
        if (left.isEmpty()) return

        if (op.isNotEmpty() && right.isNotEmpty()) {
            // Enchaînement d'opérations
            if (!evaluate()) return
        }
        op = newOp.toString()
        rebuildDisplay()
    }

    fun onEquals() {
        if (op.isNotEmpty() && right.isNotEmpty()) {
            evaluate()
        }
    }

    fun onNegate() {
        // Moins unaire sur le dernier nombre saisi
        fun toggleSign(s: String): String {
            if (s.isEmpty()) return s
            // Gestion du signe
            return if (s.startsWith("-")) s.removePrefix("-") else "-$s"
        }

        if (op.isEmpty()) {
            left = toggleSign(left)
        } else {
            right = toggleSign(right)
        }
        rebuildDisplay()
    }

    fun onDelete() {

        when {
            right.isNotEmpty() -> right = right.dropLast(1)
            op.isNotEmpty() -> op = ""
            left.isNotEmpty() -> left = left.dropLast(1)
        }
        rebuildDisplay()
    }

    fun onReset() {
        left = ""
        right = ""
        op = ""
        display.value = ""
        save()
    }

    fun onDot() {
        // Pour gérer les décimaux
        if (op.isEmpty()) {
            if (!left.contains(".")) left += "."
        } else {
            if (!right.contains(".")) right += "."
        }
        rebuildDisplay()
    }

    fun textToCopy(): String = display.value.orEmpty() //

    private fun evaluate(): Boolean {
        try {
            // Utilisation de BigDecimal pour la précision
            val a = if (left.isBlank()) BigDecimal.ZERO else BigDecimal(left)
            val b = if (right.isBlank()) BigDecimal.ZERO else BigDecimal(right)

            val res = when (op) {
                "+" -> a.add(b)
                "-" -> a.subtract(b)
                "*" -> a.multiply(b)
                "/" -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) return false // Division par 0
                    // Division avec 8 décimales, arrondi classique
                    a.divide(b, 8, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "%" -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) return false
                    a.remainder(b)
                }
                else -> return false
            }


            left = res.toPlainString()
            right = ""
            op = ""
            display.value = left
            save()
            return true
        } catch (e: Exception) {
            display.value = "Erreur"
            left = ""
            right = ""
            op = ""
            save()
            return false
        }
    }
}