package www.compiletales.wordpress.com.bookshelfreader;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CroppedImagesViewPagerAdapter extends PagerAdapter {

    ArrayList<View> views = new ArrayList<>();;

    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = views.indexOf (object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view.equals(obj);
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
        container.removeView (views.get(position));
    }

    public int addView(View v){
        return addView(v, views.size());
    }

    public int addView (View v, int position)
    {
        views.add (position, v);
        return position;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = views.get(position);
        container.addView(v);
        return v;
    }
}
