package com.example.bookcleartool

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)


        val text = "abc&#1234#&5678&#34def#425&#424"
        println(clearText(text))


        val str1 = "a.b.c"
        str1.also { it ->
            val index = it.lastIndexOf('.')
            println("${it.substring(0, index)}1${it.substring(index)}")
        }

//    println("${str1.substring(0,str1.lastIndexOf('.'))}1${str1.substring(str1.lastIndexOf('.'))}")
    }

    private fun clearText(str: String?): String? {
        if (str == null || str.isEmpty())
            return str

        var start = -1
        for (i in str.indices) {
            val c = str[i]
            if (start == -1) {
                if (c == '&') start = i
            } else if (i - start == 1) {
                if (c != '#') start = -1
            } else if (i - start > 1) {
                if (!(c.isDigit() || c === '#' || c === '&')) {
                    return "${str.substring(0, start)}${str.substring(i)}"
                }
            }
        }

        return if (start != -1) {
            str.substring(0, start)
        } else
            str
    }

    private fun clearText2(str: String?): Pair<String?, CharSequence>? {
        if (str == null || str.isEmpty())
            return null
        val matcher = Pattern.compile("&#\\d*").matcher(str)
        val list = mutableListOf<Int>()
        val spannableString = SpannableString(str)
        var hasMatched = false
        while (matcher.find()) {
            hasMatched = true
            list.add(matcher.start())
            list.add(matcher.end())
            spannableString.setSpan(
                ForegroundColorSpan(getRandomColor()),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        return if (hasMatched)
            Pair(matcher.replaceAll(""), spannableString)
        else
            null
    }

    private fun getRandomColor(): Int {
        Math.random()
        val hsv = FloatArray(3)
        hsv[0] = Math.random().toFloat() * 360
        hsv[1] = 1f
        hsv[2] = 1f
        return Color.HSVToColor(hsv)
    }
}