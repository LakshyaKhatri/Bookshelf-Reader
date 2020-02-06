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

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                Intent mainActivityIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        }, 1700);
    }
}
