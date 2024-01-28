package th.o.th.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class about extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about2);

        // Set the app version dynamically
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionTextView = findViewById(R.id.versionTextView);
        versionTextView.setText("Version " + versionName);
    }
}