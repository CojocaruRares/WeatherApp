package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var latitude = 47.15  // } -> Hardcoded latitude and longitude in case we don't receive location permission
    private var longitude = 27.58 // }
    private val API_key:String = "cb10ef58aa501287bddf82e8acc42318" // key for openWeather API
    private  lateinit var APIcaller:callAPI // object that calls multiple APIs
    private lateinit var weatherData:WeatherData // object that stores data wich will be displayed
    private lateinit var HourlyWeatherList:ArrayList<ItemData> // array of items ( each item represents an hour )
    private lateinit var ItemRecyclerView:RecyclerView // view group of items
    private lateinit var LocationProvider: FusedLocationProviderClient
    private val weatherIcon = mutableMapOf<String,String>(
        "Clear" to "https://cdn-icons-png.flaticon.com/128/4814/4814268.png",
        "Clouds" to "https://cdn-icons-png.flaticon.com/128/4503/4503018.png",
        "Rain" to "https://cdn-icons-png.flaticon.com/128/1163/1163626.png",
        "Drizzle" to "https://cdn-icons-png.flaticon.com/128/1163/1163626.png",
        "ThunderStorm" to "https://cdn-icons-png.flaticon.com/128/3445/3445722.png",
        "Snow" to "https://cdn-icons-png.flaticon.com/128/1779/1779887.png",
    ) // OpenWeather API sends a specific string ( ex. Snow ) based on weather state
      // So, in order to display a sugestive icon, we store the url of images in a hashmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ItemRecyclerView= findViewById(R.id.hourlyItem)
        ItemRecyclerView.setHasFixedSize(true)
        HourlyWeatherList = arrayListOf()
        LocationProvider = LocationServices.getFusedLocationProviderClient(this)
        CheckPermission()
        APIcaller = callAPI(latitude,longitude)
        StartApp()
    }

    private fun SetUI(data:WeatherData){
        val currentTime = Calendar.getInstance()
        val currentHourIn24Format: Int =currentTime.get(Calendar.HOUR_OF_DAY) // return the hour in 24 hrs format (ranging from 0-23)
        val image:ImageView = findViewById(R.id.icon)
        findViewById<TextView>(R.id.location).text = data.adress
        findViewById<TextView>(R.id.update_time).text = data.updateTime
        findViewById<TextView>(R.id.current_temp).text = data.temperature
        findViewById<TextView>(R.id.current_state).text = data.weatherDescription
        findViewById<TextView>(R.id.min_temp).text = data.min_temp
        findViewById<TextView>(R.id.max_temp).text = data.max_temp
        findViewById<TextView>(R.id.wind).text = data.windSpeed
        findViewById<TextView>(R.id.pressure).text = data.pressure
        findViewById<TextView>(R.id.humidity).text = data.humidity
        findViewById<TextView>(R.id.perceived_temp).text = data.perceived_temp
        findViewById<TextView>(R.id.air_quality).text = data.airQuality
        findViewById<TextView>(R.id.precipitation).text = data.precipitation
        // Get image from URL with Picasso API
        if(data.weatherDescription in weatherIcon) {
            //In case it's night time, show a corresponding image
            if ((data.weatherDescription == "Clear") && ((currentHourIn24Format >= 20 && currentHourIn24Format <= 23) || (currentHourIn24Format >= 0 && currentHourIn24Format <= 4)))
                Picasso.get().load("https://cdn-icons-png.flaticon.com/512/3026/3026366.png").resize(350, 350)
            else
                Picasso.get().load(weatherIcon[data.weatherDescription]).resize(350, 350)
                    .into(image)
        }
        else
            Picasso.get().load("https://cdn-icons-png.flaticon.com/128/305/305834.png").resize(350,350).into(image)
    }

    @SuppressLint("MissingPermission")
    fun getLocation(){
        LocationProvider.lastLocation.addOnSuccessListener {
            if(it!=null) {
                it.apply {
                    longitude = it.longitude
                    latitude = it.latitude
                }
            }
        }
    }
    //check location permission
    private fun CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),1)
        } else {
          getLocation()
        }
    }

    //fetch data from APIs in background using IO thread and then set user interface in main thread
    @OptIn(DelicateCoroutinesApi::class)
    fun StartApp(){
        GlobalScope.launch(Dispatchers.IO){

            val Weather_response = APIcaller.CurrentWeatherAPI(API_key)
            val Air_quality = APIcaller.AirQualityAPI()
            val Forecast_response = APIcaller.ForecastAPI()

            launch(Dispatchers.Main){
                // Parse Json document and get relevant data
                var jsonObj = JSONObject(Weather_response)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a",
                    Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getInt("temp").toString()+"°C"
                val min_temp= main.getInt("temp_min").toString()
                val max_temp = main.getInt("temp_max").toString()+"°C"
                val perceived_temp = main.getInt("feels_like").toString()+"°C"
                val pressure = main.getString("pressure")+" hPa"
                val humidity = main.getString("humidity")+'%'
                val windSpeed = wind.getString("speed")+" m/s"
                val weatherDescription = weather.getString("main")
                val address = jsonObj.getString("name")+", "+sys.getString("country")
                jsonObj = JSONObject(Forecast_response)
                val hourly = jsonObj.getJSONObject("hourly")
                var Precipitation_Sum = 0.0 // sum of precipitation for 24 hours
                for (i in 0..23) {
                    Precipitation_Sum += hourly.getJSONArray("precipitation").getDouble(i)
                    val item_time = hourly.getJSONArray("time").getString(i).substring(11)
                    val item_temp = hourly.getJSONArray("temperature_2m").getInt(i).toString()
                    HourlyWeatherList.add(ItemData(item_time,item_temp))
                }
                ItemRecyclerView.adapter = ItemAdapter(HourlyWeatherList)

                weatherData=WeatherData(address,updatedAtText,temp,weatherDescription,min_temp,
                    max_temp, windSpeed, pressure, humidity, perceived_temp,Air_quality,Precipitation_Sum.toString().substring(0,3)+" mm")

                SetUI(weatherData)
            }
        }
    }
}