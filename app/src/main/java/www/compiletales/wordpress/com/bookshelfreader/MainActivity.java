package www.compiletales.wordpress.com.bookshelfreader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.mindorks.paracamera.Camera;

public class MainActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE_FROM_GALLERY = 405;
    private static final int CAPTURE_IMAGE_FROM_CAMERA = 977;

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
                camera = new Camera.Builder()
                        .resetToCorrectOrientation(true)
                        .setTakePhotoRequestCode(CAPTURE_IMAGE_FROM_CAMERA)
                        .setDirectory("Bookshelfs")
                        .setName("bookshelf" + System.currentTimeMillis())
                        .setImageFormat(Camera.IMAGE_JPEG)
                        .setImageHeight(1000)
                        .build(MainActivity.this);

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
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
        }

        if (requestCode == Camera.REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Bitmap imageBitmap = camera.getCameraBitmap();
        }
    }

    private void showImagePreview(Bitmap imageBitmap){
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
        ImageView discardPreviewIcon = layout.findViewById(R.id.discard_cross_view);
        discardPreviewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePreviewPopUp.dismiss();
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
