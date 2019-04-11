package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class TemporaryActivity extends AppCompatActivity {

    String IPAddress;
    private TextView textView;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporary);
        Button getDataButton = findViewById(R.id.button);


        final EditText editText = findViewById(R.id.edit_text);
        getDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPAddress = editText.getText().toString();
                String baseURL = "http://" + IPAddress + ":8000/";
                Intent splashScreenIntent = new Intent(TemporaryActivity.this, SplashScreenActivity.class);
                splashScreenIntent.putExtra("BASE_URL", baseURL);
                startActivity(splashScreenIntent);
            }
        });


    }
}
