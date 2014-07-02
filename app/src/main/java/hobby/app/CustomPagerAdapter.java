package hobby.app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Vector;

/**
 * Created by Papucs on 2014.06.04..
 */
public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;
    private Vector<View> pages;
    private String[] titles = new String[]{"Google", "IneTrack"};

    public CustomPagerAdapter(Context context, Vector<View> pages) {
        this.mContext=context;
        this.pages=pages;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View page = pages.get(position);
        container.addView(page);
        return page;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}