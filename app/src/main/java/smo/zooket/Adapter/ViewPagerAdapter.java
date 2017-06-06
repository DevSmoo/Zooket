package smo.zooket.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import smo.zooket.FavProFragment;
import smo.zooket.FavSuperFragment;
import smo.zooket.Fragment_Product;

/**
 * Created by Smo on 5/6/2017.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int num) {
        switch (num) {
            case 0:
                return new FavProFragment();
            case 1:
                return new FavSuperFragment();
            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "محصولات";
                break;
            case 1:
                title = "فروشگاهها";
                break;
            default:
                break;
        }
        return title;
    }

}