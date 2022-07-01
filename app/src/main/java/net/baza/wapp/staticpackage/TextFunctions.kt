package net.baza.wapp.staticpackage

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast

class TextFunctions
{
    companion object
    {
        fun saveText(key: String, text: String, c: Context) {
            val prefs = c.getSharedPreferences(key, Context.MODE_PRIVATE)
            val ed = prefs.edit()
            ed.putString(key, text)
            ed.apply()
        }

        fun loadText(key: String?, c: Context): String
        {
            try
            {
                val answer = c.getSharedPreferences(key, Context.MODE_PRIVATE).getString(key, "")
                if (answer != null)
                    return answer
            } catch (e: Exception) {
                Log.e("SharedPrefsError", "Read shared prefs error: ", e)
            }
            return ""
        }

        @Synchronized
        fun showText(context: Context, text: String) {
            println("SHOW TEXT: $text")
            try {
                if (context is Activity) {
                    context.runOnUiThread {
                        if (!context.isFinishing)
                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                } else
                    Log.e("ShowTextError", "Context is invalid when show text ($text): $context")
            } /*catch (e: WindowManager.BadTokenException) {
                Log.e("ShowTextError", "BadTokenException when show text ($text): ", e)
            } */catch (e: Exception) {
                Log.e("ShowTextError", "BadTokenException when show text ($text): ", e)
            }
        }

        fun isNumber(string: String): Boolean {
            try {
                string.toInt()
                return true
            } catch (ignored: Exception) { }
            return false
        }

        fun isLong(string: String): Boolean {
            try {
                string.toLong()
                return true
            } catch (ignored: Exception) { }
            return false
        }

        fun isDouble(string: String): Boolean {
            try {
                string.toDouble()
                return true
            } catch (ignored: Exception) { }
            return false
        }
    }
}