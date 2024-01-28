package th.o.th.weather;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    String apiKey = "e3b8817bdade26035f86f6dd34a2bf66";
    String units = "metric";
    String baseURL = "https://api.openweathermap.org/data/2.5/";
    Retrofit retrofit;
    WeatherService service;

    private gpsTracker gpsTracker;
    private TextView minTempTextView, maxTempTextView, cityNameTextView,
            sunRiseTextView, sunSetTextView, pressureTextView, latitudeTextView,
            windSpeedTextView, longitudeTextView, visibilityTextView, tempTextView,
            timeZoneTextView, humidityTextView, feelsLikeTextView;
    double latitude, longitude;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //force the app to always be on night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        timeZoneTextView = findViewById(R.id.timeZone);
        feelsLikeTextView = findViewById(R.id.feelsLike);
        tempTextView = findViewById(R.id.temp);
        humidityTextView = findViewById(R.id.humidity);
        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        windSpeedTextView = findViewById(R.id.windSpeed);
        visibilityTextView = findViewById(R.id.visibility);
        pressureTextView = findViewById(R.id.pressure);
        cityNameTextView = findViewById(R.id.cityName);
        sunRiseTextView = findViewById(R.id.sunRise);
        sunSetTextView = findViewById(R.id.sunSet);
        imageView = findViewById(R.id.weatherIcon);
        minTempTextView = findViewById(R.id.minTemp);
        maxTempTextView = findViewById(R.id.maxTemp);


        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isNetworkAvailable()) {
                fetchData("london");
            } else {
                Toast.makeText(MainActivity.this, "Network Error !!!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onQueryTextSubmit(String city) {
                cityNameTextView.setText("" + city);

                if (isNetworkAvailable()) {
                    fetchData(city);
                } else {
                    Toast.makeText(MainActivity.this, "Network Error !!!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform filtering or live search as the text changes
                return true;
            }
        });

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.about:
                Intent intent = new Intent(MainActivity.this, about.class);
                startActivity(intent);
                break;
            case R.id.myLocation:
                getLocation();
                if (isNetworkAvailable()) {
                    Call<WeatherResponse> call = service.getWeatherByCoordinates(latitude, longitude, apiKey, units);
                    call.enqueue(new Callback<WeatherResponse>() {
                        @Override
                        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                            if (response.isSuccessful()) {
                                WeatherResponse weatherResponse = response.body();
                                if (weatherResponse != null) {

                                    double temperature = weatherResponse.getWeatherData().getTemperature();
                                    double visibility = weatherResponse.getVisibility();
                                    int humidity = weatherResponse.getWeatherData().getHumidity();
                                    int pressure = weatherResponse.getWeatherData().getPressure();
                                    double latitude = weatherResponse.getCoordinates().getLatitude();
                                    double longitude = weatherResponse.getCoordinates().getLongitude();
                                    double windSpeed = weatherResponse.getWindData().speed;

                                    WeatherResponse cityNameResponse = response.body();
                                    String cityName = cityNameResponse.getCityName();
                                    cityNameTextView.setText(cityName);

                                    double feelsLike = weatherResponse.getWeatherData().getFeelsLike();
                                    double tempMax = weatherResponse.getWeatherData().getTempMax();
                                    double tempMin = weatherResponse.getWeatherData().getTempMin();


                                    feelsLikeTextView.setText(feelsLike + "°C");
                                    maxTempTextView.setText(tempMax + "°C");
                                    minTempTextView.setText(tempMin + "°C");

                                    windSpeedTextView.setText("Wind Speed: " + windSpeed + "m/s");
                                    tempTextView.setText(temperature + "°C");
                                    pressureTextView.setText("Pressure: " + pressure + "hPa");
                                    humidityTextView.setText("Humidity: " + humidity + "%");
                                    latitudeTextView.setText("" + latitude);
                                    longitudeTextView.setText("" + longitude);
                                    visibilityTextView.setText(visibility + " Meter");

                                    String sunRise = convertTimestampToTime(weatherResponse.getSysData().getSunrise());
                                    String sunSet = convertTimestampToTime(weatherResponse.getSysData().getSunset());
                                    String timeZone = convertTimestampToTime(weatherResponse.getTimezone());
                                    sunRiseTextView.setText("Sun Rise: " + sunRise);
                                    sunSetTextView.setText("Sun Set: " + sunSet);
                                    timeZoneTextView.setText(timeZone);

                                    WeatherResponse iconList = response.body();
                                    List<WeatherResponse.WeatherData> weatherDataList = iconList.getWeatherDataList();

                                    String weatherIcon = weatherDataList.get(0).getIcon(); // Assuming you only need the first weather icon
                                    setIcon(imageView, weatherIcon);

                                }
                            } else {
                                // Handle error
                                Log.e("WeatherRequest", "Request failed. Response code: " + response.code());
                            }


                        }

                        @Override
                        public void onFailure(Call<WeatherResponse> call, Throwable t) {

                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Network Error !!!", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.forCast:
                Intent intent1 = new Intent(MainActivity.this, ForecastActivity.class);
                startActivity(intent1);
        }

        return super.onOptionsItemSelected(item);
    }

    public class WeatherResponse {
        @SerializedName("weather")
        private List<WeatherData> weatherDataList;
        @SerializedName("name")
        private String cityName;
        @SerializedName("visibility")
        private double visibility;
        @SerializedName("main")
        private WeatherData weatherData;
        @SerializedName("sys")
        private SysData sysData;
        @SerializedName("coord")
        private Coordinates coordinates;
        @SerializedName("timezone")
        private long timezone;
        @SerializedName("wind")
        private WindData windData;

        public List<WeatherData> getWeatherDataList() {
            return weatherDataList;
        }

        public class WindData {
            @SerializedName("speed")
            private double speed;

            // Getter method for speed
        }

        public double getVisibility() {
            return visibility;
        }

        public WindData getWindData() {
            return windData;
        }

        public String getCityName() {
            return cityName;
        }

        public class WeatherData {
            @SerializedName("icon")
            private String icon;
            @SerializedName("temp")
            private double temperature;
            @SerializedName("humidity")
            private int humidity;
            @SerializedName("pressure")
            private int pressure;
            @SerializedName("feels_like")
            private double feelsLike;
            @SerializedName("temp_max")
            private double tempMax;
            @SerializedName("temp_min")
            private double tempMin;

            public String getIcon() {
                return icon;
            }

            public double getTemperature() {
                return temperature;
            }

            public int getHumidity() {
                return humidity;
            }

            public int getPressure() {
                return pressure;
            }

            public double getFeelsLike() {
                return feelsLike;
            }

            public double getTempMax() {
                return tempMax;
            }

            public double getTempMin() {
                return tempMin;
            }
        }

        public class SysData {
            @SerializedName("sunrise")
            private long sunrise;
            @SerializedName("sunset")
            private long sunset;

            public long getSunrise() {
                return sunrise;
            }

            public long getSunset() {
                return sunset;
            }
        }

        public class Coordinates {
            @SerializedName("lat")
            private double latitude;
            @SerializedName("lon")
            private double longitude;

            public double getLatitude() {
                return latitude;
            }

            public double getLongitude() {
                return longitude;
            }
        }

        public WeatherData getWeatherData() {
            return weatherData;
        }

        public SysData getSysData() {
            return sysData;
        }

        public Coordinates getCoordinates() {
            return coordinates;
        }

        public long getTimezone() {
            return timezone;
        }
    }

    private String convertTimestampToTime(long timestamp) {
        Date date = new Date(timestamp * 1000L); // Convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public void getLocation() {
        gpsTracker = new gpsTracker(MainActivity.this);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();

        } else {
            gpsTracker.showSettingsAlert();
        }

    }

    void fetchData(String city) {
        Call<WeatherResponse> call = service.getWeatherByCity(city, apiKey, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {

                        double temperature = weatherResponse.getWeatherData().getTemperature();
                        double visibility = weatherResponse.getVisibility();
                        int humidity = weatherResponse.getWeatherData().getHumidity();
                        int pressure = weatherResponse.getWeatherData().getPressure();
                        double latitude = weatherResponse.getCoordinates().getLatitude();
                        double longitude = weatherResponse.getCoordinates().getLongitude();
                        double windSpeed = weatherResponse.getWindData().speed;

                        WeatherResponse cityNameResponse = response.body();
                        String cityName = cityNameResponse.getCityName();
                        cityNameTextView.setText(cityName);

                        double feelsLike = weatherResponse.getWeatherData().getFeelsLike();
                        double tempMax = weatherResponse.getWeatherData().getTempMax();
                        double tempMin = weatherResponse.getWeatherData().getTempMin();


                        feelsLikeTextView.setText(feelsLike + "°C");
                        maxTempTextView.setText(tempMax + "°C");
                        minTempTextView.setText(tempMin + "°C");

                        windSpeedTextView.setText("Wind Speed: " + windSpeed + "m/s");
                        tempTextView.setText(temperature + "°C");
                        pressureTextView.setText("Pressure: " + pressure + "hPa");
                        humidityTextView.setText("Humidity: " + humidity + "%");
                        latitudeTextView.setText("" + latitude);
                        longitudeTextView.setText("" + longitude);
                        visibilityTextView.setText(visibility + " Meter");

                        String sunRise = convertTimestampToTime(weatherResponse.getSysData().getSunrise());
                        String sunSet = convertTimestampToTime(weatherResponse.getSysData().getSunset());
                        String timeZone = convertTimestampToTime(weatherResponse.getTimezone());
                        sunRiseTextView.setText("Sun Rise: " + sunRise);
                        sunSetTextView.setText("Sun Set: " + sunSet);
                        timeZoneTextView.setText(timeZone);

                        WeatherResponse iconList = response.body();
                        List<WeatherResponse.WeatherData> weatherDataList = iconList.getWeatherDataList();

                        String weatherIcon = weatherDataList.get(0).getIcon(); // Assuming you only need the first weather icon

                        setIcon(imageView, weatherIcon);

                    }
                } else {
                    // Handle error
                    Log.e("WeatherRequest", "Request failed. Response code: " + response.code());
                }


            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {

            }
        });
    }

    void setIcon(ImageView imageView, String weatherIcon) {
        if (weatherIcon.equals("01d")) {
            imageView.setImageResource(R.drawable.clearsky);
        } else if (weatherIcon.equals("01n")) {
            imageView.setImageResource(R.drawable.clearsky);
        } else if (weatherIcon.equals("02n")) {
            imageView.setImageResource(R.drawable.fewclouds);
        } else if (weatherIcon.equals("02d")) {
            imageView.setImageResource(R.drawable.fewclouds);
        } else if (weatherIcon.equals("03n")) {
            imageView.setImageResource(R.drawable.clouds);
        } else if (weatherIcon.equals("03d")) {
            imageView.setImageResource(R.drawable.clouds);
        } else if (weatherIcon.equals("04n")) {
            imageView.setImageResource(R.drawable.brokenclouds);
        } else if (weatherIcon.equals("04d")) {
            imageView.setImageResource(R.drawable.brokenclouds);
        } else if (weatherIcon.equals("09n")) {
            imageView.setImageResource(R.drawable.rain);
        } else if (weatherIcon.equals("09d")) {
            imageView.setImageResource(R.drawable.rain);
        } else if (weatherIcon.equals("10n")) {
            imageView.setImageResource(R.drawable.raintwo);
        } else if (weatherIcon.equals("10d")) {
            imageView.setImageResource(R.drawable.raintwo);
        } else if (weatherIcon.equals("11n")) {
            imageView.setImageResource(R.drawable.thunder);
        } else if (weatherIcon.equals("11d")) {
            imageView.setImageResource(R.drawable.thunder);
        } else if (weatherIcon.equals("13n")) {
            imageView.setImageResource(R.drawable.snow);
        } else if (weatherIcon.equals("13d")) {
            imageView.setImageResource(R.drawable.snow);
        } else if (weatherIcon.equals("50n")) {
            imageView.setImageResource(R.drawable.mist);
        } else if (weatherIcon.equals("50d")) {
            imageView.setImageResource(R.drawable.mist);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}



