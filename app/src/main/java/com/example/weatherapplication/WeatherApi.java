package com.example.weatherapplication;

import com.example.weatherapplication.weather.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("current?")
    Call<Weather> getExpectedList(@Query("access_key") String apiKey, @Query("query") String city);

}
