package www.compiletales.wordpress.com.bookshelfreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;


public class TemporaryActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    String IPAddress;
    Bitmap imageBitmap;
//  To store json results
    HashMap<String, String> book = new HashMap<>();
    private String TAG = TemporaryActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private TextView textView;
    private String url = "";
    private TextView imagePathTextView;
    private Bitmap choosenImageBitmap;
    private String picturePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.test_view);
        imagePathTextView = findViewById(R.id.image_path_text_view);
        Button getDataButton = findViewById(R.id.button);
        Button chooseImageButton = findViewById(R.id.choose_image_button);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        final EditText editText = findViewById(R.id.edit_text);
        getDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPAddress = editText.getText().toString();
                url = "http://" + IPAddress + ":8000/api/1?format=json";
                new GetBooks().execute();
            }
        });

        Button sendDataButton = findViewById(R.id.send_data_button);

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPAddress = editText.getText().toString();
                url = "http://" + IPAddress + ":8000/api/create/";
                sendDataToServer();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            imagePathTextView.setText(picturePath);
            choosenImageBitmap = BitmapFactory.decodeFile(picturePath);
            Toast.makeText(getApplicationContext(), choosenImageBitmap.toString(), Toast.LENGTH_LONG).show();

        }
    }

    public void sendDataToServer() {

        String title;
        String description;
        JSONObject jsonData = new JSONObject();
        File imageToBeUploaded = new File(picturePath);


        pDialog = new ProgressDialog(TemporaryActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        EditText titleEditText = TemporaryActivity.this.findViewById(R.id.book_title_edit_text);
        EditText descriptionEditText = TemporaryActivity.this.findViewById(R.id.book_description_edit_text);

        title = titleEditText.getText().toString();
        description = descriptionEditText.getText().toString();

        Ion.with(getApplicationContext())
                .load("POST", url)
                .uploadProgressDialog(pDialog)
                .setTimeout(60 * 60 * 1000)
                .setMultipartParameter("title", title)
                .setMultipartParameter("description", description)
                .setMultipartFile("image", "image/jpeg", imageToBeUploaded)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        pDialog.dismiss();

                        if (e != null) {
                            Toast.makeText(getApplicationContext(), "Error uploading file", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(getApplicationContext(), "File upload complete", Toast.LENGTH_LONG).show();
                    }
                });

    }
    private class GetBooks extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(TemporaryActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler httpHandler = new HttpHandler();

            String json_str = httpHandler.makeServiceCall(url);

            if(json_str != null){
                try {
                    JSONObject jsonObject = new JSONObject(json_str);
                    String id = jsonObject.getString("id");
                    String title = jsonObject.getString("title");
                    String description = jsonObject.getString("description");
                    String imageURL = jsonObject.getString("image");

                    book.put("id", id);
                    book.put("title", title);
                    book.put("description", description);
                    book.put("imageURL", imageURL);

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            textView.setText(book.get("imageURL"));
            ImageView imageView = TemporaryActivity.this.findViewById(R.id.image_view);
            new GetImageFromURL(imageView).execute(book.get("imageURL"));
        }
    }

    public class GetImageFromURL extends AsyncTask<String, Void, Void>{
        ImageView imageView;

        public GetImageFromURL(ImageView imageView1){
            imageView = imageView1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(TemporaryActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... urls) {
            String imageURL = urls[0];

            try{
                InputStream in = new java.net.URL(imageURL).openStream();
                imageBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            try{
                imageView.setImageBitmap(imageBitmap);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.getMessage(),
                        Toast.LENGTH_LONG)
                        .show();
            }
            pDialog.dismiss();
        }
    }
}
