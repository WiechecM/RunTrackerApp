package maciek.w.runtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.maps.CameraUpdate;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;


public class TrainingViewer extends AppCompatActivity implements OnMapReadyCallback , AdapterView.OnItemSelectedListener{
    public static final int REQUEST_LOCATION_PERMITION = 99;

    Toolbar toolbar;
    GoogleMap map;
    TextView id;
    TextView textViewTVTime;
    TextView textViewTV_dist;
    TextView textViewTV_dist_unit;
    TextView textViewTV_av_speed;
    TextView textViewTV_av_speed_unit;
    TextView textViewTV_av_pace;
    TextView textViewTV_av_pace_unit;

    GraphView graph;
    LineGraphSeries<DataPoint> series;

    private Spinner intervalSpinner;
    private LinearLayout linearLayout;

    int idTraining;
    double tot_time;
    double tot_dist;
    String h_lat;
    String h_lon;
    String d_vel;
    String d_pace;
    double av_vel;
    String unit;
    String date;
    int interv;

    Point size;

    ArrayList<Double> arrayList_d_pace;
    ArrayList<Double> arrayList_d_vel;

    LatLngBounds.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_viewer);

        arrayList_d_pace = new ArrayList<>();
        arrayList_d_vel = new ArrayList<>();

        id = (TextView) findViewById(R.id.training_viewer_id);
        textViewTVTime = (TextView) findViewById(R.id.TV_time);
        textViewTV_dist = (TextView) findViewById(R.id.TV_dist);
        textViewTV_dist_unit = (TextView) findViewById(R.id.TV_dist_unit);
        textViewTV_av_speed = (TextView) findViewById(R.id.TV_av_speed);
        textViewTV_av_speed_unit = (TextView) findViewById(R.id.TV_av_speed_unit);
        textViewTV_av_pace = (TextView) findViewById(R.id.TV_av_pace);
        textViewTV_av_pace_unit = (TextView) findViewById(R.id.TV_av_pace_unit);

        graph = (GraphView) findViewById(R.id.graph);

        linearLayout = (LinearLayout) findViewById(R.id.training_viewer_linear_layout);

        //initialization of spinner
        intervalSpinner = (Spinner) findViewById(R.id.training_viewer_interval_spinner);
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this,
                R.array.intervals_imperial, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        intervalSpinner.setAdapter(adapter);
        intervalSpinner.setOnItemSelectedListener(this);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Running");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // builder to zoom the map
        builder = new LatLngBounds.Builder();

        // get the size of the device screen
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        // set the fragment height the same as width
        findViewById(R.id.training_viewer_map).getLayoutParams().height = size.x*2/3;

        // Initialize  Google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.training_viewer_map);
        mapFragment.getMapAsync(this);


        // get data from database and save it in variables
        getId();
        getDataFromDB();
        adjustData();

        //display data in
        setData();

        //display spinner
        spinnerSetUp();

        //display progress bars for each interval
        displayMidTimes();

        //plotting the velocity graph
        addGraph();

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {


        map = googleMap;
        PolylineOptions polylineOptions = createPolyline();
        LatLngBounds bounds = builder.build();
        int padding = 50;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        map.addPolyline(polylineOptions);
        map.animateCamera(cu);


    }

    private void addGraph(){
        d_VelStr2Array();

        // create points to be displayed on the plot
        DataPoint[] dataPoints = new DataPoint[arrayList_d_vel.size()];
        for (int i = 0; i <arrayList_d_vel.size(); i++) {
            dataPoints[i] = new DataPoint(i*1.0, arrayList_d_vel.get(i));
        }

        series = new LineGraphSeries<DataPoint>(dataPoints);

        graph.addSeries(series);
    }

    private void displayMidTimes(){

        //only for metric need to change distance update

        int length=arrayList_d_pace.size();
        int index=0;
        int dist=0;
        double maxTime=0.0;

        ArrayList<Double> midtimes=new ArrayList<>();
        ArrayList<Integer> middist=new ArrayList<>();

        //calculate full intervals
        while (length>=interv){

            double intervTime=0.0;

            for (int i=index;i<index+interv;i++){
                intervTime=intervTime+arrayList_d_pace.get(i);
                dist=dist+100;
            }
            midtimes.add(intervTime);
            middist.add(dist);

            index=index+interv;
            length=length-interv;

            if (intervTime>maxTime){
                maxTime=intervTime;
            }

        }
        // check if some intervals were left without full period
        if (length>0){
            double intervTime=0.0;
            int scale=0;
            for (int i=index;i<arrayList_d_pace.size();i++){
                intervTime=intervTime+arrayList_d_pace.get(i);
                dist=dist+100;
                scale=scale+1;
            }
//            midtimes.add(intervTime*scale/interv);
            midtimes.add(intervTime);

            middist.add(dist);

            if (intervTime>maxTime){
                maxTime=intervTime;
            }
        }

        //display midtimes layouts
        for (int i=0;i<midtimes.size();i++){
            addMidTimeView(middist.get(i).toString()+"m",
                    timeToString(midtimes.get(i)),  midtimes.get(i)/maxTime);
        }



    }

    private String timeToString(Double val) {
        long timeInSec = Math.round(val);
        int h=0;
        int m=0;
        int s=0;

        String strTime="";

        //setting hours
        if (timeInSec>3600){
            h = (int) ((timeInSec-timeInSec%3600)/3600);
            timeInSec=timeInSec%3600;
        }

        //setting minutes
        if (timeInSec>60){
            m = (int) ((timeInSec-timeInSec%60)/60);
            timeInSec=timeInSec%60;
        }

        //setting seconds
        if (timeInSec>0){
            s = (int) timeInSec;
        }

        //make a string
        strTime=String.valueOf(h)+":";
        if (m<10){
            strTime=strTime+"0";
        }
        strTime=strTime+String.valueOf(m);
        strTime=strTime+":";
        if (s<10){
            strTime=strTime+"0";
        }
        strTime=strTime+String.valueOf(s);


        return strTime;
    }


    private void addMidTimeView(String dist, String time, double percent) {

        final View midTimeView = getLayoutInflater().inflate(R.layout.mid_times,null,false);

        TextView textViewTime = (TextView) midTimeView.findViewById(R.id.textViewMidtimeVal);
        TextView textViewDist = (TextView) midTimeView.findViewById(R.id.textViewMidtimeDist);

        View view = midTimeView.findViewById(R.id.viewMidTimeProgressBar);

        textViewDist.setText(dist);
        textViewTime.setText(time);
        float maxWidth=size.x - (30+10+100+10)*getResources().getDisplayMetrics().density;
        view.getLayoutParams().width= (int) (maxWidth*percent);

        linearLayout.addView(midTimeView);

    }

    private void removeMidTimeViews() {

        if (linearLayout.getChildCount()>0){
            linearLayout.removeAllViews();
        }

    }


    private void getId() {
        if (getIntent().hasExtra("id")) {
            idTraining = getIntent().getIntExtra("id", -1);
        } else {
            Toast.makeText(this, "No data ", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataFromDB(){
        DataBaseHalper dataBaseHalper = new DataBaseHalper(this);
        Cursor cursor = dataBaseHalper.readTrainingDetails(idTraining);

        //to debug and see the cursor
        String str = DatabaseUtils.dumpCursorToString(cursor);

        if (cursor.getCount()==0){
            Toast.makeText(this, "Training not found", Toast.LENGTH_SHORT).show();
        }else {
            while (cursor.moveToNext()) {
                tot_time = cursor.getDouble(0);
                tot_dist = cursor.getDouble(1);
                h_lat = cursor.getString(2);
                h_lon = cursor.getString(3);
                d_vel = cursor.getString(4);
                d_pace = cursor.getString(5);
                av_vel = cursor.getDouble(6);
                unit = cursor.getString(7);
                date = cursor.getString(8);
                interv = cursor.getInt(9);
            }

        }
    }

    private void setData(){

        id.setText(String.valueOf(idTraining));
        textViewTVTime.setText(String.valueOf(tot_time));
        textViewTV_dist.setText(String.valueOf(tot_dist));
        textViewTV_av_speed.setText(String.valueOf(av_vel));
        textViewTV_av_pace.setText(String.valueOf(av_pace()));




        if (unit.equals("Metric")) {
            textViewTV_dist_unit.setText("km");
            textViewTV_av_speed_unit.setText("km/h");
            textViewTV_av_pace_unit.setText("min/km");
        }else{
            textViewTV_dist_unit.setText("mi");
            textViewTV_av_speed_unit.setText("mph");
            textViewTV_av_pace_unit.setText("min/mi");
        }


    }

    private void adjustData(){

        if (unit.equals("Metric")){
            tot_dist=roundVal(tot_dist/1000.0,2);
            av_vel=roundVal(av_vel,1);
        }
    }


    private double av_pace(){

        paceStr2Array();

        double time=0.0;
        double noInInterv=0;


        if (unit.equals("Metric")){
            noInInterv=10.0;
        }else {
            noInInterv=8.0;
        }

        for (double pace:arrayList_d_pace){
            time=time+pace;
        }
        time=time/arrayList_d_pace.size();
        double cos = time;
        time=time*noInInterv;

        return roundVal(time,1);
    }

    private void paceStr2Array(){

        String temp_d_pace=d_pace;

        while(temp_d_pace.length()>0) {
            int index = temp_d_pace.indexOf(";");
            String one_pace = temp_d_pace.substring(0,index);
            arrayList_d_pace.add(Double.parseDouble(one_pace));
            temp_d_pace = temp_d_pace.substring(index+1);
        }
    }

    private void d_VelStr2Array(){

        String temp_d_vel=d_vel;

        while(temp_d_vel.length()>0) {
            int index = temp_d_vel.indexOf(";");
            String one_d_vel = temp_d_vel.substring(0,index);
            arrayList_d_vel.add(Double.parseDouble(one_d_vel));
            temp_d_vel = temp_d_vel.substring(index+1);
        }
    }

    private PolylineOptions createPolyline(){
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(10)
                .color(Color.RED);

        // creating strings which can be cut
        String temp_h_lat = h_lat;
        String temp_h_lon = h_lon;

        double Lat=0.0,Lon=0.0;

        boolean firstLoop=true;
        while(temp_h_lat.length()>0){

            //finding the index of first occurence of ;
            int latIndex = temp_h_lat.indexOf(";");
            int lonIndex = temp_h_lon.indexOf(";");

            String TLAT = temp_h_lat.substring(0,latIndex);
            String TLON = temp_h_lon.substring(0,lonIndex);

            if (firstLoop) {
                //converting substring to double value
                Lat = Double.parseDouble(TLAT);
                Lon = Double.parseDouble(TLON);

            }else{
                //converting substring first to int value and multiply to obtain double
                int latInt = Integer.parseInt(TLAT);
                int lonInt = Integer.parseInt(TLON);
                Lat = Lat + latInt*0.00001;
                Lon = Lon + lonInt*0.00001;
            }

            polylineOptions.add(new LatLng(Lat,Lon));
            builder.include(new LatLng(Lat,Lon));

            // delete used values from the string
            if (temp_h_lat.length()>latIndex) {
                temp_h_lat = temp_h_lat.substring(latIndex+1,temp_h_lat.length());
                temp_h_lon = temp_h_lon.substring(lonIndex+1,temp_h_lon.length());
            }
            firstLoop=false;
        }

        return polylineOptions;
    }

    public Double roundVal(Double val, int decimal){
        double temp = Math.pow(10.0,decimal);
        return Math.round(val*temp)/temp;
    }

    //displays interval options in either metric or imperial
    private void spinnerSetUp(){
        ArrayAdapter<CharSequence> adapter;
        if(unit.equals("Metric")){

            adapter = ArrayAdapter.createFromResource(this,
                    R.array.intervals_metric, android.R.layout.simple_spinner_item);
        }
        else{
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.intervals_imperial, android.R.layout.simple_spinner_item);
        }

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        intervalSpinner.setAdapter(adapter);

        intervalSpinner.setSelection(interv);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


        if (unit.equals("Metric")){
            switch (position){
                case 0: interv=1; break;
                case 1: interv=2; break;
                case 2: interv=5; break;
                case 3: interv=10; break;
                case 4: interv=20; break;
                case 5: interv=50; break;
                case 6: interv=100; break;
            }
        }else {
            switch (position){
                case 0: interv=1; break;
                case 1: interv=2; break;
                case 2: interv=4; break;
                case 3: interv=8; break;
                case 4: interv=16; break;
                case 5: interv=40; break;
                case 6: interv=80; break;
            }
        }

        this.id.setText(String.valueOf(interv));
        removeMidTimeViews();
        displayMidTimes();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }
}