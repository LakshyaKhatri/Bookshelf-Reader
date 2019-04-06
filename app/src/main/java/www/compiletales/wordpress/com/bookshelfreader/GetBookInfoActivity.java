package www.compiletales.wordpress.com.bookshelfreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
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

public class GetBookInfoActivity extends AppCompatActivity {

    String baseURL;
    String objectCreatedID;
    ArrayList<BookSpine> croppedImages;
    ViewPager croppedImagesViewPager;
    CroppedImagesViewPagerAdapter croppedImagesPagerAdapter;
    LayoutInflater inflater;
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
                                                        public void onSuccess(FirebaseVisionText firebaseVisionTextHorizontal) {
                                                            final String verticalText = firebaseVisionTextHorizontal.getText();
                                                            if(verticalText.length() > 3) {
                                                                croppedImages.add(new BookSpine(
                                                                        result,
                                                                        verticalText
                                                                ));
                                                                View view = inflater.inflate(R.layout.cropped_images_view_pager_item, null);
                                                                ImageView imageView = view.findViewById(R.id.view_pager_item_image_view);
                                                                imageView.setImageBitmap(result);
                                                                croppedImagesPagerAdapter.addView(view);
                                                                croppedImagesPagerAdapter.notifyDataSetChanged();
                                                                spineLoadingView.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(result);
                                            detector.processImage(image)
                                                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                        @Override
                                                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                                            String horizontalText = firebaseVisionText.getText();
                                                            if(horizontalText.length() > 3) {
                                                                croppedImages.add(new BookSpine(
                                                                        result,
                                                                        horizontalText
                                                                ));
                                                                View view = inflater.inflate(R.layout.cropped_images_view_pager_item, null);
                                                                ImageView imageView = view.findViewById(R.id.view_pager_item_image_view);
                                                                imageView.setImageBitmap(result);
                                                                croppedImagesPagerAdapter.addView(view);
                                                                croppedImagesPagerAdapter.notifyDataSetChanged();
                                                                spineLoadingView.setVisibility(View.GONE);
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
