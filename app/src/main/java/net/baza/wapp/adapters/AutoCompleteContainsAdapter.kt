package net.baza.wapp.adapters

import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Filter
import android.widget.ProgressBar
import java.lang.Exception
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class AutoCompleteContainsAdapter<T>(
    context: Context,
    resourceid: Int,
    var textview: AutoCompleteTextView,
    filtercallback: (String) -> List<T>
) : ArrayAdapter<String?>(
    context, resourceid, CopyOnWriteArrayList()
) {
    var objects = CopyOnWriteArrayList<T>()
    var suggestions = CopyOnWriteArrayList<String>()
    var lastdate: Date
    var loadingbar: ProgressBar? = null
    var lasttext: String
    private var filtercallback: (String) -> List<T>

    constructor(
        context: Context,
        resource: Int,
        textview: AutoCompleteTextView,
        loadingbar: ProgressBar?,
        filtercallback: (String) -> List<T>
    ) : this(context, resource, textview, filtercallback) {
        this.loadingbar = loadingbar
    }

    fun getObjectByString(string: String): T? {
        val tempstring = string.trim { it <= ' ' }
        for (temp in objects)
            if (temp.toString() == tempstring)
                return temp
        return null
    }

    private var myfilter: Filter = object : Filter() {
        /*public String convertResultToString(Object resultValue) {
            String str = ((ProductDataModel) (resultValue)).getProductName();
            return str;
        }*/
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return if (constraint != null) {
                lastdate = Date()
                lasttext = constraint.toString()
                val needend = AtomicInteger(0)
                Thread {
                    try {
                        var sleeptime = 0
                        while (lasttext == textview.text.toString()) {
                            Thread.sleep(10)
                            sleeptime += 10
                            if (sleeptime > WAITTIME / 2) break
                            if (sleeptime > 200) loadingBarVisibility(View.VISIBLE)
                        }
                        if (lasttext == textview.text.toString() && sleeptime > WAITTIME / 2) {
                            loadingBarVisibility(View.VISIBLE)
                            objects.clear()
                            suggestions.clear()
                            for (temp in filtercallback(lasttext)) {
                                if (suggestions.size > 25)
                                    break
                                suggestions.add(temp.toString())
                                objects.add(temp)
                            }
                            needend.set(1)
                        } else needend.set(-1)
                    } catch (e: Exception) {
                        Log.e("ContainsAdapterError", "Error updating info: ", e)
                    }
                }.start()
                while (needend.get() == 0) {
                    try {
                        Thread.sleep(10)
                    } catch (ignored: Exception) {
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                filterResults
            } else {
                FilterResults()
            }
        }

        fun loadingBarVisibility(visibility: Int) {
            val loadingbar = loadingbar
            if (loadingbar != null) {
                if (loadingbar.context is Activity) {
                    (loadingbar.context as Activity).runOnUiThread {
                        try {
                            loadingbar.visibility = visibility
                        } catch (ignored: Exception) {
                        }
                    }
                }
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (constraint != null && results != null)
            {
                if (results.values is CopyOnWriteArrayList<*>)
                {
                    val filteredList = results.values as CopyOnWriteArrayList<String>
                    if (results.count > 0) {
                        clear()
                        for (s in filteredList) add(s)
                        lastdate = Date()
                        notifyDataSetChanged()
                    }
                }
            }
            loadingBarVisibility(View.GONE)
        }
    }

    override fun getFilter(): Filter {
        return myfilter
    }

    companion object {
        const val WAITTIME = 1000

        //масштаб экрана
        fun getDensityName(context: Context): String {
            val density = context.resources.displayMetrics.density
            if (density >= 4.0) return "xxxhdpi"
            if (density >= 3.0) return "xxhdpi"
            if (density >= 2.0) return "xhdpi"
            if (density >= 1.5) return "hdpi"
            return if (density >= 1.0) "mdpi" else "ldpi"
        }
    }

    init {
        lastdate = Date()
        lasttext = ""
        this.filtercallback = filtercallback
    }
}