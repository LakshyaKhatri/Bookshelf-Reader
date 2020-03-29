package www.compiletales.wordpress.com.bookshelfreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

public class ShowBookInfoActivity extends AppCompatActivity {

    String bookTitle;
    String baseURL = "https://lakshya1498.pythonanywhere.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_button);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bookTitle = getIntent().getStringExtra("BOOK_TITLE");
        final ImageView bookCoverImageView = findViewById(R.id.cover_image_view);
//        final LottieAnimationView loadingView = findViewById(R.id.book_loading_view);
        final TextView titleTextView = findViewById(R.id.title_text_view);
        final TextView authorTextView = findViewById(R.id.author_text_view);
        final TextView pagesTextView = findViewById(R.id.pages_text_view);
        final TextView genreTextView = findViewById(R.id.genre_text_view);
        final TextView dimensionsTextView = findViewById(R.id.dimensions_text_view);
        final TextView isbn10TextView = findViewById(R.id.isbn_ten_text_view);
        final TextView isbn13TextView = findViewById(R.id.isbn_thirteen_text_view);
        final TextView descriptionTextView = findViewById(R.id.description_text_view);
        final TextView ratingTextView = findViewById(R.id.rating_text_view);
        final TextView priceTextView = findViewById(R.id.price_text_view);
        final TextView seeMoreTextView = findViewById(R.id.see_more_text_view);
        final TextView seeLessTextView = findViewById(R.id.see_less_text_view);
        final TextView publisherTextView = findViewById(R.id.publisher_text_view);
        final Button getOnAmazonButton = findViewById(R.id.get_on_amazon_button);
        final LottieAnimationView loadingAnimationView = findViewById(R.id.book_info_loading_animation_view);

        seeMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descriptionTextView.setMaxLines(2000);
                seeLessTextView.setVisibility(View.VISIBLE);
                seeMoreTextView.setVisibility(View.INVISIBLE);
            }
        });

        seeLessTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descriptionTextView.setMaxLines(5);
                seeMoreTextView.setVisibility(View.VISIBLE);
                seeLessTextView.setVisibility(View.INVISIBLE);
            }
        });

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

                        if (e != null) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error while fetching book info",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }

                        HeadersResponse responseHeader = result.getHeaders();
                        Headers headers = responseHeader.getHeaders();
                        String bookID = headers.get("id");

                        if (bookID != null) {
                            Ion.with(ShowBookInfoActivity.this)
                                    .load(baseURL + "api/books/" + bookID + "/?format=json")
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            if (e != null) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Error while fetching book info",
                                                        Toast.LENGTH_LONG).show();
                                                finish();
                                            }

                                            Ion.with(ShowBookInfoActivity.this)
                                                    .load(result.get("book_cover_url").getAsString())
                                                    .withBitmap()
                                                    .intoImageView(bookCoverImageView)
                                            .setCallback(new FutureCallback<ImageView>() {
                                                @Override
                                                public void onCompleted(Exception e, ImageView result) {
                                                    loadingAnimationView.setVisibility(View.GONE);
                                                }
                                            });

                                            titleTextView.setText(result.get("title").getAsString());
                                            authorTextView.setText(result.get("author").getAsString());
                                            priceTextView.setText(result.get("price").getAsString());
                                            ratingTextView.setText(result.get("rating").getAsString());
                                            descriptionTextView.setText(result.get("description").getAsString());
                                            publisherTextView.setText(result.get("publisher").getAsString());
                                            isbn10TextView.setText(result.get("isbn_10").getAsString());
                                            isbn13TextView.setText(result.get("isbn_13").getAsString());
                                            pagesTextView.setText(result.get("total_pages").getAsString());
                                            genreTextView.setText(result.get("genre").getAsString());
                                            dimensionsTextView.setText(result.get("dimensions").getAsString());

                                            Layout l = descriptionTextView.getLayout();
                                            if(l != null){
                                                int lines = l.getLineCount();
                                                if(lines > 0)
                                                    if(l.getEllipsisCount(lines-1) > 0){
                                                        seeMoreTextView.setVisibility(View.VISIBLE);
                                                    }
                                            }

                                            getOnAmazonButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String url = "https://www.amazon.in/s?k=" +
                                                            titleTextView.getText().toString()
                                                            .replace(" ", "+") +
                                                            "+by+" + authorTextView.getText().toString().
                                                            replace(" ", "+");
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse(url));
                                                    startActivity(intent);
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
