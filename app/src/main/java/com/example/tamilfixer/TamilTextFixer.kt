package com.example.tamilfixer

import android.content.Context
import java.text.Normalizer

class TamilTextFixer(context: Context) {

    private val dictionary: Set<String> by lazy { loadDictionary(context) }

    private fun loadDictionary(context: Context): Set<String> {
        return try {
            context.assets.open("tamil_words.txt").bufferedReader(Charsets.UTF_8)
                .readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toHashSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private val combiningMarks = "்ாிீுூெேைொோௌ"
    private val consonantRange = '\u0B95'..'\u0BB9'

    fun fix(rawInput: String): String {
        if (rawInput.isBlank()) return rawInput

        var text = Normalizer.normalize(rawInput, Normalizer.Form.NFC)

        repeat(3) {
            text = collapseRepeatedMarks(text)
            text = collapseDuplicatedConsonants(text)
            text = mergeBrokenWords(text)
        }

        text = cleanupSpacing(text)
        return text
    }

    private fun collapseRepeatedMarks(input: String): String {
        var text = input
        for (mark in combiningMarks) {
            val pattern = Regex("$mark\\s*$mark")
            var previous: String
            do {
                previous = text
                text = text.replace(pattern, mark.toString())
            } while (text != previous)
        }
        return text
    }

    private fun collapseDuplicatedConsonants(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (i + 1 < text.length && text[i + 1] == c && c in consonantRange) {
                sb.append(c)
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun normalizeCandidate(s: String): String =
        collapseDuplicatedConsonants(collapseRepeatedMarks(s))

    private fun mergeBrokenWords(text: String): String {
        if (dictionary.isEmpty()) return text
        return text.split("\n").joinToString("\n") { mergeLine(it) }
    }

    private fun mergeLine(line: String): String {
        val tokens = line.split(" ").filter { it.isNotEmpty() }
        if (tokens.size <= 1) return line

        val result = StringBuilder()
        var i = 0
        val maxMergeSpan = 6
        while (i < tokens.size) {
            var bestEnd = i
            var bestWord = tokens[i]
            var candidate = tokens[i]
            var j = i
            while (j < tokens.size && j - i < maxMergeSpan) {
                val normalized = normalizeCandidate(candidate)
                if (normalized.length > 1 && dictionary.contains(normalized)) {
                    bestEnd = j
                    bestWord = normalized
                }
                j++
                if (j < tokens.size) candidate += tokens[j]
            }
            result.append(bestWord)
            result.append(" ")
            i = bestEnd + 1
        }
        return result.toString().trim()
    }

    private fun cleanupSpacing(text: String): String {
        return text
            .replace(Regex("\\s+([.,;?!])"), "$1")
            .replace(Regex("[ \\t]+"), " ")
            .trim()
    }
}
