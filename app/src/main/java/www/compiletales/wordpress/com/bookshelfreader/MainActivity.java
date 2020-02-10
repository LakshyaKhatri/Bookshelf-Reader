package www.compiletales.wordpress.com.bookshelfreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.mindorks.paracamera.Camera;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE_FROM_GALLERY = 405;
    private static final int CAPTURE_IMAGE_FROM_CAMERA = 977;

    String baseURL = "https://lakshya1498.pythonanywhere.com/";
    Camera camera;
    // Views
    Button captureImageFromCamera;
    Button chooseImageFromGallery;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initializations
        captureImageFromCamera = findViewById(R.id.capture_image_from_camera);
        chooseImageFromGallery = findViewById(R.id.choose_image_from_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_button);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Camera variable to capture image from camera
        camera = new Camera.Builder()
                .resetToCorrectOrientation(true)
                .setTakePhotoRequestCode(CAPTURE_IMAGE_FROM_CAMERA)
                .setDirectory("Bookshelves")
                .setName("bookshelf" + System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setImageHeight(1000)
                .build(MainActivity.this);

        chooseImageFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, CHOOSE_IMAGE_FROM_GALLERY);

            }
        });

        captureImageFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.takePicture();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE_FROM_GALLERY && resultCode == RESULT_OK && null != data) {

            imagePath = getSelectedImagePath(data);
            showImagePreview(imagePath);
        }

        if (requestCode == Camera.REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            imagePath = camera.getCameraBitmapPath();
            uploadImage(imagePath);
        }
    }

    private void showImagePreview(String chosenImagePath){
        Bitmap imageBitmap = BitmapFactory.decodeFile(chosenImagePath);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.image_preview_popup, null);
        ImageView imageView = layout.findViewById(R.id.preview_image_view);
        imageView.setImageBitmap(imageBitmap);

        final PopupWindow imagePreviewPopUp = new PopupWindow(this);
        imagePreviewPopUp.setContentView(layout);
        imagePreviewPopUp.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        imagePreviewPopUp.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        imagePreviewPopUp.setFocusable(true);
        imagePreviewPopUp.setBackgroundDrawable(new BitmapDrawable());

        imagePreviewPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, -20, 95);
        ImageView discardPreviewIcon = layout.findViewById(R.id.go_back_view);
        discardPreviewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePreviewPopUp.dismiss();
            }
        });
        ImageView proceedIcon = layout.findViewById(R.id.proceed_check_view);
        proceedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePreviewPopUp.dismiss();
                uploadImage(imagePath);
            }
        });
    }

    private String getSelectedImagePath(Intent data){
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String path =  cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    private void uploadImage(String imagePath){
        final ProgressDialog pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Detecting Books");
        pDialog.setCancelable(false);
        pDialog.show();
        Ion.with(getApplicationContext())
                .load("POST", baseURL + "api/create-bookshelf/?format=json")
                .setTimeout(60 * 60 * 40)
                .setMultipartFile("image", "image/jpeg", new File(imagePath))
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        pDialog.dismiss();
                        if (e != null){
                            Toast.makeText(
                                    getApplicationContext(),
                                    "No books found",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        HeadersResponse responseHeader = result.getHeaders();
                        Headers headers = responseHeader.getHeaders();
                        String objectCreatedID = headers.get("id");

                        if(objectCreatedID != null) {
                            Intent intent = new Intent(MainActivity.this, SpineLineDrawnPreviewActivity.class);
                            intent.putExtra("SERVER_OBJECT_ID", objectCreatedID);
                            startActivity(intent);
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "No books found",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Delete saved image from the app's folder
        camera.deleteImage();
    }
}
