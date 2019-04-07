package www.compiletales.wordpress.com.bookshelfreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ShowBookInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book_info);

        Toast.makeText(this, getIntent().getStringExtra("BOOK_TITLE"), Toast.LENGTH_LONG).show();
    }
}
