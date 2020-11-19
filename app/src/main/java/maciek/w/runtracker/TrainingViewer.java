package maciek.w.runtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class TrainingViewer extends AppCompatActivity {

    TextView date;
    TextView desc;
    TextView id;

    String dateString,descString;
    int idVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_viewer);

        date = (TextView) findViewById(R.id.training_viewer_date);
        desc = (TextView) findViewById(R.id.training_viewer_description);
        id = (TextView) findViewById(R.id.training_viewer_id);

        getData();
        setData();
    }

    private void getData() {
        if (getIntent().hasExtra("date") && getIntent().hasExtra("desc") &&
                getIntent().hasExtra("id")) {
            dateString = getIntent().getStringExtra("date");
            descString = getIntent().getStringExtra("desc");
            idVal = getIntent().getIntExtra("id", -1);
        } else {
            Toast.makeText(this, "No data ", Toast.LENGTH_SHORT).show();
        }
    }

    private void setData(){
        date.setText(dateString);
        desc.setText(descString);
        id.setText(String.valueOf(idVal));
    }
}