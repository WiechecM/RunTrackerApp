package maciek.w.runtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FriendsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0,0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_menu));

        //navigation bar
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.friends);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.friends:
                        return true;
                    case R.id.training:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.history:
                        startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.statistics:
                        startActivity(new Intent(getApplicationContext(), StatisticActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    //options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        MenuInflater inflater = getMenuInflater();
        // if id==1, no user is looged (default offline user)
        if (sharedPreferences.getInt("userId",1)==1) {
            inflater.inflate(R.menu.menu_toolbar,menu);
        }
        // if user is logged display second manu with logout
        else{
            inflater.inflate(R.menu.menu_toolbar_2,menu);
        }
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

            case R.id.log_out:
                logOut();
                return true;
        }
        return false;
    }

    private void logOut(){
        sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("userId",1);
        editor.apply();

        //redraw the menu
        this.invalidateOptionsMenu();
    }
}