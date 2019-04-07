package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

public class GetBookInfoActivity extends AppCompatActivity {

    String baseURL;
    String objectCreatedID;
    ArrayList<Bitmap> croppedImages;
    ArrayList<String> titles;
    ViewPager croppedImagesViewPager;
    CroppedImagesViewPagerAdapter croppedImagesPagerAdapter;
    LayoutInflater inflater;
    int currentSpine = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_book_info);


        baseURL = getIntent().getStringExtra("BASE_URL");
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

        final Button getSpineInfoButton = findViewById(R.id.get_spine_info_button);
        getSpineInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetBookInfoActivity.this, ShowBookInfoActivity.class);
                intent.putExtra("BOOK_TITLE", titles.get(currentSpine));
                startActivity(intent);
            }
        });
        getSpineInfoButton.setVisibility(View.GONE);

        croppedImagesViewPager.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                getSpineInfoButton.setVisibility(View.VISIBLE);
                spineLoadingView.setVisibility(View.GONE);
                croppedImagesViewPager.removeOnAttachStateChangeListener(this);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

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
                            Ion.with(GetBookInfoActivity.this)
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
}
