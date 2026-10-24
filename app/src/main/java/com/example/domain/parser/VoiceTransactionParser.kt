package com.example.domain.parser

import com.example.data.entities.CustomerEntity
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils

data class ParsedVoiceTransaction(
    val matchedCustomer: CustomerEntity?,
    val detectedCustomerName: String,
    val type: TransactionType,
    val amount: Long,
    val rawText: String,
    val confidenceNotes: String
)

object VoiceTransactionParser {

    private val banglaWordToNumber = mapOf(
        "এক" to 1L, "দুই" to 2L, "তিন" to 3L, "চার" to 4L, "পাঁচ" to 5L,
        "ছয়" to 6L, "সাত" to 7L, "আট" to 8L, "নয়" to 9L, "দশ" to 10L,
        "কুড়ি" to 20L, "বিশ" to 20L, "ত্রিশ" to 30L, "চল্লিশ" to 40L,
        "পঞ্চাশ" to 50L, "ষাট" to 60L, "সত্তর" to 70L, "আশি" to 80L,
        "নব্বই" to 90L, "একশত" to 100L, "একশ" to 100L, "দুশ" to 200L,
        "পাঁচশ" to 500L, "হাজার" to 1000L, "লাখ" to 100000L
    )

    fun parse(rawInput: String, existingCustomers: List<CustomerEntity>): ParsedVoiceTransaction {
        val text = rawInput.trim()
        val normalized = BanglaUtils.toEnglishDigits(text)

        // 1. Detect Amount
        var detectedAmount = 0L
        // Try regex for digits first
        val digitRegex = Regex("""\b(\d+)\b""")
        val digitMatch = digitRegex.find(normalized)
        if (digitMatch != null) {
            detectedAmount = digitMatch.groupValues[1].toLongOrNull() ?: 0L
        } else {
            // Check for Bangla word amounts like "পাঁচ হাজার"
            if (text.contains("হাজার")) {
                var multiplier = 1L
                for ((word, value) in banglaWordToNumber) {
                    if (text.contains(word) && value < 1000L) {
                        multiplier = value
                        break
                    }
                }
                detectedAmount = multiplier * 1000L
            } else if (text.contains("শ") || text.contains("শত")) {
                var multiplier = 1L
                for ((word, value) in banglaWordToNumber) {
                    if (text.contains(word) && value < 100L) {
                        multiplier = value
                        break
                    }
                }
                detectedAmount = multiplier * 100L
            }
        }

        // 2. Detect Transaction Type
        // If "দিলাম" / "জমা দিল" / "পরিশোধ" / "পেলাম" -> PAYMENT
        // If "বাকি" / "বাকিতে" / "পাওনা" / "মাল নিল" / "ধার" -> CREDIT
        val isPayment = text.contains("জমা") || text.contains("দিলাম") || text.contains("পরিশোধ") ||
                text.contains("পেলাম") || text.contains("পেমেন্ট") || text.contains("দিল") ||
                text.lowercase().contains("paid") || text.lowercase().contains("received")

        val detectedType = if (isPayment) TransactionType.PAYMENT else TransactionType.CREDIT

        // 3. Match Customer
        var matchedCustomer: CustomerEntity? = null
        var detectedName = ""

        // Check if any existing customer name is found in the speech text
        for (customer in existingCustomers) {
            val customerCleanName = customer.name.trim()
            if (customerCleanName.isNotEmpty() &&
                (text.contains(customerCleanName, ignoreCase = true) ||
                 text.contains(customerCleanName.replace("কে", ""), ignoreCase = true))
            ) {
                matchedCustomer = customer
                detectedName = customer.name
                break
            }
        }

        // If no direct match, try extracting the first word/token before "কে" or " "
        if (matchedCustomer == null) {
            val tokens = text.split(" ", "কে ")
            detectedName = tokens.firstOrNull()?.replace("কে", "")?.trim() ?: "নতুন কাস্টমার"
            // Search partially again
            matchedCustomer = existingCustomers.firstOrNull {
                it.name.contains(detectedName, ignoreCase = true) || detectedName.contains(it.name, ignoreCase = true)
            }
        }

        val notes = if (matchedCustomer != null) {
            "কাস্টমার পাওয়া গেছে: ${matchedCustomer.name}"
        } else {
            "নতুন নাম চিহ্নিত: $detectedName"
        }

        return ParsedVoiceTransaction(
            matchedCustomer = matchedCustomer,
            detectedCustomerName = if (matchedCustomer != null) matchedCustomer.name else detectedName,
            type = detectedType,
            amount = if (detectedAmount > 0L) detectedAmount else 500L,
            rawText = rawInput,
            confidenceNotes = notes
        )
    }
}
