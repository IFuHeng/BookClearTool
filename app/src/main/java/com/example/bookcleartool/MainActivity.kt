package com.example.bookcleartool

import android.Manifest
import android.app.ProgressDialog
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import java.io.*
import java.security.Permissions
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {


    companion object {
        const val REQUEST_CODE_STORAGE = 1
        val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val list = mutableListOf<CharSequence>()

    val progressDialog by lazy {
        ProgressDialog(this).apply { this.setCancelable(false) }.apply { this.setMessage("处理中……") }
    }
    val mListView: ListView by lazy {
        this.findViewById<ListView>(R.id.listview).apply {
            this.adapter =
                ArrayAdapter<CharSequence>(this.context, android.R.layout.simple_list_item_1, list)
        }
    }
    private val tvError: View by lazy { findViewById<View>(R.id.tvError) }
    private val cardViewError: View by lazy { findViewById<View>(R.id.cardViewError) }

    private var task: ClearTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!verifyStoragePermission()) {
            finish()
            return
        }

        if (intent != null && intent.data != null) {
            val path = Utils.turnUri2FilePath(this, intent.data)
            task = ClearTask()
            task!!.execute(path)
        } else {
            cardViewError.visibility = View.VISIBLE
        }
        findViewById<View>(R.id.btn_exit).setOnClickListener { finish() }
    }

    private fun verifyStoragePermission(): Boolean {
        //1.检测权限
        var permission = PermissionChecker.PERMISSION_GRANTED
        for (s in PERMISSIONS_STORAGE) {
            permission = permission or ActivityCompat.checkSelfPermission(this, s)
        }
        if (permission != PermissionChecker.PERMISSION_GRANTED) {
            //2.没有权限，弹出对话框申请
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_CODE_STORAGE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE) {
            var result = PermissionChecker.PERMISSION_GRANTED
            for (grantResult in grantResults) {
                result = result or grantResult
            }
            if (result == PermissionChecker.PERMISSION_GRANTED) {
                //权限申请成功
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show()
            } else {
                //权限申请失败
                Toast.makeText(this, "未获取到所有授权，退出。", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
//    private fun clearText(str: String?, ss: SpannableString? = null): Pair<String?, CharSequence?>? {
//        if (str == null || str.isEmpty())
//            return Pair(str, null)
//
//        var start = -1
//        for (i in str.indices) {
//            val c = str[i]
//            if (start == -1) {
//                if (c == '&') start = i
//            } else if (i - start == 1) {
//                if (c != '#') start = -1
//            } else if (i - start > 1) {
//                if (!(c.isDigit() || c === '#' || c === '&')){
//                    var spannableString :SpannableString? = ss
//                    if (spannableString==null)
//                        spannableString= SpannableString(str)
//                    spannableString.setSpan(ForegroundColorSpan(Color.RED),)
//                    return clearText("${str.substring(0, start)}${str.substring(i)}", spannableString)
//                }
//            }
//        }
//
//        return if (start != -1) {
//            Pair(str.substring(0, start), ss)
//        } else
//            Pair(str, ss)
//    }

    private fun clearText2(str: String?): Pair<String, CharSequence>? {
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

    private inner class ProgressBeen(open val curLine: Int, open val str: CharSequence) {
        override fun toString(): String {
            return "$curLine $str"
        }

        fun toCharSequence(): CharSequence {
            val result = SpannableStringBuilder()
            val ss = SpannableString(curLine.toString())
            ss.setSpan(RelativeSizeSpan(.7f), 0, ss.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            result.append(ss)
            result.append(str)
            return result
        }
    }

    private inner class ClearTask : AsyncTask<String, ProgressBeen, Boolean>() {
        override fun doInBackground(vararg params: String?): Boolean {
            val path = params[0]
            var reader: BufferedReader? = null
            var writer: BufferedWriter? = null

            val backPath = "${path?.substring(0, path?.lastIndexOf('.'))}1${path?.substring(
                path?.lastIndexOf('.')
            )}"
            try {
                reader = BufferedReader(InputStreamReader(FileInputStream(path), "GBK"))
                writer = BufferedWriter(OutputStreamWriter(FileOutputStream(backPath), "GBK"))

                var temp: String?
                var pair: Pair<String, CharSequence>?
                var lineNum = 0
                do {
                    temp = reader.readLine()
                    if (temp == null)
                        break
                    lineNum++
                    pair = clearText2(temp)
                    if (pair != null) {
                        writer.write(pair.first)
                        publishProgress(ProgressBeen(lineNum, pair.second))
                    } else
                        writer.write(temp)
                    writer.newLine()
                } while (true)

            } catch (e: IOException) {
                publishProgress(ProgressBeen(-1, e.message as CharSequence))
                return false
            } finally {
                reader?.close()
                writer?.close()
            }

            return true
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.show()
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            mListView.visibility = if (result) View.VISIBLE else View.GONE
            cardViewError.visibility = if (result) View.GONE else View.VISIBLE

        }

        override fun onProgressUpdate(vararg values: ProgressBeen?) {
            super.onProgressUpdate(*values)

            values[0]?.toCharSequence()?.let { list.add(it) }
            (mListView.adapter as ArrayAdapter<CharSequence>).notifyDataSetChanged()
        }

        override fun onCancelled() {
            super.onCancelled()
            progressDialog.dismiss()
        }
    }

}