package net.baza.wapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import net.baza.wapp.R
import net.baza.wapp.adapters.AutoCompleteContainsAdapter
//import net.baza.wapp.adapters.AutoCompleteContainsAdapterJava
import net.baza.wapp.adapters.WeatherDayAdapter
import net.baza.wapp.db.AppDatabase
import net.baza.wapp.models.City
import net.baza.wapp.models.WeatherDay
import net.baza.wapp.staticpackage.Constants
import net.baza.wapp.staticpackage.TextFunctions
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : AppCompatActivity()
{
    private lateinit var parentlayout: SwipeRefreshLayout
    private lateinit var nestedscrollview: NestedScrollView

    private lateinit var lastupdatetext: TextView
    private lateinit var citysearchtextview: AutoCompleteTextView
    private lateinit var autocompletecontainsadapter: AutoCompleteContainsAdapter<City>
    private lateinit var searchloadingbar: ProgressBar

    private lateinit var weatherlistview: RecyclerView
    private lateinit var weatherdayadapter: WeatherDayAdapter
    private var weatherdays = CopyOnWriteArrayList<WeatherDay>()

    private lateinit var weathernotloadedtext: TextView

    private lateinit var loadinglayout: View
    private lateinit var downloadingbar: ProgressBar
    private lateinit var downloadingtitle: TextView

    private var selectedcity: City? = null

    private var firstlaunch = true

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    val weatherloadcallback: (List<WeatherDay>) -> Unit = {
            list: List<WeatherDay> ->
        weatherdays.clear()
        weatherdays.addAll(list)

        this@MainActivity.runOnUiThread {
            weatherdayadapter.notifyDataSetChanged()
            lastupdatetext.visibility = View.VISIBLE
            lastupdatetext.text = "${this@MainActivity.resources.getString(R.string.last_update_prefix)} ${Constants.LASTUPDATEFORMAT.format(weatherdays[0].update)}"
            weathernotloadedtext.visibility = View.GONE
        }

        TextFunctions.showText(this@MainActivity, "Weather info updated!")
    }

    @SuppressLint("NotifyDataSetChanged", "ResourceType")
    val weathererrorcallback: () -> Unit = {
        weatherdays.clear()
        this@MainActivity.runOnUiThread {
            weatherdayadapter.notifyDataSetChanged()
            lastupdatetext.visibility = View.GONE
            lastupdatetext.text = ""
            weathernotloadedtext.visibility = View.VISIBLE
            weathernotloadedtext.text = this@MainActivity.resources.getString(R.id.weathernotloadedtext)
        }

        TextFunctions.showText(this@MainActivity, "Error info updating!")
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        initActions()
    }

    private fun initViews()
    {
        parentlayout         = findViewById(R.id.parentlayout)
        nestedscrollview     = findViewById(R.id.nestedscrollview)

        lastupdatetext       = findViewById(R.id.lastupdatetext)
        citysearchtextview   = findViewById(R.id.citysearchtextview)
        searchloadingbar     = findViewById(R.id.searchloadingbar)

        weatherlistview      = findViewById(R.id.weatherlistview)
        weathernotloadedtext = findViewById(R.id.weathernotloadedtext)

        loadinglayout        = findViewById(R.id.loading_layout)
        downloadingbar       = findViewById(R.id.downloadingbar)
        downloadingtitle     = findViewById(R.id.downloadingtitle)
    }

    private fun initActions()
    {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        this.supportActionBar?.hide()
        this.actionBar?.hide()

        autocompletecontainsadapter =
            AutoCompleteContainsAdapter(this, android.R.layout.select_dialog_item, citysearchtextview, searchloadingbar)
            {   searchrequest ->
                AppDatabase.invoke(this@MainActivity).cityBase().containsSearch(searchrequest)
            }

        citysearchtextview.setAdapter(autocompletecontainsadapter)

        citysearchtextview.setOnFocusChangeListener { _: View?, b: Boolean ->
            if (b && citysearchtextview.text.toString() == resources.getText(R.string.search_city_tint))
                citysearchtextview.setText("")
            if (!b && (citysearchtextview.text.isEmpty()))
                citysearchtextview.setText(resources.getText(R.string.search_city_tint))
        }

        citysearchtextview.setOnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
            val newcity =
                autocompletecontainsadapter.getObjectByString(citysearchtextview.text.toString())
            if (newcity != null) {
                selectedcity = newcity

                TextFunctions.saveText(Constants.LASTSELECTEDCITYSHAREDPREFS, "${newcity.id}", this@MainActivity)

                WeatherDay.getWeather(this@MainActivity, newcity, false, weatherloadcallback, weathererrorcallback)

                val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                //if (citysearchtextview != null && imm != null)
                    imm.hideSoftInputFromWindow(citysearchtextview.windowToken, 0)
                TextFunctions.showText(this, "City selected: $selectedcity")
            } else
                Log.e("NotFoundError", "City not found by input ${citysearchtextview.text} !")
        }

        weatherdayadapter = WeatherDayAdapter(weatherdays)

        weatherlistview.layoutManager = LinearLayoutManager(this)
        weatherlistview.adapter = weatherdayadapter

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.weather_layout_divider)!!)
        weatherlistview.addItemDecoration(itemDecoration)

        parentlayout.setOnRefreshListener {
            if (selectedcity != null)
                WeatherDay.getWeather(this@MainActivity, selectedcity!!, false, weatherloadcallback, weathererrorcallback)
            else
                TextFunctions.showText(this@MainActivity, "City isn't selected!")
            parentlayout.isRefreshing = false
        }

        parentlayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (firstlaunch)
            {
                firstlaunch = false

                val defaultcities = City.defaultCities(this@MainActivity)

                val database = AppDatabase.invoke(this@MainActivity)
                val count = database.cityBase().getCount()

                println("CITIES IN BASE COUNT: $count")

                if (database.cityBase().getCount() == 0)
                    database.cityBase().insertAll(defaultcities)

                val citystr = TextFunctions.loadText(Constants.LASTSELECTEDCITYSHAREDPREFS, this@MainActivity)
                if (TextFunctions.isLong(citystr))
                {
                    val citylist = database.cityBase().getById(citystr.toLong())
                    if (citylist.isNotEmpty() && weatherdays.size == 0)
                    {
                        selectedcity = citylist[0]
                        //val weatherlist = database.weatherBase().getByCity(citylist[0].id)
                        //if (weatherlist.isNotEmpty() && Date().time - weatherlist[0].update.time > 3600000)
                            WeatherDay.getWeather(this@MainActivity, selectedcity!!, false, weatherloadcallback, weathererrorcallback)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED ||
                     ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                {
                    /*if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        Dialogs.showPermissionsDescription(this@MainActivity)
                    else*/
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constants.SAVE_CITIES_ARCHIVE_MEMORY_PERMISSION)
                }
                else
                    City.updateCities(this@MainActivity)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.SAVE_CITIES_ARCHIVE_MEMORY_PERMISSION) {
            var allPermissions = true
            for (i in permissions.indices)
                if ((permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE ||
                     permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    allPermissions = false
                    break
                }
            if (allPermissions)
                City.updateCities(this@MainActivity)
            else
                TextFunctions.showText(this@MainActivity, "We can't update cities list without memory permission!")
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                v.clearFocus()
                hideKeyboard(v)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun hideKeyboard(v: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (v != null) {
            this@MainActivity.runOnUiThread {
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }
}