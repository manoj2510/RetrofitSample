package com.example.weatherapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.weatherapplication.weather.Current;
import com.example.weatherapplication.weather.Location;
import com.example.weatherapplication.weather.Weather;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView mCurrentWeather, mCurrentTemperature, mCurrentCity;
    private ImageView mCurrentWeatherIcon;
    private CoordinatorLayout mCoordinatorLayout;
    private OkHttpClient.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (isInternetConnected()) {
            callService();
        } else {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Internet not available", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    private void initView() {
        mCurrentWeather = findViewById(R.id.current_weather);
        mCurrentTemperature = findViewById(R.id.current_temperature);
        mCurrentCity = findViewById(R.id.current_city);
        mCurrentWeatherIcon = findViewById(R.id.current_weather_icon);
        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    private void callService() {
        String apiKey = "cc70283f87dc7273bc4c1253b5e07a9b";
        String serviceName = "http://api.weatherstack.com/";

        builder = getHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serviceName)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();
        WeatherApi gi = retrofit.create(WeatherApi.class);
        Call<Weather> call = gi.getExpectedList(apiKey, "Mumbai");
        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Log.e("Okay", "onResponse: 1");
                if (response != null && response.body() != null) {
                    Weather weather = response.body();
                    Location location = weather.getLocation();
                    Log.e("Okay", "onResponse: 2");
                    if (weather.getCurrent() != null) {
                        Current current = weather.getCurrent();
                        Log.e("Okay", "onResponse: 3");
                        setValues(location, current);
                    }
                }else {
                    showSnackBar();
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                showSnackBar();
            }
        });

    }

    private void setValues(Location location, Current current) {
        String temp = String.valueOf(current.getTemperature());
        String IMAGE_URL = current.getWeatherIcons().get(0);
        String city = location.getName();
        mCurrentWeather.setText(current.getWeatherDescriptions().get(0));
        mCurrentTemperature.setText(temp + "  " + getResources().getString(R.string.degree_celsius));
        mCurrentCity.setText(city);
        Picasso.get()
                .load(IMAGE_URL)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(mCurrentWeatherIcon);
    }

    private void showSnackBar() {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Something went wrong!", Snackbar.LENGTH_INDEFINITE)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar snackbar1 = Snackbar.make(mCoordinatorLayout, "Undo Successful", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });
        snackbar.show();
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else
            return false;
    }

    public OkHttpClient.Builder getHttpClient() {
        if (builder == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder client = new OkHttpClient.Builder();
            client.addInterceptor(loggingInterceptor);
            client.writeTimeout(60000, TimeUnit.MILLISECONDS);
            client.readTimeout(60000, TimeUnit.MILLISECONDS);
            client.connectTimeout(60000, TimeUnit.MILLISECONDS);
            return client;
        }
        return builder;
    }
}
