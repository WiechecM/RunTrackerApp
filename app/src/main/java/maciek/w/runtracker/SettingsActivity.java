package maciek.w.runtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Switch aSwitch;
    private Toolbar toolbar;
    private Spinner intervalSpinner;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0,0);
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        aSwitch = (Switch) findViewById(R.id.unitSwitch);
        intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);

        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this,
                R.array.intervals_imperial, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        intervalSpinner.setAdapter(adapter);
        intervalSpinner.setOnItemSelectedListener(this);


        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);

        // adding back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //switch was clicked listener
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                save(isChecked);
                update();
            }
        });

        update();
    }


    public void update(){
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        aSwitch.setChecked(sharedPreferences.getBoolean("Units",false));


        ArrayAdapter<CharSequence> adapter;
        if(sharedPreferences.getBoolean("Units",false)){
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.intervals_imperial, android.R.layout.simple_spinner_item);
        }
        else{
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.intervals_metric, android.R.layout.simple_spinner_item);
        }

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        intervalSpinner.setAdapter(adapter);

        intervalSpinner.setSelection(sharedPreferences.getInt("Intervals",3));
    }

    public void save(Boolean isChecked){
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("Units",isChecked);
        Toast.makeText(SettingsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        editor.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("Intervals",position);
        Toast.makeText(SettingsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
