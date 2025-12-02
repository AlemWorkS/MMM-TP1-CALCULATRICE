package com.example.mmm_tp1_calculatrice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorViewModel(private val state: SavedStateHandle) : ViewModel() {

    val display = MutableLiveData(state["display"] ?: "")
    // On garde les nombres sous forme de String pour faciliter la saisie, mais on traitera en BigDecimal
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
        // Logique d'affichage simple : Gauche Op Droite
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
        if (!c.isDigit() && c != '.') return // Autoriser le point si on veut, sinon juste digit

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
            // Cas bonus : continuer le calcul sur le résultat précédent si left est vide
            left = display.value!!
        }
        if (left.isEmpty()) return

        if (op.isNotEmpty() && right.isNotEmpty()) {
            // Enchaînement d'opérations (ex: 5 + 5 + ... -> affiche 10 + ...) [cite: 23]
            if (!evaluate()) return
        }
        op = newOp.toString()
        rebuildDisplay()
    }

    fun onEquals() {
        if (op.isNotEmpty() && right.isNotEmpty()) {
            evaluate() // [cite: 24]
        }
    }

    fun onNegate() {
        // [cite: 25] Moins unaire sur le dernier nombre saisi
        fun toggleSign(s: String): String {
            if (s.isEmpty()) return s
            // Gestion intelligente du signe
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
        // [cite: 26, 27, 28] Effacer dernier caractère
        when {
            right.isNotEmpty() -> right = right.dropLast(1)
            op.isNotEmpty() -> op = ""
            left.isNotEmpty() -> left = left.dropLast(1)
        }
        rebuildDisplay()
    }

    fun onReset() {
        // [cite: 29] Tout effacer
        left = ""
        right = ""
        op = ""
        display.value = ""
        save()
    }

    fun onDot() {
        // Pour gérer les décimaux si tu le souhaites (ex: 5.5)
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

            // Formatage : si le résultat est entier (ex: 5.0), on enlève le .0, sinon on affiche normal
            // stripTrailingZeros peut parfois laisser des notations scientifiques, toPlainString corrige ça
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