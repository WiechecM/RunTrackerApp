package maciek.w.runtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;

    private String dates[], descriptions[];
    private DataBaseHalper dataBaseHalper;
    private ArrayList<String> dates_al;
    private ArrayList<String> descriptions_al;
    private ArrayList<Integer> trainingID;

    RecyclerView recyclerView;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0,0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_menu));

        sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        recyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);

        //navigation bar
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.history);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.friends:
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.training:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.history:
                        return true;
                    case R.id.statistics:
                        startActivity(new Intent(getApplicationContext(), StatisticActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        //gathering data from the database
        dataBaseHalper = new DataBaseHalper(this);
        dates_al = new ArrayList<>();
        descriptions_al = new ArrayList<>();
        trainingID = new ArrayList<>();
        storeDataInArrays();


        //temporarty data
        dates= new String[]{"2020.01.01 18.00", "2020.01.02 17.00","2020.01.04 11.05"};
        descriptions = new String[]{"2.4km in 3m34s","2.6km in 3m56s","2.9km in 4m"};

        RVAdapter rvAdapter = new RVAdapter(this,dates_al,descriptions_al,trainingID);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    void storeDataInArrays(){
        Cursor cursor = dataBaseHalper.readTrainingList(sharedPreferences.getInt("userId",0));
       //DatabaseUtils.dumpCursorToString(cursor);
        if (cursor.getCount()==0){
            Toast.makeText(this, "There is no trainings to display", Toast.LENGTH_SHORT).show();
        }else {
            while (cursor.moveToNext()){
                dates_al.add(cursor.getString(0));
                String totDist,totTime;
                totDist=cursor.getString(1);
                totTime=cursor.getString(2);
                descriptions_al.add(totDist+"m in "+totTime+"s");
                trainingID.add(cursor.getInt(3));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}