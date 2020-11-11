package maciek.w.runtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextEmail;
    private EditText editTextUserNameRegister;
    private EditText editTextPasswordRegister;
    private EditText editTextRepeatPassword;

    private Button buttonRegister;
    private Button buttonCancel;

    private Toolbar toolbar;
    DataBaseHalper dataBaseHalper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = (EditText) findViewById(R.id.EditTextName);
        editTextSurname = (EditText) findViewById(R.id.EditTextSurname);
        editTextEmail = (EditText) findViewById(R.id.EditTextEmail);
        editTextUserNameRegister = (EditText) findViewById(R.id.EditTextUserNameRegister);
        editTextPasswordRegister = (EditText) findViewById(R.id.EditTextPasswordRegister);
        editTextRepeatPassword = (EditText) findViewById(R.id.EditTextRepeatPassword);

        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);

        dataBaseHalper = new DataBaseHalper(this);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);

        // adding back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isUsernameCorrect = false;
                boolean isPasswordCorrect = false;

                String name = editTextName.getText().toString();
                String surname = editTextSurname.getText().toString();
                String email = editTextEmail.getText().toString();
                String userName = editTextUserNameRegister.getText().toString();
                String password = editTextPasswordRegister.getText().toString();
                String repeatPassword = editTextRepeatPassword.getText().toString();

                // check if username is free
                if(dataBaseHalper.isUsernameFree(userName)){
                    isUsernameCorrect=true;
                }
                else {
                    Toast.makeText(RegisterActivity.this,
                            "The username is already taken", Toast.LENGTH_SHORT).show();
                }

                // check if passward was typed correctly
                if (password.equals(repeatPassword)){
                    isPasswordCorrect=true;
                }
                else {
                    Toast.makeText(RegisterActivity.this, "Reapeted password is incorrect", Toast.LENGTH_SHORT).show();
                }

                //create user
                if(isPasswordCorrect && isUsernameCorrect){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("name",name);
                    contentValues.put("surname",surname);
                    contentValues.put("email",email);
                    contentValues.put("username",userName);
                    contentValues.put("password",password);

                    dataBaseHalper.createUser(contentValues);
                    Toast.makeText(RegisterActivity.this, "User registered", Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0,0);
            }
        });

    }
}