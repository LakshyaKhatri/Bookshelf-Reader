package www.compiletales.wordpress.com.bookshelfreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;

public class ShowBookInfoActivity extends AppCompatActivity {

    String bookTitle;
    String baseURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book_info);

        baseURL = getIntent().getStringExtra("BASE_URL");
        bookTitle = getIntent().getStringExtra("BOOK_TITLE");
        final ImageView bookCoverImageView = findViewById(R.id.cover_image_view);
        final LottieAnimationView loadingView = findViewById(R.id.book_loading_view);
        final TextView titleTextView = findViewById(R.id.title_text_view);
        final TextView authorTextView = findViewById(R.id.author_text_view);
        final TextView pagesTextView = findViewById(R.id.pages_text_view);
        final TextView categoryTextView = findViewById(R.id.category_text_view);
        final TextView dimensionsTextView = findViewById(R.id.dimensions_text_view);
        final TextView isbn10TextView = findViewById(R.id.isbn_ten_text_view);
        final TextView isbn13TextView = findViewById(R.id.isbn_thirteen_text_view);
        final TextView descriptionTextView = findViewById(R.id.description_text_view);
        final TextView ratingTextView = findViewById(R.id.rating_text_view);
        final TextView priceTextView = findViewById(R.id.price_text_view);


        Toast.makeText(this, bookTitle, Toast.LENGTH_SHORT).show();
        JsonObject json = new JsonObject();
        json.addProperty("title", bookTitle);

        Ion.with(this)
                .load(baseURL + "api/add-book/?format=json")
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {

                        if (e != null){
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error while fetching book info",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }

                        HeadersResponse responseHeader = result.getHeaders();
                        Headers headers = responseHeader.getHeaders();
                        String bookID = headers.get("id");

                        if (bookID != null){
                            Ion.with(ShowBookInfoActivity.this)
                                    .load(baseURL + "api/books/" + bookID + "/?format=json")
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            if (e != null){
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Error while fetching book info",
                                                        Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                            Ion.with(ShowBookInfoActivity.this)
                                                    .load(result.get("book_cover_url").getAsString())
                                                    .withBitmap()
                                                    .intoImageView(bookCoverImageView).setCallback(new FutureCallback<ImageView>() {
                                                @Override
                                                public void onCompleted(Exception e, ImageView result) {
                                                    loadingView.setVisibility(View.GONE);
                                                }
                                            });

                                            Ion.with(ShowBookInfoActivity.this)
                                                    .load(result.get("google_books_link").getAsString())
                                                    .asJsonObject()
                                                    .setCallback(new FutureCallback<JsonObject>() {
                                                        @Override
                                                        public void onCompleted(Exception e, JsonObject result) {
                                                            try {
                                                                JsonObject book = result.getAsJsonArray("items")
                                                                        .get(0).getAsJsonObject().getAsJsonObject("volumeInfo");
                                                                String title = book.get("title").getAsString();
                                                                titleTextView.setText(title);
                                                                String author = book.get("authors").getAsJsonArray().get(0).getAsString();
                                                                authorTextView.setText(author);
                                                                String description = book.get("description").getAsString();
                                                                descriptionTextView.setText(description);
                                                                String isbn13 = book.getAsJsonArray("industryIdentifiers")
                                                                        .get(0).getAsJsonObject().get("identifier").getAsString();
                                                                isbn13TextView.setText(isbn13);
                                                                String isbn10 = book.getAsJsonArray("industryIdentifiers")
                                                                        .get(1).getAsJsonObject().get("identifier").getAsString();
                                                                isbn10TextView.setText(isbn10);
                                                                String pageCount = book.get("pageCount").getAsString();
                                                                pagesTextView.setText(pageCount);
                                                                String category = book.get("categories").getAsJsonArray().get(0).getAsString();
                                                                categoryTextView.setText(category);
                                                                String rating = book.get("averageRating").getAsString();
                                                                ratingTextView.setText(rating);
                                                            } catch (Exception ex){
                                                                Toast.makeText(ShowBookInfoActivity.this,
                                                                        ex.getMessage(),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error while fetching book info",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }

                    }
                });
    }
}
