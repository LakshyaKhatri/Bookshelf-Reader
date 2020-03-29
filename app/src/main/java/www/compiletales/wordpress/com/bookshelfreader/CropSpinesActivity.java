package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class CropSpinesActivity extends AppCompatActivity {

    String baseURL = "https://lakshya1498.pythonanywhere.com/";
    String objectCreatedID;
    ArrayList<Bitmap> croppedImages;
    ArrayList<String> titles;
    ViewPager croppedImagesViewPager;
    CroppedImagesViewPagerAdapter croppedImagesPagerAdapter;
    LayoutInflater inflater;
    int currentSpine = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_spines);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_button);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        objectCreatedID = getIntent().getStringExtra("SERVER_OBJECT_ID");

        final FirebaseVisionTextRecognizer detector =
                FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();

        final LottieAnimationView spineLoadingView = findViewById(R.id.spine_loading_view);
        croppedImagesViewPager = findViewById(R.id.cropped_images_view_pager);
        croppedImagesPagerAdapter = new CroppedImagesViewPagerAdapter();
        croppedImagesViewPager.setAdapter(croppedImagesPagerAdapter);
        inflater = LayoutInflater.from(getApplicationContext());
        croppedImages = new ArrayList<>();
        titles = new ArrayList<>();

        croppedImagesViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                currentSpine = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        final CircleIndicator indicator = findViewById(R.id.viewpager_indicator);
        indicator.setViewPager(croppedImagesViewPager);

        final Button getSpineInfoButton = findViewById(R.id.get_spine_info_button);
        getSpineInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentSpine < 0)
                    return;
                try {
                    Intent intent = new Intent(CropSpinesActivity.this, ShowBookInfoActivity.class);
                    intent.putExtra("BOOK_TITLE", titles.get(currentSpine));
                    startActivity(intent);
                } catch(Exception e){
                    Toast.makeText(CropSpinesActivity.this, "Please wait.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Ion.with(this)
                .load(baseURL + "api/spines/" + objectCreatedID + "/?format=json")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        String croppedImageURL;
                        for(int i = 0; i < result.size(); i++){
                            croppedImageURL = result.get(i).getAsJsonObject()
                                            .get("image").getAsString();
                            Ion.with(CropSpinesActivity.this)
                                    .load(croppedImageURL)
                                    .setTimeout(60 * 60 * 200)
                                    .asBitmap()
                                    .setCallback(new FutureCallback<Bitmap>() {
                                        @Override
                                        public void onCompleted(Exception e, final Bitmap result) {
                                            Bitmap spineRotated = rotateBitmap(result, 270.0f);
                                            FirebaseVisionImage rotatedImage = FirebaseVisionImage.fromBitmap(spineRotated);
                                            detector.processImage(rotatedImage)
                                                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                        @Override
                                                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                                            List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
                                                            String verticalText = "";
                                                            for(int i = 0; i < blocks.size(); i++)
                                                                verticalText += " " + blocks.get(i).getText().toUpperCase();
                                                                if (verticalText.length() > 3 ) {
                                                                    if(!(croppedImages.contains(result))){
                                                                        croppedImages.add(result);
                                                                        titles.add(verticalText);
                                                                        View view = inflater.inflate(R.layout.cropped_images_view_pager_item, null);
                                                                        ImageView imageView = view.findViewById(R.id.view_pager_item_image_view);
                                                                        imageView.setImageBitmap(result);
                                                                        croppedImagesPagerAdapter.addView(view);
                                                                        croppedImagesPagerAdapter.notifyDataSetChanged();
                                                                        indicator.setViewPager(croppedImagesViewPager);
                                                                        spineLoadingView.setVisibility(View.GONE);
                                                                    } else {
                                                                        int indexOfConflictedTitle = croppedImages.indexOf(result);
                                                                        String newText = titles.get(indexOfConflictedTitle);
                                                                        newText = newText + " " + verticalText;
                                                                        titles.remove(indexOfConflictedTitle);
                                                                        titles.add(indexOfConflictedTitle, newText);
                                                                    }
                                                                }
                                                        }
                                                    });
                                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(result);
                                            detector.processImage(image)
                                                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                        @Override
                                                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                                            List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
                                                            String horizontalText = "";
                                                            for(int i = 0; i < blocks.size(); i++)
                                                                horizontalText += " " + blocks.get(i).getText().toUpperCase();
                                                            if(horizontalText.length() > 3) {
                                                                if(!(croppedImages.contains(result))) {
                                                                    croppedImages.add(result);
                                                                    titles.add(horizontalText);
                                                                    View view = inflater.inflate(R.layout.cropped_images_view_pager_item, null);
                                                                    ImageView imageView = view.findViewById(R.id.view_pager_item_image_view);
                                                                    imageView.setImageBitmap(result);
                                                                    croppedImagesPagerAdapter.addView(view);
                                                                    croppedImagesPagerAdapter.notifyDataSetChanged();
                                                                    indicator.setViewPager(croppedImagesViewPager);
                                                                    spineLoadingView.setVisibility(View.GONE);
                                                                } else {
                                                                    int indexOfConflictedTitle = croppedImages.indexOf(result);
                                                                    String newText = titles.get(indexOfConflictedTitle);
                                                                    newText = newText + " " + horizontalText;
                                                                    titles.remove(indexOfConflictedTitle);
                                                                    titles.add(indexOfConflictedTitle, newText);
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }

                    }
                });
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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
