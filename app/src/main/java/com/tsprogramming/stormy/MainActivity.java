package com.tsprogramming.stormy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.tsprogramming.stormy.weather.Current;
import com.tsprogramming.stormy.weather.Forecast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    FusedLocationProviderClient fusedLocationProviderClient;
    public double latitude, longitude;
    Geocoder geocoder;
    List<Address> addresses;
    protected Location lastLocation;
    String cityName;

    private Forecast mForecast;
    @BindView(R.id.timeLabel)
    TextView mTimeLabel;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.humidityValue)
    TextView mHumidityValue;
    @BindView(R.id.precipValue)
    TextView mPrecipValue;
    @BindView(R.id.summaryLabel)
    TextView mSummaryLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.locationLabel)
    TextView mLocationLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.VISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDisplay();
            }
        });

        String forecastURL = "https://api.darksky.net/forecast/2dcd73471199d7947c1d4701c44d9d57/40,32 "; //+ latitude + "," + longitude;
        String apiKey = "2dcd73471199d7947c1d4701c44d9d57";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {

            addresses =(geocoder.getFromLocation(latitude, longitude, 1));
            Log.d(TAG, "" + addresses.get(0));
            cityName = addresses.get(0).getLocality();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isNetworkAvailable()) {
            toggleRefresh();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastURL).build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "Main UI code is running");


        if (!checkPermissions()) {

            requestPermissions();

        } else {

            getLastLocation();
            Log.d(TAG, "" + longitude + latitude);

        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        } else {
            mRefreshImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        Current current = mForecast.getmCurrent();
        mTemperatureLabel.setText(current.getmTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        mHumidityValue.setText(current.getmHumidity() + "");
        mPrecipValue.setText(current.getmPrecipChance() + "%");
        mSummaryLabel.setText(current.getmSummary());
        mLocationLabel.setText(cityName);
        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException{
        Forecast forecast = new Forecast();

        forecast.setmCurrent(getCurrentDetails(jsonData));
        return forecast;
    }



    private Current getCurrentDetails(String jsonData) throws JSONException{
    JSONObject forecast = new JSONObject(jsonData);
    String timezone = forecast.getString("timezone");
    Log.i(TAG, "From JSON: " + timezone);
    JSONObject currently = forecast.getJSONObject("currently");
    Current current = new Current();
    current.setmHumidity(currently.getDouble("humidity"));
        current.setmTime(currently.getLong("time"));
        current.setmIcon(currently.getString("icon"));
        current.setmPrecipChance(currently.getDouble("precipProbability"));
        current.setmSummary(currently.getString("summary"));
        current.setmTemperature(currently.getDouble("temperature"));
        current.setmTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());

    return current;
    }


    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
AlertDialogFragment dialog = new AlertDialogFragment();
dialog.show(getFragmentManager(), "error_dialog");

    }
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;


    }
    @SuppressWarnings("MissingPermission")
    private void getLastLocation(){
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                lastLocation = task.getResult();
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                Log.d(TAG, "" + longitude + latitude);
            }});
    }
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale) {
            startLocationPermissionRequest();
        }
    }
    private void startLocationPermissionRequest() {

        ActivityCompat.requestPermissions(MainActivity.this,

                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},

                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}
