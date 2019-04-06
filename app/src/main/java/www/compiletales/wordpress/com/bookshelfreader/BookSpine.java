package www.compiletales.wordpress.com.bookshelfreader;

import android.graphics.Bitmap;

public class BookSpine {
    private Bitmap spineImage;
    private String bookSpineText;

    public BookSpine(Bitmap spineImage, String bookSpineText) {
        this.spineImage = spineImage;
        this.bookSpineText = bookSpineText;
    }

    public Bitmap getSpineImage() {
        return spineImage;
    }

    public String getBookSpineText() {
        return bookSpineText;
    }

    public void setSpineImage(Bitmap spineImage) {
        this.spineImage = spineImage;
    }

    public void setBookSpineText(String bookSpineText) {
        this.bookSpineText = bookSpineText;
    }
}
