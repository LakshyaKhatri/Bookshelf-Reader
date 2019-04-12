package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final LottieAnimationView splashScreenAnimationView = findViewById(R.id.splash_screen_animation_view);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                Intent mainActivityIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                String baseURL = getIntent().getStringExtra("BASE_URL");
                mainActivityIntent.putExtra("BASE_URL", baseURL);
                startActivity(mainActivityIntent);
            }
        }, 1700);
    }
}
