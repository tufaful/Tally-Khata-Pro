package com.example.domain.calculator

import com.example.domain.utils.BanglaUtils

object CalculatorEngine {

    fun calculateItemTotal(quantity: Double, unitPrice: Long, discount: Long = 0L): Long {
        val gross = (quantity * unitPrice).toLong()
        val net = gross - discount
        return kotlin.math.max(0L, net)
    }

    /**
     * Evaluates a mathematical expression like "5 * 250" or "10 * 120 + 500".
     * Handles both English digits and Bangla digits.
     */
    fun evaluateExpression(input: String): Long? {
        val normalized = BanglaUtils.toEnglishDigits(input.trim())
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        if (normalized.isEmpty()) return null

        return try {
            val parser = SimpleExprParser(normalized)
            parser.parse().toLong()
        } catch (e: Exception) {
            null
        }
    }

    private class SimpleExprParser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                } else return x
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            return x
        }
    }
}
