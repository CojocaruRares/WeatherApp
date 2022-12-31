package com.example.weatherapp

data class WeatherData(
    var adress:String,
    var updateTime:String,
    var temperature:String,
    var weatherDescription:String,
    var min_temp:String,
    var max_temp:String,
    var windSpeed:String,
    var pressure:String,
    var humidity:String,
    var perceived_temp:String,
    var airQuality:String,
    var precipitation:String
)
