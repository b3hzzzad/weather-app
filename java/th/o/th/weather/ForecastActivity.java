package th.o.th.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ForecastActivity extends AppCompatActivity {

    String apiKey = "e3b8817bdade26035f86f6dd34a2bf66";
    private RecyclerView recyclerView;
    private WeatherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeatherAdapter();
        recyclerView.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<WeatherData> call = apiInterface.getWeatherData("paris", "metric", apiKey);
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful()) {
                    List<WeatherList> weatherList = response.body().getList();
                    if (weatherList != null && weatherList.size() > 0) {
                        // Get weather data for the next 5 days
                        List<WeatherList> nextFiveDays = weatherList.subList(0, 5);
                        adapter.setData(nextFiveDays);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {

            }
        });
    }

    public class WeatherData {
        @SerializedName("list")
        @Expose
        private List<WeatherList> list = null;

        public List<WeatherList> getList() {
            return list;
        }

        public void setList(List<WeatherList> list) {
            this.list = list;
        }
    }

    public class WeatherList {
        @SerializedName("dt")
        @Expose
        private Integer dt;
        @SerializedName("main")
        @Expose
        private Main main;
        @SerializedName("weather")
        @Expose
        private List<Weather> weather = null;

        public Integer getDt() {
            return dt;
        }

        public void setDt(Integer dt) {
            this.dt = dt;
        }

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public void setWeather(List<Weather> weather) {
            this.weather = weather;
        }

        public String getDate() {
            if (dt != null) {
                Date date = new Date(dt * 1000);
                SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return format.format(date);
            }
            return null;
        }
    }

    public class Main {
        @SerializedName("temp")
        @Expose
        private Double temp;

        @SerializedName("temp_max")
        private double tempMax;
        @SerializedName("temp_min")
        private double tempMin;

        public Double getTemp() {
            return temp;
        }

        public Double getTempMax() {
            return tempMax;
        }

        public Double getTempMin() {
            return tempMin;
        }

        public void setTemp(Double temp) {
            this.temp = temp;
        }
    }

    public class Weather {
        @SerializedName("temp_max")
        private double tempMax;
        @SerializedName("temp_min")
        private double tempMin;

        public double getTempMin() {
            return tempMin;
        }

        public double getTempMax() {
            return tempMax;
        }

        @SerializedName("description")
        @Expose
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public interface ApiInterface {
        @GET("forecast")
        Call<WeatherData> getWeatherData(
                @Query("q") String city,
                @Query("units") String units,
                @Query("appid") String apiKey);
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
        private List<WeatherList> weatherList;

        public void setData(List<WeatherList> data) {
            weatherList = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeatherList weather = weatherList.get(position);

            // Set the data to the views in the ViewHolder
            holder.textViewMaxTemp.setText("Max Temp " + weather.getMain().getTempMax());
            holder.textViewMinTemp.setText(String.valueOf("Min Temp " + weather.getMain().getTempMin()));
            holder.textViewDescription.setText(weather.getWeather().get(0).getDescription());
            holder.textViewDate.setText(weather.getDate());

            // Load icon using Glide
//            String iconUrl = "https://openweathermap.org/img/w/" + weather.getWeather().get(0).getIcon() + ".png";
//            Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.imageViewIcon);

        }

        @Override
        public int getItemCount() {
            return weatherList != null ? weatherList.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageViewIcon;
            TextView textViewMaxTemp;
            TextView textViewMinTemp;
            TextView textViewDescription;
            TextView textViewDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
                textViewMaxTemp = itemView.findViewById(R.id.textViewMaxTemp);
                textViewMinTemp = itemView.findViewById(R.id.textViewMinTemp);
                textViewDescription = itemView.findViewById(R.id.textViewDescription);
                textViewDate = itemView.findViewById(R.id.textViewDate);

            }
        }
    }

    private String convertTimestampToTime(long timestamp) {
        Date date = new Date(timestamp * 1000L); // Convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }


}