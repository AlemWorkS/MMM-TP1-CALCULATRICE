package com.example.mmm_tp1_calculatrice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.math.BigInteger

class CalculatorViewModel(private val state: SavedStateHandle) : ViewModel() {

    val display = MutableLiveData(state["display"] ?: "")
    private var left = state.get<String>("left") ?: ""       // opérande gauche
    private var right = state.get<String>("right") ?: ""      // opérande droite
    private var op = state.get<String>("op") ?: ""            // + - * / %

    private fun save() {
        state["display"] = display.value
        state["left"] = left
        state["right"] = right
        state["op"] = op
    }

    private fun rebuildDisplay() {
        val text = buildString {
            append(left)
            if (op.isNotEmpty()) append(" $op ")
            append(right)
        }
        display.value = text
        save()
    }

    fun onDigit(c: Char) {
        if (!c.isDigit()) return
        if (op.isEmpty()) {
            // saisie du 1er nombre
            left += c
        } else {
            // saisie du 2e nombre
            right += c
        }
        rebuildDisplay()
    }

    fun onOperator(newOp: Char) {
        if (newOp !in charArrayOf('+','-','*','/','%')) return
        if (left.isEmpty()) return // rien à faire si aucun nombre saisi
        if (op.isNotEmpty() && right.isNotEmpty()) {
            // déjà une opération complète -> évaluer d'abord
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
        // Remplace le DERNIER nombre par son opposé
        fun toggleSign(s: String): String {
            if (s.isEmpty()) return s
            if (s == "0" || s == "-0") return "0"
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
        // Efface le dernier caractère (chiffre ou opération)
        when {
            right.isNotEmpty() -> {
                right = right.dropLast(1)
            }
            op.isNotEmpty() -> {
                op = ""
            }
            left.isNotEmpty() -> {
                left = left.dropLast(1)
            }
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
        // Bouton “.” non requis dans le TP -> on peut ignorer ou afficher un message via l'Activity
    }

    fun textToCopy(): String = display.value.orEmpty()

    private fun evaluate(): Boolean {
        try {
            val a = toBigInt(left)
            val b = toBigInt(right)
            val res = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> {
                    if (b == BigInteger.ZERO) return false
                    a / b
                }
                "%" -> {
                    if (b == BigInteger.ZERO) return false
                    a % b
                }
                else -> return false
            }
            left = res.toString()
            right = ""
            op = ""
            display.value = left
            save()
            return true
        } catch (_: Exception) {
            onReset()
            display.value = "Erreur"
            save()
            return false
        }
    }

    private fun toBigInt(s: String): BigInteger =
        if (s.isBlank()) BigInteger.ZERO else BigInteger(s)
}
