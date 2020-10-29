package maciek.w.runtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.Polyline;
import com.google.android.libraries.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final int REQUEST_LOCATION_PERMITION = 99;
    GoogleMap map;
    boolean start = false;
    boolean chronomWork;
    Double prevLat;
    Double prevLon;
    Double totDist=0.0;
    Double velocity;
    private static final String TAG = "DB";

    private TextView textViewLatStart;
    private TextView textViewLatStop;
    private TextView textViewLonStart;
    private TextView textViewLonStop;
    private TextView textViewDist;
    private TextView textViewTime;
    private TextView textViewVel;
    private TextView textViewVelLabel;
    private TextView textViewSTOP;
    private Button buttonStartStop;
    private Double StartLat, StartLon, StopLat, StopLon;
    private Chronometer chronometer;

    private ArrayList<DeltaDTV> intervals = new ArrayList<DeltaDTV>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //enableMyLocation();

        textViewLatStart = (TextView) findViewById(R.id.textViewLatStart);
        textViewLatStop = (TextView) findViewById(R.id.textViewLatStop);
        textViewLonStart = (TextView) findViewById(R.id.textViewLonStart);
        textViewLonStop = (TextView) findViewById(R.id.textViewLonStop);
        textViewDist = (TextView) findViewById(R.id.textViewDist);
//        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewVel = (TextView) findViewById(R.id.textViewVel);
        textViewVelLabel = (TextView) findViewById(R.id.textViewVelLabel);
        textViewSTOP = (TextView) findViewById(R.id.textViewSTOP);
        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        chronometer = (Chronometer) findViewById(R.id.chronometer);


        final distCalc calculator = new distCalc(0.0, 0.0, 0.0, 0.0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng currentLoc;

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();


                // if started take current loc as STOP and calc dist
                if (start) {

                    textViewLatStop.setText(String.valueOf(latitude));
                    textViewLonStop.setText(String.valueOf(longitude));
                    StopLat = latitude;
                    StopLon = longitude;
                    start = !start;
                    buttonStartStop.setText("START");
                    textViewSTOP.setText("STOP");
                    textViewVelLabel.setText("Avr Vel: ");
                    textViewVel.setText(String.valueOf(totDist/(SystemClock.elapsedRealtime()-chronometer.getBase())/1000));

                    chronometer.stop();



                } else { //is not started take current loc as START and start chronometer

                    textViewLatStart.setText(String.valueOf(latitude));
                    textViewLonStart.setText(String.valueOf(longitude));
                    prevLat = latitude;
                    prevLon = longitude;
//                    StartLat=latitude;
//                    StartLon= longitude;
                    textViewLatStop.setText("");
                    textViewLonStop.setText("");
                    start = !start;
                    buttonStartStop.setText("STOP");
                    textViewSTOP.setText("CURRENT");
                    textViewVelLabel.setText("Cur Vel: ");

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    map.clear();

                }


            }
        });

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {


                if (SystemClock.elapsedRealtime() - chronometer.getBase() > 900) {

                    Log.i(TAG, "Tick 1");
                    LatLng currentLoc;
                    distCalc delta = new distCalc(0.0, 0.0, 1.0, 1.0);

                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }


                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    delta.setLoc(prevLat, prevLon, latitude, longitude);

                    totDist = totDist + delta.getDist();
                    textViewDist.setText(String.valueOf(totDist));


                    intervals.add(new DeltaDTV(delta.getDist(), 1.0));

                    Polyline line = map.addPolyline(new PolylineOptions()
                            .add(new LatLng(prevLat,prevLon), new LatLng(latitude, longitude))
                            .width(5).color(Color.RED));


                    if (intervals.size() < 5) {
                        Double vel = 0.0;
                        for (DeltaDTV interv : intervals) {
                            vel = vel + interv.getdDist();
                        }
                        velocity = vel / intervals.size();
                    } else {

                        Double vel = 0.0;
                        for (int i = intervals.size() - 5; i < intervals.size() - 1; i++) {
                            vel = vel + intervals.get(i).getdDist();
                        }
                        velocity = vel / 5;
                    }
                    textViewVel.setText(String.valueOf(velocity));
                    prevLat = latitude;
                    prevLon = longitude;
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng Krakow = new LatLng(50.039269, 19.926125);

        map.moveCamera(CameraUpdateFactory.newLatLng(Krakow));
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMITION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMITION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

}