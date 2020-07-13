package com.nv.nvgeowatch;
 
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener locationListener;
ProgressBar progressBar;
    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;
    int i=0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
startlistning();
    }

public void startlistning(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
}

    }
    public void updatelocationinfo(final Location location){

       // progressBar.setVisibility(View.GONE);
        TextView lattextView =(TextView) findViewById(R.id.lat);
        TextView lontextView =(TextView) findViewById(R.id.lon);
        TextView alttextView =(TextView) findViewById(R.id.alt);
        TextView acctextView =(TextView) findViewById(R.id.acc);
        lattextView.setText("Latitude :  " + location.getLatitude());
        lontextView.setText("Longitude : " + location.getLongitude());
        alttextView.setText("Altitude : " + location.getAltitude());
        acctextView.setText("Accuracy : " + location.getAccuracy());
        Geocoder geocoder= new Geocoder(getApplicationContext(), Locale.getDefault());

        try {


String adress = "could not find adress";


List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
if (addresses!=null && addresses.size()>0){
  adress = "Address : ";
if (addresses.get(0).getSubThoroughfare() != null) {
    adress += addresses.get(0).getSubThoroughfare() + "";

}
    if (addresses.get(0).getSubAdminArea() != null) {
        adress += addresses.get(0).getSubAdminArea() + "\n";

    }
    if (addresses.get(0).getThoroughfare() != null) {
        adress += addresses.get(0).getThoroughfare() + "\n";

    }
    if (addresses.get(0).getLocality() != null) {
        adress += addresses.get(0).getLocality() + "\n";

    }if (addresses.get(0).getAdminArea() != null) {
        adress += addresses.get(0).getAdminArea() + "\n";

    }
    if (addresses.get(0).getPostalCode() != null) {
        adress += addresses.get(0).getPostalCode() + "\n";

    }
    if (addresses.get(0).getCountryName() != null) {
        adress += addresses.get(0).getCountryName() + "\n";


}}
            TextView textView = (TextView) findViewById(R.id.add);
            textView.setText(adress);









        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        progressBar.setProgress(i);
        progressBar.setIndeterminate(true);
         progressBar.setVisibility(View.VISIBLE);
        mCountDownTimer=new CountDownTimer(5000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {
               // Log.v("Log_tag", "Tick of Progress"+ i+ millisUntilFinished);
                i++;
                progressBar.setProgress((int)i*100/(5000/1000));

            }

            @Override
            public void onFinish() {
                i++;
                progressBar.setProgress(100);
                progressBar.setVisibility(View.GONE);
            }
        };
        mCountDownTimer.start();
            //  progressBar = (ProgressBar) findViewById(R.id.progressBar);
      //  progressBar.setIndeterminate(true);
      //  progressBar.setVisibility(View.VISIBLE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updatelocationinfo(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
           startlistning();
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            if (location!= null){
                updatelocationinfo(location);}
        }}
    }


}
