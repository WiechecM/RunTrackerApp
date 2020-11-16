package maciek.w.runtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editTextUserName;
    private EditText editTextPassword;
    private Button buttonLogin;
    DataBaseHalper dataBaseHalper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextPassword = (EditText) findViewById(R.id.EditTextPassword);
        editTextUserName = (EditText) findViewById(R.id.EditTextUserName);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        dataBaseHalper = new DataBaseHalper(this);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);

        // adding back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUserName.getText().toString();
                String password = editTextPassword.getText().toString();

                if(dataBaseHalper.isLoginValid(username,password)){
                    SharedPreferences sharedPreferences = getSharedPreferences(
                            getResources().getString(R.string.SHARED_PREFS),MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    //saving the user id
                    editor.putInt("userId",dataBaseHalper.getUserID(username));
                    editor.apply();

                    // opening the statistics activity
                    startActivity(new Intent(getApplicationContext(), StatisticActivity.class));
                    overridePendingTransition(0,0);
                    Toast.makeText(LoginActivity.this, "Hi "+username+"!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}