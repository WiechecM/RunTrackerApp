package maciek.w.runtracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.Polyline;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int REQUEST_LOCATION_PERMITION = 99;
    GoogleMap map;
    boolean start = false;
    Double prevLat;
    Double prevLon;
    Double totDist=0.0;
    Double velocity;
    Double totTime=0.0;
    private static final String TAG = "DB";


    private TextView textViewDist;
    private TextView textViewVel;
    private TextView textViewVelLabel;
    private ImageButton buttonStartStop;
    private TextView textViewDistUnits;
    private TextView textViewVelUnits;
    private Double StartLat, StartLon, StopLat, StopLon;
    private Chronometer chronometer;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;

    private ArrayList<DeltaDTV> intervals = new ArrayList<DeltaDTV>();
    private ArrayList<Double> paceMetric = new ArrayList<Double>();
    private ArrayList<Double> paceImperial = new ArrayList<Double>();



    //double tap back button to exit the app
    boolean doubleBackToExitPressedOnce = false;

   
    //closing the app after double click the back button
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            System.exit(0);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    //options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_in:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                overridePendingTransition(0,0);
                return true;

            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0,0);
                return true;

            case R.id.sign_up:
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                overridePendingTransition(0,0);
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //clear database for debug purpose
//        DataBaseHalper dataBaseHalper = new DataBaseHalper(this);
//        dataBaseHalper.clearDatabase();
//
//        sharedPreferences = getSharedPreferences(
//                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("firstStart",true);
//        editor.apply();



        //initialization during first launch
        appInit();

        textViewDist = (TextView) findViewById(R.id.textViewDist);
//        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewVel = (TextView) findViewById(R.id.textViewVel);
        textViewVelLabel = (TextView) findViewById(R.id.textViewVelLabel);
        buttonStartStop = (ImageButton) findViewById(R.id.buttonStartStop);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        textViewDistUnits = (TextView) findViewById(R.id.textViewDistUnits);
        textViewVelUnits = (TextView) findViewById(R.id.textViewVelUnits);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_menu));

        //navigation bar
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.training);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.friends:
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.training:
                        return true;
                    case R.id.history:
                        startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.statistics:
                        startActivity(new Intent(getApplicationContext(), StatisticActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        //prepare the map
        final distCalc calculator = new distCalc(0.0, 0.0, 0.0, 0.0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // start button listener
        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng currentLoc;

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();


                // if started take current loc as STOP and calc dist
                if (start) {

                    StopLat = latitude;
                    StopLon = longitude;
                    start = !start;
                    buttonStartStop.setBackground(getDrawable(R.drawable.ic_play));
                    textViewVelLabel.setText("Avr Vel: ");

                    totTime=  (SystemClock.elapsedRealtime()-chronometer.getBase())/1000.0;
                    textViewVel.setText(String.valueOf(totDist/totTime));

                    chronometer.stop();
                    // enable the navigation bar
                    for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                        bottomNavigationView.getMenu().getItem(i).setEnabled(true);
                    }

                    //saving the training in the database
                    saveTraining();

                } else { //is not started take current loc as START and start chronometer


                    prevLat = latitude;
                    prevLon = longitude;
                    start = !start;
                    buttonStartStop.setBackground(getDrawable(R.drawable.ic_stop));
                    textViewVelLabel.setText("Cur Vel: ");

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    map.clear();

                    //disable the navigation bar
                    for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                        bottomNavigationView.getMenu().getItem(i).setEnabled(false);
                    }

                    //disable the toolbar

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
                        return;
                    }


                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    //calculate the distance and vel in this second
                    delta.setLoc(prevLat, prevLon, latitude, longitude);

                    // update the intervals array
                    DeltaDTV deltaDTV = new DeltaDTV(delta.getDist(), 1.0);
                    intervals.add(deltaDTV);

                    // draw a line on the map
                    Polyline line = map.addPolyline(new PolylineOptions()
                            .add(new LatLng(prevLat,prevLon), new LatLng(latitude, longitude))
                            .width(5).color(Color.RED));


                    //calculate the average velocity
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

                    // update the total distance and check if it reached 100m interval
                    checkPace(deltaDTV);
                    totDist = totDist + delta.getDist();
                    textViewDist.setText(String.valueOf(totDist));

                    // update previous coordinates
                    prevLat = latitude;
                    prevLon = longitude;
                }
            }
        });

        update();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMITION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    public Boolean checkPace(DeltaDTV delta){


        SharedPreferences sharedPreferences =getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        Boolean units = sharedPreferences.getBoolean("Units",false);


        // if metric or imperial
        if (units){
            //imperial
            //smallest step is 1/8 of mile

        }
        else {
            //metric
            // smallest step is 100m

            Double totDistAfterStep = totDist + delta.getdDist();
            int  intervNoBefore = (int) ((totDist - totDist % 100) / 100);
            int intervNoAfter = (int) ((totDistAfterStep - totDistAfterStep % 100) / 100);
            Double time = (SystemClock.elapsedRealtime() - chronometer.getBase())/1000.0;


            //if distance didnt reach a new full interval
            if (intervNoBefore==intervNoAfter) {
                return false;
            }
            //if distance reached a new full interval
            else {
                //time from previous interval
                Double division = (100.0 - totDist % 100) / delta.getdDist();

                if (paceMetric.isEmpty()) {
                    time=time-1.0+1.0*division;
                    time=Math.round(time*10)/10.0;
                } else {
                    Double prevTime=0.0;
                    for(Double T:paceMetric){
                        prevTime=prevTime+T;
                    }
                    time=time-1.0+1.0*division-prevTime;
                    time=Math.round(time*10)/10.0;

                }
                paceMetric.add(time);


                // checking what notification level is saved in shared prefs
                int notificationInterval=0;
                switch (sharedPreferences.getInt("Intervals",3)){
                    case 0: notificationInterval=1; break;
                    case 1: notificationInterval=2; break;
                    case 2: notificationInterval=5; break;
                    case 3: notificationInterval=10; break;
                    case 4: notificationInterval=20; break;
                    case 5: notificationInterval=50; break;
                    case 6: notificationInterval=100; break;
                    default: notificationInterval=10; break;
                }

                // if interval not triggered no notification will be displayed
                if(intervNoAfter%notificationInterval==0){

                    Double interTime=0.0;
                    for(int i=paceMetric.size()-notificationInterval;i<=paceMetric.size()-1;i++){
                        interTime=interTime+paceMetric.get(i);
                    }

                    // make notification about the pace in interval
                    Toast.makeText(this,intervNoAfter*100+"m in "+interTime.toString(),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

            }
        }

        return false;
    }

    public void update(){

        // creating the shared preferences editor
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        if(sharedPreferences.getBoolean("Units",false)) {

            textViewDistUnits.setText("miles");
            textViewVelUnits.setText("mph");
        }
        else {
            textViewDistUnits.setText("km");
            textViewVelUnits.setText("km/h");
        }
    }

    void saveTraining(){

    DataBaseHalper dataBaseHalper = new DataBaseHalper(this);

        SharedPreferences sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);

        String unit ="";
        if (sharedPreferences.getBoolean("Units",false))   unit="Imperial";
        else unit="Metric";


        ContentValues contentValues = new ContentValues();

        contentValues.put("id_user",sharedPreferences.getInt("userId",0));
        contentValues.put("tot_time",totTime);
        contentValues.put("tot_dist",totDist);
        contentValues.put("d_dist",dDistToString());
        contentValues.put("d_vel",dVelToString());
        contentValues.put("d_pace",dPaceToString());
        contentValues.put("av_vel",totDist/totTime);
        contentValues.put("unit",unit);
        contentValues.put("date",dateToString());
        
        dataBaseHalper.addTraining(contentValues);
        Toast.makeText(this, "Training saved", Toast.LENGTH_SHORT).show();
    }

    String dDistToString(){
        String d_dist="";
        for (DeltaDTV interv :intervals){
            d_dist=d_dist+interv.dDistToString()+";";
        }
        return d_dist;
    }

    String dVelToString(){
        String d_vel="";
        for (DeltaDTV interv :intervals){
            d_vel=d_vel+interv.dVelToString()+";";
        }
        return d_vel;
    }

    String dPaceToString(){
        SharedPreferences sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        String d_pace="";

        if (sharedPreferences.getBoolean("Units",false)){
            //Imperial pace to string
            for (Double pace :paceImperial){
                d_pace=d_pace+String.valueOf(pace)+";";
            }
        }
        else{
            //metric pace to string
            for (Double pace :paceMetric){
                d_pace=d_pace+String.valueOf(pace)+";";
            }
        }

        return d_pace;
    }

    String dateToString(){
//        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
//
//        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String datetime = dateformat.format(c.getTime());
        return datetime;
    }

    void appInit(){
        SharedPreferences sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("firstStart",true)){

            //creating default user to use app without account
            ContentValues contentValues = new ContentValues();
            contentValues.put("name","user");
            contentValues.put("surname","default");
            contentValues.put("email","");
            contentValues.put("username","");
            contentValues.put("password","");
            contentValues.put("units","Metric");
            contentValues.put("interval",4);

            DataBaseHalper dataBaseHalper = new DataBaseHalper(this);
            dataBaseHalper.createUser(contentValues);


            editor.putInt("userId",dataBaseHalper.getUserID(""));
            editor.putBoolean("firstStart",false);
            editor.apply();

        }
    }
}