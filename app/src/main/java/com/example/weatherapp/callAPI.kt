package com.example.weatherapp

import org.json.JSONObject
import java.net.URL

class callAPI(private val latitude:Double, private val longitude:Double) {

    fun CurrentWeatherAPI(API:String):String{
        val Current_Weather_response:String
        Current_Weather_response = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$API").readText(
            Charsets.UTF_8
        )
      return Current_Weather_response
    }

    fun AirQualityAPI():String{
        val Air_Quality_response:String
        Air_Quality_response = URL("https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&hourly=pm10,nitrogen_dioxide,ozone").readText(
            Charsets.UTF_8
        )
        val JsonObj = JSONObject(Air_Quality_response)
        val hourly = JsonObj.getJSONObject("hourly")
        var pm10 = 0.0
        var NO2 = 0.0
        var O3 = 0.0
        for (i in 0..23){
            pm10 += hourly.getJSONArray("pm10").getDouble(i)
            NO2 += hourly.getJSONArray("nitrogen_dioxide").getDouble(i)
            O3 += hourly.getJSONArray("ozone").getDouble(i)
        }
        pm10 /= 24
        NO2 /= 24
        O3 /= 24

        if(NO2<50 && pm10<25 && O3<60)
            return "Good"
        else if (NO2<100 && pm10<50 && O3<120)
            return "Fair"
        else if(NO2<200 && pm10<90 && O3<180)
            return "Moderate"
        else if(NO2<400 && pm10<180 && O3<240)
            return "Poor"
        else return "Hazardous"
    }

    fun ForecastAPI():String{
        val Forecast_response:String
        Forecast_response = URL("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&hourly=temperature_2m,precipitation").readText(
            Charsets.UTF_8
        )
        return Forecast_response
    }
}