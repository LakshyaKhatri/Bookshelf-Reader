package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // TODO: Remove this.
        Intent mainActivityIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
        String baseURL = getIntent().getStringExtra("BASE_URL");
        mainActivityIntent.putExtra("BASE_URL", baseURL);
        startActivity(mainActivityIntent);
    }
}
