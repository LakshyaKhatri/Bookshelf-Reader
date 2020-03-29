package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class SpineLineDrawnPreviewActivity extends AppCompatActivity {

    String baseURL = "https://lakshya1498.pythonanywhere.com/";
    String objectCreatedID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spine_line_drawn_preview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_button);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        objectCreatedID = getIntent().getStringExtra("SERVER_OBJECT_ID");
        final ImageView imageView = findViewById(R.id.spine_line_drawn_image_view);
        final LottieAnimationView loading = findViewById(R.id.loading_view);

        Ion.with(this)
                .load(baseURL + "api/bookshelf/" + objectCreatedID + "/?format=json")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        String spineDrawnImageURL = result.get("spine_line_drawn_image").getAsString();

                        Ion.with(getApplicationContext())
                                .load(spineDrawnImageURL)
                                .withBitmap()
                                .intoImageView(imageView)
                                .setCallback(new FutureCallback<ImageView>() {
                                    @Override
                                    public void onCompleted(Exception e, ImageView result) {
                                        loading.setVisibility(View.GONE);
                                    }
                                });
                    }
                });

        findViewById(R.id.cancel_cropping_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.proceed_cropping_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        SpineLineDrawnPreviewActivity.this,
                        CropSpinesActivity.class);
                intent.putExtra("SERVER_OBJECT_ID", objectCreatedID);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
